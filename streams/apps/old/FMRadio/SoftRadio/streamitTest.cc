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
#include "VrSubtractor.h"
#include "VrCharToFloat.h"
#include "VrFloatToChar.h"
#include "VrAdderFour.h"
#include "VrWeightedJoin.h"
#include "VrWeightedSplit.h"

int main(void) {
  
  // Create Modules
  VrFileSource<char>* source = new VrFileSource<char>("floats5000",1);
  VrFileSink<char>* sink = new VrFileSink<char>("streamitOut");

  //need to add:
  // 1) something to combine chars into floats
  VrCharToFloat* charToFloat = new VrCharToFloat();
  
  // 2) an initial lowpassfilter with parameters equal to streamit version DONE

  VrRealFIRfilter<float,float>* lowpass = 
    new VrRealFIRfilter<float,float>(5, 108000000, 100, 1);

  VrWeightedSplit<float>* split = new VrWeightedSplit<float>();

  // 3) a demodulator of some kind

  //ADD LATER
  
  // 4) an equalizer with 4 bandpass filters comprised of 2 lowpassfilters each

  //make bandpass with (lowpassa-lowpassb) added four-way
  VrAdderFour* adder = new VrAdderFour();
  VrWeightedJoin<float>* adderJoin = new VrWeightedJoin<float>();

  VrSubtractor* sub1 = new VrSubtractor();
  VrSubtractor* sub2 = new VrSubtractor();
  VrSubtractor* sub3 = new VrSubtractor();
  VrSubtractor* sub4 = new VrSubtractor();
  VrWeightedJoin<float>* subJoin1 = new VrWeightedJoin<float>();
  VrWeightedJoin<float>* subJoin2 = new VrWeightedJoin<float>();
  VrWeightedJoin<float>* subJoin3 = new VrWeightedJoin<float>();
  VrWeightedJoin<float>* subJoin4 = new VrWeightedJoin<float>();
  
  VrRealFIRfilter<float,float>* lowpass1a = 
    new VrRealFIRfilter <float,float>(2500, 50, 1);
  VrRealFIRfilter<float,float>* lowpass1b = 
    new VrRealFIRfilter<float,float>(1250, 50, 1);
  VrRealFIRfilter<float,float>* lowpass2a = 
    new VrRealFIRfilter<float,float>(5000, 50, 1);
  VrRealFIRfilter<float,float>* lowpass2b = 
    new VrRealFIRfilter<float,float>(2500, 50, 1);
  VrRealFIRfilter<float,float>* lowpass3a = 
    new VrRealFIRfilter<float,float>(10000, 50, 1);
  VrRealFIRfilter<float,float>* lowpass3b = 
    new VrRealFIRfilter<float,float>(5000, 50, 1);
  VrRealFIRfilter<float,float>* lowpass4a = 
    new VrRealFIRfilter<float,float>(20000, 50, 1);
  VrRealFIRfilter<float,float>* lowpass4b = 
    new VrRealFIRfilter<float,float>(10000, 50, 1);

  // 5) something to separate floats into chars

  VrFloatToChar* floatToChar = new VrFloatToChar(); 
  
  // Connect Modules
  
  CONNECT(sink, floatToChar, 1, 8);
  CONNECT(floatToChar, adder, 1, 32);
  CONNECT(adder, adderJoin, 1, 32);
  (*adderJoin).connect_dest(sub1, 1);
  (*adderJoin).connect_dest(sub2, 1);
  (*adderJoin).connect_dest(sub3, 1);
  (*adderJoin).connect_dest(sub4, 1);
  CONNECT(sub1, subJoin1, 1, 32);
  CONNECT(sub2, subJoin2, 1, 32);
  CONNECT(sub3, subJoin3, 1, 32);
  CONNECT(sub4, subJoin4, 1, 32);
  (*subJoin1).connect_dest(lowpass1a, 1);
  (*subJoin1).connect_dest(lowpass1b, 1);
  (*subJoin2).connect_dest(lowpass2a, 1);
  (*subJoin2).connect_dest(lowpass2b, 1);
  (*subJoin3).connect_dest(lowpass3a, 1);
  (*subJoin3).connect_dest(lowpass3b, 1);
  (*subJoin4).connect_dest(lowpass4a, 1);
  (*subJoin4).connect_dest(lowpass4b, 1);
  (*split).connect_dest(lowpass1a,1);
  (*split).connect_dest(lowpass1b,1);
  (*split).connect_dest(lowpass2a,1);
  (*split).connect_dest(lowpass2b,1);
  (*split).connect_dest(lowpass3a,1);
  (*split).connect_dest(lowpass3b,1);
  (*split).connect_dest(lowpass4a,1);
  (*split).connect_dest(lowpass4b,1);
  CONNECT(split, lowpass, 1, 32);
  CONNECT(lowpass, charToFloat, 1, 32);
  CONNECT(charToFloat, source, 1, 8);
  
  
  // Start System
  sink->setup();
  sink->start(-1);
}






