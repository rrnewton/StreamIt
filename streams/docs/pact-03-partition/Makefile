S	= java -Xmx1700M at.dms.kjc.Main -s -N 3
GREEDY	= $(S) -a -raw
DYNPRG  = $(S) -y -raw
GATHER	= make -f Makefile.streamit run && (cat results.out >> results.all || echo) && rm -f results.out

all: paper

scaling:
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && $(GREEDY) 2 -Y BeamFormer.java

numbers: fir beamformer bitonic

beamformer:
	cd $(STREAMIT_HOME)/apps/benchmarks/beamformer/streamit && rm results.all && make
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

bitonic:
	cd $(STREAMIT_HOME)/apps/benchmarks/bitonic-sort/streamit && rm results.all
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

fir:
	cd $(STREAMIT_HOME)/apps/benchmarks/fir/streamit && rm results.all
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

paper:
	latex paper
	bibtex paper
	latex paper
	latex paper
	dvips paper.dvi -o paper.ps

clean:
	rm -f paper.pdf paper.ps paper.dvi paper.log paper.aux paper.bbl paper.blg
