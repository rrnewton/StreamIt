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

#include "VrTestSource.h"
#include "VrFloatSink.h"
#include "VrComplexMultiDimFir.h"
#include "VrComplexBeamFormer.h"
#include "VrComplexToMag.h"
#include "VrSimpleFilter.h"

int main(void) {
  int numChannels           = 1;
    int numSamples            = 2;
    int numBeams              = 1;
      int numCoarseFilterTaps   = 1;
      int numFineFilterTaps     = 1;
      int coarseDecimationRatio = 1;
      int fineDecimationRatio   = 1;
  int numSegments           = 1;
  int numPostDec1           = numSamples/coarseDecimationRatio;
  int numPostDec2           = numPostDec1/fineDecimationRatio;
  int mfSize                = numSegments*numPostDec2;
//  int pulseSize             = numPostDec2/2;
//  int predecPulseSize       = pulseSize*coarseDecimationRatio*fineDecimationRatio;
//  int targetBeam            = numBeams/4;
//  int targetSample          = numSamples/4;
//  int targetSamplePostdec   = 1 + targetSample/coarseDecimationRatio/fineDecimationRatio;
//  float dOverLambda         = 0.5f;
//  float cfarThreshold       = 0.95f*dOverLambda*numChannels*(0.5f*pulseSize);
 
  // Create Modules
  VrTestSource* source = new VrTestSource();
  VrFloatSink* sink = new VrFloatSink();
 
 VrSimpleFilter<float, float>* test = new VrSimpleFilter<float, float>(1);
 VrSimpleFilter<float, float>* test1 = new VrSimpleFilter<float, float>(1);
 VrSimpleFilter<float, float>* test2 = new VrSimpleFilter<float, float>(1);
 VrSimpleFilter<float, float>* test3 = new VrSimpleFilter<float, float>(1);
 VrSimpleFilter<float, float>* test4 = new VrSimpleFilter<float, float>(1);


  VrComplexMultiDimFir* cmdf1 = 
    new VrComplexMultiDimFir(numCoarseFilterTaps, numSamples, coarseDecimationRatio);
  VrComplexMultiDimFir* cmdf2 = 
    new VrComplexMultiDimFir(numFineFilterTaps, numPostDec1, fineDecimationRatio);
  VrComplexBeamFormer* cbf = new VrComplexBeamFormer(numBeams, numChannels, numPostDec2);
  VrComplexMultiDimFir* cmdf3 = new VrComplexMultiDimFir(mfSize, mfSize, 1);
  VrComplexToMag* ctm = new VrComplexToMag();
  
  CONNECT(sink, ctm, 1, 32);
  CONNECT(ctm, cmdf3, 1, 32);
  CONNECT(cmdf3, cbf, 1, 32);
  CONNECT(cbf, cmdf2, 1, 32);
  CONNECT(cmdf2, cmdf1, 1, 32);
  CONNECT(cmdf1, source, 1, 32);
 

  // Start System
  sink->setup();
  sink->start(-1);
}


