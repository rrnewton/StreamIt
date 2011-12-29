package at.dms.kjc.vanillaSlice;

import at.dms.kjc.backendSupport.BackEndFactory;
import at.dms.kjc.backendSupport.BackEndScaffold;
import at.dms.kjc.backendSupport.CodeStoreHelper;
import at.dms.kjc.backendSupport.CodeStoreHelperJoiner;
import at.dms.kjc.backendSupport.CodeStoreHelperSimple;
import at.dms.kjc.backendSupport.CodeStoreHelperSplitter;
import at.dms.kjc.backendSupport.GetOrMakeChannel;
import at.dms.kjc.backendSupport.IntraSSGChannel;
import at.dms.kjc.backendSupport.ProcessFilterWorkNode;
import at.dms.kjc.backendSupport.ProcessInputFilterNode;
import at.dms.kjc.backendSupport.ProcessOutputFilterNode;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InputNode;
import at.dms.kjc.slir.InternalFilterNode;
import at.dms.kjc.slir.IntraSSGEdge;
import at.dms.kjc.slir.OutputNode;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.WorkNode;

/**
 * Specialization of {@link at.dms.kjc.backendSupport.BackEndFactory} for uniprocessor backend.
 * Provides some specializations for the uniprocssor backend directly, and links to others in separate classes.
 * @author dimock
 *
 */
public class UniBackEndFactory extends BackEndFactory<
    UniProcessors,
    UniProcessor,
    UniComputeCodeStore,
    Integer> { 

    private UniProcessors processors ;

    /**
     * Constructor if creating UniBackEndFactory before layout
     * Creates <b>numProcessors</b> processors.
     * @param numProcessors  number of processors to create.
     */
    public UniBackEndFactory(Integer numProcessors) {
       this(new UniProcessors(numProcessors));
    }
    
    /**
     * Constructor if creating UniBackEndFactory after layout.
     * Call it with same collection of processors used in Layout.
     * @param processors  the existing collection of processors.
     */
    public UniBackEndFactory(UniProcessors processors) {
        if (processors == null) {
            processors = new UniProcessors(1);
        }
        this.processors = processors;
    }
    
    private BackEndScaffold scaffolding = null;

    @Override
    public  BackEndScaffold getBackEndMain() {
        if (scaffolding == null) {
            scaffolding  = new BackEndScaffold();
        }
        return scaffolding;
    }
    
    
    @Override
    public UniProcessors getComputeNodes() {
        return processors;
    }

    
    @Override
    public UniComputeCodeStore getComputeCodeStore(UniProcessor parent) {
        return parent.getComputeCode();
    }

    @Override
    public UniProcessor getComputeNode(Integer specifier) {
        return processors.getNthComputeNode(specifier);
    }
    
    /**
     * Process an input slice node: find the correct ProcElement(s) and add joiner code, and buffers.
     * please delegate work to some other object.
     * @param input           the InputSliceNode 
     * @param whichPhase      INIT / PRIMEPUMP / STEADY
     * @param rawChip         the available compute nodes.
     * 
     */
    @Override
    public void processFilterInputNode(InputNode input,
            SchedulingPhase whichPhase, UniProcessors computeNodes) {
        new ProcessInputFilterNode(input,whichPhase,this).processInputSliceNode();

    }
    
    /**
     * Process all filter slice nodes in a Slice (just one in a SimpleSlice): find the correct ProcElement(s) and add filter code.
     * please delegate work to some other object.
     * @param slice           Slice containing filters
     * @param whichPhase      INIT / PRIMEPUMP / STEADY
     * @param computeNodes    the available compute nodes.
     */
    @Override
    public void processFilterSlices(Filter slice, 
            SchedulingPhase whichPhase, UniProcessors computeNodes) {
        throw new AssertionError("processFilterSlices called, back end should be calling processFilterSlice(singular)");
        
    }

    /**
     * Process a filter slice node: find the correct ProcElement(s) and add code and buffers.
     * please delegate work to some other object.
     * @param filter          the FilterSliceNode.
     * @param whichPhase      INIT / PRIMEPUMP / STEADY
     * @param computeNodes    the available compute nodes.
     */

    public void processFilterWorkNode(WorkNode filter,
            SchedulingPhase whichPhase, UniProcessors computeNodes) {
        new ProcessFilterWorkNode(filter,whichPhase,this).processFilterSliceNode();
    }

    /**
     * Process an output slice node: find the correct ProcElement(s) and add splitter code, and buffers.
     * please delegate work to some other object.
     * @param output          the OutputSliceNode.
     * @param whichPhase      INIT / PRIMEPUMP / STEADY
     * @param computeNodes    the available compute nodes.
     */
    @Override
    public void processFilterOutputNode(OutputNode output,
            SchedulingPhase whichPhase, UniProcessors computeNodes) {
        new ProcessOutputFilterNode(output,whichPhase,this).processOutputSliceNode();

    }

    private GetOrMakeChannel channelTypeSelector = new GetOrMakeChannel(this);
    
    @Override
    public IntraSSGChannel getChannel(IntraSSGEdge e) {
        return channelTypeSelector.getOrMakeChannel(e);
    }

    @Override
    public IntraSSGChannel getChannel(InternalFilterNode src, InternalFilterNode dst) {
        throw new AssertionError("Getting channel by src, dst not supported.");
    }
    

    /** name of variable used as bound for number of iterations. */
    public static final String iterationBound = "_iteration_bound";

    @Override
    public CodeStoreHelper getCodeStoreHelper(InternalFilterNode node) {
        if (node instanceof WorkNode) {
            // simply do appropriate wrapping of calls...
            return new CodeStoreHelperSimple((WorkNode)node,this);
        } else if (node instanceof InputNode) {
            // CodeStoreHelper that does not expect a work function.
            // Can we combine with above?
            return new CodeStoreHelperJoiner((InputNode)node, this);
        } else {
            return new CodeStoreHelperSplitter((OutputNode)node,this);
        }
        
    }

}
