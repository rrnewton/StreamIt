#!/usr/bin/env python
import os
import subprocess
import time
import re

class Configs:
    nofusion, fusion, dynamic = range(3)

streamit_home = os.environ['STREAMIT_HOME']
strc          = os.path.join(streamit_home, 'strc')

tests        = [
    (Configs.nofusion, [strc, '-smp', '1', '--perftest', '--noiter', '--nofuse'], './smp1' ),
    (Configs.fusion, [strc, '-smp', '1', '--perftest', '--noiter'], './smp1' ),
    (Configs.dynamic, [strc, '-smp', '1', '--perftest', '--noiter'], './smp1' ),
    ]

def generate(test, num_filters, work):
    op = 'void->void pipeline test {\n';
    op += '    add FileReader<float>(\"../input/floats.in\");\n'
    op += '    for(int i=1; i<=' + str(num_filters) + '; i++)\n'
    if test[0] == Configs.nofusion:
        op += '        add Fstatic();\n'
    if test[0] == Configs.fusion:
        op += '        add Fstatic();\n'
    if test[0] == Configs.dynamic:        
        op += '        add Fdynamic();\n'
    op += '    add FileWriter<float>(stdout);\n'
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
    subprocess.call(cmd, stdout=open(os.devnull, "w"), stderr=subprocess.STDOUT)
    assert os.path.exists(exe)


def run_one(test):
    print 'run_one'
    exe = test[2]
    results = []
    if test[0] == Configs.nofusion:
        test_type = 'no-fusion'
    elif test[0] == Configs.fusion:
        test_type = 'fusion'
    elif test[0] == Configs.dynamic:
        test_type = 'dynamic'    
    (stdout, error) = subprocess.Popen([exe], stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
    regex = re.compile('input=(\d+) outputs=(\d+) ignored=(\d+) start=\d+:\d+ end=\d+:\d+ delta=(\d+):(\d+)')
    #                        input=20 outputs=20 ignored=10 start=1327947510:94511000 end=1327947510:94718000 delta=0:207000
    for m in regex.finditer(stdout):
        results = ([test_type] + [m.group(1)] + [m.group(2)] + [m.group(3)] + [m.group(4)] + [m.group(5)])       
    return results

def doit(x):
    print 'x[4]=%f' % float(x[4]) * 1000000000
    print 'x[5]=%f' % float(x[5])
    return x[5]

def run(test, attempts):
    results = []
    for num in range(attempts):
         result = run_one(test)
         results.append(result)
         #for result in results:
         print result
    # 1000000000 nanoseconds in 1 second    
    times = map(lambda x: x[5], results)
    #times = map(doit, results)
    avg = reduce(lambda x, y: float(x) + float(y), times) / len(times)    
    return avg

def print_all(work, nofusion_results, fusion_results, dynamic_results):
    file = 'work' + str(work) + '.dat'
    with open(file, 'w') as f:
        s = '#%s\t%s\t%s\t%s\t%s' % ( 'filters', 'work', 'nofusion', 'fusion', 'dynamic')
        print s
        f.write(s + '\n')  
        for x, y, z in zip(nofusion_results, fusion_results, dynamic_results):
            s = '%d\t%d\t%0.2f\t%0.2f\t%0.2f' % (x[2], x[1], x[3], y[3], z[3])
            print s
            f.write(s + '\n')  

def plot(work):
    data = 'work' + str(work) + '.dat'
    output = 'work' + str(work) + '.ps'
    cmd = "plot \"" + data + "\" u 1:3 t \'nofusion\' w linespoints, \"" + data + "\" u 1:4 t \'fusion\' w linespoints, \"" + data + "\" u 1:5 t \'dynamic\' w linespoints"
    with open('./tmp.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        #f.write('set title \"Static vs Dynamic, Iterations=1000, Cost=%d,\"\n' % cost)
        f.write('set xlabel \"Filters\"\n');
        f.write('set ylabel \"Nanoseconds\"\n');
        f.write(cmd)
    os.system('gnuplot ./tmp.gnu')

def main():
    attempts = 3
    ignore = 10
    filters = [1, 2, 4, 8, 16, 32]
    total_work = [100]   
    for work in total_work:
        nofusion_results = []
        fusion_results = []
        dynamic_results = []
        for num_filters in filters:
            for test in tests:
                generate(test, num_filters, work)
                compile(test, work, ignore)
                avg =  run(test, attempts)
                if test[0] == Configs.nofusion:
                    x = ('no-fusion', work, num_filters, avg)
                    print x
                    nofusion_results.append(('no-fusion', work, num_filters, avg))
                elif test[0] == Configs.fusion:
                    fusion_results.append(('fusion', work, num_filters, avg))
                    x = ('fusion', work, num_filters, avg)
                    print x
                elif test[0] == Configs.dynamic:
                    dynamic_results.append(('dynamic', work, num_filters, avg))
                    x = ('dynamic', work, num_filters, avg)
                    print x
        print_all(work, nofusion_results, fusion_results, dynamic_results)
        plot(work)
                    
if __name__ == "__main__":
    main()
