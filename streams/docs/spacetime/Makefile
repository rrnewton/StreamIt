main.ps: clean main.dvi
	dvips main.dvi -o main.ps

main.pdf: clean main.dvi main.ps
	ps2pdf main.ps main.pdf

main.dvi: $(FILES)
	latex main
	bibtex main
	latex main
	latex main

clean:
	$(RM) *.aux *.bbl *.blg *.dvi *.log
