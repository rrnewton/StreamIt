S	= java -Xmx1700M at.dms.kjc.Main -s -M -N 3
SJ	= $(S) -J
GREEDY	= $(S) -a -raw
DYNPRG  = $(S) -y -raw
GREEDYJ	= $(SJ) -a -raw
DYNPRGJ = $(SJ) -y -raw
GATHER	= make -f Makefile.streamit run && (cat results.out >> results.all || echo) && rm -f results.out
# no latency on hops
GATHERNL = perl -pi -e 's/C1H2/C1H0/g' Makefile.streamit && make -f Makefile.streamit run && (cat results.out >> results.all || echo) && rm -f results.out

all: paper

scaling:
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 2 -Y BeamFormer.java

numbers: bitonic fm beamformer fir

beamformer:
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && (rm results.all || echo) && make
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 2 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 3 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 4 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 5 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 6 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 7 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 8 SerializedBeamFormer.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 2 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 3 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 4 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 5 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 6 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 7 SerializedBeamFormer.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 8 SerializedBeamFormer.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 2 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 2 SerializedBeamFormer.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 3 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 3 SerializedBeamFormer.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 4 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 4 SerializedBeamFormer.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 5 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 5 SerializedBeamFormer.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 6 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 6 SerializedBeamFormer.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 7 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 7 SerializedBeamFormer.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== greedy 8 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 8 SerializedBeamFormer.java && $(GATHERNL) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 2 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 2 SerializedBeamFormer.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 3 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 3 SerializedBeamFormer.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 4 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 4 SerializedBeamFormer.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 5 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 5 SerializedBeamFormer.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 6 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 6 SerializedBeamFormer.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 7 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 7 SerializedBeamFormer.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && echo ========== dynprg 8 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(DYNPRG) 8 SerializedBeamFormer.java && $(GATHERNL) || echo

bitonic:
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && rm results.all || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 2 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 3 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 4 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 5 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 6 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 7 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 8 BitonicSort.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 2 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 3 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 4 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 5 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 6 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 7 BitonicSort.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 8 BitonicSort.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 2  \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 2 BitonicSort.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 3  \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 3 BitonicSort.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 4  \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 4 BitonicSort.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 5  \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 5 BitonicSort.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 6  \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 6 BitonicSort.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 7  \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 7 BitonicSort.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== greedy 8  \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(GREEDY) 8 BitonicSort.java && $(GATHERNL) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 2  \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 2 BitonicSort.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 3  \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 3 BitonicSort.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 4  \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 4 BitonicSort.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 5  \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 5 BitonicSort.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 6  \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 6 BitonicSort.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 7  \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 7 BitonicSort.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && echo ========== dynprg 8  \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && $(DYNPRG) 8 BitonicSort.java && $(GATHERNL) || echo

fir:
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && rm results.all || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 2 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 3 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 4 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 5 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 6 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 7 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 8 FIRfine.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 2 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 3 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 4 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 5 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 6 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 7 FIRfine.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 8 FIRfine.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 2 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 2 FIRfine.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 3 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 3 FIRfine.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 4 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 4 FIRfine.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 5 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 5 FIRfine.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 6 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 6 FIRfine.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 7 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 7 FIRfine.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== greedy 8 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(GREEDY) 8 FIRfine.java && $(GATHERNL) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 2 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 2 FIRfine.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 3 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 3 FIRfine.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 4 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 4 FIRfine.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 5 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 5 FIRfine.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 6 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 6 FIRfine.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 7 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 7 FIRfine.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && echo ========== dynprg 8 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && $(DYNPRG) 8 FIRfine.java && $(GATHERNL) || echo

fm:
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && (rm results.all || echo) && make
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 2 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 3 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 4 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 5 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 6 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 7 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 8 LinkedFMTest.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 2 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 2 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 3 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 3 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 4 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 4 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 5 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 5 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 6 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 6 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 7 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 7 LinkedFMTest.java && $(GATHER) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 8 >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 8 LinkedFMTest.java && $(GATHER) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 2 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 2 LinkedFMTest.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 3 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 3 LinkedFMTest.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 4 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 4 LinkedFMTest.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 5 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 5 LinkedFMTest.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 6 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 6 LinkedFMTest.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 7 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 7 LinkedFMTest.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy 8 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDY) 8 LinkedFMTest.java && $(GATHERNL) || echo

	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 2 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 2 LinkedFMTest.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 3 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 3 LinkedFMTest.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 4 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 4 LinkedFMTest.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 5 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 5 LinkedFMTest.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 6 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 6 LinkedFMTest.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 7 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 7 LinkedFMTest.java && $(GATHERNL) || echo
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg 8 \(no latency\) >> results.all
	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRG) 8 LinkedFMTest.java && $(GATHERNL) || echo

# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy -J 2 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDYJ) 2 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy -J 3 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDYJ) 3 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy -J 4 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDYJ) 4 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy -J 5 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDYJ) 5 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy -J 6 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDYJ) 6 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy -J 7 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDYJ) 7 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== greedy -J 8 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(GREEDYJ) 8 LinkedFMTest.java && $(GATHER) || echo

# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg -J 2 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRGJ) 2 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg -J 3 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRGJ) 3 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg -J 4 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRGJ) 4 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg -J 5 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRGJ) 5 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg -J 6 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRGJ) 6 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg -J 7 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRGJ) 7 LinkedFMTest.java && $(GATHER) || echo
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && echo ========== dynprg -J 8 >> results.all
# 	cd $(STREAMIT_HOME)/apps/benchmarks/fm/streamit && $(DYNPRGJ) 8 LinkedFMTest.java && $(GATHER) || echo

paper:
	latex paper
	bibtex paper
	latex paper
	latex paper
	dvips paper.dvi -o paper.ps

clean:
	rm -f paper.pdf paper.ps paper.dvi paper.log paper.aux paper.bbl paper.blg
