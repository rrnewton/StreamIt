#!/usr/bin/env python
import os
import subprocess
import time
import re

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
    op += '        push(pop())\n;'
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
    print op;
    with open("test.str", 'w') as f:
        f.write(op)      

def compile(test, outputs, ignore, core):
    exe = './smp' + str(core)
    if test == Configs.static:
        cmd = [strc, '--perftest', '--noiter',
               '--outputs', str(outputs), '--preoutputs', str(ignore), '-smp', str(core), 'test.str' ]
    else:
        cmd = [strc, '--perftest', '--noiter', '--nofuse',
               '--outputs', str(outputs), '--preoutputs', str(ignore), '-smp', str(core), 'test.str' ]
    print cmd
    subprocess.call(cmd, stdout=open(os.devnull, "w"), stderr=subprocess.STDOUT)
    assert os.path.exists(exe)


def run_one(core):
    print 'run_one'
    exe = './smp' + str(core)
    results = []
    (stdout, error) = subprocess.Popen([exe], stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
    regex = re.compile('input=(\d+) outputs=(\d+) ignored=(\d+) start=\d+:\d+ end=\d+:\d+ delta=(\d+):(\d+)')
    for m in regex.finditer(stdout):
        results = ([core] + [m.group(1)] + [m.group(2)] + [m.group(3)] + [m.group(4)] + [m.group(5)])       
    return results

def run(core, attempts):
    results = []
    for num in range(attempts):
         result = run_one(core)
         results.append(result)
         print result
    # 1000000000 nanoseconds in 1 second    
    times = map(lambda x: x[5], results)
    #times = map(doit, results)
    avg = reduce(lambda x, y: float(x) + float(y), times) / len(times)    
    return avg

def print_all(ratio, static_results, dynamic_results):
    file = 'fission' + str(int(ratio * 100)) + '.dat'
    with open(file, 'w') as f:
        # s = '#%s\t%s\t%s' % ( 'ratio', 'cores', 'avg')
        # print s
        # f.write(s + '\n')  
        # for r in dynamic_results:
        #     print r
        #     s = '%0.2f\t%d\t%0.2f' % (r[0], r[1], r[2])
        #     print s
        #     f.write(s + '\n')
        s = '#%s\t%s\t%s\t%s' % ( 'ratio', 'cores', 'static', 'dynamic')
        print s
        f.write(s + '\n')  
        for x, y in zip(static_results, dynamic_results):
            s = '%0.2f\t%d\t%0.2f\t%0.2f' % (x[0], x[1], x[2], y[2])
            print s
            f.write(s + '\n')

def plot(ratio):
    data = 'fission' + str(int(ratio * 100)) + '.dat'
    output = 'fission' + str(int(ratio * 100)) + '.ps'
    # cmd = "plot \"" + data + "\" u 2:3 w linespoints"
    cmd = "plot \"" + data + "\" u 2:3 t \'static\' w linespoints, \"" + data + "\" u 2:4 t \'dynamic\' w linespoints, \"" + data + "\""    
    with open('./tmp.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        #f.write('set title \"Static vs Dynamic, Iterations=1000, Cost=%d,\"\n' % cost)
        f.write('set xlabel \"Cores\"\n');
        f.write('set ylabel \"Nanoseconds\"\n');
        f.write(cmd)
    os.system('gnuplot ./tmp.gnu')

def main():         
    attempts = 3
    ignore = 10
    outputs = 1000
    cores = [1, 2, 4, 8, 16, 32]
    ratios = [0.10, 0.50, 0.90]
    total_work = [100]   
    for work in total_work:
        for ratio in ratios:
            static_results = []
            dynamic_results = []
            for test in [Configs.static, Configs.dynamic]:
                for core in cores:
                    generate(test, work, ratio)
                    compile(test, outputs, ignore, core)
                    avg =  run(core, attempts)
                    print 'avg=' + str(avg)
                    if test == Configs.static:
                        static_results.append((ratio, core, avg))
                    else:
                        dynamic_results.append((ratio, core, avg))
                    print_all(ratio, static_results, dynamic_results);
            plot(ratio)
                    
if __name__ == "__main__":
    main()
