/* -*- Mode: c++ -*-
 *
 *  Copyright 1997 Massachusetts Institute of Technology
 * 
 *  Permission to use, copy, modify, distribute, and sell this software and its
 *  documentation for any purpose is hereby granted without fee, provided that
 *  the above copyright notice appear in all copies and that both that
 *  copyright notice and this permission notice appear in supporting
 *  documentation, and that the name of M.I.T. not be used in advertising or
 *  publicity pertaining to distribution of the software without specific,
 *  written prior permission.  M.I.T. makes no representations about the
 *  suitability of this software for any purpose.  It is provided "as is"
 *  without express or implied warranty.
 * 
 */

#include <VrFileSink.h>
#include <VrFileSource.h>
#include <VrRealFIRfilter.h>

#include "VrSimpleFilter.h"
#include "VrLowPassFilter.h"
#include "VrFMDemodulator.h"
#include "VrTestSource.h"
#include "VrFloatSink.h"
#include "VrDuplicateSplitter.h"
#include "VrRoundRobinJoiner.h"
#include "VrSimpleFilterSplit.h"
#include "VrSimpleFilterJoin.h"

#include "VrFloatSubtract.h"
#include "VrFloatAdder.h"

int main(void) {
  float rate = 200000;
 
  // Create Modules
  VrTestSource* source = new VrTestSource();
  VrFloatSink* sink = new VrFloatSink();
  VrLowPassFilter* lowpass = new VrLowPassFilter(1, 200000, 108000000, 100, 4);
  VrFMDemodulator* FMDemod = new VrFMDemodulator(8, 200000, 27000, 10000);
  
  VrLowPassFilter* lowpass1 = new VrLowPassFilter(1, rate, 1250, 50, 0);
  VrLowPassFilter* lowpass2 = new VrLowPassFilter(1, rate, 2500, 50, 0);
  VrLowPassFilter* lowpass3 = new VrLowPassFilter(1, rate, 2500, 50, 0);
  VrLowPassFilter* lowpass4 = new VrLowPassFilter(1, rate, 5000, 50, 0);
  VrLowPassFilter* lowpass5 = new VrLowPassFilter(1, rate, 5000, 50, 0);
  VrLowPassFilter* lowpass6 = new VrLowPassFilter(1, rate, 10000, 50, 0);
  VrLowPassFilter* lowpass7 = new VrLowPassFilter(1, rate, 10000, 50, 0);
  VrLowPassFilter* lowpass8 = new VrLowPassFilter(1, rate, 20000, 50, 0);
 
  VrFloatSubtract* fsub1 = new VrFloatSubtract();
  VrFloatSubtract* fsub2 = new VrFloatSubtract();
  VrFloatSubtract* fsub3 = new VrFloatSubtract();
  VrFloatSubtract* fsub4 = new VrFloatSubtract();
  
  VrFloatAdder* fadder = new VrFloatAdder();

  CONNECT(sink, fadder, 1, 32);
   
  CONNECT(fadder, fsub4, 1, 32);
  CONNECT(fadder, fsub3, 1, 32);
  CONNECT(fadder, fsub2, 1, 32);
  CONNECT(fadder, fsub1, 1, 32);

  CONNECT(fsub4, lowpass8, 1, 32); 
  CONNECT(fsub4, lowpass7, 1, 32); 

  CONNECT(fsub3, lowpass6, 1, 32); 
  CONNECT(fsub3, lowpass5, 1, 32); 

  CONNECT(fsub2, lowpass4, 1, 32); 
  CONNECT(fsub2, lowpass3, 1, 32); 

  CONNECT(fsub1, lowpass2, 1, 32); 
  CONNECT(fsub1, lowpass1, 1, 32); 

  CONNECTN(lowpass8, FMDemod, 7, 1, 32);
  CONNECTN(lowpass7, FMDemod, 6, 1, 32);
  CONNECTN(lowpass6, FMDemod, 5, 1, 32);
  CONNECTN(lowpass5, FMDemod, 4, 1, 32);
  CONNECTN(lowpass4, FMDemod, 3, 1, 32);
  CONNECTN(lowpass3, FMDemod, 2, 1, 32);
  CONNECTN(lowpass2, FMDemod, 1, 1, 32);
  CONNECTN(lowpass1, FMDemod, 0, 1, 32);  
  
  CONNECT(FMDemod, lowpass, 1, 32);
  
  CONNECT(lowpass, source, 1, 32);
 

  // Start System
  sink->setup();
  sink->start(-1);
}




