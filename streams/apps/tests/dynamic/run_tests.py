#!/usr/bin/env python
import os
import subprocess
import glob
import filecmp

# def print_header():
#     print('#%s\t%s\t%s\t%s\t%s' % ('iter', 'mult', 'cost', 'static', 'dynamic'))


# def write_header(f):
#     f.write('#%s\t%s\t%s\t%s\t%s\n' % ('iter', 'mult', 'cost', 'static', 'dynamic'))


# def print_result(f, mult, i, cost, sta_avg, dyn_avg):    
#     print('%d\t%d\t%d\t%f\t%f' % (i, mult, cost, sta_avg, dyn_avg))
#     f.write('%d\t%d\t%d\t%f\t%f\n' % (i, mult, cost, sta_avg, dyn_avg))

def run_strc(filename):
    cmd = ["strc", "-smp", "2", "-i", "10", filename]
    return subprocess.Popen(cmd)

def run_make(filename):
    cmd = ["make"]
    return subprocess.Popen(cmd)

def run_exe(filename):
    output = filename + '.out'
    cmd = './smp2 > ' + output
    os.system(cmd)

def cleanup():
    cmd = 'rm -f *.c *.h *.dot *.java *.o smp Makefile cases/*.java'
    print cmd + '\n'
    os.system(cmd)


def compare(f1, f2):
    if (filecmp.cmp(f1, f2)):
        return 'success'
    else:
        return "FAIL"

# def plot_cost_fixed(cost):
#     f = open('./tmp.gnu', 'w')
#     cmd = "plot \"cost-fixed%d.dat\" u 2:4 t \'static\' w linespoints, \"cost-fixed%d.dat\" u 2:5 t \'dynamic\' w linespoints" % (cost, cost)
#     f.write('set terminal postscript\n')
#     f.write('set output \"cost-fixed%d.ps\"\n' % cost)
#     f.write('set title \"Static vs Dynamic, Iterations=1000, Cost=%d,\"\n' % cost)
#     f.write('set xlabel \"Multiplicity\"\n');
#     f.write('set ylabel \"Clock Cycles\"\n');
#     f.write(cmd)
#     f.close()
#     os.system('gnuplot ./tmp.gnu')

# def plot_mult_fixed(mult):
#     f = open('./tmp.gnu', 'w')
#     cmd = "plot \"mult-fixed%d.dat\" u 3:4 t \'static\' w linespoints, \"mult-fixed%d.dat\" u 3:5 t \'dynamic\' w linespoints" % (mult, mult)
#     f.write('set terminal postscript\n')
#     f.write('set output \"mult-fixed%d.ps\"\n' % mult)
#     f.write('set title \"Static vs Dynamic, Iterations=1000, Mult=%d,\"\n' % mult)
#     f.write('set xlabel \"Cost\"\n');
#     f.write('set ylabel \"Clock Cycles\"\n');
#     f.write(cmd)
#     f.close()
#     os.system('gnuplot ./tmp.gnu')



def main():
    path = 'cases/'
    results = []
    for infile in glob.glob( os.path.join(path, '*.str') ):
        print "current file is: " + infile
        print "Compile StreamIt code."
        p = run_strc(infile)
        p.wait()
        print "Compile C code."
        p = run_make(infile)
        p.wait()
        run_exe(infile)        
        ret = compare(infile + '.out', infile + '.exp')
        results.append(infile + ' : ' + ret)
        cleanup()
    print '**********************************'
    t = '\n'
    print t.join(results)

        
       
        
if __name__ == "__main__":
    main()

