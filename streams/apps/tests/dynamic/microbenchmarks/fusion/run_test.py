#!/usr/bin/env python
import os
import subprocess
import time
import re
import math

class Configs:
    nofusion, fusion, dynamic, lockfree, threadopt, threadbatch = range(6)

streamit_home = os.environ['STREAMIT_HOME']
strc          = os.path.join(streamit_home, 'strc')

tests        = [
    (Configs.lockfree, [strc, '-smp', '1', '--perftest', '--noiter', '--lockfree'], './smp1' ),
    (Configs.nofusion, [strc, '-smp', '1', '--perftest', '--noiter', '--nofuse'], './smp1' ),
    (Configs.fusion, [strc, '-smp', '1', '--perftest', '--noiter'], './smp1' ),
    (Configs.dynamic, [strc, '-smp', '1', '--perftest', '--noiter'], './smp1' ),
    (Configs.threadopt, [strc, '-smp', '1', '--perftest', '--noiter', '--threadopt'], './smp1' ),
    (Configs.threadbatch, [strc, '-smp', '1', '--perftest', '--noiter', '--threadbatch', '100', '--threadopt'], './smp1' )
    ]

def generate(test, num_filters, work):
    op = 'void->void pipeline test {\n';
    op += '    add FileReader<float>(\"../input/floats.in\");\n'
    op += '    for(int i=1; i<=' + str(num_filters) + '; i++)\n'
    if test[0] == Configs.nofusion:
        op += '        add Fstatic();\n'
    elif test[0] == Configs.fusion:
        op += '        add Fstatic();\n'
    elif test[0] == Configs.dynamic:        
        op += '        add Fdynamic();\n'
    elif test[0] == Configs.lockfree:        
        op += '        add Fdynamic();\n'        
    elif test[0] == Configs.threadopt:        
        op += '        add Fdynamic();\n'
    elif test[0] == Configs.threadbatch:        
        op += '        add Fdynamic();\n'
    op += '    add FileWriter<float>("./test.out");\n'
    op += '}\n'
    op += '\n'
    op += 'float->float filter Fstatic() {\n'
    op += '    work pop 1 push 1 {\n'
    op += '        int i;\n'
    op += '        float x;\n'
    op += '        x = pop();\n'
    op += '        for (i = 0; i < ' + str(work/num_filters) + '; i++) {\n'
    op += '            x += i * 3.0 - 1.0;\n'
    op += '        }\n'
    op += '        push(x);\n'
    op += '    }\n'
    op += '}\n'
    op += '\n'
    op += 'float->float filter Fdynamic() {\n'
    op += '    work pop * push * {\n'
    op += '        int i;\n'
    op += '        float x;\n'
    op += '        x = pop();\n'
    op += '        for (i = 0; i < ' + str(work/num_filters) + '; i++) {\n'
    op += '            x += i * 3.0 - 1.0;\n'
    op += '        }\n'
    op += '        push(x);\n'
    op += '    }\n'
    op += '}\n'

    with open("test.str", 'w') as f:
        f.write(op)      

def compile(test, work, ignore):
    flags = test[1]
    exe = test[2]
    cmd = flags + ['--outputs', str(work), '--preoutputs', str(ignore), 'test.str' ]
    print cmd
    subprocess.call(cmd, stdout=open(os.devnull, "w"), stderr=subprocess.STDOUT)
    assert os.path.exists(exe)


def run_one(test):
    exe = test[2]
    results = []
    if test[0] == Configs.nofusion:
        test_type = 'no-fusion'
    elif test[0] == Configs.fusion:
        test_type = 'fusion'
    elif test[0] == Configs.dynamic:
        test_type = 'dynamic'    
    elif test[0] == Configs.lockfree:
        test_type = 'lockfree'
    elif test[0] == Configs.threadopt:
        test_type = 'threadopt' 
    elif test[0] == Configs.threadbatch:
        test_type = 'threadbatch' 
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

