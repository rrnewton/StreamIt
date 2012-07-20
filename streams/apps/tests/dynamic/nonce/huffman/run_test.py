#!/usr/bin/env python
import os
import subprocess
import time
import re
import struct
import math

FNULL = open('/dev/null', 'w')

class Configs:
    static, dynamic = range(2)

streamit_home = os.environ['STREAMIT_HOME']
strc          = os.path.join(streamit_home, 'strc')

def generate(outputs):
    print 'generate' 
    with open('ints.in', 'wb') as f:
        # this is an example of a huffman tree
        for j in range(outputs):
            for i in [116, 104, 105, 115, 32, 105, 115, 32, 97, 110,
                      32, 101, 120, 97, 109, 112, 108, 101, 32, 111,
                      102, 32, 97, 32, 104, 117, 102, 102, 109, 97,
                      110, 32, 116, 114, 101, 101 ] :
                val = struct.pack('i', i) 
                f.write(val) 
    
def compile(cores, test, outputs, ignore):
    cmd = ["strc", "-smp", str(cores), "--perftest", "--outputs", str(outputs), '--preoutputs', str(ignore), '--noiter', 'HuffmanStatic.str']    
    if test == Configs.dynamic:
        cmd = ["strc", "-smp", str(cores), "--perftest", "--outputs", str(outputs), '--preoutputs', str(ignore), "--threadopt", '--noiter', 'HuffmanDynamic.str']    
    print ' '.join(cmd)
    subprocess.call(cmd, stdout=FNULL, stderr=FNULL)
    exe = './smp' + str(cores)     
    assert os.path.exists(exe)


def run_one(test, core):
    exe = './smp' + str(core)     
    results = []
    if test == Configs.static:
        test_type = 'static'
    elif test == Configs.dynamic:
        test_type = 'dynamic'    
    (stdout, error) = subprocess.Popen([exe], stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
    regex = re.compile('input=(\d+) outputs=(\d+) ignored=(\d+) start=\d+:\d+ end=\d+:\d+ delta=(\d+):(\d+)')
    for m in regex.finditer(stdout):
        results = ([test_type] + [m.group(1)] + [m.group(2)] + [m.group(3)] + [m.group(4)] + [m.group(5)])       
        print results
    return results

def run(test, cores, attempts, outputs):
    results = []
    for num in range(attempts):
         result = run_one(test, cores)
         results.append(result)
         #for result in results:
         print result         
    # 1000000000 nanoseconds in 1 second    
    times = map(lambda x:  (long(x[4]) * 1000000000L) + long(x[5]) , results)
    tputs =  map(lambda x: (float(outputs)/float(x)) * 1000000000L , times)
    mean = reduce(lambda x, y: float(x) + float(y), tputs) / len(tputs)    
    deviations = map(lambda x: x - mean, tputs)
    squares = map(lambda x: x * x, deviations)
    dev = math.sqrt(reduce(lambda x, y: x + y, squares) /  (len(squares) - 1))
    print 'mean=' + str(mean) + ' dev=' + str(dev)
    return (mean, dev)

def print_all(static_results, dynamic_results):
    file = 'huffman.dat'
    with open(file, 'w') as f:
        s = '#selectivity\tsta-avg\tsta-dev\tdyn-avg\tdyn-dev'
        print s
        f.write(s + '\n')    
        for static, dynamic in zip(static_results, dynamic_results):
            s = '%d\t%f\t%f' % (static[1], static[2], static[3])
            s += '\t%f\t%f' % (dynamic[2], dynamic[3]) 
            print s
            f.write(s + '\n')
            
    # file = 'vwap-normalized.dat'
    # with open(file, 'w') as f:
    #     s = '#selectivity\t'
    #     s += '\t'.join(["%s-sta-norm\t%s-sta-dev" % (x[2], x[2]) for x in static_results[0]])
    #     s += '\t' + '\t'.join(["%s-dyn-avg\t%s-dyn-avg" % (x[2], x[2]) for x in dynamic_results[0]])
    #     print s
    #     f.write(s + '\n')    
    #     for static, dynamic in zip(static_results, dynamic_results):
    #         #base =  static[0][3]
    #         s = '%d\t' % (static[0][1]) 
    #         s += '\t'.join(["%f\t%f" % (d[3], d[4]) for d in static])
    #         s += '\t' + '\t'.join(["%f\t%f" % (d[3], d[4]) for d in dynamic])
    #         print s
    #         f.write(s + '\n')    
         

def plot(cores):
    data = 'huffman.dat'
    output = 'huffman.ps'  
    cmd = "plot "
    cmd += "\"" + data + "\" u 1:2 t \'static\' w linespoints lc rgb \"blue\""
    cmd += ", \"" + data + "\" u 1:2:3 notitle w yerrorbars"
    cmd += ", \"" + data + "\" u 1:4 t \'dynamic\' w linespoints lc rgb \"red\""
    cmd += ", \"" + data + "\" u 1:4:5 notitle w yerrorbars"  
    with open('./huffman.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key left center\n');
        #f.write('set logscale x\n');
        f.write('set title \"Huffman Encoder / Decoder\"\n')
        f.write('set xlabel \"Cores\"\n');
        f.write('set ylabel \"Throughput\"\n');
        f.write(cmd)
    os.system('gnuplot ./huffman.gnu')
    os.system('ps2pdf ' + output)

def main():    
    attempts = 3
    ignore = 32
    outputs = 10000
    cores = [1,2,4,8,16,32]
    static_results = []
    dynamic_results = []
    with open('./partial-results.dat', 'w') as f:        
        generate(outputs)
        for core in cores:        
            for test in [Configs.static, Configs.dynamic]:    
                compile(core, test, outputs, ignore)
                (avg, dev) =  run(test, core, attempts, outputs)
                if test == Configs.static:
                    x = ('static', core, avg, dev)
                    s = 'static\t%d\t%f\t%f' % (core, avg, dev)
                    f.write(s + '\n')   
                    print x
                    static_results.append(x)
                elif test == Configs.dynamic:
                    x = ('dynamic', core, avg, dev)
                    s = 'dynamic\t%d\t%f\t%f' % (core, avg, dev)
                    f.write(s + '\n')   
                    print x          
                    dynamic_results.append(x)

        print_all(static_results, dynamic_results)
        plot(cores)

                    
if __name__ == "__main__":
    main()
