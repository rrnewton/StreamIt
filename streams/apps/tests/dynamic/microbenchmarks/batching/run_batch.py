#!/usr/bin/env python
import os
import subprocess
import time
import re
import math

class Configs:
    fusion, threadbatch = range(2)

streamit_home = os.environ['STREAMIT_HOME']
strc          = os.path.join(streamit_home, 'strc')

static_test = (Configs.fusion, [strc, '-smp', '1', '--perftest', '--noiter'], './smp1' )
dynamic_test = (Configs.threadbatch, [strc, '-smp', '1', '--perftest', '--noiter', '--threadopt'], './smp1' )

def generate(test, num_filters, work):
    op = 'void->void pipeline test {\n';
    op += '    add FileReader<float>(\"../input/floats.in\");\n'
    op += '    for(int i=1; i<=' + str(num_filters) + '; i++)\n'
    if test[0] == Configs.fusion:
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
    if test[0] == Configs.fusion:
        cmd = flags + ['--outputs', str(work), '--preoutputs', str(ignore), 'test.str' ]
    else:
        cmd = flags + ['--threadbatch', str(batch), '--outputs', str(work), '--preoutputs', str(ignore), 'test.str' ]
    print ' '.join(cmd)
    subprocess.call(cmd, stdout=open(os.devnull, "w"), stderr=subprocess.STDOUT)
    assert os.path.exists(exe)


def run_one(test):
    exe = test[2]
    results = []
    if test[0] == Configs.fusion:
        test_type = 'no-fusion'
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
    tputs =  map(lambda x: (float(outputs)/float(x)) * 1000000000L , times)
    mean = reduce(lambda x, y: float(x) + float(y), tputs) / len(tputs)    
    deviations = map(lambda x: x - mean, tputs)
    squares = map(lambda x: x * x, deviations)
    dev = math.sqrt(reduce(lambda x, y: x + y, squares) /  (len(squares) - 1))
    return (mean, dev)


def print_all(work, batching, static_results, threadbatch_results):
    file = 'batch' + str(work) + '.dat'
    with open(file, 'w') as f:
        print '--------------------'
        s = '#work\tfilters\tstatic-avg\tstatic-dev ' + '\t'.join(["%s-avg\t%s-dev" % (b, b) for b in batching])
        print s
        f.write(s + '\n')  
        for x, y in zip(static_results, threadbatch_results):
            raw = ('\t'.join('%d' % x[i+1] for i in range (4)))  + '\t' + y[3]
            print raw
            f.write(raw + '\n')  

    print '+++++++++ Sorted By Batch size +++++++++++'
    
    file = 'batch-normalized' + str(work) + '.dat'
    base = static_results[0]
    with open(file, 'w') as f:
        s = '#work\tfilters\t ' + '\t'.join(["%s-norm" % b for b in batching])
        print s
        f.write(s + '\n')
        for x, y in zip(static_results, threadbatch_results):
            vals = y[3].split('\t')
            averages = map(lambda i: vals[i],filter(lambda i: i%2 == 0,range(len(vals))))
            normalized = ('\t'.join('%d' % x[i+1] for i in range (2))) + '\t' + '\t'.join('%f' % (float(a)/base[3]) for a in averages) 
            print normalized
            f.write(normalized + '\n')

 
    all_vals = []
    for r in threadbatch_results:
        vals = r[3].split('\t')
        averages = map(lambda i: vals[i],filter(lambda i: i%2 == 0,range(len(vals))))
        all_vals.append(averages)
        print r


    print '+++++++++ Sorted By Operators +++++++++++'

    file = 'batch-operators' + str(work) + '.dat'
    with open(file, 'w') as f:
        s = '#work\tbatching\t' + '\t'.join(["%d-filter" % t[2] for t in threadbatch_results])
        f.write(s + '\n')
        print s
        for j in range(len(all_vals[0])):
            s = str(work) + '\t' + str(batching[j])
            for i in range(len(threadbatch_results)):
                s += '\t' + str(float(all_vals[i][j])/base[3])                
            print s
            f.write(s + '\n')



