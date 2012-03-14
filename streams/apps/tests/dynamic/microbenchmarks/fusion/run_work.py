#!/usr/bin/env python
import os
import subprocess
import time
import re
import math


def print_all(work, nofusion_results, fusion_results, threadopt_results, threadbatch_results):
    file = 'fusion' + str(work) + '.dat'
    with open(file, 'w') as f:
        s = '#%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s' % ( 'filters', 'work', 'nofusion', 'dev', 'fusion', 'dev', 'threadopt', 'dev', 'threadbatch', 'dev')
        print s
        f.write(s + '\n')  
        for x, y, t, b in zip(nofusion_results, fusion_results, threadopt_results, threadbatch_results):
            s = '%d\t%d\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f\t%0.2f' % (x[2], x[1], x[3], x[4], y[3], y[4], t[3], t[4], b[3], b[4])
            print s
            f.write(s + '\n')
    file = 'fusion-normalized' + str(work) + '.dat'
    nofusion = nofusion_results[0]
    with open(file, 'w') as f:
        s = '#%s\t%s\t%s\t%s\t%s' % ( 'filters', 'work', 'fusion', 'threadopt', 'threadbatch')
        print s
        f.write(s + '\n')
        for fusion, threadopt, threadbatch in zip(fusion_results,threadopt_results, threadbatch_results):
            s = '%d\t%d\t%0.2f\t%0.2f\t%0.2f' % (fusion[2], fusion[1], (fusion[3]/nofusion[3]), (threadopt[3]/nofusion[3]), (threadbatch[3]/nofusion[3]))
            print s
            f.write(s + '\n')



def plot():
    data =  'work-normalized.dat'
    output =  'work-normalized.ps'
    cmd = "plot "
    cmd += "\"" + data + "\" u 2:4 notitle w linespoints,"
    cmd += "\"" + "\" u 2:4:(sprintf(\"[%.0f,%.1f]\",$2,$4)) notitle with labels offset 0.25,1.75"
    with open('./tmp.gnu', 'w') as f:        
        f.write('set terminal postscript\n')
        f.write('set output \"' + output + '\"\n')
        f.write('set log x\n')
        f.write('set key left top\n');
        f.write('set title \"Fusion Experiment, 2 Operators, Increasing Workload, Normalized\"\n')
        f.write('set xlabel \"Work\"\n');
        f.write('set ylabel \"Throughput normalized to static throughput with 1 core\"\n');
        f.write(cmd)
    os.system('gnuplot ./tmp.gnu')

    
def main():

    file = 'work-normalized.dat'
    with open(file, 'w') as fout:
        s = '#%s\t%s\t%s\t%s\t%s' % ( 'filters', 'work', 'fusion', 'threadopt', 'threadbatch')
        fout.write(s + '\n')
        total_work = [1, 10, 100, 1000, 10000, 100000]
        for work in total_work:
            filename = 'fusion-normalized' + str(work) + '.dat'
            with open(filename, 'r') as fin:   
                for line in fin:
                    matchObj = re.match( r'^2.*', line)
                    if matchObj:
                        print line
                        fout.write(line)
    plot()

if __name__ == "__main__":
    main()
