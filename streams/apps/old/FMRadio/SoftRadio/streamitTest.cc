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

  // 3) a demodulator of some kind

  //ADD LATER
  
  // 4) an equalizer with 4 bandpass filters comprised of 2 lowpassfilters each

  //make bandpass with (lowpassa-lowpassb) added four-way
  VrAdderFour* adder = new VrAdderFour();

  VrSubtractor* sub1 = new VrSubtractor();
  VrSubtractor* sub2 = new VrSubtractor();
  VrSubtractor* sub3 = new VrSubtractor();
  VrSubtractor* sub4 = new VrSubtractor();

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
  CONNECT(adder, sub1, 1, 32);
  CONNECT(adder, sub2, 1, 32);
  CONNECT(adder, sub3, 1, 32);
  CONNECT(adder, sub4, 1, 32);
  CONNECT(sub1, lowpass1a, 1, 32);
  CONNECT(sub1, lowpass1b, 1, 32);
  CONNECT(sub2, lowpass2a, 1, 32);
  CONNECT(sub2, lowpass2b, 1, 32);
  CONNECT(sub3, lowpass3a, 1, 32);
  CONNECT(sub3, lowpass3b, 1, 32);
  CONNECT(sub4, lowpass4a, 1, 32);
  CONNECT(sub4, lowpass4b, 1, 32);
  CONNECT(lowpass1a, lowpass, 1, 32);
  CONNECT(lowpass1b, lowpass, 1, 32);
  CONNECT(lowpass2a, lowpass, 1, 32);
  CONNECT(lowpass2b, lowpass, 1, 32);
  CONNECT(lowpass3a, lowpass, 1, 32);
  CONNECT(lowpass3b, lowpass, 1, 32);
  CONNECT(lowpass4a, lowpass, 1, 32);
  CONNECT(lowpass4b, lowpass, 1, 32);
  CONNECT(lowpass, charToFloat, 1, 32);
  CONNECT(charToFloat, source, 1, 8);
  
  // Start System
  sink->setup();
  sink->start(-1);
}






