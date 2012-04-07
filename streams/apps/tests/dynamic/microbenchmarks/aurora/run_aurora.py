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
    def __init__(self, name, testtype, source, cores, preoutputs, outputs, work, ratio, batch, attempts, flags):
        self.name = name
        self.testtype = testtype
        self.source = source
        self.cores = cores
        self.preoutputs = preoutputs
        self.outputs = outputs
        self.flags = flags
        self.exe = './smp' + str(cores)
        self.work = work
        self.ratio = ratio
        self.attempts = attempts
        self.batch = batch
        self.mean = 0.0
        self.dev = 0.0
    def get_cmd(self):
        cmd = [strc, '-smp', str(self.cores), '--perftest', '--noiter', '--outputs', str(self.outputs), '--preoutputs', str(self.preoutputs)]
        if self.batch > 1:
            cmd += ['--threadbatch', str(self.batch)]
        cmd += self.flags
        cmd += [self.source]

        return cmd
    def to_string(self):
        s = self.name 
        s += ' work=' + str(self.work)
        s += ' work=' + str(self.ratio)
        s += ' cmd=' + ' '.join(self.get_cmd())     
        print s
    def generate(self):
        ratio = 0.5
        linestring = open('test.tmpl', 'r').read()
        test_template = string.Template(linestring)        
        s = test_template.substitute(workstatic=str(int(self.ratio * self.work)),workdynamic=str(int((1 - self.ratio) * self.work)));
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
        for m in regex.finditer(stdout):
            results = (m.group(1), m.group(2), m.group(3), m.group(4), m.group(5))       
        print results
        return results
    def run(self):
        results = []
        for num in range(self.attempts):
            results.append(self.run_one())            
        # 1000000000 nanoseconds in 1 second    
        times = map(lambda x:  (long(x[3]) * 1000000000L) + long(x[4]) , results)
        mean = reduce(lambda x, y: float(x) + float(y), times) / len(times)    
        deviations = map(lambda x: x - mean, times)
        squares = map(lambda x: x * x, deviations)
        dev = math.sqrt(reduce(lambda x, y: x + y, squares) /  (len(squares) - 1))
        self.mean = mean
        self.dev = dev
        x = (self.name, self.cores, self.preoutputs, self.outputs, self.work, self.ratio, mean, dev)
        print x
        return x
        

# def print_all(work, batching, static_results, threadbatch_results):
#     file = 'batch' + str(work) + '.dat'
#     with open(file, 'w') as f:
#         print '--------------------'
#         s = '#work\tfilters\tstatic-avg\tstatic-dev ' + '\t'.join(["%s-avg\t%s-dev" % (b, b) for b in batching])
#         print s
#         f.write(s + '\n')  
#         for x, y in zip(static_results, threadbatch_results):
#             raw = ('\t'.join('%d' % x[i+1] for i in range (4)))  + '\t' + y[3]
#             print raw
#             f.write(raw + '\n')  

#     print '++++++++++++++++++++'
#     file = 'batch-normalized' + str(work) + '.dat'
#     with open(file, 'w') as f:
#         s = '#work\tfilters\t ' + '\t'.join(["%s-norm" % b for b in batching])
#         print s
#         f.write(s + '\n')  
#         for x, y in zip(static_results, threadbatch_results):
#             vals = y[3].split('\t')
#             averages = map(lambda i: vals[i],filter(lambda i: i%2 == 0,range(len(vals))))
#             normalized = ('\t'.join('%d' % x[i+1] for i in range (2))) + '\t' + '\t'.join('%f' % (float(a)/x[3]) for a in averages) 
#             print normalized
#             f.write(normalized + '\n')  


def print_tests_by_batching(batching, all_tests):
    file = 'aurora.dat'
    with open(file, 'w') as f:
        batch_list = filter(lambda test: test.batch == batching[0], all_tests)
        s = '#\tbatch'
        for test in batch_list:
            s += '\t' + test.name + '-mean\t' + test.name + '-dev'
        print s
        f.write(s + '\n')  
        for batch in batching:
            s = '\t' + str(batch)
            batch_list = filter(lambda test: test.batch == batch, all_tests)
            for test in batch_list:            
                s += '\t' + str(test.mean) + '\t' + str(test.dev)
            print s
            f.write(s + '\n')
            
def plot_tests_by_batching(batching, all_tests):
    data = 'aurora.dat'
    output = 'aurora.ps'
    gnu = './aurora.gnu'    
    batch_list = filter(lambda test: test.batch == batching[0], all_tests)
    plot = "plot"
    cmds = []
    i = 2
    for test in batch_list:
        cmd = " \"" + data + "\" u 1:" + str(i) +  " t \'" + test.name + ' cores' + "\' w linespoints"
        cmds.append(cmd)
        i = i+2
    print ','.join(cmds)
    with open(gnu, 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key right top\n')
        f.write('set title \"Aurora Experiment\"\n')
        f.write('set log x\n')
        f.write('set xlabel \"Batch Size\"\n');
        f.write('set ylabel \"Nanoseconds\"\n');
        f.write( plot + ','.join(cmds))
    os.system('gnuplot ' + gnu)
    os.system('ps2pdf ' + output)


def plot_normalized(work, outputs, batching):
    data = 'batch-normalized' + str(work) + '.dat'
    output = 'batch-normalized' + str(work) + '.ps'
    cmds = []
    for i in range(len(batching)):
        cmds.append("\"" + data + "\" u 2:" + str(i+3) + " t \'batch=" + str(batching[i]) + "\' w linespoints")        
    with open('./tmp.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set key left top\n')
        f.write('set log x\n')
        f.write('set title \"Fusion Experiment With Batching Normalized, Work=%d, Outputs=%d\"\n' % (work, outputs))
        f.write('set xlabel \"Operators\"\n');
        f.write('set ylabel \"Throughput normalized to static throughput with 1 core\"\n');
        f.write( 'plot ' + ','.join(cmds))
    os.system('gnuplot ./tmp.gnu')
    os.system('ps2pdf ' + output)


def do_test(test):
    test.generate()
    test.compile()
    return test.run()

def main():
    attempts = 3
    preoutputs = 1000
    outputs = 2000
    work = 2000
    ratio = 0.50
    all_tests = []
    batching = [1, 10, 100]
    cores = [1, 4, 8]

    for batch in batching:
        for core in cores:
            all_tests.append(Test('aurora-' + str(core), Configs.nofusion, 'test.str', core, preoutputs, outputs, work, ratio, batch, attempts, ['--nofizz']))
        for core in cores:
            all_tests.append(Test('streamit-' + str(core), Configs.nofusion, 'test.str', core, preoutputs, outputs, work, ratio, batch, attempts, ['--threadopt']))
        
    all_results = map(lambda test: do_test(test), all_tests)

    print_tests_by_batching(batching, all_tests)
    plot_tests_by_batching(batching, all_tests)
                    
if __name__ == "__main__":
    main()
