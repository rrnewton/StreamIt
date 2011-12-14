#!/bin/sh

tests="arrayinit 
arraytest 
builtins-test   
cell   
complex   
complexid   
constprop  
dynamic  
field-init  
field-prop  
filereader  
filewriter  
fir-test  
fission  
flybit  
fuse  
fuse-test  
fusion-scaling  
hello-separate  
hello-simple  
hello-splits  
indirect  
lifter  
linear-partition  
lineartest  
macro  
nulljoiner  
partition  
peek-pipe  
prework  
recursive  
rounding  
script-ratios  
simple-split  
smp  
splitjoins  
struct  
unroll  
weighted-rr"

# remove old tests
if [ -d ${STREAMIT_HOME}/users.qms ]
then
rm -rf ${STREAMIT_HOME}/users.qms
fi

#clean up old test
${STREAMIT_HOME}/regtest/qmtest/streamitqm clean

# create the xml file
all="<regtest>\n"
for f in ${tests}
do
 all+="  <test root=\"${STREAMIT_HOME}/apps/tests/${f}\"/>\n"
done
all+="  <option target=\"smp2\"/>\n"
all+="</regtest>"
echo ${all} > smp.xml

#set up new test
${STREAMIT_HOME}/regtest/qmtest/streamitqm setup ${STREAMIT_HOME}/apps/tests/dynamic/qmtest/smp.xml

# set up path
export QMTEST_CLASS_PATH=${STREAMIT_HOME}/QMTest

#run test
qmtest -D ${STREAMIT_HOME} run
