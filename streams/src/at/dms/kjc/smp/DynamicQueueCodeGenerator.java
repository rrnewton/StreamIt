package at.dms.kjc.smp;

import java.io.FileWriter;
import java.io.IOException;
import at.dms.kjc.JEmittedTextExpression;
import at.dms.kjc.JExpressionStatement;
import at.dms.kjc.KjcOptions;

public class DynamicQueueCodeGenerator {

	private StringBuffer hBuffer;
	private StringBuffer cBuffer;

	public DynamicQueueCodeGenerator() { 
		hBuffer = new StringBuffer();
		cBuffer = new StringBuffer();

		hBuffer.append("#ifndef DYNAMIC_QUEUE_H\n");
		hBuffer.append("#define DYNAMIC_QUEUE_H\n\n");
		hBuffer.append("#include <pthread.h>\n\n");
		
		cBuffer.append("#include \"dynamic_queue.h\"\n");
        cBuffer.append("#include \"globals.h\"\n");
		cBuffer.append("#include <stdlib.h>\n");
		cBuffer.append("#include <stdio.h>\n");
		cBuffer.append("#include <string.h>\n\n");
		cBuffer.append("#define CAPACITY 1024\n\n");
		
	}	
	
	public void writeToFiles() {
		try {
			hBuffer.append("\n\n#endif\n");
			FileWriter fw = new FileWriter("dynamic_queue.h");
			fw.write(hBuffer.toString());
			fw.close();
		}
		catch (IOException e) {
			System.err.println("Error writing dynamic_queue.h file!");
		}
		try {			
			FileWriter fw = new FileWriter("dynamic_queue.c");
			fw.write(cBuffer.toString());
			fw.close();
		}
		catch (IOException e) {
			System.err.println("Error writing dynamic_queue.c file!");
		}
	}

	private void addGrow(String type) {
		hBuffer.append("int " + type + "_queue_grow(" + type + "_queue_ctx_ptr q);\n");
		
		cBuffer.append("int\n");
		cBuffer.append(type + "_queue_grow(" + type + "_queue_ctx_ptr q) {\n");
		cBuffer.append("  int old_first = q->first;\n");
		cBuffer.append("  int old_last = q->last;\n");
		cBuffer.append("  int old_capacity = q->capacity;\n");
		cBuffer.append("  int old_size = q->size;\n");
		cBuffer.append("  " + type + "* old_buffer = q->buffer;\n");
		cBuffer.append("  int capacity = q->capacity * 2;\n");
		cBuffer.append("  q->buffer = (" + type + "*)malloc(sizeof(" + type + ") * capacity );\n");
		cBuffer.append("  if (q->buffer == NULL) {\n");
		cBuffer.append("    fprintf(stderr,\"" + type + "_queue_create unable to allocate memory\");\n");
		cBuffer.append("    exit(1);\n");
		cBuffer.append("  }\n");
		cBuffer.append("  q->capacity = capacity;\n");
		cBuffer.append("  q->max = capacity - 1;\n");
		cBuffer.append("  q->size = 0;\n");
		cBuffer.append("  q->first = 1;\n");
		cBuffer.append("  q->last = 0;\n");
		cBuffer.append("  if (old_last < old_first) {\n");
		cBuffer.append("    memcpy(&q->buffer[1], &old_buffer[old_first], (old_capacity - old_first) * sizeof(" + type + "));\n");
		cBuffer.append("    memcpy(&q->buffer[(old_capacity - old_first) + 1], &old_buffer[0], (old_last + 1) * sizeof(" + type + "));\n");
		cBuffer.append("  } else {\n");
		cBuffer.append("    memcpy(&q->buffer[1], &old_buffer[1], old_size * sizeof(" + type + "));\n");
		cBuffer.append("  }\n");
		cBuffer.append("  q->first = 1;\n");
		cBuffer.append("  q->last = old_size ;\n");
		cBuffer.append("  q->size = old_size;\n");
		cBuffer.append("  return capacity;\n");
		cBuffer.append("}\n\n");
				
	}
	
	private void addPush(String type) {
		hBuffer.append("void " + type + "_queue_push(" + type + "_queue_ctx_ptr q, " + type + " elem);\n");
	
		cBuffer.append("void\n");
		cBuffer.append(type + "_queue_push(" + type + "_queue_ctx_ptr q, " + type + " elem) {\n");
        cBuffer.append("  pthread_mutex_lock(&q->lock);\n");
		cBuffer.append("  if (q->size == q->capacity) {\n");
		cBuffer.append("    " + type + "_queue_grow(q);\n");
		cBuffer.append("  }\n");
		cBuffer.append("  q->size++;\n");
		cBuffer.append("  q->last = (q->last + 1) & q->max ;\n");
		cBuffer.append("  q->buffer[q->last] = elem;\n");
		cBuffer.append("  pthread_mutex_unlock(&q->lock);\n");
		cBuffer.append("}\n\n");
	
	}

