#include "queue.h"
#include <stdlib.h>

struct queue_ctx {
  void** buffer;
  int    size;
  int    first;
  int    last;
  int    capacity;
};

queue_ctx_ptr 
queue_create(int capacity) { 
  queue_ctx_ptr q;  
  log("queue_::queue_create");
  if (capacity < 0) {
    error("queue_::queue_create capacity is too small");
    exit(1);
  }
  q = (queue_ctx*)malloc(sizeof(struct queue_ctx));
  if (q == NULL) {
    error("queue_::queue_create unable to allocate memory");
    exit(1);
  }
  q->buffer = (void**)malloc(sizeof(void*) * capacity);
  if (q->buffer == NULL) {
    error("queue_::queue_create unable to allocate memory");
    exit(1);
  }
  q->capacity = capacity;
  queue_clear(q);
  return q;
}

void        
queue_destroy(queue_ctx_ptr q) { 
  log("queue_::queue_destroy");
  if (q != NULL) {
    free(q->buffer);
    free(q);
  }
}

static int next(int i, queue_ctx_ptr q) {
  //return (++i) % q->capacity;
  if (++i == q->capacity) {
    i = 0;
  }
  return i;
}

int        
queue_push(queue_ctx_ptr q, void* elem) { 
  // log("queue::queue_push");
  if (queue_is_full(q)) {
    error("queue::queue_push queue is full.");
    return 1;
  } else {
    q->size++;
    q->last = next(q->last, q);
    q->buffer[q->last] = elem;
    printf("queue::queue_push first %d\n", q->first);
    printf("queue::queue_push last %d\n", q->last);
    return 0;
  }
}

void*       
queue_pop(queue_ctx_ptr q) { 
  // log("queue::queue_pop");
  if (queue_is_empty(q)) {
    error("queue::queue_pop queue is empty.");
    return NULL;
  } else {
    void * elem = q->buffer[q->first];
    printf("queue::queue_pop first %d\n", q->first);
    printf("queue::queue_pop last %d\n", q->last);
    q->size--;
    q->first = next(q->first, q);
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
queue_is_empty(queue_ctx_ptr q) { 
  return (q->size == 0); 
}

int         
queue_is_full(queue_ctx_ptr q) { 
  return (q->size == q->capacity); 
}

