#!/usr/bin/env python
import os
import subprocess
import time
import re

streamit_home = os.environ['STREAMIT_HOME']
strc          = os.path.join(streamit_home, 'strc')

# void->void pipeline test1 {
#     add FileReader<float>("../input/floats.in");
#     add F1(100);
#     add FileWriter<float>(stdout);
# }

# float->float filter F1 {
#   work pop 1 push * {
#     push(pop());
#   }
# }

# float->float filter F2 {
#   work pop * push 1 {
#     push(pop());
#   }
# }

# float->float filter F(int n) {
#   work pop 1 push * {
#     int i;
#     float x;
#     x = pop();
#     for (i = 0; i < n; i++) {
#       x += i * 3.0 - 1.0;
#     }
#     push(x);
#   }
# }

def compile(outputs):
    exe = './smp1' 
    cmd = [strc, '-smp', '1', '--perftest', '--outputs', str(outputs), 'test.str' ]
    print cmd
    subprocess.call(cmd, stdout=open(os.devnull, "w"), stderr=subprocess.STDOUT)
    assert os.path.exists(exe)


def make_streamit_app():
    with open("test.str", 'w') as f:
        f.write('void->void pipeline test {\n')
        f.write('    add FileReader<float>(\"../input/floats.in\");\n')
        f.write('    add Fstatic(100);\n')
        f.write('    add FileWriter<float>(stdout);\n')
        f.write('}\n')
        f.write('\n')
        f.write('float->float filter Fstatic(int n) {\n')
        f.write('    work pop 1 push 1 {\n')
        f.write('        int i;\n')
        f.write('        float x;\n')
        f.write('        x = pop();\n')
        f.write('        for (i = 0; i < n; i++) {\n')
        f.write('            x += i * 3.0 - 1.0;\n')
        f.write('        }\n')
        f.write('        push(x);\n')
        f.write('    }\n')
        f.write('}\n')
        f.write('\n')
        f.write('float->float filter Fdynamic(int n) {\n')
        f.write('    work pop 1 push * {\n')
        f.write('        int i;\n')
        f.write('        float x;\n')
        f.write('        x = pop();\n')
        f.write('        for (i = 0; i < n; i++) {\n')
        f.write('            x += i * 3.0 - 1.0;\n')
        f.write('        }\n')
        f.write('        push(x);\n')
        f.write('    }\n')
        f.write('}\n')


        

def main():
    make_streamit_app()
    compile(100)
                    
if __name__ == "__main__":
    main()
