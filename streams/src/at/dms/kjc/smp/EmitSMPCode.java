package at.dms.kjc.smp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.dms.kjc.JExpression;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JStatement;
import at.dms.kjc.KjcOptions;
import at.dms.kjc.backendSupport.ComputeNode;
import at.dms.kjc.backendSupport.EmitCode;
import at.dms.kjc.common.CodegenPrintWriter;
import at.dms.kjc.sir.SIRCodeUnit;

/**
 * Emit c code for tiles
 * 
 * @author mgordon
 * 
 */
public class EmitSMPCode extends EmitCode {

    public final String MAIN_FILE = "main.c";

    private boolean isDynamic = false;
    private Set<String> dominated = null;
    Map<Integer, String> threadIdToType = null;

    public EmitSMPCode(SMPBackEndFactory backEndFactory, boolean isDynamic,
             Set<String> dominated,
             Map<Integer, String> threadIdToType) {
        super(backEndFactory);
        this.isDynamic = isDynamic;
        this.dominated = dominated;
        this.threadIdToType = threadIdToType;
    }

    public EmitSMPCode() {
        super(null);
        this.isDynamic = false;
    }

    public void doit() {
        try {               
            // if load balancing, instrument steady-state loop before
            // steady-state barrier
            if (KjcOptions.loadbalance) {
                LoadBalancer.instrumentSteadyStateLoopsBeforeBarrier();
            }

            // if profiling, add instrumentation before steady-state barrier
            if (KjcOptions.profile) {
                Profiler.instrumentBeforeBarrier();
            }

            // for all the cores, add a barrier at the end of the steady state,
            // do it here
            // because we are done with all code gen
            SMPComputeCodeStore.addBarrierSteady();

            // if load balancing, instrument steady-state loop after
            // steady-state barrier
            if (KjcOptions.loadbalance) {
                LoadBalancer.instrumentSteadyStateLoopsAfterBarrier();
            }

            // if profiling, add instrumentation after steady-state barrier
            if (KjcOptions.profile) {
                Profiler.instrumentAfterBarrier();
            }

            // make sure each thread exits properly
            for (Core core : SMPBackend.chip.getCores()) {
                if (!core.getComputeCode().shouldGenerateCode())
                    continue;

                // core.getComputeCode().addSteadyLoopStatement(Util.toStmt("foo(NULL)"));
                core.getComputeCode().addCleanupStatement(
                        Util.toStmt("pthread_exit(NULL)"));
            }

            // add call to buffer initialization and CPU affinity setting
            for (Core core : SMPBackend.chip.getCores()) {
                if (!core.getComputeCode().shouldGenerateCode())
                    continue;

                core.getComputeCode().addFunctionCallFirst(
                        core.getComputeCode().getBufferInitMethod(),
                        new JExpression[0]);

                if (!KjcOptions.nobind) {
                    JExpression[] setAffinityArgs = new JExpression[1];
                    setAffinityArgs[0] = new JIntLiteral(core.getCoreID());
                    core.getComputeCode().addFunctionCallFirst(
                            "setCPUAffinity", setAffinityArgs);
                }
            }

            // Standard final optimization of a code unit before code emission:
            // unrolling and constant prop as allowed, DCE, array destruction
            // into scalars.
            for (Core core : SMPBackend.chip.getCores()) {
                if (!core.getComputeCode().shouldGenerateCode())
                    continue;

                System.out.println("Optimizing core " + core.getCoreID()
                        + "...");
                (new at.dms.kjc.sir.lowering.FinalUnitOptimize()).optimize(core
                        .getComputeCode());
            }

            // make sure that variables and methods are unique across cores
            List<SMPComputeCodeStore> codeStores = new LinkedList<SMPComputeCodeStore>();
            for (Core core : SMPBackend.chip.getCores()) {
                if (!core.getComputeCode().shouldGenerateCode())
                    continue;

                codeStores.add(core.getComputeCode());
            }

            CodeStoreRenameAll.renameOverAllCodeStores(codeStores);

            // write out C code for tiles
            for (Core core : SMPBackend.chip.getCores()) {
                // if no code was written to this core's code store, then skip
                // it
                if (!core.getComputeCode().shouldGenerateCode())
                    continue;

                String outputFileName = "core" + core.getCoreID() + ".c";
                CodegenPrintWriter p = new CodegenPrintWriter(
                        new BufferedWriter(
                                new FileWriter(outputFileName, false)));

                generateIncludes(p);

                this.emitCodeForComputeNode(core, p);

                p.close();
            }

            // generate main file that will initialize threads for each core
            generateMainFile();

            // generate header containing global variables
            generateGlobalsHeader();

            // generate header containing code to get clock cycles
            generateClockHeader();

            // generate header containing barrier implementation
            generateBarrierCode();

            // generate the makefile that will compile all the tile executables
            generateMakefile();

            // generate load balancing code
            if (KjcOptions.loadbalance)
                LoadBalancer.generateLoadBalancerCode();

        } catch (IOException e) {
            throw new AssertionError("I/O error" + e);
        }
    }

