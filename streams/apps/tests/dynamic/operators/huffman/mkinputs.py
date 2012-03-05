#!/usr/bin/env python
import os
import struct
import random

    
def main():
    input_text =  "this is an example of a huffman tree"
    with open('tmp.bin', 'wb') as f:        
        for c in input_text:
            print c
            x = ord(c)
            print x
            val = struct.pack('i', x)
            f.write(val)

    # print 'hello'
    # with open('tmp.bin', 'wb') as f:        
    #     for i in range(1, 100):
    #         #r = random.uniform(1, 10)            
    #         x = random.randint(1, 10)
    #         for j in range(1, x):
    #             print str(i) +  ',' +  str(x)
    #             time = struct.pack('f', i)
    #             val = struct.pack('f', x)
    #             f.write(time)
    #             f.write(val)

                    
if __name__ == "__main__":
    main()
