S	  = java -Xmx1700M at.dms.kjc.Main -s -N 10 -T
SMAG      = $(S) -M 
GREEDY	  = $(S) -a -raw
DYNPRG    = $(S) -y -raw
GREEDYMAG = $(SMAG) -a -raw
DYNPRGMAG = $(SMAG) -y -raw
GATHER	  = perl -pi -e 's/LIMIT\ \=\ TRUE//g' Makefile.streamit && make -f Makefile.streamit run && (cat results.out >> results.all || echo) && rm -f results.out

all: paper

scaling:
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 2 -Y BeamFormer.java

numbers: bitonic beamformer fir fft fm filterbank mergesort

beamformer:
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && (rm results.all || echo) && make
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 2 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 2 SerializedBeamFormer.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 3 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 3 SerializedBeamFormer.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 4 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 4 SerializedBeamFormer.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 5 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 5 SerializedBeamFormer.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 6 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 6 SerializedBeamFormer.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 7 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 7 SerializedBeamFormer.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 8 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 8 SerializedBeamFormer.java && $(GATHER) || echo

# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 2 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 2 SerializedBeamFormer.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 3 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 3 SerializedBeamFormer.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 4 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 4 SerializedBeamFormer.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 5 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 5 SerializedBeamFormer.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 6 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 6 SerializedBeamFormer.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 7 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 7 SerializedBeamFormer.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 8 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 8 SerializedBeamFormer.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== magic dynprg 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRGMAG) 2 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== magic dynprg 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRGMAG) 3 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== magic dynprg 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRGMAG) 4 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== magic dynprg 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRGMAG) 5 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== magic dynprg 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRGMAG) 6 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== magic dynprg 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRGMAG) 7 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== magic dynprg 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRGMAG) 8 SerializedBeamFormer.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== magic greedy 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDYMAG) 2 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== magic greedy 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDYMAG) 3 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== magic greedy 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDYMAG) 4 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== magic greedy 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDYMAG) 5 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== magic greedy 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDYMAG) 6 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== magic greedy 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDYMAG) 7 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== magic greedy 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDYMAG) 8 SerializedBeamFormer.java && $(GATHER) || echo

bitonic:
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && rm results.all || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 2 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 2 BitonicSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 3 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 3 BitonicSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 4 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 4 BitonicSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 5 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 5 BitonicSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 6 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 6 BitonicSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 7 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 7 BitonicSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 8 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 8 BitonicSort.java && $(GATHER) || echo

# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 2 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 2 BitonicSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 3 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 3 BitonicSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 4 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 4 BitonicSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 5 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 5 BitonicSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 6 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 6 BitonicSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 7 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 7 BitonicSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 8 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 8 BitonicSort.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== magic dynprg 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRGMAG) 2 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== magic dynprg 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRGMAG) 3 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== magic dynprg 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRGMAG) 4 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== magic dynprg 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRGMAG) 5 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== magic dynprg 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRGMAG) 6 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== magic dynprg 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRGMAG) 7 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== magic dynprg 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRGMAG) 8 BitonicSort.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== magic greedy 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDYMAG) 2 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== magic greedy 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDYMAG) 3 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== magic greedy 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDYMAG) 4 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== magic greedy 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDYMAG) 5 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== magic greedy 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDYMAG) 6 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== magic greedy 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDYMAG) 7 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== magic greedy 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDYMAG) 8 BitonicSort.java && $(GATHER) || echo

fir:
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && rm results.all || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 2 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 2 FIRfine.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 3 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 3 FIRfine.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 4 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 4 FIRfine.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 5 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 5 FIRfine.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 6 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 6 FIRfine.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 7 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 7 FIRfine.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 8 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 8 FIRfine.java && $(GATHER) || echo

# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 2 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 2 FIRfine.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 3 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 3 FIRfine.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 4 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 4 FIRfine.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 5 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 5 FIRfine.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 6 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 6 FIRfine.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 7 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 7 FIRfine.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 8 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 8 FIRfine.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== magic dynprg 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRGMAG) 2 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== magic dynprg 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRGMAG) 3 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== magic dynprg 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRGMAG) 4 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== magic dynprg 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRGMAG) 5 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== magic dynprg 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRGMAG) 6 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== magic dynprg 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRGMAG) 7 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== magic dynprg 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRGMAG) 8 FIRfine.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== magic greedy 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDYMAG) 2 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== magic greedy 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDYMAG) 3 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== magic greedy 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDYMAG) 4 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== magic greedy 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDYMAG) 5 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== magic greedy 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDYMAG) 6 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== magic greedy 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDYMAG) 7 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== magic greedy 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDYMAG) 8 FIRfine.java && $(GATHER) || echo

fm:
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && (rm results.all || echo) && make
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 2 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 2 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 3 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 3 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 4 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 4 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 5 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 5 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 6 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 6 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 7 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 7 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 8 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 8 LinkedFMTest.java && $(GATHER) || echo

# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 2 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 2 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 3 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 3 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 4 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 4 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 5 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 5 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 6 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 6 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 7 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 7 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 8 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 8 LinkedFMTest.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== magic dynprg 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRGMAG) 2 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== magic dynprg 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRGMAG) 3 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== magic dynprg 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRGMAG) 4 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== magic dynprg 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRGMAG) 5 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== magic dynprg 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRGMAG) 6 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== magic dynprg 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRGMAG) 7 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== magic dynprg 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRGMAG) 8 LinkedFMTest.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== magic greedy 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDYMAG) 2 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== magic greedy 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDYMAG) 3 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== magic greedy 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDYMAG) 4 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== magic greedy 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDYMAG) 5 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== magic greedy 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDYMAG) 6 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== magic greedy 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDYMAG) 7 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== magic greedy 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDYMAG) 8 LinkedFMTest.java && $(GATHER) || echo

fft:
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && (rm results.all || echo) && make
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== greedy 2 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(GREEDY) 2 LinkedFFT2.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== greedy 3 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(GREEDY) 3 LinkedFFT2.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== greedy 4 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(GREEDY) 4 LinkedFFT2.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== greedy 5 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(GREEDY) 5 LinkedFFT2.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== greedy 6 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(GREEDY) 6 LinkedFFT2.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== greedy 7 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(GREEDY) 7 LinkedFFT2.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== greedy 8 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(GREEDY) 8 LinkedFFT2.java && $(GATHER) || echo

# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== dynprg 2 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(DYNPRG) 2 LinkedFFT2.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== dynprg 3 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(DYNPRG) 3 LinkedFFT2.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== dynprg 4 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(DYNPRG) 4 LinkedFFT2.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== dynprg 5 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(DYNPRG) 5 LinkedFFT2.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== dynprg 6 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(DYNPRG) 6 LinkedFFT2.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== dynprg 7 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(DYNPRG) 7 LinkedFFT2.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== dynprg 8 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(DYNPRG) 8 LinkedFFT2.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== magic dynprg 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(DYNPRGMAG) 2 LinkedFFT2.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== magic dynprg 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(DYNPRGMAG) 3 LinkedFFT2.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== magic dynprg 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(DYNPRGMAG) 4 LinkedFFT2.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== magic dynprg 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(DYNPRGMAG) 5 LinkedFFT2.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== magic dynprg 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(DYNPRGMAG) 6 LinkedFFT2.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== magic dynprg 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(DYNPRGMAG) 7 LinkedFFT2.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== magic dynprg 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(DYNPRGMAG) 8 LinkedFFT2.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== magic greedy 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(GREEDYMAG) 2 LinkedFFT2.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== magic greedy 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(GREEDYMAG) 3 LinkedFFT2.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== magic greedy 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(GREEDYMAG) 4 LinkedFFT2.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== magic greedy 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(GREEDYMAG) 5 LinkedFFT2.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== magic greedy 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(GREEDYMAG) 6 LinkedFFT2.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== magic greedy 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(GREEDYMAG) 7 LinkedFFT2.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && echo ========== magic greedy 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fft/streamit && $(GREEDYMAG) 8 LinkedFFT2.java && $(GATHER) || echo