    /**
     * Given a ComputeNode and a CodegenPrintWrite, print all code for the
     * ComputeNode. Channel information relevant to the ComputeNode is printed
     * based on data in the BackEndFactory passed when this class was
     * instantiated.
     * 
     * @param n
     *            The ComputeNode to emit code for.
     * @param p
     *            The CodegenPrintWriter (left open on return).
     */
    @Override
    public void emitCodeForComputeStore(SIRCodeUnit fieldsAndMethods,
            ComputeNode n, CodegenPrintWriter p, CodeGen codegen) {

        p.println("// code for core " + n.getUniqueId());
        p.println(((Core) n).getComputeCode().getGlobalText());

        // System.out.println("EmitSMPCode.emitCodeForComputeStore, code for core="
        // + n.getUniqueId());

        // generate function prototypes for methods so that they can call each
        // other
        // in C.
        codegen.setDeclOnly(true);
        for (JMethodDeclaration method : fieldsAndMethods.getMethods()) {
            method.accept(codegen);
        }
        p.println("");
        codegen.setDeclOnly(false);

        // generate code for ends of channels that connect to code on this
        // ComputeNode
        Set<RotatingBuffer> outputBuffers = RotatingBuffer
                .getOutputBuffersOnCore((Core) n);
        Set<InputRotatingBuffer> inputBuffers = RotatingBuffer
                .getInputBuffersOnCore((Core) n);

        // externs
        for (RotatingBuffer c : outputBuffers) {
            if (c.writeDeclsExtern() != null) {
                for (JStatement d : c.writeDeclsExtern()) {
                    d.accept(codegen);
                }
            }
        }

        for (RotatingBuffer c : inputBuffers) {
            if (c.readDeclsExtern() != null) {
                for (JStatement d : c.readDeclsExtern()) {
                    d.accept(codegen);
                }
            }
        }

        for (RotatingBuffer c : outputBuffers) {
            if (c.dataDecls() != null) {
                // wrap in #ifndef for case where different ends have
                // are in different files that eventually get concatenated.
                p.println();
                p.println("#ifndef " + c.getIdent() + "_CHANNEL_DATA");
                for (JStatement d : c.dataDecls()) {
                    d.accept(codegen);
                    p.println();
                }
                p.println("#define " + c.getIdent() + "_CHANNEL_DATA");
                p.println("#endif");
            }
        }

        for (RotatingBuffer c : inputBuffers) {
            if (c.dataDecls() != null && !outputBuffers.contains(c)) {
                p.println("#ifndef " + c.getIdent() + "_CHANNEL_DATA");
                for (JStatement d : c.dataDecls()) {
                    d.accept(codegen);
                    p.println();
                }
                p.println("#define " + c.getIdent() + "_CHANNEL_DATA");
                p.println("#endif");
            }
        }

        for (RotatingBuffer c : outputBuffers) {
            p.println("/* output buffer " + "(" + c.getIdent() + " of "
                    + c.getFilterNode() + ") */");
            if (c.writeDecls() != null) {
                for (JStatement d : c.writeDecls()) {
                    d.accept(codegen);
                    p.println();
                }
            }
            if (c.pushMethod() != null) {
                c.pushMethod().accept(codegen);
            }
        }

        for (RotatingBuffer c : inputBuffers) {
            p.println("/* input buffer (" + c.getIdent() + " of "
                    + c.getFilterNode() + ") */");
            if (c.readDecls() != null) {
                for (JStatement d : c.readDecls()) {
                    d.accept(codegen);
                    p.println();
                }
            }
            if (c.peekMethod() != null) {
                c.peekMethod().accept(codegen);
            }
            if (c.assignFromPeekMethod() != null) {
                c.assignFromPeekMethod().accept(codegen);
            }
            if (c.popMethod() != null) {
                c.popMethod().accept(codegen);
            }
            if (c.assignFromPopMethod() != null) {
                c.assignFromPopMethod().accept(codegen);
            }
            if (c.popManyMethod() != null) {
                c.popManyMethod().accept(codegen);
            }
        }
        p.println("");

        p.println("");

        // generate declarations for fields
        for (JFieldDeclaration field : fieldsAndMethods.getFields()) {
            // System.out.println("EmitSMPCode.emitCodeForComputeStore generating field: "
            // + field.getVariable().getIdent());
            field.accept(codegen);
        }
        p.println("");

        // handle the buffer initialization method separately because we do not
        // want it
        // optimized (it is not in the methods list of the code store
        ((Core) n).getComputeCode().getBufferInitMethod().accept(codegen);

        // generate functions for methods
        codegen.setDeclOnly(false);

        for (JMethodDeclaration method : fieldsAndMethods.getMethods()) {
            method.accept(codegen);
        }

    }