def plot_operators(work, outputs, filters):
    data = 'batch-operators' + str(work) + '.dat'
    output = 'batch-operators' + str(work) + '.ps'
    cmds = []
    for i in range(len(filters)):
        cmds.append("\"" + data + "\" u 2:" + str(i+3) + " t \'" + str(filters[i]) + " operators\' w linespoints")        
    with open('./batch-operators.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key right bottom\n')
        f.write('set log x\n')
        f.write('set yrange [ 0 : ]\n');
        f.write('set title \"Fusion Experiment With Batching Normalized, Work=%d, Outputs=%d\"\n' % (work, outputs))
        f.write('set xlabel \"Batch Size\"\n');
        f.write('set ylabel \"Throughput normalized to static throughput with 1 core\"\n');
        f.write( 'plot ' + ','.join(cmds))
    os.system('gnuplot ./batch-operators.gnu')
    os.system('ps2pdf ' + output)


            
def plot(work, outputs, batching):
    data = 'batch' + str(work) + '.dat'
    output = 'batch' + str(work) + '.ps'
    cmd = "plot"
    cmd += " \"" + data + "\" u 2:3 t \'static\' w linespoints, \""
    cmd += "\" u 2:3:4 notitle w yerrorbars"
    cmds = [cmd]
    for i in range(len(batching)):
        cmds.append(" \"" + data + "\" u 2:" + str((i*2)+5) + " t \'batch size=" + str(batching[i]) + "\' w linespoints, \"" + "\" u 2:" + str((i*2)+6) + ":" + str((i*2)+6) + " notitle w yerrorbars")       
    print ','.join(cmds)    
    with open('./batch.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key right bottom\n')
        f.write('set title \"Fusion Experiment, Work=%d, Outputs=%d\"\n' % (work, outputs))
        f.write('set xlabel \"Operators\"\n');
        f.write('set ylabel \"Nanoseconds\"\n');
        f.write( ','.join(cmds))
    os.system('gnuplot ./batch.gnu')
    os.system('ps2pdf ' + output)



def plot_normalized(work, outputs, batching):
    data = 'batch-normalized' + str(work) + '.dat'
    output = 'batch-normalized' + str(work) + '.ps'
    cmds = []
    for i in range(len(batching)):
        cmds.append("\"" + data + "\" u 2:" + str(i+3) + " t \'batch=" + str(batching[i]) + "\' w linespoints")        
    with open('./batch-normalized.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key right bottom\n')
        f.write('set log x\n')
        f.write('set yrange [ 0 :  ]\n');
        f.write('set title \"Fusion Experiment With Batching Normalized, Work=%d, Outputs=%d\"\n' % (work, outputs))
        f.write('set xlabel \"Operators\"\n');
        f.write('set ylabel \"Throughput normalized to static throughput with 1 core\"\n');
        f.write( 'plot ' + ','.join(cmds))
    os.system('gnuplot ./batch-normalized.gnu')
    os.system('ps2pdf ' + output)


def main():
    attempts = 3
    # ignore = 320000
    # outputs = 640000
    # filters = [1, 2, 4, 8, 16, 32]
    # batching = [1, 10, 100, 1000, 10000 ]
    ignore = 10
    outputs = 100
    filters = [1, 2, 4]
    batching = [1, 10, 100]
    total_work = [1, 1000]
    for work in total_work:
        static_results = []      
        threadbatch_results = []
        for num_filters in filters:
            # run the static case
            test = static_test
            generate(test, num_filters, work)
            compile(test, 0, outputs, ignore)
            (avg, dev) =  run(test, attempts, outputs)
            static_results.append(('no-fusion', work, num_filters, avg, dev))
            test = dynamic_test
            generate(test, num_filters, work)
            results = []
            for batch in batching:
                compile(test, batch, outputs, batch)
                (avg, dev) =  run(test, attempts, outputs)
                x = ('threadbatch', work, batch, num_filters, avg, dev)
                print x
                results.append((str(avg), str(dev)))
            y = "\t".join(item for pair in results for item in pair)
            print y
            threadbatch_results.append(('threadbatch', work, num_filters, y))
                        
        print_all(work, batching, static_results, threadbatch_results)
        plot(work, outputs, batching)
        plot_normalized(work, outputs, batching)
        plot_operators(work, outputs, filters)

                    
if __name__ == "__main__":
    main()
