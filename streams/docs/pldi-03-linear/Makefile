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

main.dvi: main.tex references.bib *.tex images/*.eps
	latex main
	bibtex main
	latex main
	latex main

clean:
	rm -f main.pdf main.ps main.dvi main.log main.aux main.bbl main.blg
	rm -rf sub