    private void generateBarrierCode() throws IOException {
        CodegenPrintWriter p;

        p = new CodegenPrintWriter(new BufferedWriter(new FileWriter(
                "barrier.h", false)));
        p.println("#ifndef BARRIER_H");
        p.println("#define BARRIER_H");
        p.println();
        p.println("typedef struct barrier {");
        p.println("  int num_threads;");
        p.println("  volatile int count;");
        p.println("  volatile int generation;");
        p.println("} barrier_t;");
        p.println();
        p.println("extern int FetchAndDecr(volatile int *mem);");
        p.println("extern void barrier_init(barrier_t *barrier, int num_threads);");
        p.println("extern void barrier_wait(barrier_t *barrier);");
        p.println();
        p.println("#endif");
        p.close();

        p = new CodegenPrintWriter(new BufferedWriter(new FileWriter(
                "barrier.c", false)));
        p.println("#include \"barrier.h\"");
        p.println();
        p.println("int FetchAndDecr(volatile int *mem)");
        p.println("{");
        p.println("  int val = -1;");
        p.println();
        p.println("  asm volatile (\"lock; xaddl %0,%1\"");
        p.println("		: \"=r\" (val), \"=m\" (*mem)");
        p.println("		: \"0\" (val), \"m\" (*mem)");
        p.println("		: \"memory\", \"cc\");");
        p.println("  return val - 1;");
        p.println("}");
        p.println();
        p.println("void barrier_init(barrier_t *barrier, int num_threads) {");
        p.println("  barrier->num_threads = num_threads;");
        p.println("  barrier->count = num_threads;");
        p.println("  barrier->generation = 0;");
        p.println("}");
        p.println();
        p.println("void barrier_wait(barrier_t *barrier) {");
        p.println("  int cur_gen = barrier->generation;");
        p.println();
        p.println("  if(FetchAndDecr(&barrier->count) == 0) {");
        p.println("    barrier->count = barrier->num_threads;");
        p.println("    barrier->generation++;");
        p.println("  }");
        p.println("  else {");
        p.println("    while(cur_gen == barrier->generation);");
        p.println("  }");
        p.println("}");
        p.close();
    }

    private void generateClockHeader() throws IOException {
        CodegenPrintWriter p = new CodegenPrintWriter(new BufferedWriter(
                new FileWriter("rdtsc.h", false)));

        p.println("#ifndef RDTSC_H");
        p.println("#define RDTSC_H");
        p.println("");
        p.println("#include <stdint.h>");
        p.println("");
        p.println("/* Returns the number of clock cycles that have passed since the machine");
        p.println(" * booted up. */");
        p.println("static __inline__ uint64_t rdtsc(void)");
        p.println("{");
        p.println("  uint32_t hi, lo;");
        p.println("  asm volatile (\"rdtsc\" : \"=a\"(lo), \"=d\"(hi));");
        p.println("  return ((uint64_t ) lo) | (((uint64_t) hi) << 32);");
        p.println("}");
        p.println("");
        p.println("#endif");
        p.close();
    }

