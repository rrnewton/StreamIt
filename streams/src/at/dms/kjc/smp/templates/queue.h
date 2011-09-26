#include <stdio.h>
#include <stdlib.h>

#ifndef _Queue_h_
#define _Queue_h_

#define error(str)        fprintf(stderr, "%s\n", str)
#define log(str)          fprintf(stdout, "%s\n", str)
#define TYPE int

struct queue_ctx {
  TYPE* buffer;
  int    size;
  int    first;
  int    last;
  int    capacity;
  int    max; /* always capacity - 1 */
};

typedef struct queue_ctx * queue_ctx_ptr;

//queue_ctx_ptr queue_create(int capacity);
queue_ctx_ptr queue_create();
void          queue_destroy(queue_ctx_ptr q);
int    queue_push(queue_ctx_ptr q, TYPE elem);
TYPE   queue_pop(queue_ctx_ptr q, int * success);
void          queue_clear(queue_ctx_ptr q);
int           queue_grow(queue_ctx_ptr q);
void          queue_print(queue_ctx_ptr q);

#endif /* _Queue_h_ */
