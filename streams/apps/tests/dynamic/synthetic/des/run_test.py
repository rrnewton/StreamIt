#!/usr/bin/env python
import os
import subprocess
import time
import re
import math

FNULL = open('/dev/null', 'w')

class Configs:
    static, dynamic = range(2)

streamit_home = os.environ['STREAMIT_HOME']
strc          = os.path.join(streamit_home, 'strc')

def compile(cores, test, work, ignore):
    cmd = ["strc", "-smp", str(cores), "--perftest", "--outputs", str(work), '--preoutputs', str(ignore), '--noiter', 'DES2.str']    
    if test == Configs.dynamic:
        cmd = ["strc", "-smp", str(cores), "--perftest", "--outputs", str(work), '--preoutputs', str(ignore), "--threadopt", '--noiter', 'DES2Dynamic.str']    
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

def run(test, cores, attempts):
    results = []
    for num in range(attempts):
         result = run_one(test, cores)
         results.append(result)
         print result         
    # 1000000000 nanoseconds in 1 second    
    times = map(lambda x:  (long(x[4]) * 1000000000L) + long(x[5]) , results)
    mean = reduce(lambda x, y: float(x) + float(y), times) / len(times)    
    deviations = map(lambda x: x - mean, times)
    squares = map(lambda x: x * x, deviations)
    dev = math.sqrt(reduce(lambda x, y: x + y, squares) /  (len(squares) - 1))
    return (mean, dev)

def print_all(static_results, dynamic_results):
    file = 'des.dat'
    with open(file, 'w') as f:
        s = '#%s\t%s\t%s\t%s\t%s' % ( 'cores', 'static', 'dev', 'dynamic', 'dev')
        print s
        f.write(s + '\n')  
        for static, dynamic in zip(static_results, dynamic_results):
            s = '%d\t%0.2f\t%0.2f\t%0.2f\t%0.2f' % (static[1], static[2], static[3], dynamic[2], dynamic[3])
            print s
            f.write(s + '\n')
    file = 'des-normalized.dat'
    base = static_results[0]
    total_work = base[2]
    with open(file, 'w') as f:
        s = '#%s\t%s' % ( 'cores', 'dynamic')
        print s
        f.write(s + '\n')  
        for static, dynamic in zip(static_results, dynamic_results):
            s = '%d\t%0.2f\t%0.2f\t%0.2f\t%0.2f' % (static[1], static[2]/base[2],  static[3]/base[2], dynamic[2]/base[2], dynamic[3]/base[2])
            print s
            f.write(s + '\n')
        

def plot():
    data = 'des.dat'
    output = 'des.ps'  
    cmd = "plot \""
    cmd += data + "\" u 1:2 t \'static\' w linespoints, \""
    cmd += "\" u 1:2:3 notitle w yerrorbars, \""
    cmd += data + "\" u 1:4 t \'dynamic\' w linespoints, \""
    cmd += "\" u 1:4:5 notitle w yerrorbars"    
    with open('./des.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key left top\n');
        f.write('set title \"Synthetic Dynamism DES\"\n')
        f.write('set xlabel \"Cores\"\n');
        f.write('set ylabel \"Nanoseconds\"\n');
        f.write(cmd)
    os.system('gnuplot ./des.gnu')
    os.system('ps2pdf ' + output)


def plot_normalized():
    data = 'des-normalized.dat'
    output = 'des-normalized.ps'  
    cmd = "plot "
    cmd += "\"" + data + "\" u 1:4 t \'dynamic\' w linespoints, "
    cmd += "\"" + "\" u 1:4:5 notitle w yerrorbars, "    
    cmd += "\"" + data + "\" u 1:2 t \'static\' w linespoints, "
    cmd += "\"" + "\" u 1:2:3 notitle w yerrorbars"    
    with open('./des-normalized.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key left top\n');
        f.write('set title \"Synthetic Dynamism DES Normalized\"\n')
        f.write('set xlabel \"Cores\"\n');
        f.write('set ylabel \"Throughput normalized to static throughput with 1 core\"\n');
        f.write(cmd)
    os.system('gnuplot ./des-normalized.gnu')
    os.system('ps2pdf ' + output)

    
def main():
    attempts = 3
    ignore = 131072
    # outputs = 655360
    outputs = 1310720
    cores = [1, 2, 4, 8, 16, 32]    
    #cores = [1, 2, 4]    
    static_results = []
    dynamic_results = []
    batch_results = []
    for core in cores:
        for test in [Configs.static, Configs.dynamic]:
            compile(core, test, outputs, ignore)
            (avg, dev) =  run(test, core, attempts)
            if test == Configs.static:
                x = ('static', core, avg, dev)
                print x
                static_results.append(x)
            elif test == Configs.dynamic:
                x = ('dynamic', core, avg, dev)
                print x          
                dynamic_results.append(x)                    
    print_all(static_results, dynamic_results)
    plot()
    plot_normalized()
                    
if __name__ == "__main__":
    main()