    private void generateGlobalsHeader() throws IOException {
        CodegenPrintWriter p = new CodegenPrintWriter(new BufferedWriter(
                new FileWriter("globals.h", false)));

        p.println("#ifndef GLOBALS_H");
        p.println("#define GLOBALS_H");
        p.println();

        p.println("#include \"barrier.h\"");
        p.println("#include \"structs.h\"");
        p.println("#include \"pthread.h\"");
        p.println("#include <unistd.h>");
        if (isDynamic) {
            p.println("#include \"dynamic_queue.h\"");
        }
        p.println("#include <stdint.h>");
        p.println();

        p.println("#define ITERATIONS " + KjcOptions.numbers);
        p.println();

        p.println("#define minf(a, b) ((a) < (b) ? (a) : (b))");
        p.println("#define maxf(a, b) ((a) > (b) ? (a) : (b))");
        p.println();

        if (KjcOptions.iterations != -1) {
            p.println("// Number of steady-state iterations");
            p.println("extern int maxSteadyIter;");
            p.println();
        }

        if (KjcOptions.outputs != -1) {
            p.println("// Number of steady-state outputs");
            p.println("extern int maxOutputs;");
            p.println();
        }

        p.println("// Global barrier");
        p.println("extern barrier_t barrier;");
        p.println();

        p.println("// exit function");
        p.println("extern void streamit_exit(int n);");
        p.println();



        if (isDynamic) {
            int numDynamicReaders = threadIdToType.keySet().size();
            System.out.println("EmitSMPCode.generateGlobalsHeader numDynamicReaders=" + numDynamicReaders);

            p.println();
            p.println("#define DYNAMIC_READERS  " + numDynamicReaders);
            p.println("#define ASLEEP          0");
            p.println("#define AWAKE           1");
            p.println("#define DYN_READER      0");
            p.println("#define MASTER          1");

            for (Integer threadId : threadIdToType.keySet()) {
                String type = threadIdToType.get(threadId);
                // System.out.println("EmitSMPCode.generateGlobalHeader " +
                // "extern " + type + "_queue_ctx_ptr   dyn_buf_" + threadId +
                // ";");
                p.println("extern " + type + "_queue_ctx_ptr   dyn_buf_"
                        + threadId + ";");
            }

            p.println("extern pthread_cond_t        thread_conds    [DYNAMIC_READERS][2];");
            p.println("extern pthread_mutex_t       thread_mutexes  [DYNAMIC_READERS][2];");
            p.println("extern int                   thread_to_sleep [DYNAMIC_READERS][2];");
        }

        p.println("// Shared buffers");
        p.println(SMPBackend.chip.getOffChipMemory().getComputeCode()
                .getGlobalText());

        p.println("// CPU Affinity");
        p.println("extern void setCPUAffinity(int core);");
        p.println();

        if (KjcOptions.profile)
            Profiler.emitProfilerExternGlobals(p);

        if (KjcOptions.loadbalance)
            LoadBalancer.emitLoadBalancerExternGlobals(p);

        p.println("// Thread entry points");
        for (Core core : SMPBackend.chip.getCores()) {
            if (!core.getComputeCode().shouldGenerateCode())
                continue;
            p.println("extern void *" + core.getComputeCode().getMyMainName()
                    + "(void * arg);");
        }
        p.println();

        p.println("// Helper entry points");
        for (Core core : SMPBackend.chip.getCores()) {
            Set<JMethodDeclaration> helperMethods = core.getComputeCode()
                    .getDynamicThreadHelperMethods();
            for (JMethodDeclaration decl : helperMethods) {
                p.println("extern void * " + decl.getName() + "(void * arg);");
            }
        }
        p.println();
        for (Core core : SMPBackend.chip.getCores()) {
            for (JFieldDeclaration fieldDecl : core.getComputeCode()
                    .getExternFields().values()) {
                p.println("extern " + fieldDecl.getType() + " "
                        + fieldDecl.getVariable().getIdent() + ";");
            }
        }
        
//        for (String key : ThreadMapper.getMapper().getDominators().keySet()) {
//            p.println("extern int " + key + "_multiplier;");
//            for (String val : ThreadMapper.getMapper().getDominators().get(key))
//            p.println("extern int " + val + "_multiplier;");            
//        }
//        

        if (KjcOptions.perftest) {
            p.println();
            p.println("extern int perfTestNumInputs;");
            p.println("extern timespec endTime;");
            p.println("extern timespec startTime;");
            p.println("extern void start_time();");
        }        

        p.println();
        p.println("// Intra-SSG synchronization tokens");
        for (String str : SMPComputeCodeStore.getTokenNames()) {
            p.println("extern volatile int " + str + ";");
        }

        p.println("#endif");
        p.close();
    }

