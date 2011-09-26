#include "queue.h"
#include <stdlib.h>
#include <string.h>

#define CAPACITY 1024

queue_ctx_ptr 
// queue_create(int capacity) { 
queue_create() { 
  queue_ctx_ptr q;  
  /* if (capacity < 0) { */
  /*   error("queue_::queue_create capacity is too small"); */
  /*   exit(1); */
  /* } */
  q = (queue_ctx_ptr)malloc(sizeof(struct queue_ctx));
  if (q == NULL) {
    error("queue_::queue_create unable to allocate memory");
    exit(1);
  }
  q->buffer = (TYPE*)malloc(sizeof(TYPE) * CAPACITY);
  if (q->buffer == NULL) {
    error("queue_::queue_create unable to allocate memory");
    exit(1);
  }
  q->capacity = CAPACITY;
  q->max = CAPACITY - 1 ;
  queue_clear(q);
  return q;
}

void        
queue_destroy(queue_ctx_ptr q) { 
  if (q != NULL) {
    free(q->buffer);
    free(q);
  }
}

int        
queue_push(queue_ctx_ptr q, TYPE elem) { 
  if (q->size == q->capacity) {
    queue_grow(q);
  } 
  q->size++;

  q->last = (q->last + 1) & q->max ; 

  //  if (++(q->last) == q->last) {
  //  q->last = 0;
  // }

  q->buffer[q->last] = elem;
  return 0;  
}

TYPE       
queue_pop(queue_ctx_ptr q, int * success) { 
  if (q->size == 0) {
    error("queue::queue_pop queue is empty.");
    *success = 0;
    return 0;
  } else {
    TYPE elem = q->buffer[q->first];
    q->size--;

    q->first = (q->first + 1) & q->max ; 
    *success = 1;
    return elem;
  }
}

void        
queue_clear(queue_ctx_ptr q) {
  q->size = 0;
  q->first = 1;
  q->last = 0;
}

int        
queue_grow(queue_ctx_ptr q) { 

  int old_first = q->first;
  int old_last = q->last;
  int old_capacity = q->capacity;
  int old_size = q->size;
  TYPE* old_buffer = q->buffer;

  int capacity = q->capacity * 2;  
  q->buffer = (TYPE*)malloc(sizeof(TYPE) * capacity );
  if (q->buffer == NULL) {
    error("queue_::queue_grow unable to allocate memory");
    exit(1);
  }
  q->capacity = capacity;
  q->max = capacity - 1;
  q->size = 0;
  q->first = 1;
  q->last = 0;
  if (old_last < old_first) {
    memcpy(&q->buffer[1], &old_buffer[old_first], (old_capacity - old_first) * sizeof(TYPE));
    memcpy(&q->buffer[(old_capacity - old_first) + 1], &old_buffer[0], (old_last + 1) * sizeof(TYPE));
  } else {
    memcpy(&q->buffer[1], &old_buffer[1], old_size * sizeof(TYPE));
  }
  q->first = 1;
  q->last = old_size ;
  q->size = old_size;  
  return capacity;
}

void queue_print(queue_ctx_ptr q) { 
  int i, j = 0;
  j = q->first;
  printf ("first = %d, size = %d capacity = %d :: ", j, q->size, q->capacity);
  for (i=0; i < q->capacity; i++) {
    printf ("%d ", q->buffer[j]);
    if (++j == q->capacity) {
      j = 0;
    }
  }
  printf ("\n");
}
