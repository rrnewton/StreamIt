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
#include <VrSigSource.h>
#include <VrGnuPlotSink.h>
#include <VrAudioSink.h>
#include "VrNullSink.h"
#include <VrGuppiSource.h>
#include <VrFakeGuppiSource.h>
#include <VrRealFIRfilter.h>
#include <VrComplexFIRfilter.h>
#include <VrQuadratureDemod.h>
#include <VrConnect.h>
#include <VrComplex.h>
#include <VrMultiTask.h>
#include <VrPerfGraph.h>
#include "VrTestSource.h"

int main(void) {

  int cpuRate;
  int gupRate;
  int CFIRdecimate;
  int quadRate;
  int RFIRdecimate;
  int audioRate;
  //  int cTaps;
  unsigned int targettime;
  //  unsigned int actualtime;
  int seconds = 5;

  //  cTaps = 800;
  cpuRate = 530000000;
  gupRate = 33000000;
  CFIRdecimate = 825;
  quadRate = gupRate / CFIRdecimate;
  RFIRdecimate = 5;
  audioRate = quadRate / RFIRdecimate;
  targettime = (unsigned int)((1/(float)(audioRate) * cpuRate) - 2000);

  const int numChannels=4;
  float center_freq[] = {10370400, 10355400, 10340400, 10960500};
  float gain[] = {2.0, 2.0, 2.0, 2.0};
  int num_taps[] = {400, 400, 400, 400};

  // Create Modules
  //VrFakeGuppiSource<char>* source = new VrFakeGuppiSource<char>(200,gupRate);
  VrTestSource* source = new VrTestSource();
  
  VrComplexFIRfilter<char>* channel_filter =
    new VrComplexFIRfilter<char>(numChannels, CFIRdecimate, 
					     num_taps, center_freq, gain);

  VrQuadratureDemod<float>* demod[numChannels];
  VrRealFIRfilter<float,short>* if_filter[numChannels];
  VrNullSink<short>* sink[numChannels];

  VrMultiTask *multi = new VrMultiTask();
  
  int i;
 
  for (i = 0; i < numChannels; i++)
    {
      demod[i] = new VrQuadratureDemod<float>(0.0);
      if_filter[i] = new VrRealFIRfilter<float,short>(RFIRdecimate,4000.0,20,1.0);
      sink[i] = new VrNullSink<short>(1024);

      multi->add(sink[i]);
      // Connect Modules

      CONNECT(sink[i], if_filter[i], audioRate, 16);
      CONNECT(if_filter[i], demod[i], audioRate, 32);
      CONNECTN(demod[i], channel_filter, i, quadRate, 64);

    }

  CONNECT(channel_filter, source, gupRate, 8);

  // Start System
  multi->setup();
  multi->start(-1);

}