    public void generateIncludes(CodegenPrintWriter p) {
        p.println("#ifndef _GNU_SOURCE");
        p.println("#define _GNU_SOURCE");
        p.println("#endif");
        p.println();
        p.println("#include <stdio.h>"); // in case of FileReader / FileWriter
        p.println("#include <math.h>"); // in case math functions
        p.println("#include <stdlib.h>");
        p.println("#include <unistd.h>");
        p.println("#include <stdint.h>");
        p.println("#include <fcntl.h>");
        p.println("#include <pthread.h>");
        p.println("#include <sched.h>");
        p.println("#include <sys/types.h>");
        p.println("#include <sys/stat.h>");
        p.println("#include <sys/mman.h>");
        p.println("#include <string.h>");
        p.println();
        p.println("#include \"globals.h\"");
        p.println("#include \"barrier.h\"");
        p.println("#include \"rdtsc.h\"");
        p.println("#include \"structs.h\"");
        
        if (KjcOptions.perftest) {
            p.println("#include <time.h>");
        }

        if (isDynamic) {
            p.println("#include \"dynamic_queue.h\"");
        }

        if (KjcOptions.loadbalance)
            p.println("#include \"load_balancer.h\"");

        if (KjcOptions.fixedpoint)
            p.println("#include \"fixed.h\"");

        p.println();
        p.println();

        if (isDynamic) {
            p.println("#define QUEUE_SIZE 2");
        }

        p.println();
        p.println();
    }

