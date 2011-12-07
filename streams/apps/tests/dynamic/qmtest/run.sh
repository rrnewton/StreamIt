#!/bin/sh

# remove old tests

if [ -d ${STREAMIT_HOME}/users.qms ]
then
rm -rf ${STREAMIT_HOME}/users.qms
fi

#clean up old test
${STREAMIT_HOME}/regtest/qmtest/streamitqm clean

# create the xml file
echo "<regtest>
  <test root=\"${STREAMIT_HOME}/apps/tests/complex\"/>
  <test root=\"${STREAMIT_HOME}/apps/tests/fuse\"/>
  <option target=\"smp2\"/>
</regtest> " > smp.xml

#set up new test
${STREAMIT_HOME}/regtest/qmtest/streamitqm setup ${STREAMIT_HOME}/apps/tests/dynamic/qmtest/smp.xml

# set up path
export QMTEST_CLASS_PATH=${STREAMIT_HOME}/QMTest

#run test
qmtest -D ${STREAMIT_HOME} run
