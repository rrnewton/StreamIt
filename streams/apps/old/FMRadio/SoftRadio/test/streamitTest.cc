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

  // Create Modules
  VrTestSource* source = new VrTestSource();
  VrFloatSink* sink = new VrFloatSink(6);
  
  VrSimpleFilterSplit<float, float>* f1 = new VrSimpleFilterSplit<float, float>(3, 1);
  VrSimpleFilter<float, float>* f2 = new VrSimpleFilter<float, float>(2);
  VrSimpleFilter<float, float>* f3 = new VrSimpleFilter<float, float>(3);
  VrSimpleFilter<float, float>* f4 = new VrSimpleFilter<float, float>(4); 
  VrSimpleFilterJoin<float, float>* f5 = new VrSimpleFilterJoin<float, float>(3, 5); 
 
  
  CONNECT(sink, f5, 1, 32);
  CONNECT(f5, f4, 1, 32);
  CONNECT(f5, f3, 1, 32);
  CONNECT(f5, f2, 1, 32);
  CONNECTN(f4, f1, 2, 1, 32);
  CONNECTN(f3, f1, 1, 1, 32);
  CONNECTN(f2, f1, 0, 1, 32);
  CONNECT(f1, source, 1, 32);


  // Start System
  sink->setup();
  sink->start(-1);
}




