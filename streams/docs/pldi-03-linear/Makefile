all: main.ps

main.ps: main.dvi
	dvips -P cmz -t letter -o main.ps main.dvi
#	cp main.ps ${HOME}/public_html/pldi.ps

main.pdf: main.ps
	ps2pdf main.ps

images/part-algorithm.eps: part-algorithm.tex
	enscript -Ejava -s 0.8 -f Courier2 -B -o images/part-algorithm.ps part-algorithm.tex || echo
	ps2epsi images/part-algorithm.ps
	mv part-algorithm.epsi images/part-algorithm.eps

images/part-algorithm2.eps: part-algorithm2.tex
	enscript -Ejava -s 0.8 -f Courier2 -B -o images/part-algorithm2.ps part-algorithm2.tex || echo
	ps2epsi images/part-algorithm2.ps
	mv part-algorithm2.epsi images/part-algorithm2.eps

images/part-algorithm3.eps: part-algorithm3.tex
	enscript -Ejava -s 0.8 -f Courier3 -B -o images/part-algorithm3.ps part-algorithm3.tex || echo
	ps2epsi images/part-algorithm3.ps
	mv part-algorithm3.epsi images/part-algorithm3.eps

stream-graphs: streamgraphs/*.dot
	dot -Tps streamgraphs/FIRProgram-linear-linear-simple.dot > streamgraphs/fir.ps
	dot -Tps streamgraphs/SamplingRateConverter-linear-linear-simple.dot > streamgraphs/sample.ps
	dot -Tps streamgraphs/TargetDetect-linear-linear-simple.dot > streamgraphs/target.ps
	dot -Tps streamgraphs/LinkedFMTest-linear-linear-simple.dot > streamgraphs/fm.ps
	dot -Tps streamgraphs/CoarseSerializedBeamFormer-2linear-linear-simple.dot > streamgraphs/bf.ps
	dot -Tps streamgraphs/FilterBank-linear-linear-simple.dot > streamgraphs/fb.ps
	dot -Tps streamgraphs/ChannelVocoder-linear-linear-simple.dot > streamgraphs/vocoder.ps
	dot -Tps streamgraphs/Oversampler-linear-linear-simple.dot > streamgraphs/oversampler.ps
	dot -Tps streamgraphs/OneBitDToA-linear-linear-simple.dot > streamgraphs/onebit.ps



main.dvi: main.tex references.bib *.tex images/*.eps images/part-algorithm.eps images/part-algorithm2.eps images/part-algorithm3.eps stream-graphs
	latex main
	bibtex main
	latex main
	latex main

clean:
	rm -f main.pdf main.ps main.dvi main.log main.aux main.bbl main.blg
	rm -rf sub streamgraphs/*.ps images/part-algorithm*.eps images/part-algorithm*.ps
