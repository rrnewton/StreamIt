all: main.ps

sub: clean main.ps
	rm -rf sub
	mkdir sub
	dvips -o sub/main.ps main.dvi
	gzip sub/main.ps
	ps2pdf main.ps
	mv main.pdf sub


main.ps: main.dvi
	dvips -Ppdf -o main.ps main.dvi

main.dvi: main.tex references.bib *.tex images/*.eps
	latex main
	bibtex main
	latex main
	latex main

clean:
	rm -f main.pdf main.ps main.dvi main.log main.aux main.bbl main.blg
	rm -rf sub
