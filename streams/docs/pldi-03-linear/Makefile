all: main.ps


main.ps: main.dvi
	dvips -Ppdf -o main.ps main.dvi

main.dvi: main.tex references.bib *.tex images/*.eps
	latex main
	bibtex main
	latex main
	latex main

clean:
	rm -f main.pdf main.ps main.dvi main.log main.aux main.bbl main.blg
