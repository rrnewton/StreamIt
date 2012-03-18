#!/usr/bin/env python
import os
import subprocess
import time
import re
import math

class Configs:
    static, dynamic = range(2)

streamit_home = os.environ['STREAMIT_HOME']
strc          = os.path.join(streamit_home, 'strc')

def compile(filename, cores, test, work, ignore):
    cmd = ["strc", "-smp", str(cores), "--outputs", str(work), '--preoutputs', str(ignore), '--noiter', filename]    
    if test == Configs.dynamic:
        cmd = ["strc", "-smp", str(cores), "--outputs", str(work), '--preoutputs', str(ignore), "--threadopt", '--noiter', filename]    
    print ' '.join(cmd)
    return subprocess.Popen(cmd, stdout=FNULL, stderr=FNULL)


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
    return results

def run(test, attempts):
    results = []
    for num in range(attempts):
         result = run_one(test)
         results.append(result)
         #for result in results:
         print result         
    # 1000000000 nanoseconds in 1 second    
    times = map(lambda x:  (long(x[4]) * 1000000000L) + long(x[5]) , results)
    mean = reduce(lambda x, y: float(x) + float(y), times) / len(times)    
    deviations = map(lambda x: x - mean, times)
    squares = map(lambda x: x * x, deviations)
    dev = math.sqrt(reduce(lambda x, y: x + y, squares) /  (len(squares) - 1))
    return (mean, dev)

def print_all(work, nofusion_results, fusion_results, threadopt_results, threadbatch_results):
    file = 'fusion' + str(work) + '.dat'
    with open(file, 'w') as f:
        s = '#%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s' % ( 'filters', 'work', 'nofusion', 'dev', 'fusion', 'dev', 'threadopt', 'dev', 'threadbatch', 'dev')
        print s
        f.write(s + '\n')  
        for x, y, t, b in zip(nofusion_results, fusion_results, threadopt_results, threadbatch_results):
            s = '%d\t%d\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f' % (x[2], x[1], x[3], x[4], y[3], y[4], t[3], t[4], b[3], b[4])
            print s
            f.write(s + '\n')
    file = 'fusion-normalized' + str(work) + '.dat'
    nofusion = nofusion_results[0]
    with open(file, 'w') as f:
        s = '#%s\t%s\t%s\t%s\t%s' % ( 'filters', 'work', 'fusion', 'threadopt', 'threadbatch')
        print s
        f.write(s + '\n')
        for fusion, threadopt, threadbatch in zip(fusion_results,threadopt_results, threadbatch_results):
            s = '%d\t%d\t%0.2f\t%0.2f\t%0.2f' % (fusion[2], fusion[1], (fusion[3]/nofusion[3]), (threadopt[3]/nofusion[3]), (threadbatch[3]/nofusion[3]))
            print s
            f.write(s + '\n')


def plot(work, outputs):
    data = 'fusion' + str(work) + '.dat'
    output = 'fusion' + str(work) + '.ps'
    cmd = "plot \""
    cmd += data + "\" u 1:3 t \'nofusion\' w linespoints, \""
    cmd += "\" u 1:3:4 notitle w yerrorbars, \""
    cmd += data + "\" u 1:5 t \'fusion\' w linespoints, \""
    cmd += "\" u 1:5:6 notitle w yerrorbars, \""
    #cmd += data + "\" u 1:7 t \'dynamic\' w linespoints, \""
    #cmd += "\" u 1:7:8 notitle w yerrorbars, \""
    #cmd += data + "\" u 1:9 t \'lockfree\' w linespoints, \""
    #cmd += "\" u 1:9:10 notitle w yerrorbars, \""
    cmd += data + "\" u 1:7 t \'threadopt\' w linespoints, \""
    cmd += "\" u 1:7:8 notitle w yerrorbars, \""
    cmd += data + "\" u 1:9 t \'threadbatch\' w linespoints, \""
    cmd += "\" u 1:9:10 notitle w yerrorbars"

    
    with open('./tmp.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key left top\n');
        f.write('set title \"Fusion Experiment, Work=%d, Outputs=%d\"\n' % (work, outputs))
        f.write('set xlabel \"Operators\"\n');
        f.write('set ylabel \"Nanoseconds\"\n');
        f.write(cmd)
    os.system('gnuplot ./tmp.gnu')

def plot_normalized(work, outputs):
    data = 'fusion-normalized' + str(work) + '.dat'
    output = 'fusion-normalized' + str(work) + '.ps'
    cmd = "plot "
    cmd += "\"" + data + "\" u 1:5 t \'batching=100\' w linespoints,"
    cmd += "\"" + "\" u 1:4:(sprintf(\"[%.0f,%.1f]\",$1,$4)) notitle with labels offset 0.25,1.75,"
    cmd += "\"" + data + "\" u 1:3 t \'fusion\' w linespoints,"
    cmd += "\"" + data + "\" u 1:4 t \'threadopt\' w linespoints"

    #cmd += "\"" + data + "\" u 1:4 t \'dynamic\' w linespoints,"
    #cmd += "\"" + data + "\" u 1:5 t \'lockfree\' w linespoints,"

    
    with open('./tmp.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key left top\n');
        f.write('set title \"Fusion Experiment Normalized, Work=%d, Outputs=%d\"\n' % (work, outputs))
        f.write('set xlabel \"Operators\"\n');
        f.write('set ylabel \"Throughput normalized to static throughput with 1 core\"\n');
        f.write(cmd)
    os.system('gnuplot ./tmp.gnu')
    
def main():
    attempts = 3
    ignore = 100
    outputs = 1000
    cores = [1]
    
    static_results = []
    dynamic_results = []

    
    for core in cores:
        for test in [Configs.static, Configs.dynamic]:
            compile('FFT5.str', core, test, outputs, ignore)
            
          #   compile(test, outputs, ignore)
        #     (avg, dev) =  run(test, attempts)
        #     if test[0] == Configs.nofusion:
        #             x = ('no-fusion', work, num_filters, avg, dev)
        #             print x
        #             nofusion_results.append(('no-fusion', work, num_filters, avg, dev))
        #         elif test[0] == Configs.fusion:
        #             fusion_results.append(('fusion', work, num_filters, avg, dev))
        #             x = ('fusion', work, num_filters, avg, dev)
        #             print x          
                    
        # print_all(work, nofusion_results, fusion_results, threadopt_results, threadbatch_results)
        # plot(work, outputs)
        # plot_normalized(work, outputs)
                    
if __name__ == "__main__":
    main()
