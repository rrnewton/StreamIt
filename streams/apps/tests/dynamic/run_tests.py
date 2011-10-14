#!/usr/bin/env python
import os
import subprocess
import glob
import filecmp
import sys 

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

def run_one(infile):
    print "current file is: " + infile
    print "Compile StreamIt code."
    p = run_strc(infile)
    p.wait()
    print "Compile C code."
    p = run_make(infile)
    p.wait()

def run_test(infile):
    print "current file is: " + infile
    print "Compile StreamIt code."
    p = run_strc(infile)
    p.wait()
    print "Compile C code."
    p = run_make(infile)
    p.wait()
    run_exe(infile)        
    ret = infile + ' : ' + compare(infile + '.out', infile + '.exp')
    cleanup()
    return ret

def run_all():
    path = 'cases/'
    results = []
    for infile in glob.glob( os.path.join(path, '*.str') ):
        results.append(run_test(infile))
    t = '\n'
    return t.join(results)

def main():
    if (len(sys.argv) > 1):
        if (sys.argv[1] == 'clean'):
            cleanup()
        else:
            run_one(sys.argv[1])
    else: 
        print(run_all())
                    
if __name__ == "__main__":
    main()