    private void generateTime(CodegenPrintWriter p) {
        p.println("#include <time.h>");
        p.println("#include <sys/time.h>");
        p.println("#include <stdio.h>");
        p.println("#ifdef __MACH__");
        p.println("#include <mach/clock.h>");
        p.println("#include <mach/mach.h>");
        p.println("#endif");
        p.println();
        p.println();
        p.println("void curr_time(struct timespec *ts) {");
        p.println("#ifdef __MACH__");
        p.println("  clock_serv_t cclock;");
        p.println("  mach_timespec_t mts;");
        p.println("  host_get_clock_service(mach_host_self(), CALENDAR_CLOCK, &cclock);");
        p.println("  clock_get_time(cclock, &mts);");
        p.println("  mach_port_deallocate(mach_task_self(), cclock);");
        p.println("  ts->tv_sec = mts.tv_sec;");
        p.println("  ts->tv_nsec = mts.tv_nsec;");
        p.println(" #else");
        p.println("  clock_gettime(CLOCK_PROCESS_CPUTIME_ID, ts);");
        p.println(" #endif");
        p.println(" }");

        p.println();
        p.println();
        
        p.println("timespec diff(timespec start, timespec end) {");
        p.println("  timespec temp;");
        p.println("  if ((end.tv_nsec-start.tv_nsec)<0) {");
        p.println("    temp.tv_sec = end.tv_sec-start.tv_sec-1;");
        p.println("    temp.tv_nsec = 1000000000+end.tv_nsec-start.tv_nsec;");
        p.println("  } else {");
        p.println("    temp.tv_sec = end.tv_sec-start.tv_sec;");
        p.println("    temp.tv_nsec = end.tv_nsec-start.tv_nsec;");
        p.println("  }");
        p.println("  return temp;");
        p.println("}");
        
        p.println();
        p.println();

    }
    
    
    private void generateMainFile() throws IOException {
        CodegenPrintWriter p = new CodegenPrintWriter(new BufferedWriter(
                new FileWriter(MAIN_FILE, false)));

        generateIncludes(p);

        if (KjcOptions.perftest) {
            generateTime(p);
        }

        if (KjcOptions.iterations != -1) {
            p.println("// Number of steady-state iterations");
            p.println("int maxSteadyIter = " + KjcOptions.iterations + ";");
            p.println();
        }

        if (KjcOptions.outputs != -1) {
            p.println("// Number of steady-state outputs");
            p.println("int maxOutputs = " + KjcOptions.outputs + ";");
            p.println();
        }

          

        if (isDynamic) {
            for (Integer threadId : threadIdToType.keySet()) {
                String type = threadIdToType.get(threadId);
                p.println(type + "_queue_ctx_ptr   dyn_buf_" + threadId + ";");
            }

            p.println();			
            p.println("pthread_cond_t        thread_conds    [DYNAMIC_READERS][2];");
            p.println("pthread_mutex_t       thread_mutexes  [DYNAMIC_READERS][2];");
            p.println("int                   thread_to_sleep [DYNAMIC_READERS][2];");
        }

        p.println();

        for (Core core : SMPBackend.chip.getCores()) {
            for (JFieldDeclaration fieldDecl : core.getComputeCode()
                    .getExternFields().values()) {
                p.println(fieldDecl.getType() + " "
                        + fieldDecl.getVariable().getIdent() + ";");
            }
        }

//        for (String key : ThreadMapper.getMapper().getDominators().keySet()) {
//            p.println("int " + key + "_multiplier;");
//            for (String val : ThreadMapper.getMapper().getDominators().get(key))
//            p.println("int " + val + "_multiplier;");            
//        }
        
        
        p.println();

        if (KjcOptions.perftest) {
            p.println("int perfTestNumInputs;");
            p.println("timespec endTime;");
            p.println("timespec startTime;");
            p.println("int startedTiming;");
            p.println();
        }

        p.println("// Global barrier");
        p.println("barrier_t barrier;");
        p.println();

        p.println();
        p.println("// Intra-SSG synchronization tokens");
        for (String str : SMPComputeCodeStore.getTokenNames()) {
            p.println("volatile int " + str + ";");
        }
        p.println();

        generateSetAffinity(p);

        generateExit(p);

        generateStartTime(p);
        
        if (KjcOptions.profile) {
            Profiler.emitProfilerGlobals(p);
            Profiler.generateProfilerOutputCode(p);
        }

        if (KjcOptions.loadbalance) {
            LoadBalancer.emitLoadBalancerGlobals(p);
            LoadBalancer.emitLoadBalancerInit(p);
        }

        p.println("// main() Function Here");
        p.println("int main(int argc, char** argv) {");
        p.indent();

        // figure out how many cores will participate in barrier
        int barrier_count = 0;
        for (Core core : SMPBackend.chip.getCores())
            if (core.getComputeCode().shouldGenerateCode())
                barrier_count++;

        p.println();
        p.println("// Initialize barrier");
        p.println("barrier_init(&barrier, " + barrier_count + ");");

        
        if (KjcOptions.perftest) {
            p.println();
            p.println("startedTiming = 0;");
            p.println();
        }     
        
        if (isDynamic) {
            p.println();
            p.println("// Initialize dynamic queues");

            for (Integer threadId : threadIdToType.keySet()) {
                String type = threadIdToType.get(threadId);
                p.println("dyn_buf_" + threadId + " = " + type
                        + "_queue_create();");
            }

            p.println();
            p.println("// Initialize mutexes and condition variables");
            p.println();
            p.println("int i, j = 0;");
            p.println("for (i = 0; i < DYNAMIC_READERS; i++) {");
            p.indent();
            p.println("for (j = 0; j < 2; j++) {");
            p.indent();

            p.println("pthread_cond_init(&thread_conds[i][j], NULL);");
            p.println("pthread_mutex_init(&thread_mutexes[i][j], NULL);");
            p.println("thread_to_sleep[i][j] = ASLEEP; /* 1 is asleep */");
            p.outdent();
            p.println("}");
            p.outdent();
            p.println("}");
            p.println();
        }

        for (Core core : SMPBackend.chip.getCores()) {
            for (JFieldDeclaration fieldDecl : core.getComputeCode()
                    .getExternFields().values()) {
                String ident = fieldDecl.getVariable().getIdent();
                if (dominated.contains(ident.substring(0, ident.length()
                        - "_multiplier".length()))) {
                    p.println(ident + " = 0;");
                } else {
                    p.println(ident + " = 1;");
                }
            }
        }

//        for (String key : ThreadMapper.getMapper().getDominators().keySet()) {
//            p.println(key + "_multiplier" + " = 1;");
//            for (String val : ThreadMapper.getMapper().getDominators().get(key))
//            p.println(val + "_multiplier" + " = 0;");            
//        }
        
        p.println();

        if (KjcOptions.perftest) {
            p.println("perfTestNumInputs = 0;");
            p.println();
        }

        p.println("// Intra-SSG synchronization tokens");
        for (String str : SMPComputeCodeStore.getTokenNames()) {
            p.println(str + " = 0;");
        }
        p.println();

        if (KjcOptions.loadbalance) {
            p.println();
            p.println("// Initalize load balancer");
            p.println(LoadBalancer.initLoadBalancerMethodName() + "();");
        }

        p.println();
        p.println("// Spawn threads");
        p.println("int rc;");
        p.println("pthread_attr_t attr;");
        p.println("void *status;");
        p.println();
        p.println("pthread_attr_init(&attr);");
        p.println("pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);");

        for (Core core : SMPBackend.chip.getCores()) {
            if (!core.getComputeCode().shouldGenerateCode())
                continue;

            p.println();
            p.println("pthread_t thread_n" + core.getCoreID() + ";");
            p.println("if ((rc = pthread_create(&thread_n" + core.getCoreID()
                    + ", NULL, "
                    + core.getComputeCode().getMainFunction().getName()
                    + ", (void *)NULL)) < 0)");
            p.indent();
            p.println("printf(\"Error creating thread for core "
                    + core.getCoreID() + ": %d\\n\", rc);");
            p.outdent();
        }

        if (isDynamic) {
            for (Core core : SMPBackend.chip.getCores()) {
                Set<JMethodDeclaration> helperMethods = core.getComputeCode()
                        .getDynamicThreadHelperMethods();

                for (JMethodDeclaration decl : helperMethods) {
                    p.println();
                    String varName = "thread_" + decl.getName();
                    p.println("pthread_t " + varName + ";");
                    p.println("if ((rc = pthread_create(&" + varName + ", NULL, " + decl.getName() + ", (void *)NULL)) < 0)");
                    p.indent();
                    p.println("printf(\"Error creating helper "
                            + ": %d\\n\", rc);");
                    p.outdent();

                }
            }
        }

        p.println();
        p.println("pthread_attr_destroy(&attr);");

        for (Core core : SMPBackend.chip.getCores()) {
            if (!core.getComputeCode().shouldGenerateCode())
                continue;

            p.println();
            p.println("if ((rc = pthread_join(thread_n" + core.getCoreID()
                    + ", &status)) < 0) {");
            p.indent();
            p.println("printf(\"Error joining thread for core "
                    + core.getCoreID() + ": %d\\n\", rc);");
            p.println("exit(-1);");
            p.outdent();
            p.println("}");
        }

        p.println();
        p.println("// Exit");
        p.println("exit(0);");
        p.outdent();
        p.println("}");
        p.close();
    }

