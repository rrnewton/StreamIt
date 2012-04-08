#!/usr/bin/env python
import os
import subprocess
import time
import re
import math
import string


class Configs:
    nofusion, threadbatch = range(2)

streamit_home = os.environ['STREAMIT_HOME']
strc          = os.path.join(streamit_home, 'strc')

class Test:
    def __init__(self, name, testtype, source, cores, preoutputs, outputs, work, thread, attempts, flags):
        self.name = name
        self.testtype = testtype
        self.source = source
        self.cores = cores
        self.preoutputs = preoutputs
        self.outputs = outputs
        self.flags = flags
        self.exe = './smp' + str(cores)
        self.work = work
        self.attempts = attempts
        self.thread = thread
        self.mean = 0.0
        self.dev = 0.0
    def get_cmd(self):
        cmd = [strc, '-smp', str(self.cores), '--perftest', '--noiter',  '--threadopt', '--outputs', str(self.outputs), '--preoutputs', str(self.preoutputs)]
        cmd += self.flags
        cmd += [self.source]

        return cmd
    def to_string(self):
        s = self.name 
        s += ' work=' + str(self.work)
        s += ' cmd=' + ' '.join(self.get_cmd())     
        print s
    def generate(self):
        ratio = 0.5
        linestring = open('test.tmpl', 'r').read()
        test_template = string.Template(linestring)        
        #s = test_template.substitute(workstatic=str(int(self.ratio * self.work)),workdynamic=str(int((1 - self.ratio) * self.work)));
        s = test_template.substitute(threads=str(self.thread),work=str(self.work/self.thread));
        with open("test.str", 'w') as f:
            f.write(s)      
            #print s                
    def compile(self):
        cmd = self.get_cmd()
        print ' '.join(cmd)
        subprocess.call(cmd, stdout=open(os.devnull, "w"), stderr=subprocess.STDOUT)
        assert os.path.exists(self.exe)
    def run_one(self):
        (stdout, error) = subprocess.Popen([self.exe], stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
        regex = re.compile('input=(\d+) outputs=(\d+) ignored=(\d+) start=\d+:\d+ end=\d+:\d+ delta=(\d+):(\d+)')

        print 'RUN ONE'
        for m in regex.finditer(stdout):
            results = (m.group(1), m.group(2), m.group(3), m.group(4), m.group(5))       
            print results
            return results
    def run(self, outputs):
        results = []
        for num in range(self.attempts):
            results.append(self.run_one())
        # 1000000000 nanoseconds in 1 second    
        times = map(lambda x:  (long(x[3]) * 1000000000L) + long(x[4]) , results)
        tputs =  map(lambda x: (float(outputs)/float(x)) * 1000000000L , times)
        mean = reduce(lambda x, y: float(x) + float(y), tputs) / len(tputs)    
        deviations = map(lambda x: x - mean, tputs)
        squares = map(lambda x: x * x, deviations)
        dev = math.sqrt(reduce(lambda x, y: x + y, squares) /  (len(squares) - 1))
        self.mean = mean
        self.dev = dev
        x = (self.name, self.cores, self.preoutputs, self.outputs, self.work, self.thread, mean, dev)
        print x
        return x
                       
def print_tests_by_threading(cores, threads, all_tests):
    naive_tests = filter(lambda test: test.name.startswith('naive'), all_tests)
    streamit_tests = filter(lambda test: test.name.startswith('streamit'), all_tests)
    file = 'naive.dat'
    with open(file, 'w') as f: 
        s = '#\tcores\tfilters\tnaive\tdev\tstreamit\tdev'
        print s
        f.write(s + '\n') 
        for naive, streamit in zip(naive_tests, streamit_tests):
            s = '\t' + str(naive.cores)
            s += '\t' + str(naive.thread)
            s += '\t' + str(naive.mean) + '\t' +  str(naive.dev) + '\t'
            s += '\t' + str(streamit.mean) + '\t' +  str(streamit.dev) + '\t'
            print s
            f.write(s + '\n')  

    
            
def plot_tests_by_threading(cores, threads, all_tests):
    data = 'naive.dat'
    output = 'naive.ps'
    gnu = './naive.gnu'       
    cmd = "plot "
    cmd += " \"" + data + "\" u 2:5 t \'streamit\' w linespoints ls 2,\\\n"    
    cmd += " \"" + data + "\" u 2:5:6 notitle w yerrorbars,\\\n"
    cmd += " \"" + data + "\" u 2:3 t \'naive\' w linespoints ls 1,\\\n"    
    cmd += " \"" + data + "\" u 2:3:4 notitle w yerrorbars"
    with open(gnu, 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set termoption enhanced\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key right top\n')
        f.write('set title \"Naive Experiment\" font "Helvetica,20" \n')
        f.write('set xlabel \"Number of threads\" font "Helvetica,20" \n');        
        f.write('set ylabel \"Throughput (data items / sec )\" font "Helvetica,20"\n');
        f.write('set style line 1 lt 1 linecolor rgb "blue"\n');
        f.write('set style line 2 lt 1 linecolor rgb "red"\n');        
        f.write(cmd)
    os.system('gnuplot ' + gnu)
    os.system('ps2pdf ' + output)


def do_test(test, outputs):
    test.generate()
    test.compile()
    return test.run(outputs)

def main():
    attempts = 3
    preoutputs = 10
    outputs = 100     
    all_tests = []
    works = [1000]
    threads = [2, 4, 8, 16, 32]
    cores = [1]

    for work in works:
        for core in cores:
            for thread in threads:
                all_tests.append(Test('naive-' + str(thread), Configs.nofusion, 'test.str', core, preoutputs, outputs, work, thread, attempts, ['--naive']))
            for thread in threads:
                all_tests.append(Test('streamit-' + str(thread), Configs.nofusion, 'test.str', core, preoutputs, outputs, work, thread, attempts, []))
        
    all_results = map(lambda test: do_test(test, outputs), all_tests)

    print_tests_by_threading(cores, threads, all_tests)
    plot_tests_by_threading(cores, threads, all_tests)
                    
if __name__ == "__main__":
    main()