	private void addPeek(String type) {
		hBuffer.append(type + " " + type + "_queue_peek(" + type + "_queue_ctx_ptr q, int threadIndex, int num_multipliers, int ** multipliers, int index);\n");			
		cBuffer.append(type + " " + type + "_queue_peek(" + type + "_queue_ctx_ptr q, int threadIndex, int num_multipliers, int ** multipliers, int index) {\n");				
		cBuffer.append("  pthread_mutex_lock(&thread_mutexes[threadIndex][DYN_READER]);\n");
        cBuffer.append("  int i = 0;\n");
        cBuffer.append("  int size = 0;\n");
		
        cBuffer.append("  pthread_mutex_lock(&q->lock);\n");
        cBuffer.append("  size = q->size;\n");
        cBuffer.append("  pthread_mutex_unlock(&q->lock);\n");
        
        cBuffer.append("  while (size <= index) {\n");		
		cBuffer.append("    for (i = 0; i < num_multipliers; i++) {\n");
		cBuffer.append("       *multipliers[i] = 0;\n");
		cBuffer.append("    }\n");		
        
		cBuffer.append("    pthread_mutex_lock(&thread_mutexes[threadIndex][MASTER]);\n");
		cBuffer.append("    thread_to_sleep[threadIndex][MASTER] = AWAKE;\n");
		cBuffer.append("    pthread_mutex_unlock(&thread_mutexes[threadIndex][MASTER]);\n");
		cBuffer.append("    pthread_cond_signal(&thread_conds[threadIndex][MASTER]);\n");
		cBuffer.append("    pthread_cond_wait(&thread_conds[threadIndex][DYN_READER], &thread_mutexes[threadIndex][DYN_READER]);\n");
        cBuffer.append("    pthread_mutex_lock(&q->lock);\n");
        cBuffer.append("    size = q->size;\n");
        cBuffer.append("    pthread_mutex_unlock(&q->lock);\n");        		
		cBuffer.append("  }\n");
		cBuffer.append("  pthread_mutex_unlock(&thread_mutexes[threadIndex][DYN_READER]);\n");
		cBuffer.append("  pthread_mutex_lock(&q->lock);\n");
		cBuffer.append("  int x = (q->first + index) & q->max;\n");
		cBuffer.append("  " + type + " elem = q->buffer[x];\n");		
		cBuffer.append("  for (i = 0; i < num_multipliers; i++) {\n");
		cBuffer.append("     *multipliers[i] = 1;\n");
		cBuffer.append("  }\n");		
        cBuffer.append("  pthread_mutex_unlock(&q->lock);\n");
		cBuffer.append("  return elem;\n");
		cBuffer.append("}\n\n");		
	}
	
	private void addPopMany(String type) {
		hBuffer.append("void " + type + "_queue_pop_many(" + type + "_queue_ctx_ptr q, int threadIndex, int num_multipliers, int ** multipliers, int amount);\n");			
		cBuffer.append("void " + type + "_queue_pop_many(" + type + "_queue_ctx_ptr q, int threadIndex, int num_multipliers, int ** multipliers, int amount) {\n");				
		cBuffer.append("  int i = 0;\n");
		cBuffer.append("  for (i = 0; i < amount; i++) {\n");
		cBuffer.append("    " + type + "_queue_pop(q, threadIndex, num_multipliers, multipliers);\n");				
		cBuffer.append("  }\n");		
		cBuffer.append("}\n\n");		
	}
			
	
	public void addPopSource(String type) {
        hBuffer.append(type + " " + type + "_queue_pop_source();\n");          
        cBuffer.append("static int fileReadIndex__0 = 0;\n");
        cBuffer.append(type + " " + type + "_queue_pop_source() {\n");           
        cBuffer.append("  " + type + " elem = fileReadBuffer[fileReadIndex__0];\n");
        cBuffer.append("  fileReadIndex__0++;\n");
        if (KjcOptions.perftest) {
            cBuffer.append("  perfTestNumInputs++;\n");        
        }
        cBuffer.append("  if(fileReadIndex__0 + 1 >= num_inputs) fileReadIndex__0 = 0;\n");
        cBuffer.append("  return elem;\n");
        cBuffer.append("}\n");
	}
	
