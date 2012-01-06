#!/usr/bin/env python
import os
import subprocess
import time
import re

streamit_home = os.environ['STREAMIT_HOME']
strc          = os.path.join(streamit_home, 'strc')
test_root     = os.path.join(streamit_home, 'apps', 'tests', 'dynamic')
test_cases    = os.path.join(test_root, 'cases')
tests        = [ ('test9.str', 'test10.str') ]

def print_header():
    print('#%s\t%s\t%s' % ('cost', 'static', 'dynamic'))

def write_header(f):
    f.write('#%s\t%s\t%s\n' % ('cost', 'static', 'dynamic'))

def print_result(f, cost, sta_avg, dyn_avg):    
    print('%d\t%f\t%f' % (cost, sta_avg, dyn_avg))
    f.write('%d\t%f\t%f\n' % (cost, sta_avg, dyn_avg))

def compile(num_cores, num_iters, n, test):
    target = os.path.join(test_cases, test)
    exe = os.path.join(test_root, 'smp' + str(num_cores))
    cmd = [strc, '-smp', str(num_cores), '-N', str(1), '--outputs', str(num_iters), target ]
    subprocess.call(cmd, stdout=open(os.devnull, "w"), stderr=subprocess.STDOUT)
    assert os.path.exists(exe)

def run_one(num_cores, test_dir):
    exe = os.path.join(test_root, 'smp' + str(num_cores))
    (output, error) = subprocess.Popen([exe], stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
    regex = re.compile('Average cycles per SS for 1 iterations: (\d+)')
    results = []
    for m in regex.finditer(output):
        results.append(m.group(1));
    avg = reduce(lambda x, y: float(x) + float(y), results) / len(results)  
    return avg

def get_result(num_cores, test):
    results = []
    for run in range(3):
        results.append(run_one(num_cores, test))
    avg = reduce(lambda x, y: float(x) + float(y), results) / len(results)
    return avg

def replace(test, cost):
    target = os.path.join(test_cases, test)
    out = os.path.join(test_cases, test)
    o = open(out,"w")
    data = open(target + ".template").read()
    o.write( re.sub("COST_VALUE",str(cost),data)  )
    o.close()

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

def main():
    cores = [2]
    iterations = [10]
    nvalues = [1]
    costs = [100, 250, 500, 750, 1000]    
    f = open('./results.dat', 'w')
    write_header(f)
    print_header()
    for cost in costs:
        for num_cores in cores:
            for (dynamic_test, static_test) in tests:
                replace(dynamic_test, cost);
                replace(static_test, cost);
                compile(num_cores, 10, 1, dynamic_test)
                dynamic_avg = get_result(num_cores, dynamic_test)
                compile(num_cores, 10, 1, static_test)
                static_avg = get_result(num_cores, static_test)
                #print 'dynamic_avg time is %f' % dynamic_avg
                #print 'static_avg time is %f' % static_avg
                print_result(f, cost, static_avg, dynamic_avg)
    f.close()
    plot()

if __name__ == "__main__":
    main()

