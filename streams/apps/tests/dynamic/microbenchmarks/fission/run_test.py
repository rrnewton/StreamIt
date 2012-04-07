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


def generate(test, work, ratio):
    op = 'void->void pipeline test {\n';
    op += '    add FileReader<float>(\"../input/floats.in\");\n'
    if test == Configs.static:
        op += '    add FstaticX();\n'
    else:
        op += '    add Fdynamic();\n'
    op += '    add Fdummy();\n'
    op += '    add Fstatic();\n'
    op += '    add FileWriter<float>(\"test.out\");\n'
    op += '}\n'
    op += '\n'
    op += 'float->float filter Fstatic() {\n'
    op += '    work pop 1 push 1 {\n'
    op += '        int i;\n'
    op += '        float x;\n'
    op += '        x = pop();\n'
    op += '        for (i = 0; i < ' + str(int(ratio * work)) + '; i++) {\n'
    op += '            x += i * 3.0 - 1.0;\n'
    op += '        }\n'
    op += '        push(x);\n'
    op += '    }\n'
    op += '}\n'
    op += '\n'
    op += 'float->float filter FstaticX() {\n'
    op += '    work pop 1 push 1 {\n'
    op += '        int i;\n'
    op += '        float x;\n'
    op += '        x = pop();\n'
    op += '        for (i = 0; i < ' + str(int((1 - ratio) * work))  + '; i++) {\n'
    op += '            x += i * 3.0 - 1.0;\n'
    op += '        }\n'
    op += '        push(x);\n'
    op += '    }\n'
    op += '}\n'
    op += '\n'
    op += 'float->float filter Fdummy() {\n'
    op += '    work pop 1 push 1 {\n'
    op += '        push(pop());\n'
    op += '    }\n'
    op += '}\n'
    op += '\n'    
    op += 'float->float filter Fdynamic() {\n'
    op += '    work pop * push * {\n'
    op += '        int i;\n'
    op += '        float x;\n'
    op += '        x = pop();\n'
    op += '        for (i = 0; i < ' + str(int((1 - ratio) * work))  + '; i++) {\n'
    op += '            x += i * 3.0 - 1.0;\n'
    op += '        }\n'
    op += '        push(x);\n'
    op += '    }\n'
    op += '}\n'
    # print op;
    with open("test.str", 'w') as f:
        f.write(op)      

def compile(test, outputs, ignore, core):
    exe = './smp' + str(core)
    if test == Configs.static:
        cmd = [strc, '--perftest', '--noiter',
               '--outputs', str(outputs), '--preoutputs', str(ignore), '-smp', str(core), 'test.str' ]
    else:
        cmd = [strc, '--perftest', '--noiter', '--threadopt',
               '--outputs', str(outputs), '--preoutputs', str(ignore), '-smp', str(core), 'test.str' ]
    print ' '.join(cmd)
    subprocess.call(cmd, stdout=open(os.devnull, "w"), stderr=subprocess.STDOUT)
    assert os.path.exists(exe)


