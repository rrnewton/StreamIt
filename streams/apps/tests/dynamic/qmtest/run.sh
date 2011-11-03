#!/bin/sh

#clean up old test
${STREAMIT_HOME}/regtest/qmtest/streamitqm clean

#set up new test
${STREAMIT_HOME}/regtest/qmtest/streamitqm setup ${STREAMIT_HOME}/apps/tests/dynamic/qmtest/smp.xml

# set up path
export QMTEST_CLASS_PATH=${STREAMIT_HOME}/QMTest

#run test
qmtest -D ${STREAMIT_HOME} run
