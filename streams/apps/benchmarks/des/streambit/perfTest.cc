#include <stdio.h> 
#include <stdlib.h>
#include <sys/time.h>
#include "output.h"

int NITERS_ = 300000;
#define K  100

int nmasters = 2;
unsigned masterin[128] =  { /* 0x01234567, 0x89ABCDEF */ 
			    0x95F8A5E5,  0xDD31D900 };
unsigned masterout[128] = { /* 0x85E81354, 0x0F0AB405 */
			    0x80000000, 0x00000000
			   };


void DES_KeyGen_filter(unsigned* Input, unsigned* Output);

int main(int argc, char** argv){
  FilterState fs;
    
  unsigned inb[K], outb[K];  
  unsigned output[128];
  
  unsigned key_in[200], key_out[200];

  printf("STARTING    \n");  
  for(int q=0; q<10; q+=2){
    key_in[q+0]= 0x01010101; /* 0x13345779; */
    key_in[q+1]= 0x01010101; /* 0x9BBCDFF1; */
  }
  
  for(int i=0; i<K; ++i){
    inb[i] = random();
  }
  INIT(&fs);
  DES_KeyGen_filter(key_in, key_out);
  
  {
    printf("TESTING CORRECTNESS \n");
    unsigned * inputptr = masterin;
    unsigned * outputptr = output;
    int t=0;
    for(int i=0; i<3; ++i){    
      DES(inputptr, outputptr, key_out+0*3, key_out+1*3,key_out+2*3 , key_out+3*3, 
	    key_out+4*3, key_out+5*3, key_out+6*3, key_out+7*3, key_out+8*3,
	    key_out+9*3, key_out+10*3, key_out+11*3, key_out+12*3, 
	    key_out+13*3, key_out+14*3, key_out+15*3, &fs);
      for(int j=0; j<OUTPUT_RATE; ++j){
	if( t < nmasters){
	  if( *outputptr != masterout[t]){
	    printf("ERROR, your program was not correct\n Failed for test input %d. Input  %0x it produced output  %0x but it should have produced  %0x", t, *inputptr, *outputptr, masterout[t]);
	    return (1); 
	  }
	  ++outputptr;
	  ++inputptr;
	  ++t;
	}
      }
    }
  }

  printf("YOUR PROGRAM WAS CORRECT. \n Timing Static for %d 32-bit words.\n", NITERS_*INPUT_RATE);    
    
  {
    struct timeval stime, endtime; stime.tv_sec; stime.tv_usec;
    gettimeofday(&stime, NULL);
    for(int i=0; i<NITERS_; ++i){
      DES(inb, output, key_out+0*3, key_out+1*3,key_out+2*3 , key_out+3*3, 
	    key_out+4*3, key_out+5*3, key_out+6*3, key_out+7*3, key_out+8*3,
	    key_out+9*3, key_out+10*3, key_out+11*3, key_out+12*3, 
	    key_out+13*3, key_out+14*3, key_out+15*3, &fs);
    }
    gettimeofday(&endtime, NULL);


    unsigned long long tott = 1000000*(endtime.tv_sec - stime.tv_sec)+
      (endtime.tv_usec - stime.tv_usec);
    float speed = (((float)NITERS_)*INPUT_RATE)/((float)tott);
  
    printf("TIME %d %d   %d   \n", endtime.tv_sec - stime.tv_sec,
	   endtime.tv_usec - stime.tv_usec, tott);
  
    printf("STATIC RATE = %f words per microsecond\n", speed);
  }

  {
    struct timeval stime, endtime; stime.tv_sec; stime.tv_usec;
    gettimeofday(&stime, NULL);
    for(int i=0; i<NITERS_; ++i){
      unsigned * tmp_in = inb;
      unsigned * tmp_out = outb;
      for(int j=0; j<K; j+= OUTPUT_RATE){
	DES(tmp_in, tmp_out, key_out+0*3, key_out+1*3,key_out+2*3 , key_out+3*3, 
	    key_out+4*3, key_out+5*3, key_out+6*3, key_out+7*3, key_out+8*3,
	    key_out+9*3, key_out+10*3, key_out+11*3, key_out+12*3, 
	    key_out+13*3, key_out+14*3, key_out+15*3, &fs);
	tmp_in += INPUT_RATE;
	tmp_out += OUTPUT_RATE;
      }
    }
    gettimeofday(&endtime, NULL);

    unsigned long long tott = 1000000*(endtime.tv_sec - stime.tv_sec)+
      (endtime.tv_usec - stime.tv_usec);
    float speed = (((float)NITERS_)*K)/((float)tott);
  
    printf("TIME %d %d   %d   \n", endtime.tv_sec - stime.tv_sec,
	   endtime.tv_usec - stime.tv_usec, tott);
  
    printf("MEMORY RATE = %f words per microsecond\n", speed);
  }



  return 0;
}

