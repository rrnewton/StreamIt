#!/usr/bin/env python
import os
import struct
import random

    
def main():
    print 'hello'
    with open('tmp.bin', 'wb') as f:        
        for i in range(1, 1000):
            s = struct.pack('f', 1.0)
            f.write(s)
            r = random.uniform(1, 10)
            print r
            x = random.randint(1, 10) 
            print x
                    
if __name__ == "__main__":
    main()
