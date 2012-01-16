#!/usr/bin/env python
import os
import subprocess
import time
import re

streamit_home = os.environ['STREAMIT_HOME']
strc          = os.path.join(streamit_home, 'strc')
test_root     = os.path.join(streamit_home, 'apps', 'tests', 'dynamic', 'experiments')
tests        = [ ('fft', 'FFT5.str') ]

def print_header():
    print('#%s\t%s\t%s' % ('cost', 'static', 'dynamic'))

def write_header(f):
    f.write('#%s\t%s\t%s\n' % ('cost', 'static', 'dynamic'))

def print_result(f, cost, sta_avg, dyn_avg):    
    print('%d\t%f\t%f' % (cost, sta_avg, dyn_avg))
    f.write('%d\t%f\t%f\n' % (cost, sta_avg, dyn_avg))

def compile(num_cores, output, root, test):
    #exe = os.path.join(test_root, 'smp' + str(num_cores))
    exe = 'smp' + str(num_cores)
    os.chdir(root)
    cmd = [strc, '-smp', str(num_cores), '--perftest', '--outputs', str(output), test ]
    #print cmd
    subprocess.call(cmd, stdout=open(os.devnull, "w"), stderr=subprocess.STDOUT)
    assert os.path.exists(exe)
    os.chdir(test_root)

def run_one(num_cores, test_dir, output, test_type):
    os.chdir(test_dir)
    exe = './smp' + str(num_cores)
    #print 'run_one test_dir=' + test_dir + ' exe=' + exe  
    (stdout, error) = subprocess.Popen([exe], stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
    regex = re.compile('input=(\d+) delta=(\d+):(\d+)')
    results = []
    #print 'run_one in =' + test_dir
    for m in regex.finditer(stdout):
        #print 'inputs=' + m.group(1) + ' seconds=' + m.group(2) + ' nanoseconds=' + m.group(3)
        results.append(test_type + ' inputs=' + m.group(1) + ' outputs=' + str(output) + ' seconds=' + m.group(2) + ' nanoseconds=' + m.group(3))
        #results.append(m.group(1));
        #avg = reduce(lambda x, y: float(x) + float(y), results) / len(results)  
        #return avg
    os.chdir(test_root)
    return results

def get_result(num_cores, test, output, test_type):
    results = []
    for run in range(3):
        results.append(run_one(num_cores, test, output, test_type))
        #avg = reduce(lambda x, y: float(x) + float(y), results) / len(results)
        #return avg
    return results

def plot():
    f = open('./tmp.gnu', 'w')
    cmd = "plot \"results.dat\" u 1:2 t \'static\' w linespoints, \"results.dat\" u 1:3 t \'dynamic\' w linespoints"
    f.write('set terminal postscript\n')
    f.write('set output \"results.ps\"\n')
    #f.write('set title \"Static vs Dynamic, Iterations=1000, Cost=%d,\"\n' % cost)
    f.write('set xlabel \"Cost\"\n');
    f.write('set ylabel \"Clock Cycles\"\n');
    f.write(cmd)
    f.close()
    os.system('gnuplot ./tmp.gnu')

def pr(results):
    for result in results:
        print result
    

def main():
    cores = [2]
    outputs = [1000]
    for num_cores in cores:
        for output in outputs:
            for (subdir, streamit_file) in tests:
                static_test = os.path.join(test_root, 'static', subdir, 'streamit')
                dynamic_test = os.path.join(test_root, 'dynamic', subdir, 'streamit')
                compile(num_cores, output, static_test, streamit_file)
                compile(num_cores, output, dynamic_test, streamit_file)
                print '=========================='
                results = get_result(num_cores, static_test, output, 'static')
                pr(results)
                results = get_result(num_cores, dynamic_test, output, 'dynamic')
                pr(results)


    #cores = [2]
    #iterations = [10]
    #nvalues = [1]
    #f = open('./results.dat', 'w')
    #write_header(f)
    #print_header()
    #for num_cores in cores:
    #    for (dynamic_test, static_test) in tests:
    #        replace(dynamic_test, cost);
    #        replace(static_test, cost);
    #        compile(num_cores, 10, 1, dynamic_test)
    #        dynamic_avg = get_result(num_cores, dynamic_test)
    #        compile(num_cores, 10, 1, static_test)
    #        static_avg = get_result(num_cores, static_test)
    #        #print 'dynamic_avg time is %f' % dynamic_avg
    #        #print 'static_avg time is %f' % static_avg
    #        print_result(f, cost, static_avg, dynamic_avg)
    #f.close()
    #plot()

if __name__ == "__main__":
    main()