    private void generateExit(CodegenPrintWriter p) {
        p.println();
        p.println("void streamit_exit(int n) {");
        if (KjcOptions.perftest) {
            generatePrintResults(p);
        }               
        p.println("  exit(n);");
        p.println("}");
        p.println();

    }

    private void generatePrintResults(CodegenPrintWriter p) {    
        p.println("  curr_time(&endTime);");                
        p.println("  printf (\"input=%d delta=%ld:%ld\\n\", perfTestNumInputs, diff(startTime,endTime).tv_sec, diff(startTime,endTime).tv_nsec);");            
    }
    
    private void generateStartTime(CodegenPrintWriter p) {
        if (KjcOptions.perftest) {
            p.println();
            p.println("void start_time() {");
            p.println("  if (startedTiming == 0) {");
            p.println("    curr_time(&startTime);");
            p.println("    startedTiming = 1;");
            p.println("  }");
            p.println("}");
            p.println();
        }               
    }

    private void generateMakefile() throws IOException {
        CodegenPrintWriter p = new CodegenPrintWriter(new BufferedWriter(
                new FileWriter("Makefile", false)));

        // p.println("CC = icc");

        p.println("UNAME := $(shell uname)");
        p.println("CC = g++");
        p.println("CFLAGS = -O2 -lrt");
        p.println("ifeq ($(UNAME), Darwin)");
        p.println("CFLAGS = -O2 -vec-report0");
        p.println("endif");
        p.println("INCLUDES = ");

        if (KjcOptions.perftest) {        
            p.println("LIBS = -pthread -lstdc++ -lrt");
            p.println("ifeq ($(UNAME), Darwin)");
            p.println("# do something for OSX");
            p.println("LIBS = -pthread -lstdc++");
            p.println("endif");
        } else {
            p.println("LIBS = -pthread -lstdc++");
        }
        
        p.print("OBJS = main.o barrier.o ");
        if (isDynamic) {
            p.print("dynamic_queue.o ");
        }
        if (KjcOptions.loadbalance)
            p.print("load_balancer.o ");
        for (Core core : SMPBackend.chip.getCores()) {
            if (!core.getComputeCode().shouldGenerateCode())
                continue;
            p.print("core" + core.getCoreID() + ".o ");
        }
        p.println();
        p.println();

        p.println("all: $(OBJS)");
        p.println("\t$(CC) $(CFLAGS) $(LIBS) $(OBJS) -o "
                + (KjcOptions.output == null ? "smp" + KjcOptions.smp
                        : KjcOptions.output));
        p.println();
        p.println("clean:");
        p.println("\trm *.o "
                + (KjcOptions.output == null ? "smp" + KjcOptions.smp
                        : KjcOptions.output));
        p.println();

        p.println();
        p.println("realclean:");
        p.println("\trm *.o *.dot *.c *.h *.java "
                + (KjcOptions.output == null ? "smp" + KjcOptions.smp
                        : KjcOptions.output));
        p.println();

        p.println("main.o: main.c");
        p.println("\t$(CC) $(CFLAGS) $(INCLUDES) -c main.c");
        p.println();

        p.println("barrier.o: barrier.c");
        p.println("\t$(CC) $(CFLAGS) $(INCLUDES) -c barrier.c");
        p.println();

        p.println("dynamic_queue.o: dynamic_queue.c");
        p.println("\t$(CC) $(CFLAGS) $(INCLUDES) -c dynamic_queue.c");
        p.println();

        if (KjcOptions.loadbalance) {
            p.println("load_balancer.o: load_balancer.c");
            p.println("\t$(CC) $(CFLAGS) $(INCLUDES) -c load_balancer.c");
            p.println();
        }

        for (Core core : SMPBackend.chip.getCores()) {
            if (!core.getComputeCode().shouldGenerateCode())
                continue;
            p.println("core" + core.getCoreID() + ".o: core" + core.getCoreID()
                    + ".c");
            p.println("\t$(CC) $(CFLAGS) $(INCLUDES) -c core"
                    + core.getCoreID() + ".c");
            p.println();
        }
        p.close();
    }

    private void generateSetAffinity(CodegenPrintWriter p) {
        p.println("// Set CPU affinity for thread");
        p.println("void setCPUAffinity(int core) {");
        p.indent();

        p.println("#ifndef __APPLE__");
        p.println("cpu_set_t cpu_set;");
        p.println("CPU_ZERO(&cpu_set);");
        p.println("CPU_SET(core, &cpu_set);");
        p.println();

        p.println("if(pthread_setaffinity_np(pthread_self(), sizeof(cpu_set_t), &cpu_set) < 0) {");
        p.indent();
        p.println("printf(\"Error setting pthread affinity\\n\");");
        p.println("exit(-1);");
        p.outdent();
        p.println("}");

        p.println("#endif");
        p.outdent();
        p.println("}");
        p.println();
    }
}
