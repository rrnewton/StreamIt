#!/usr/bin/env python
import os
import subprocess
import time
import gplot
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

def print_result(cost, sta_avg, dyn_avg):    
    print('%d\t%f\t%f' % (cost, sta_avg, dyn_avg))
    #f.write('%d\t%f\t%f' % (cost, sta_avg, dyn_avg))

def compile(num_cores, num_iters, n, test):
    target = os.path.join(test_cases, test)
    exe = os.path.join(test_root, 'smp' + str(num_cores))
    cmd = [strc, '-smp', str(num_cores), '-N', str(1), '-i', str(num_iters), target ]
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

def main():
    cores = [2]
    iterations = [10]
    nvalues = [1]
    costs = [1, 10, 100, 1000]
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
                print_result(cost, static_avg, dynamic_avg)    
                   




    # i = 1000
    # mults = [1, 10, 100, 250, 500, 750, 1000]
    # cost = [1, 10, 100, 250, 500, 750, 1000]
    # print_header()

    # dyn_avg_result = []
    # sta_avg_result = []
    # for c in cost:
    #     f = open('./cost-fixed' + str(c) + '.dat', 'w')
    #     write_header(f)
    #     for mult in mults:
    #         dyn_result = []
    #         sta_result = []
    #         for run in range(3):
    #             sta_result.append(run_one('streamit/smp2', mult, i, c))
    #         for run in range(3):
    #             dyn_result.append(run_one('streamit_dynamic/smp2', mult, i, c))
    #         sta_avg = reduce(lambda x, y: float(x) + float(y), sta_result) / len(sta_result)
    #         dyn_avg = reduce(lambda x, y: float(x) + float(y), dyn_result) / len(dyn_result)    
    #         print_result(f, mult, i, c, sta_avg, dyn_avg)
    #         dyn_avg_result.append(dyn_avg)
    #         sta_avg_result.append(sta_avg);
    #     f.close()
    #     plot_cost_fixed(c)

    # print_header()

    # m = 0;
    # for mult in mults:
    #     f = open('./mult-fixed' + str(mult) + '.dat', 'w')
    #     write_header(f)
    #     n = 0;
    #     for c in cost:            
    #         sta_avg = sta_avg_result[m + n]
    #         dyn_avg = dyn_avg_result[m + n]
    #         print_result(f, mult, i, c, sta_avg, dyn_avg)
    #         n = n + len(mults)
    #     f.close()
    #     plot_mult_fixed(mult)
    #     m = m + 1
    
if __name__ == "__main__":
    main()

