all: main.ps

main.ps: main.dvi 
	dvips -P cmz -t letter -o main.ps main.dvi

main.pdf: main.ps
	ps2pdf main.ps

main.dvi: main.tex references.bib *.tex *.eps *.cls
	latex main
	bibtex main
	latex main
	latex main

clean:
	rm -f main.pdf main.ps main.dvi main.log main.aux main.bbl main.blg
