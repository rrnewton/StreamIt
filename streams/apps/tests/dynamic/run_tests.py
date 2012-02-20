#!/usr/bin/env python

import os
import subprocess
import threading
import glob
import filecmp
import sys

class Command(object):
    def __init__(self, cmd):
        self.cmd = cmd
        self.process = None

    def run(self, timeout):
        def target():
            self.process = subprocess.Popen(self.cmd, shell=True)
            self.process.communicate()
        thread = threading.Thread(target=target)
        thread.start()
        thread.join(timeout)
        if thread.is_alive():
            self.process.terminate()
            thread.join()

FNULL = open('/dev/null', 'w')

def run_strc(filename, cores):
    cmd = ["strc", "-smp", str(cores), "--outputs", "10", "-regtest", "--threadopt", filename]    
    print ' '.join(cmd)
    return subprocess.Popen(cmd, stdout=FNULL, stderr=FNULL)

def run_make(filename):
    cmd = ["make"]
    return subprocess.Popen(cmd, stdout=FNULL, stderr=FNULL)

def run_exe(filename, core):
    output = filename + '.out'
    cmd = './smp' + str(core) + ' > ' + output
    command = Command(cmd)
    command.run(timeout=10)

def cleanup():
    cmd = 'rm -f *.c *.h *.dot *.java *.o smp2 Makefile cases/*.java'
    # print cmd + '\n'
    os.system(cmd)

def compare(f1, f2):
    if (filecmp.cmp(f1, f2)):
        return 'success'
    else:
        return "FAIL"

def run_one(infile, cores):
    print "current file is: " + infile
    print "Compile StreamIt code."
    p = run_strc(infile, cores)
    p.wait()
    print "Compile C code."
    p = run_make(infile)
    p.wait()

def run_test(infile, cores):
    print "Testing with input file: " + infile + "."
    #print "Compile StreamIt code."
    p = run_strc(infile, cores)
    p.wait()
    #print "Compile C code."
    p = run_make(infile)
    p.wait()
    run_exe(infile, cores)        
    ret = infile + ' : ' + compare(infile + '.out', infile + '.exp')
    cleanup()
    return ret

def run_all():
    path = 'cases/'
    results = []
    cores = [1,2]
    for core in cores:
        for infile in glob.glob( os.path.join(path, '*.str') ):
            results.append(run_test(infile, core))
    t = '\n'
    return t.join(results)

def main():
    if (len(sys.argv) > 1):
        if (sys.argv[1] == 'clean'):
            cleanup()
        else:
            run_one(sys.argv[1], 2)
    else:
        ret = run_all();
        print "\nRESULTS:"
        print(ret)
        print ""
                    
if __name__ == "__main__":
    main()

