all: main.pdf

main.ps: main.dvi 
	dvips -P cmz -t letter -o main.ps main.dvi
	cp main.ps ${HOME}/public_html/lctes.ps

main.pdf: main.ps
	ps2pdf main.ps
	cp main.pdf ${HOME}/public_html/lctes.pdf

main.dvi: main.tex references.bib *.tex #*.eps
	latex main
	bibtex main
	latex main
	latex main

clean:
	rm -f main.pdf main.ps main.dvi main.log main.aux main.bbl main.blg
