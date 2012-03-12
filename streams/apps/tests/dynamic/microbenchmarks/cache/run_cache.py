#!/usr/bin/env python
import os
import subprocess
import time
import re
import math

class Configs:
    nofusion, threadbatch = range(2)

streamit_home = os.environ['STREAMIT_HOME']
strc          = os.path.join(streamit_home, 'strc')

static_test = (Configs.nofusion, [strc, '-smp', '2', '--perftest', '--noiter', '--nofuse'], './smp2' )
dynamic_test = (Configs.threadbatch, [strc, '-smp', '2', '--perftest', '--noiter', '--threadopt'], './smp2' )

def generate(test, num_filters, work):
    op = 'void->void pipeline test {\n';
    op += '    add FileReader<float>(\"../input/floats.in\");\n'
    op += '    for(int i=1; i<=' + str(num_filters) + '; i++)\n'
    if test[0] == Configs.nofusion:
        op += '        add Fstatic();\n'
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

def compile(test, batch, work, ignore):
    flags = test[1]
    exe = test[2]
    cmd = flags + ['--outputs', str(work), '--preoutputs', str(ignore), 'test.str' ]
    if test[0] == Configs.nofusion:
        cmd = flags + ['--outputs', str(work), '--preoutputs', str(ignore), 'test.str' ]
    else:
        cmd = flags + ['--threadbatch', str(batch), '--outputs', str(work), '--preoutputs', str(ignore), 'test.str' ]
    print ' '.join(cmd)
    subprocess.call(cmd, stdout=open(os.devnull, "w"), stderr=subprocess.STDOUT)
    assert os.path.exists(exe)


def run_one(test):
    exe = test[2]
    results = []
    if test[0] == Configs.nofusion:
        test_type = 'no-fusion'
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


def print_all(work, batching, static_results, threadbatch_results):
    file = 'cache' + str(work) + '.dat'
    with open(file, 'w') as f:
        print '--------------------'
        s = '#work\tfilters\tbatch\tstatic-avg\tstatic-dev\tdynamic-avg\tdynamic-dev'
        print s
        f.write(s + '\n')
        x = static_results[0]
        for y in threadbatch_results:
            raw =  ('\t'.join('%d' % y[i] for i in [1, 2, 3]))
            raw += '\t' + ('\t'.join('%d' % x[i] for i in [3, 4]))
            raw += '\t' + ('\t'.join('%f' % y[i] for i in [4, 5]))            
            print raw
            f.write(raw + '\n')  

    print '++++++++++++++++++++'
    file = 'cache-normalized' + str(work) + '.dat'
    with open(file, 'w') as f:
        s = '#work\tfilters\tbatch\tnorm'
        print s
        f.write(s + '\n')  
        x = static_results[0]
        for y in threadbatch_results:
            raw =  ('\t'.join('%d' % y[i] for i in [1, 2, 3]))
            raw += '\t' + '%f' % (y[4]/x[3])
            print raw
            f.write(raw + '\n')  
 
            
def plot(work, outputs, batching):
    data = 'cache' + str(work) + '.dat'
    output = 'cache' + str(work) + '.ps'
    cmd = "plot"
    cmd += " \"" + data + "\" u 3:4 t \'nofusion\' w linespoints, \""
    cmd += "\" u 3:4:5 notitle w yerrorbars, "
    cmd += " \"" + data + "\" u 3:6 t \'batching\' w linespoints, \""
    cmd += "\" u 3:6:7 notitle w yerrorbars"    
    #cmds = [cmd]
    #for i in range(len(batching)):
    #    cmds.append(" \"" + data + "\" u 2:" + str((i*2)+5) + " t \'batch=" + str(batching[i]) + "\' w linespoints, \"" + "\" u 2:" + str((i*2)+6) + ":" + str((i*2)+6) + " notitle w yerrorbars")       
    #print ','.join(cmds)
    print cmd
    with open('./tmp.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set title \"Fusion Experiment, Work=%d, Outputs=%d\"\n' % (work, outputs))
        f.write('set xlabel \"Batch Size\"\n');
        f.write('set ylabel \"Nanoseconds\"\n');
        #f.write( ','.join(cmds))
        f.write( cmd)
    os.system('gnuplot ./tmp.gnu')


def plot_normalized(work, outputs, batching):
    data = 'cache-normalized' + str(work) + '.dat'
    output = 'cache-normalized' + str(work) + '.ps'
    cmd = "plot"
    cmd += " \"" + data + "\" u 3:4 notitle w linespoints"        
    with open('./tmp.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        #f.write('set label "Yield Point" at 1.5,.8\n')
        f.write('set title \"Fusion Experiment Normalized, Work=%d, Outputs=%d\"\n' % (work, outputs))
        f.write('set xlabel \"Batch Size\"\n');
        f.write('set ylabel \"Throughput normalized to static throughput\"\n');
        f.write( cmd)        
    os.system('gnuplot ./tmp.gnu')

def main():
    attempts = 3
    ignore = 1000
    outputs = 10000000
    filters = [2]
    batching = [100000, 200000, 300000, 400000, 500000, 600000, 700000, 800000, 900000, 1000000]
    total_work = [1000]
    #attempts = 2
    #ignore = 1
    #outputs = 100
    #filters = [2]
    #batching = [1, 2]
    #total_work = [1000]
    for work in total_work:
        static_results = []      
        threadbatch_results = []
        for num_filters in filters:
            # run the static case
            test = static_test
            generate(test, num_filters, work)
            compile(test, 0, outputs, ignore)
            (avg, dev) =  run(test, attempts)
            static_results.append(('no-fusion', work, num_filters, avg, dev))
            test = dynamic_test
            generate(test, num_filters, work)
            results = []
            for batch in batching:
                compile(test, batch, outputs, batch-1)
                (avg, dev) =  run(test, attempts)                
                results.append((batch, str(avg), str(dev)))
                x = ('threadbatch', work, num_filters, batch, avg, dev)
                print x
                threadbatch_results.append(x)
                #y = "\t".join(item for pair in results for item in pair)
                #print 'y='
                #print y
                #threadbatch_results.append(('threadbatch', work, num_filters, y))
                        
        print_all(work, batching, static_results, threadbatch_results)
        plot(work, outputs, batching)
        plot_normalized(work, outputs, batching)
                    
if __name__ == "__main__":
    main()