def print_all(work, nofusion_results, fusion_results, dynamic_results, lockfree_results, threadopt_results, threadbatch_results):
    file = 'fusion' + str(work) + '.dat'
    with open(file, 'w') as f:
        s = '#%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s' % ( 'filters', 'work', 'nofusion', 'dev', 'fusion', 'dev', 'dynamic', 'dev', 'lockfree', 'dev', 'threadopt', 'dev', 'threadbatch', 'dev')
        print s
        f.write(s + '\n')  
        for x, y, z, l, t, b in zip(nofusion_results, fusion_results, dynamic_results, lockfree_results, threadopt_results, threadbatch_results):
            s = '%d\t%d\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f' % (x[2], x[1], x[3], x[4], y[3], y[4], z[3], z[4], l[3], l[4], t[3], t[4], b[3], b[4])
            print s
            f.write(s + '\n')
    file = 'fusion-normalized' + str(work) + '.dat'
    nofusion = nofusion_results[0]
    with open(file, 'w') as f:
        s = '#%s\t%s\t%s\t%s\t%s\t%s\t%s' % ( 'filters', 'work', 'fusion','dynamic', 'lockfree', 'threadopt', 'threadbatch')
        print s
        f.write(s + '\n')
        for fusion, dynamic, lockfree, threadopt, threadbatch in zip(fusion_results, dynamic_results, lockfree_results, threadopt_results, threadbatch_results):
            s = '%d\t%d\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f' % (fusion[2], fusion[1], (fusion[3]/nofusion[3]), (dynamic[3]/nofusion[3]), (lockfree[3]/nofusion[3]), (threadopt[3]/nofusion[3]), (threadbatch[3]/nofusion[3]))
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
    cmd += data + "\" u 1:11 t \'threadopt\' w linespoints, \""
    cmd += "\" u 1:11:12 notitle w yerrorbars, \""
    cmd += data + "\" u 1:13 t \'threadbatch\' w linespoints, \""
    cmd += "\" u 1:13:14 notitle w yerrorbars"

    
    with open('./tmp.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key left top\n');
        f.write('set title \"Fusion Experiment, Work=%d, Outputs=%d\"\n' % (work, outputs))
        f.write('set xlabel \"Filters\"\n');
        f.write('set ylabel \"Nanoseconds\"\n');
        f.write(cmd)
    os.system('gnuplot ./tmp.gnu')

def plot_normalized(work, outputs):
    data = 'fusion-normalized' + str(work) + '.dat'
    output = 'fusion-normalized' + str(work) + '.ps'
    cmd = "plot "
    cmd += "\"" + data + "\" u 1:7 t \'batching=100\' w linespoints,"
    cmd += "\"" + "\" u 1:4:(sprintf(\"[%.0f,%.1f]\",$1,$4)) notitle with labels offset 0.25,1.75,"
    cmd += "\"" + data + "\" u 1:3 t \'fusion\' w linespoints,"
    cmd += "\"" + data + "\" u 1:4 t \'dynamic\' w linespoints,"
    cmd += "\"" + data + "\" u 1:5 t \'lockfree\' w linespoints,"
    cmd += "\"" + data + "\" u 1:6 t \'threadopt\' w linespoints"

    
    with open('./tmp.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key left top\n');
        f.write('set title \"Fusion Experiment Normalized, Work=%d, Outputs=%d\"\n' % (work, outputs))
        f.write('set xlabel \"Filters\"\n');
        f.write('set ylabel \"Throughput normalized to static throughput with 1 core\"\n');
        f.write(cmd)
    os.system('gnuplot ./tmp.gnu')
    
def main():
    attempts = 3
    ignore = 1000
    outputs = 100000
    filters = [1, 2, 4, 8, 16, 32]
    total_work = [1, 100, 1000, 10000, 100000]
    #ignore = 10
    #outputs = 1000
    #filters = [2, 4]
    #total_work = [1000]   
    for work in total_work:
        nofusion_results = []
        fusion_results = []
        dynamic_results = []
        lockfree_results = []
        threadopt_results = []
        threadbatch_results = []
        for num_filters in filters:
            for test in tests:
                generate(test, num_filters, work)
                compile(test, outputs, ignore)
                (avg, dev) =  run(test, attempts)
                if test[0] == Configs.nofusion:
                    x = ('no-fusion', work, num_filters, avg, dev)
                    print x
                    nofusion_results.append(('no-fusion', work, num_filters, avg, dev))
                elif test[0] == Configs.fusion:
                    fusion_results.append(('fusion', work, num_filters, avg, dev))
                    x = ('fusion', work, num_filters, avg, dev)
                    print x
                elif test[0] == Configs.dynamic:
                    dynamic_results.append(('dynamic', work, num_filters, avg, dev))
                    x = ('dynamic', work, num_filters, avg, dev)
                    print x
                elif test[0] == Configs.lockfree:
                    lockfree_results.append(('lockfree', work, num_filters, avg, dev))
                    x = ('lockfree', work, num_filters, avg, dev)
                    print x
                elif test[0] == Configs.threadopt:
                    threadopt_results.append(('threadopt', work, num_filters, avg, dev))
                    x = ('threadopt', work, num_filters, avg, dev)
                    print x
                elif test[0] == Configs.threadbatch:
                    threadbatch_results.append(('threadbatch', work, num_filters, avg, dev))
                    x = ('threadbatch', work, num_filters, avg, dev)
                    print x

                    
        print_all(work, nofusion_results, fusion_results, dynamic_results, lockfree_results, threadopt_results, threadbatch_results)
        plot(work, outputs)
        plot_normalized(work, outputs)
                    
if __name__ == "__main__":
    main()
