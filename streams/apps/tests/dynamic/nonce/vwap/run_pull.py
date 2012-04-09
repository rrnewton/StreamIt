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


def generate(selectivity, outputs):
    print 'generate ' + str(selectivity)
    with open('floats.in', 'wb') as f:                
        for j in range(outputs):
            val = struct.pack('f', 2.0)  # time
            f.write(val)                 # val
            val = struct.pack('f', 1.0)
            f.write(val)
            for i in range(1, selectivity):
                val = struct.pack('f', 1.0) # time
                f.write(val)       
                val = struct.pack('f', 1.0) # val
                f.write(val)       

def compile(cores, test, outputs, ignore):
    cmd = ["strc", "-smp", str(cores), "--perftest", "--outputs", str(outputs), '--preoutputs', str(ignore), '--noiter', '--selective', 'VWAPStatic.str']    
    if test == Configs.dynamic:
        cmd = ["strc", "-smp", str(cores), "--perftest", "--outputs", str(outputs), '--preoutputs', str(ignore), "--threadopt", '--noiter', '--nofuse', '--selective', 'VWAPDynamicPull.str']    
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
    return (mean, dev)

def print_all(static_results, dynamic_results):
    file = 'vwap-pull.dat'
    with open(file, 'w') as f:
        s = '#selectivity\t'
        s += '\t'.join(["%s-sta-avg\t%s-sta-dev" % (s[2], s[2]) for s in static_results[0]])
        s += '\t' + '\t'.join(["%s-dyn-avg\t%s-dyn-dev" % (s[2], s[2]) for s in dynamic_results[0]])
        print s
        f.write(s + '\n')    
        for static, dynamic in zip(static_results, dynamic_results):
            s = '%d\t' % (static[0][1]) 
            s += '\t'.join(["%f\t%f" % (d[3], d[4]) for d in static])
            s += '\t' + '\t'.join(["%f\t%f" % (d[3], d[4]) for d in dynamic])
            print s
            f.write(s + '\n')                 
    file = 'vwap-normalized-pull.dat'
    with open(file, 'w') as f:
        s = '#selectivity\t'
        s += '\t'.join(["%s-sta-norm\t%s-sta-dev" % (x[2], x[2]) for x in static_results[0]])
        s += '\t' + '\t'.join(["%s-dyn-avg\t%s-dyn-avg" % (x[2], x[2]) for x in dynamic_results[0]])
        print s
        f.write(s + '\n')    
        for static, dynamic in zip(static_results, dynamic_results):
            #base =  static[0][3]
            s = '%d\t' % (static[0][1]) 
            s += '\t'.join(["%f\t%f" % (d[3], d[4]) for d in static])
            s += '\t' + '\t'.join(["%f\t%f" % (d[3], d[4]) for d in dynamic])
            print s
            f.write(s + '\n')           


def plot_normalized(cores):
    data = 'vwap-normalized-pull.dat'
    output = 'vwap-normalized-pull.ps'  
    cmd = "plot "
    i = 2    
    for core in cores:
        if i != 2:
            cmd += ', '
        cmd += "\"" + data + "\" u 1:" + str(i) + " t \'static-" + str(core) + " \' w linespoints lc rgb \"blue\""
        cmd += ", \"" + data + "\" u 1:" + str(i) + ":" + str(i+1) + " notitle w yerrorbars"
        i = i + 2
    for core in cores:
        cmd += ", \"" + data + "\" u 1:" + str(i) + " t \'dynamic-" + str(core) + "\' w linespoints lc rgb \"red\""
        cmd += ", \"" + data + "\" u 1:" + str(i) + ":" + str(i+1) + " notitle w yerrorbars"
        i = i + 2
    with open('./vwap-normalized-pull.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key left center\n');
        f.write('set logscale x\n');
        f.write('set title \"VWAP Operator Normalized\"\n')
        f.write('set xlabel \"Frequency\"\n');
        f.write('set ylabel \"Throughput normalized to static throughput with 1 core\"\n');
        f.write(cmd)
    os.system('gnuplot ./vwap-normalized-pull.gnu')
    os.system('ps2pdf ' + output)
    
    
def main():    
    attempts = 3
    ignore = 32
    outputs = 10000
    selectivities = [1, 10, 100, 1000, 10000]
    cores = [1,8]
    static_results = []
    dynamic_results = []
    with open('./partial-results.dat', 'w') as f:        
        for selectivity in selectivities:
            static = []
            dynamic = []
            for core in cores:        
                for test in [Configs.static, Configs.dynamic]:
                    generate(selectivity)
                    compile(core, test, outputs, ignore)
                    (avg, dev) =  run(test, core, attempts, outputs)
                    if test == Configs.static:
                        x = ('static', selectivity, core, avg, dev)
                        s = 'static\t%d\t%d\t%f\t%f' % (selectivity, core, avg, dev)
                        f.write(s + '\n')   
                        print x
                        static.append(x)
                    elif test == Configs.dynamic:
                        x = ('dynamic', selectivity, core, avg, dev)
                        s = 'dynamic\t%d\t%d\t%f\t%f' % (selectivity, core, avg, dev)
                        f.write(s + '\n')   
                        print x          
                        dynamic.append(x)
            dynamic_results.append(dynamic)
            static_results.append(static)
        print_all(static_results, dynamic_results)
        plot_normalized(cores)

                    
if __name__ == "__main__":
    main()
