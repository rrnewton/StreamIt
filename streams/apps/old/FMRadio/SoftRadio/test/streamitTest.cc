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

int main(void) {
  
  // Create Modules
  VrTestSource* source = new VrTestSource();
  VrFloatSink* sink = new VrFloatSink();

  VrLowPassFilter* lowpass = new VrLowPassFilter(200000, 108000000, 100, 4);
  VrFMDemodulator* FMDemod = new VrFMDemodulator(200000, 27000, 10000);

  CONNECT(sink, FMDemod, 1, 32);
  CONNECT(FMDemod, lowpass, 1, 32);
  CONNECT(lowpass, source, 1, 32);

  // Start System
  sink->setup();
  sink->start(-1);
}






