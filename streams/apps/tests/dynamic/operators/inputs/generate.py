#!/usr/bin/env python
import os
import struct

    
def main():
    print 'hello'
    with open('tmp.bin', 'wb') as f:        
        for i in range(1, 1000):
            s = struct.pack('f', 1.0)
            f.write(s)

                    
if __name__ == "__main__":
    main()