	private void addPop(String type) {
		hBuffer.append(type + " " + type + "_queue_pop(" + type + "_queue_ctx_ptr q, int threadIndex, int num_multipliers, int ** multipliers);\n");			
		cBuffer.append(type + " " + type + "_queue_pop(" + type + "_queue_ctx_ptr q, int threadIndex, int num_multipliers, int ** multipliers) {\n");		

		// start of new pop implementation
		cBuffer.append("  pthread_mutex_lock(&thread_mutexes[threadIndex][DYN_READER]);\n");
        cBuffer.append("  int i = 0;\n");
        cBuffer.append("  int size = 0;\n");
        cBuffer.append("  pthread_mutex_lock(&q->lock);\n");
        cBuffer.append("  size = q->size;\n");
        cBuffer.append("  pthread_mutex_unlock(&q->lock);\n");        
        
        cBuffer.append("  while ((size == 0)) {\n");
		
		cBuffer.append("    for (i = 0; i < num_multipliers; i++) {\n");
		cBuffer.append("       *multipliers[i] = 0;\n");
		cBuffer.append("    }\n");
				
		cBuffer.append("    pthread_mutex_lock(&thread_mutexes[threadIndex][MASTER]);\n");
		cBuffer.append("    thread_to_sleep[threadIndex][MASTER] = AWAKE;\n");
		cBuffer.append("    pthread_mutex_unlock(&thread_mutexes[threadIndex][MASTER]);\n");
		cBuffer.append("    pthread_cond_signal(&thread_conds[threadIndex][MASTER]);\n");
		cBuffer.append("    pthread_cond_wait(&thread_conds[threadIndex][DYN_READER], &thread_mutexes[threadIndex][DYN_READER]);\n");
        
        cBuffer.append("    pthread_mutex_lock(&q->lock);\n");
        cBuffer.append("    size = q->size;\n");
        cBuffer.append("    pthread_mutex_unlock(&q->lock);\n");
        
		cBuffer.append("  }\n");
		cBuffer.append("  pthread_mutex_unlock(&thread_mutexes[threadIndex][DYN_READER]);\n");

        cBuffer.append("  pthread_mutex_lock(&q->lock);\n");
		// end of new pop implementation
		cBuffer.append("  " + type + " elem = q->buffer[q->first];\n");
		cBuffer.append("  q->size--;\n");
		cBuffer.append("  q->first = (q->first + 1) & q->max;\n");
		
		cBuffer.append("  for (i = 0; i < num_multipliers; i++) {\n");
		cBuffer.append("     *multipliers[i] = 1;\n");
		cBuffer.append("  }\n");
		
        cBuffer.append("  pthread_mutex_unlock(&q->lock);\n");
		cBuffer.append("  return elem;\n");
		cBuffer.append("}\n");
	
	}

	private void addCtx(String type) {
		hBuffer.append("struct " + type +"_queue_ctx {\n");
		hBuffer.append("  " + type + "* buffer;\n");
		hBuffer.append("  int    size;\n");
		hBuffer.append("  int    first;\n");
		hBuffer.append("  int    last;\n");
		hBuffer.append("  int    capacity;\n");
		hBuffer.append("  int    max;\n");
		hBuffer.append("  pthread_mutex_t lock;\n");
		hBuffer.append("};\n\n");
		hBuffer.append("typedef struct " + type +"_queue_ctx * "+ type +"_queue_ctx_ptr;\n\n");
	}

	public void addQueueType(String type) {
		addCtx(type);
		addCreate(type);
		addGrow(type);
		addPush(type);	
		addPop(type);		
		addPopMany(type);		
		addPeek(type);		
	}

	private void addCreate(String type) {
		hBuffer.append(type + "_queue_ctx_ptr " + type +"_queue_create();\n");				
		cBuffer.append(type + "_queue_ctx_ptr\n");
		cBuffer.append(type +"_queue_create() {\n");
		cBuffer.append("  " + type + "_queue_ctx_ptr q;\n");
		cBuffer.append("  q = (" + type + "_queue_ctx_ptr)malloc(sizeof(struct " + type + "_queue_ctx));\n");
		cBuffer.append("  if (q == NULL) {\n");
		cBuffer.append("    fprintf(stderr, \"" + type + "_queue_create unable to allocate memory\");\n");
		cBuffer.append("    exit(1);\n");
		cBuffer.append("  }\n");
		cBuffer.append("  q->buffer = (" + type + "*)malloc(sizeof(" + type + ") * CAPACITY);\n");
		cBuffer.append("  if (q->buffer == NULL) {\n");
		cBuffer.append("    fprintf(stderr,\"" + type + "_queue_create unable to allocate memory\");\n");
		cBuffer.append("    exit(1);\n");
		cBuffer.append("  }\n");
		cBuffer.append("  q->capacity = CAPACITY;\n");
		cBuffer.append("  q->max = CAPACITY - 1 ;\n");
		cBuffer.append("  q->size = 0;\n");
		cBuffer.append("  q->first = 1;\n");
		cBuffer.append("  q->last = 0;\n");
		cBuffer.append("  pthread_mutex_init(&q->lock, NULL);\n");
		cBuffer.append("  return q;\n");
		cBuffer.append("}\n\n");
	}
}