filterbank:
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && (rm results.all || echo) && make
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== greedy 2 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(GREEDY) 2 LinkedFBtest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== greedy 3 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(GREEDY) 3 LinkedFBtest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== greedy 4 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(GREEDY) 4 LinkedFBtest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== greedy 5 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(GREEDY) 5 LinkedFBtest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== greedy 6 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(GREEDY) 6 LinkedFBtest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== greedy 7 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(GREEDY) 7 LinkedFBtest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== greedy 8 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(GREEDY) 8 LinkedFBtest.java && $(GATHER) || echo

# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== dynprg 2 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(DYNPRG) 2 LinkedFBtest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== dynprg 3 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(DYNPRG) 3 LinkedFBtest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== dynprg 4 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(DYNPRG) 4 LinkedFBtest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== dynprg 5 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(DYNPRG) 5 LinkedFBtest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== dynprg 6 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(DYNPRG) 6 LinkedFBtest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== dynprg 7 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(DYNPRG) 7 LinkedFBtest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== dynprg 8 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(DYNPRG) 8 LinkedFBtest.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== magic dynprg 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(DYNPRGMAG) 2 LinkedFBtest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== magic dynprg 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(DYNPRGMAG) 3 LinkedFBtest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== magic dynprg 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(DYNPRGMAG) 4 LinkedFBtest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== magic dynprg 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(DYNPRGMAG) 5 LinkedFBtest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== magic dynprg 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(DYNPRGMAG) 6 LinkedFBtest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== magic dynprg 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(DYNPRGMAG) 7 LinkedFBtest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== magic dynprg 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(DYNPRGMAG) 8 LinkedFBtest.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== magic greedy 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(GREEDYMAG) 2 LinkedFBtest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== magic greedy 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(GREEDYMAG) 3 LinkedFBtest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== magic greedy 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(GREEDYMAG) 4 LinkedFBtest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== magic greedy 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(GREEDYMAG) 5 LinkedFBtest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== magic greedy 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(GREEDYMAG) 6 LinkedFBtest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== magic greedy 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(GREEDYMAG) 7 LinkedFBtest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && echo ========== magic greedy 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/filterbank/streamit && $(GREEDYMAG) 8 LinkedFBtest.java && $(GATHER) || echo

mergesort:
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && (rm results.all || echo)
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== greedy 2 >> results.all
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(GREEDY) 2 MergeSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== greedy 3 >> results.all
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(GREEDY) 3 MergeSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== greedy 4 >> results.all
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(GREEDY) 4 MergeSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== greedy 5 >> results.all
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(GREEDY) 5 MergeSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== greedy 6 >> results.all
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(GREEDY) 6 MergeSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== greedy 7 >> results.all
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(GREEDY) 7 MergeSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== greedy 8 >> results.all
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(GREEDY) 8 MergeSort.java && $(GATHER) || echo

# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== dynprg 2 >> results.all
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(DYNPRG) 2 MergeSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== dynprg 3 >> results.all
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(DYNPRG) 3 MergeSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== dynprg 4 >> results.all
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(DYNPRG) 4 MergeSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== dynprg 5 >> results.all
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(DYNPRG) 5 MergeSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== dynprg 6 >> results.all
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(DYNPRG) 6 MergeSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== dynprg 7 >> results.all
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(DYNPRG) 7 MergeSort.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== dynprg 8 >> results.all
# 	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(DYNPRG) 8 MergeSort.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== magic dynprg 2 >> results.all
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(DYNPRGMAG) 2 MergeSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== magic dynprg 3 >> results.all
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(DYNPRGMAG) 3 MergeSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== magic dynprg 4 >> results.all
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(DYNPRGMAG) 4 MergeSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== magic dynprg 5 >> results.all
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(DYNPRGMAG) 5 MergeSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== magic dynprg 6 >> results.all
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(DYNPRGMAG) 6 MergeSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== magic dynprg 7 >> results.all
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(DYNPRGMAG) 7 MergeSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== magic dynprg 8 >> results.all
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(DYNPRGMAG) 8 MergeSort.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== magic greedy 2 >> results.all
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(GREEDYMAG) 2 MergeSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== magic greedy 3 >> results.all
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(GREEDYMAG) 3 MergeSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== magic greedy 4 >> results.all
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(GREEDYMAG) 4 MergeSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== magic greedy 5 >> results.all
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(GREEDYMAG) 5 MergeSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== magic greedy 6 >> results.all
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(GREEDYMAG) 6 MergeSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== magic greedy 7 >> results.all
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(GREEDYMAG) 7 MergeSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && echo ========== magic greedy 8 >> results.all
	cd $(STREAMIT_HOME)/docs/spaa-03-load/apps/mergesort && $(GREEDYMAG) 8 MergeSort.java && $(GATHER) || echo

paper:
# 	enscript -Ejava -s 0.8 -f Courier2 -B -o algorithm.ps algorithm.tex || echo
# 	ps2epsi algorithm.ps
# 	mv algorithm.epsi algorithm.eps
# 	enscript -Ejava -s 0.8 -f Courier2 -B -o algorithm2.ps algorithm2.tex  || echo
# 	ps2epsi algorithm2.ps
# 	mv algorithm2.epsi algorithm2.eps
# 	enscript -Ejava -s 0.8 -f Courier2 -B -o algorithm3.ps algorithm3.tex || echo
# 	ps2epsi algorithm3.ps
# 	mv algorithm3.epsi algorithm3.eps
# 	enscript -Ejava -s 0.8 -f Courier2 -B -o algorithm4.ps algorithm4.tex || echo
# 	ps2epsi algorithm4.ps
# 	mv algorithm4.epsi algorithm4.eps
# 	enscript -Ejava -s 0.8 -f Courier2 -B -o algorithm5.ps algorithm5.tex || echo
# 	ps2epsi algorithm5.ps
# 	mv algorithm5.epsi algorithm5.eps
# 	enscript -Ejava -s 0.8 -f Courier2 -B -o algorithm6.ps algorithm6.tex || echo
# 	ps2epsi algorithm6.ps
# 	mv algorithm6.epsi algorithm6.eps
# 	dot -Tps refactor1.dot -o refactor1.ps
# 	ps2epsi refactor1.ps
# 	mv refactor1.epsi refactor1.eps
# 	dot -Tps refactor2.dot -o refactor2.ps
# 	ps2epsi refactor2.ps
# 	mv refactor2.epsi refactor2.eps
# 	dot -Tps sync1.dot -o sync1.ps
# 	ps2epsi sync1.ps
# 	mv sync1.epsi sync1.eps
# 	dot -Tps sync2.dot -o sync2.ps
# 	ps2epsi sync2.ps
# 	mv sync2.epsi sync2.eps
# 	dot -Tps matchsync1.dot -o matchsync1.ps
# 	ps2epsi matchsync1.ps
# 	mv matchsync1.epsi matchsync1.eps
# 	dot -Tps matchsync2.dot -o matchsync2.ps
# 	ps2epsi matchsync2.ps
# 	mv matchsync2.epsi matchsync2.eps
	latex paper
	bibtex paper
	latex paper
	latex paper
	dvips -P cmz paper.dvi -o paper.ps
	ps2pdf paper.ps
	mv paper.pdf thies.pdf

clean:
	rm -f paper.pdf paper.ps paper.dvi paper.log paper.aux paper.bbl paper.blg
