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
    #(Configs.lockfree, [strc, '-smp', '1', '--perftest', '--noiter', '--lockfree'], './smp1' ),
    #(Configs.nofusion, [strc, '-smp', '1', '--perftest', '--noiter', '--nofuse', '--printf'], './smp1' ),
    #(Configs.dynamic, [strc, '-smp', '1', '--perftest', '--noiter'], './smp1' ),
    # (Configs.threadbatch, [strc, '-smp', '1', '--perftest', '--noiter', '--threadbatch', '100', '--threadopt'], './smp1' )
    (Configs.fusion, [strc, '-smp', '1', '--perftest', '--noiter'], './smp1' ),
    (Configs.threadopt, [strc, '-smp', '1', '--perftest', '--noiter', '--threadopt'], './smp1' )
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
    print ' '.join(cmd)
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

def run(test, attempts, outputs):
    results = []
    for num in range(attempts):
         result = run_one(test)
         results.append(result)
         #for result in results:
         print result         
    # 1000000000 nanoseconds in 1 second    
    times = map(lambda x:  (long(x[4]) * 1000000000L) + long(x[5]) , results)
    # tputs =  map(lambda x: (float(outputs)/float(x)) * 1000000000L , times)
    mean = reduce(lambda x, y: float(x) + float(y), times) / len(times)    
    deviations = map(lambda x: x - mean, times)
    squares = map(lambda x: x * x, deviations)
    dev = math.sqrt(reduce(lambda x, y: x + y, squares) /  (len(squares) - 1))
    return (mean, dev)

def print_all(work, fusion_results, threadopt_results):
    file = 'fusion' + str(work) + '.dat'
    with open(file, 'w') as f:
        s = '#%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s' % ( 'filters', 'work', 'fusion', 'dev', 'threadopt', 'dev', 'threadbatch', 'dev')
        print s
        f.write(s + '\n')  
        for x, t in zip(fusion_results, threadopt_results):
            s = '\t%d\t%d\t%0.2f\t%0.2f\t%0.2f\t%0.2f' % (x[2], x[1], x[3], x[4], t[3], t[4])
            print s
            f.write(s + '\n')
    file = 'fusion-normalized' + str(work) + '.dat'
    base = fusion_results[0]
    with open(file, 'w') as f:
        s = '#%s\t%s\t%s\t%s\t%s\t%s' % ( 'filters', 'work', 'threadopt', 'dev', 'threadbatch', 'dev')
        print s
        f.write(s + '\n')
        for fusion, threadopt in zip(fusion_results,threadopt_results ):
            s = '\t%d\t%d\t%f\t%f' % (fusion[2], fusion[1], (threadopt[3]/base[3]), (threadopt[4]/base[3])  )
            print s
            f.write(s + '\n')


def plot(work, outputs):
    data = 'fusion' + str(work) + '.dat'
    output = 'fusion' + str(work) + '.ps'
    cmd = "plot "
    cmd += "\"" + data + "\" u 1:3 t \'static\' w linespoints,"
    cmd += "\"\" u 1:3:4 notitle w yerrorbars, "
    #cmd += "\"" + data + "\" u 1:7 t \'dynamic w/ batching=100\' w linespoints, "
    #cmd += "\"\" u 1:7:8 notitle w yerrorbars, "
    cmd += "\"" + data + "\" u 1:5 t \'dynamic\' w linespoints, "
    cmd += "\"\" u 1:5:6 notitle w yerrorbars"
    with open('./fusion.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key right top\n');
        f.write('set title \"Fusion Experiment, Work=%d, Outputs=%d\"\n' % (work, outputs))
        f.write('set xlabel \"Operators\"\n');
        f.write('set ylabel \"Throughput (data items/second)\"\n');
        f.write(cmd)
    os.system('gnuplot ./fusion.gnu')
    os.system('ps2pdf ' + output)

def plot_normalized(work, outputs):
    data = 'fusion-normalized' + str(work) + '.dat'
    output = 'fusion-normalized' + str(work) + '.ps'
    cmd = "plot "
    cmd += "\"" + data + "\" u 1:3 notitle w linespoints,"
    cmd += "\"\" u 1:3:4 notitle w yerrorbars"
    with open('./fusion-normalized.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key right\n');
        f.write('set yrange [ 0 : ]\n');
        f.write('set title \"Fusion Experiment Normalized, Work=%d, Outputs=%d\"\n' % (work, outputs))
        f.write('set xlabel \"Operators\"\n');
        f.write('set ylabel \"Throughput normalized to static throughput with 1 core\"\n');
        f.write(cmd)
    os.system('gnuplot ./fusion-normalized.gnu')
    os.system('ps2pdf ' + output)
    
def main():
    attempts = 3
    ignore = 3200
    outputs = 100000
    filters = [1, 2, 4, 8, 16, 32]
    total_work = [1, 10, 100, 1000, 100000]
    # total_work = [1]
    # ignore = 10
    # outputs = 1000
    # filters = [2, 4]
    for work in total_work:        
        fusion_results = []
        threadopt_results = []
        for num_filters in filters:
            for test in tests:
                generate(test, num_filters, work)
                compile(test, outputs, ignore)
                (avg, dev) =  run(test, attempts, outputs)
                if test[0] == Configs.fusion:
                    x = ('fusion', work, num_filters, avg, dev)
                    print x
                    fusion_results.append(('fusion', work, num_filters, avg, dev))               
                elif test[0] == Configs.threadopt:
                    threadopt_results.append(('threadopt', work, num_filters, avg, dev))
                    x = ('threadopt', work, num_filters, avg, dev)
                    print x                                  
        print_all(work, fusion_results, threadopt_results)
        plot(work, outputs)
        plot_normalized(work, outputs)
                    
if __name__ == "__main__":
    main()