def run_one(core):
    exe = './smp' + str(core)
    results = []
    (stdout, error) = subprocess.Popen([exe], stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
    regex = re.compile('input=(\d+) outputs=(\d+) ignored=(\d+) start=\d+:\d+ end=\d+:\d+ delta=(\d+):(\d+)')
    for m in regex.finditer(stdout):
        results = ([core] + [m.group(1)] + [m.group(2)] + [m.group(3)] + [m.group(4)] + [m.group(5)])       
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

def print_all(work, ratio, static_results, dynamic_results):
    base = static_results[0]
    total_work = base[2]
    dynamic_work = total_work * (1 - ratio)
    static_work = total_work - dynamic_work
    file = 'fission' + str(int(ratio * 100)) + '_' + str(work)  + '.dat'
    with open(file, 'w') as f:
        s = '#\t%s\t%s\t%s\t%s\t%s\t%s\t%s' % ( 'ratio', 'cores', 'static', 'static-dev', 'dynamic', 'dynamic-dev', 'ideal' )
        print s
        f.write(s + '\n')  
        for x, y in zip(static_results, dynamic_results):
            ideal = dynamic_work + ( static_work /  x[1])
            s = '\t%f\t%d\t%f\t%f\t%f\t%f\t%f' % (x[0], x[1], x[2], x[3], y[2], y[3], ideal)
            print s
            f.write(s + '\n')
    file = 'fission-normalized' + str(int(ratio * 100)) + '_' + str(work) + '.dat'
    with open(file, 'w') as f:
        s = '#\t%s\t%s\t%s\t%s\t%s\t%s\t%s' % ( 'ratio', 'cores', 'static', 'dev', 'dynamic', 'dev', 'ideal')
        print s
        f.write(s + '\n')
        for static, dynamic in zip(static_results, dynamic_results):
            ideal = dynamic_work + ( static_work /  static[1])
            s = '\t%f\t%d\t%f\t%f\t%f\t%f\t%f' % (static[0], static[1], (static[2]/base[2]), (static[3]/base[2]), (dynamic[2]/base[2]), (dynamic[3]/base[2]), (1/(ideal/base[2])) )
            print s
            f.write(s + '\n')


def plot(ratio, work, outputs):
    data = 'fission' + str(int(ratio * 100)) + '_' + str(work) + '.dat'
    output = 'fission' + str(int(ratio * 100)) + '_' + str(work) + '.ps'
    cmd = "plot \""
    cmd += data + "\" u 2:3 t \'static\' w linespoints, \""
    cmd += "\" u 2:3:4 notitle w yerrorbars, \""
    cmd += data + "\" u 2:5 t \'dynamic\' w linespoints, \""
    cmd += "\" u 2:5:6 notitle w yerrorbars, \""
    cmd += data + "\" u 2:7 t \'ideal\' w linespoints"
    with open('./fission.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key center top\n');
        f.write('set title \"Fission Experiment, Ratio=%f static, Work=%d, Outputs=%d\"\n' % (ratio, work, outputs))
        f.write('set xlabel \"Cores\"\n');
        f.write('set ylabel \"Nanoseconds\"\n');
        f.write(cmd)
    os.system('gnuplot ./fission.gnu')


def plot_normalized(ratio, work, outputs):
    data = 'fission-normalized' + str(int(ratio * 100)) + '_' + str(work) + '.dat'
    output = 'fission-normalized' + str(int(ratio * 100)) + '_' + str(work) + '.ps'
    cmd = "plot "
    cmd += "\"" + data + "\" u 2:7 t \"{/Helvetica=20  ideal}\" w linespoints, \\\n"
    cmd += "\"" + data + "\" u 2:3 t \"{/Helvetica=20  static}\" w linespoints, \\\n"
    cmd += "\"" + data + "\" u 2:3:4 notitle w yerrorbars, \\\n"
    cmd += "\"" + data + "\" u 2:5t \"{/Helvetica=20  dynamic}\" w linespoints,\\\n"
    cmd += "\"" + data + "\" u 2:5:6 notitle w yerrorbars"    
    with open('./fission-normalized.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set termoption enhanced\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key center left\n');
        f.write('set yrange [ 0 : ]\n');
        f.write('set xtics (1,2,4,8,16,32) font "Helvetica,20"\n');
        f.write('set title \"Fission Experiment Normalized, Ratio=%f static, Work=%d, Outputs=%d\"\n' % (ratio, work, outputs))
        f.write('set xlabel \"Cores\" font \"Helvetica,20\"\n');
        f.write('set ylabel \"Throughput normalized to static throughput on 1 core\" font \"Helvetica,20\"\n');
        f.write(cmd)
    os.system('gnuplot ./fission-normalized.gnu')


def main():         
    attempts = 3
    #ignore = 1024
    #outputs = 100000
    #cores = [1, 2, 4, 8, 16, 32]

    ignore = 32
    outputs = 10000
    cores = [1, 2, 4]

    #ratios = [0.10, 0.50, 0.90]
    ratios = [0.90]
    #total_work = [100, 1000, 10000]
    total_work = [1000]
    for work in total_work:
        for ratio in ratios:
            static_results = []
            dynamic_results = []
            for test in [Configs.static, Configs.dynamic]:
                for core in cores:
                    generate(test, work, ratio)
                    compile(test, outputs, ignore, core)
                    (avg, dev) = run(core, attempts, outputs)
                    print 'avg=' + str(avg)
                    print 'dev=' + str(dev)
                    if test == Configs.static:
                        static_results.append((ratio, core, avg, dev))
                    else:
                        dynamic_results.append((ratio, core, avg, dev))
                    print_all(work, ratio, static_results, dynamic_results);
            plot(ratio, work, outputs)
            plot_normalized(ratio, work, outputs)
                    
if __name__ == "__main__":
    main()
