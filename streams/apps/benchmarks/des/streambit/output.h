#ifndef OUTPUT_H
#define OUTPUT_H

typedef struct{
}  FilterState;


void INIT(FilterState * local_state);


/*   DES filter: Input Rate = 64
      Output Rate = 64     */
extern int OUTPUT_RATE; 
extern int INPUT_RATE; 


void DES(unsigned *in_Input_0_0  , unsigned *out_Output_0_1  , unsigned *in_PARAM_P0_0_51  , unsigned *in_PARAM_P2_0_86  , unsigned *in_PARAM_P4_0_113  , unsigned *in_PARAM_P6_0_140  , unsigned *in_PARAM_P8_0_167  , unsigned *in_PARAM_P10_0_194  , unsigned *in_PARAM_P12_0_221  , unsigned *in_PARAM_P14_0_248  , unsigned *in_PARAM_P16_0_275  , unsigned *in_PARAM_P18_0_302  , unsigned *in_PARAM_P20_0_329  , unsigned *in_PARAM_P22_0_356  , unsigned *in_PARAM_P24_0_383  , unsigned *in_PARAM_P26_0_410  , unsigned *in_PARAM_P28_0_437  , unsigned *in_PARAM_P30_0_464  , FilterState * local_state);


#endif 
