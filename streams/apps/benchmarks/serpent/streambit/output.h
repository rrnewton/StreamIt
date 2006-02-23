#ifndef OUTPUT_H
#define OUTPUT_H

typedef struct{
}  FilterState;


void INIT(FilterState * local_state);


/*   Serpent filter: Input Rate = 128
      Output Rate = 128     */
extern int OUTPUT_RATE; 
extern int INPUT_RATE; 


void Serpent(unsigned *in_Input_0_0  , unsigned *out_Output_0_1  , unsigned *in_PARAM_P0_0_163  , unsigned *in_PARAM_P2_0_329  , unsigned *in_PARAM_P4_0_495  , unsigned *in_PARAM_P6_0_661  , unsigned *in_PARAM_P8_0_827  , unsigned *in_PARAM_P10_0_993  , unsigned *in_PARAM_P12_0_1159  , unsigned *in_PARAM_P14_0_1325  , unsigned *in_PARAM_P16_0_1491  , unsigned *in_PARAM_P18_0_1657  , unsigned *in_PARAM_P20_0_1823  , unsigned *in_PARAM_P22_0_1989  , unsigned *in_PARAM_P24_0_2155  , unsigned *in_PARAM_P26_0_2321  , unsigned *in_PARAM_P28_0_2487  , unsigned *in_PARAM_P30_0_2653  , unsigned *in_PARAM_P32_0_2819  , unsigned *in_PARAM_P34_0_2985  , unsigned *in_PARAM_P36_0_3151  , unsigned *in_PARAM_P38_0_3317  , unsigned *in_PARAM_P40_0_3483  , unsigned *in_PARAM_P42_0_3649  , unsigned *in_PARAM_P44_0_3815  , unsigned *in_PARAM_P46_0_3981  , unsigned *in_PARAM_P48_0_4147  , unsigned *in_PARAM_P50_0_4313  , unsigned *in_PARAM_P52_0_4479  , unsigned *in_PARAM_P54_0_4645  , unsigned *in_PARAM_P56_0_4811  , unsigned *in_PARAM_P58_0_4977  , unsigned *in_PARAM_P60_0_5143  , unsigned *in_PARAM_P62_0_5309  , unsigned *in_PARAM_P64_0_5437  , FilterState * local_state);


#endif 
