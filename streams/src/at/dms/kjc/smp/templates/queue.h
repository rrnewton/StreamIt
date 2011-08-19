#include <stdio.h>
#include <stdlib.h>

#ifndef _Queue_h_

#define error(str)        fprintf(stderr, "%s\n", str)
#define log(str)          fprintf(stdout, "%s\n", str)

struct queue_ctx ;
typedef struct queue_ctx * queue_ctx_ptr;

queue_ctx_ptr queue_create(int capacity);
void          queue_destroy(queue_ctx_ptr q);
int           queue_push(queue_ctx_ptr q, void* elem);
void*         queue_pop(queue_ctx_ptr q);
void          queue_clear(queue_ctx_ptr q);
int           queue_is_empty(queue_ctx_ptr q);
int           queue_is_full(queue_ctx_ptr q);

#endif /* _Queue_h_ */
