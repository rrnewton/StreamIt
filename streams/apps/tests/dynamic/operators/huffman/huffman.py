#!/usr/bin/env python
import os
import struct
import random
import numpy
import heapq
import itertools

# For the priority queue

pq = []                         # list of entries arranged in a heap
entry_finder = {}               # mapping of tasks to entries
REMOVED = '<removed-task>'      # placeholder for a removed task
counter = itertools.count()
uniqueidx = -1
    
def add_task(task, priority=0):
    'Add a new task or update the priority of an existing task'
    if task in entry_finder:
        remove_task(task)
    count = next(counter)
    entry = [priority, count, task]
    entry_finder[task] = entry
    heapq.heappush(pq, entry)

def remove_task(task):
    'Mark an existing task as REMOVED.  Raise KeyError if not found.'
    entry = entry_finder.pop(task)
    entry[-1] = REMOVED

def pop_task():
    'Remove and return the lowest priority task. Raise KeyError if empty.'
    while pq:
        priority, count, task = heapq.heappop(pq)
        if task is not REMOVED:
            del entry_finder[task]
            return priority, task
    raise KeyError('pop from an empty priority queue')

def freq(text):
    arr=numpy.zeros(26,numpy.float64)  
    for l in text:  
        x=ord(l)  
        if (x>=97 and x<=122):  
            arr[x-97]+=1.0  
    arr/=max(arr)
    return arr

def main():
    text = "this is an example of a huffman tree"
    arr = freq(text)
    uidx = -1.0
    h = []
    for idx, val in enumerate(arr):
        print val, idx+97
        if (val > 0):
            add_task(idx+97, val)
    print "---------"
    while len(pq) > 1:
        (p1, n1) = pop_task()
        (p2, n2) = pop_task()
        print  (p1, n1), (p2, n2)
        add_task(uidx, p1+p2)
        uidx = uidx - 1
                    
if __name__ == "__main__":
    main()
