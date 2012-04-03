package at.dms.kjc.smp;

import java.io.FileWriter;
import java.io.IOException;
import at.dms.kjc.KjcOptions;

public class DynamicQueueCodeGenerator {

    /**
     * An interface that corresponds to the API for
     * different queue implementations.
     * @author soule
     *
     */
    private interface Generator {
        void addCreate(String type);
        void addCtx(String type);
        void addGrow(String type);
        void addIncludes();
        void addPeek(String type);
        void addPeekSource(String type);
        void addPop(String type);      
        void addPopMany(String type);
        void addPopManySource(String type);
        void addPopSource(String type);
        void addPush(String type);
    }
    
    /**
     * A generator for a dynamic queue implementation with locking
     * @author soule
     *
     */
    public class LockedGeneratorThreadOpt implements Generator {

        public void addCreate(String type) {
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

        public void addCtx(String type) {
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

        public void addGrow(String type) {
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

        public void addIncludes() { 
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

        public void addPeek(String type) {
            hBuffer.append(type + " " + type + "_queue_peek(" + type + "_queue_ctx_ptr q, int threadIndex, int nextIndex, int num_multipliers, int ** multipliers, int index, int num_tokens, volatile int ** tokens);\n");            
            cBuffer.append(type + " " + type + "_queue_peek(" + type + "_queue_ctx_ptr q, int threadIndex, int nextIndex, int num_multipliers, int ** multipliers, int index, int num_tokens, volatile int ** tokens) {\n");               
            cBuffer.append("  pthread_mutex_lock(&thread_mutexes[threadIndex]);\n");
            cBuffer.append("  int i = 0;\n");
            cBuffer.append("  int size = 0;\n");
            cBuffer.append("  pthread_mutex_lock(&q->lock);\n");
            cBuffer.append("  size = q->size;\n");
            cBuffer.append("  pthread_mutex_unlock(&q->lock);\n");
            cBuffer.append("  while (size <= index) {\n");      
            cBuffer.append("    for (i = 0; i < num_multipliers; i++) {\n");
            cBuffer.append("       *multipliers[i] = 0;\n");
            cBuffer.append("    }\n");      
            cBuffer.append("    for (i = 0; i < num_tokens; i++) {\n");
            cBuffer.append("       *tokens[i] = 1;\n");
            cBuffer.append("    }\n");
            cBuffer.append("    pthread_mutex_lock(&thread_mutexes[nextIndex]);\n");
            cBuffer.append("    thread_to_sleep[nextIndex] = AWAKE;\n");
            cBuffer.append("    pthread_mutex_unlock(&thread_mutexes[nextIndex]);\n");
            cBuffer.append("    pthread_cond_signal(&thread_conds[nextIndex]);\n");
            cBuffer.append("    pthread_cond_wait(&thread_conds[threadIndex], &thread_mutexes[threadIndex]);\n");
            cBuffer.append("    pthread_mutex_lock(&q->lock);\n");
            cBuffer.append("    size = q->size;\n");
            cBuffer.append("    pthread_mutex_unlock(&q->lock);\n");                
            cBuffer.append("  }\n");
            cBuffer.append("  pthread_mutex_unlock(&thread_mutexes[threadIndex]);\n");
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

        public void addPeekSource(String type) {
            hBuffer.append(type + " " + type + "_queue_peek_source(int index);\n");                 
            cBuffer.append(type + " " + type + "_queue_peek_source(int index) {\n");    
            cBuffer.append("  return fileReadBuffer[(fileReadIndex__0 + index) & num_inputs];\n");
            cBuffer.append("}\n");
        }

        public void addPop(String type) {
            hBuffer.append(type + " " + type + "_queue_pop(" + type + "_queue_ctx_ptr q, int threadIndex, int nextIndex, int num_multipliers, int ** multipliers, int num_tokens, volatile int ** tokens);\n");            
            cBuffer.append(type + " " + type + "_queue_pop(" + type + "_queue_ctx_ptr q, int threadIndex, int nextIndex, int num_multipliers, int ** multipliers, int num_tokens, volatile int ** tokens) {\n");       
            cBuffer.append("  pthread_mutex_lock(&thread_mutexes[threadIndex]);\n");
            cBuffer.append("  int i = 0;\n");
            cBuffer.append("  int size = 0;\n");
            cBuffer.append("  pthread_mutex_lock(&q->lock);\n");
            cBuffer.append("  size = q->size;\n");
            cBuffer.append("  pthread_mutex_unlock(&q->lock);\n");        
            cBuffer.append("  while ((size == 0)) {\n");
            cBuffer.append("    for (i = 0; i < num_multipliers; i++) {\n");
            cBuffer.append("       *multipliers[i] = 0;\n");
            cBuffer.append("    }\n");
            cBuffer.append("    for (i = 0; i < num_tokens; i++) {\n");
            cBuffer.append("       *tokens[i] = 1;\n");
            cBuffer.append("    }\n");
            cBuffer.append("    pthread_mutex_lock(&thread_mutexes[nextIndex]);\n");
            cBuffer.append("    thread_to_sleep[nextIndex] = AWAKE;\n");
            cBuffer.append("    pthread_mutex_unlock(&thread_mutexes[nextIndex]);\n");
            cBuffer.append("    pthread_cond_signal(&thread_conds[nextIndex]);\n");
            cBuffer.append("    pthread_cond_wait(&thread_conds[threadIndex], &thread_mutexes[threadIndex]);\n");
            cBuffer.append("    pthread_mutex_lock(&q->lock);\n");
            cBuffer.append("    size = q->size;\n");
            cBuffer.append("    pthread_mutex_unlock(&q->lock);\n");
            cBuffer.append("  }\n");
            cBuffer.append("  pthread_mutex_unlock(&thread_mutexes[threadIndex]);\n");
            cBuffer.append("  pthread_mutex_lock(&q->lock);\n");
            cBuffer.append("  " + type + " elem = q->buffer[q->first];\n");
            cBuffer.append("  q->size--;\n");
            cBuffer.append("  q->first = (q->first + 1) & q->max;\n");
            cBuffer.append("  for (i = 0; i < num_multipliers; i++) {\n");
            cBuffer.append("      *multipliers[i] = 1;\n");
            cBuffer.append("  }\n");
            cBuffer.append("  pthread_mutex_unlock(&q->lock);\n");
            cBuffer.append("  return elem;\n");
            cBuffer.append("}\n");
        }

        public void addPopMany(String type) {
            hBuffer.append("void " + type + "_queue_pop_many(" + type + "_queue_ctx_ptr q, int threadIndex, int nextIndex, int num_multipliers, int ** multipliers, int amount, int num_tokens, volatile int ** tokens);\n");          
            cBuffer.append("void " + type + "_queue_pop_many(" + type + "_queue_ctx_ptr q, int threadIndex, int nextIndex, int num_multipliers, int ** multipliers, int amount, int num_tokens, volatile int ** tokens) {\n");             
            cBuffer.append("  int i = 0;\n");
            cBuffer.append("  for (i = 0; i < amount; i++) {\n");
            cBuffer.append("    " + type + "_queue_pop(q, threadIndex, nextIndex, num_multipliers, multipliers, num_tokens, tokens);\n");              
            cBuffer.append("  }\n");        
            cBuffer.append("}\n\n");        
        }

        public void addPopManySource(String type) {
            hBuffer.append("void " + type + "_queue_pop_many_source(int amount);\n");          
            cBuffer.append("void " + type + "_queue_pop_many_source(int amount) {\n");             
            cBuffer.append("  int i = 0;\n");
            cBuffer.append("  for (i = 0; i < amount; i++) {\n");
            cBuffer.append("    " + type + "_queue_pop_source();\n");              
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
            cBuffer.append("  if(fileReadIndex__0 + 1 > num_inputs) fileReadIndex__0 = 0;\n");
            cBuffer.append("  return elem;\n");
            cBuffer.append("}\n");
        }

        public void addPush(String type) {
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
    }
    
    /**
     * A generator for a dynamic queue implementation with locking
     * @author soule
     *
     */
    public class LockedGenerator implements Generator {

        public void addCreate(String type) {
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

        public void addCtx(String type) {
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

        public void addGrow(String type) {
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

        public void addIncludes() { 
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

        public void addPeek(String type) {
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
            cBuffer.append("      *multipliers[i] = 1;\n");
            cBuffer.append("  }\n");        
            cBuffer.append("  pthread_mutex_unlock(&q->lock);\n");
            cBuffer.append("  return elem;\n");
            cBuffer.append("}\n\n");        
        }

        public void addPeekSource(String type) {
            hBuffer.append(type + " " + type + "_queue_peek_source(int index);\n");                 
            cBuffer.append(type + " " + type + "_queue_peek_source(int index) {\n");    
            cBuffer.append("  return fileReadBuffer[(fileReadIndex__0 + index) & num_inputs];\n");
            cBuffer.append("}\n");
        }

        public void addPop(String type) {
            hBuffer.append(type + " " + type + "_queue_pop(" + type + "_queue_ctx_ptr q, int threadIndex, int num_multipliers, int ** multipliers);\n");            
            cBuffer.append(type + " " + type + "_queue_pop(" + type + "_queue_ctx_ptr q, int threadIndex, int num_multipliers, int ** multipliers) {\n");       
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

        public void addPopMany(String type) {
            hBuffer.append("void " + type + "_queue_pop_many(" + type + "_queue_ctx_ptr q, int threadIndex, int num_multipliers, int ** multipliers, int amount);\n");          
            cBuffer.append("void " + type + "_queue_pop_many(" + type + "_queue_ctx_ptr q, int threadIndex, int num_multipliers, int ** multipliers, int amount) {\n");             
            cBuffer.append("  int i = 0;\n");
            cBuffer.append("  for (i = 0; i < amount; i++) {\n");
            cBuffer.append("    " + type + "_queue_pop(q, threadIndex, num_multipliers, multipliers);\n");              
            cBuffer.append("  }\n");        
            cBuffer.append("}\n\n");        
        }

        public void addPopManySource(String type) {
            hBuffer.append("void " + type + "_queue_pop_many_source(int amount);\n");          
            cBuffer.append("void " + type + "_queue_pop_many_source(int amount) {\n");             
            cBuffer.append("  int i = 0;\n");
            cBuffer.append("  for (i = 0; i < amount; i++) {\n");
            cBuffer.append("    " + type + "_queue_pop_source();\n");              
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
            cBuffer.append("  if(fileReadIndex__0 + 1 > num_inputs) fileReadIndex__0 = 0;\n");
            cBuffer.append("  return elem;\n");
            cBuffer.append("}\n");
        }

        public void addPush(String type) {
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
    }

    /**
     * A generator for a lock-free dynamic queue implementation.
     * @author soule
     *
     */
    public class LockFreeGenerator implements Generator {

        public void addCreate(String type)  { 
            System.out.println("DynamicQueueCodeGenerator.LockFreeGenerator.addCreate()");
            hBuffer.append(type + "_queue_ctx_ptr " + type +"_queue_create();\n");              
            cBuffer.append(type + "_queue_ctx_ptr\n");
            cBuffer.append(type +"_queue_create() {\n");
            cBuffer.append("    " + type + "_queue_ctx_ptr q;\n");
            cBuffer.append("    q = (" + type + "_queue_ctx_ptr)malloc(sizeof(struct " + type + "_queue_ctx));\n");
            cBuffer.append("    assert (q != NULL);\n");
            cBuffer.append("    q->capacity = SIZE+1;\n");
            cBuffer.append("    q->tail = 0;\n");
            cBuffer.append("    q->head = 0;\n");
            cBuffer.append("    q->array = (float*)malloc(sizeof(" + type + ") * q->capacity);\n");
            cBuffer.append("    return q;\n");
            cBuffer.append("    }\n");
        }
        
        public void addCtx(String type) { 
            System.out.println("DynamicQueueCodeGenerator.LockFreeGenerator.addCtx()");
            hBuffer.append("struct " + type + "_queue_ctx {\n");
            hBuffer.append("    volatile unsigned int tail;\n"); 
            hBuffer.append("    " + type + " * array;\n");
            hBuffer.append("    unsigned int capacity;\n");
            hBuffer.append("    int    max;\n");
            hBuffer.append("    volatile unsigned int head;\n");            
            hBuffer.append("};\n\n");
            hBuffer.append("typedef struct " + type +"_queue_ctx * "+ type +"_queue_ctx_ptr;\n\n");
        }

        public void addGrow(String type) { 
            System.out.println("DynamicQueueCodeGenerator.LockFreeGenerator.addGrow()");
        }
        
        public void addIncludes() {
            System.out.println("DynamicQueueCodeGenerator.LockFreeGenerator.addIncludes()");
            hBuffer.append("#ifndef DYNAMIC_QUEUE_H\n");
            hBuffer.append("#define DYNAMIC_QUEUE_H\n\n");
            hBuffer.append("#include <pthread.h>\n\n");
            cBuffer.append("#include \"dynamic_queue.h\"\n");
            cBuffer.append("#include \"globals.h\"\n");
            cBuffer.append("#include <stdlib.h>\n");
            cBuffer.append("#include <stdio.h>\n");
            cBuffer.append("#include <assert.h>\n");
            cBuffer.append("#include <string.h>\n\n");
            cBuffer.append("#define SIZE 1023\n\n");
        }
        public void addPeek(String type)   { 
            System.out.println("DynamicQueueCodeGenerator.LockFreeGenerator.addPeek()");
        }
        public void addPeekSource(String type)   { 
            System.out.println("DynamicQueueCodeGenerator.LockFreeGenerator.addPeekSource()");
        }
        public void addPop(String type)  { 
            System.out.println("DynamicQueueCodeGenerator.LockFreeGenerator.addPop()");
            hBuffer.append(type + " " + type + "_queue_pop(" + type + "_queue_ctx_ptr q, int threadIndex, int num_multipliers, int ** multipliers);\n");            
            cBuffer.append(type + " " + type + "_queue_pop(" + type + "_queue_ctx_ptr q, int threadIndex, int num_multipliers, int ** multipliers) {\n");       
            cBuffer.append("    while(q->head == q->tail) {\n");            
            cBuffer.append("    int i = 0;");
            cBuffer.append("    for (i = 0; i < num_multipliers; i++) {\n");
            cBuffer.append("       *multipliers[i] = 0;\n");
            cBuffer.append("    }\n");
            cBuffer.append("    pthread_mutex_lock(&thread_mutexes[threadIndex][MASTER]);\n");
            cBuffer.append("    thread_to_sleep[threadIndex][MASTER] = AWAKE;\n");
            cBuffer.append("    pthread_mutex_unlock(&thread_mutexes[threadIndex][MASTER]);\n");
            cBuffer.append("    pthread_cond_signal(&thread_conds[threadIndex][MASTER]);\n");
            cBuffer.append("    pthread_cond_wait(&thread_conds[threadIndex][DYN_READER], &thread_mutexes[threadIndex][DYN_READER]);\n");
            cBuffer.append("  }\n");
            cBuffer.append("  pthread_mutex_unlock(&thread_mutexes[threadIndex][DYN_READER]);\n");
            cBuffer.append("    float item_ = q->array[q->head];\n");
            cBuffer.append("    q->head = (q->head+1) & SIZE ;\n");            
            cBuffer.append("    return item_;\n");
            cBuffer.append("}\n");
        }    
        public void addPopMany(String type)   { 
            System.out.println("DynamicQueueCodeGenerator.LockFreeGenerator.addPopMany()");
        }
        public void addPopManySource(String type)   { 
            System.out.println("DynamicQueueCodeGenerator.LockFreeGenerator.addPopManySource()");
        }
        public void addPopSource(String type)  { 
            System.out.println("DynamicQueueCodeGenerator.LockFreeGenerator.addPopSource()");
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
        public void addPush(String type)  { 
            System.out.println("DynamicQueueCodeGenerator.LockFreeGenerator.addPush()");
            hBuffer.append("void " + type + "_queue_push(" + type + "_queue_ctx_ptr q, " + type + " elem);\n");
            cBuffer.append("void\n");
            cBuffer.append(type + "_queue_push(" + type + "_queue_ctx_ptr q, " + type + " elem) {\n");           
            cBuffer.append("    int nextTail = (q->tail+1) & SIZE ;\n");                       
            cBuffer.append("    if(nextTail != q->head) {\n");
            cBuffer.append("        q->array[q->tail] = elem;\n");
            cBuffer.append("        q->tail = nextTail;\n");
            cBuffer.append("    } else {\n");
            cBuffer.append("        // queue was full\n");
            cBuffer.append("        // return false;\n");
            cBuffer.append("        assert(false);\n");
            cBuffer.append("    }\n");
            cBuffer.append("}\n");
        }
    }  

    private StringBuffer hBuffer;
    private StringBuffer cBuffer;
    private Generator generator;

    public DynamicQueueCodeGenerator() {
        hBuffer = new StringBuffer();
        cBuffer = new StringBuffer();
        if (KjcOptions.lockfree) {          
            generator = new LockFreeGenerator();
        } else if (KjcOptions.threadopt) {
            generator = new LockedGeneratorThreadOpt();
        } else {
            generator = new LockedGenerator();
        }
        generator.addIncludes();
    }


    public void addQueueType(String type) {
        generator.addCtx(type);
        generator.addCreate(type);
        generator.addGrow(type);
        generator.addPush(type);  
        generator.addPop(type);       
        generator.addPopMany(type);       
        generator.addPeek(type);      
    }

    public void addSource(String type) {
        generator.addPopSource(type);
        generator.addPopManySource(type);
        generator.addPeekSource(type);
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


}