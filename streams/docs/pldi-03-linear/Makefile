all: main.ps

sub: clean main.ps
	rm -rf sub
	mkdir sub
	cp *.tex sub
	mv sub/main.tex sub/p94-lamb.tex
	dvips -o sub/p94-lamb.ps main.dvi
	ps2pdf main.ps
	mv main.pdf sub/p94-lamb.pdf

main.ps: main.dvi
	dvips -Ppdf -o main.ps main.dvi

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

main.dvi: main.tex references.bib *.tex images/*.eps images/part-algorithm.eps images/part-algorithm2.eps images/part-algorithm3.eps
	latex main
	bibtex main
	latex main
	latex main

clean:
	rm -f main.pdf main.ps main.dvi main.log main.aux main.bbl main.blg
	rm -rf sub
