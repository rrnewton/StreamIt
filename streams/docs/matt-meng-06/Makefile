all: madrake-thesis-06.pdf

madrake-thesis-06.pdf: main.pdf.ps
	ps2pdf main.pdf.ps madrake-thesis-06.pdf

madrake-thesis-06.ps: clean main.dvi
	dvips main.dvi -o madrake-thesis-06.ps

main.pdf.ps: clean main.dvi
	dvips main.dvi -P cmz -o main.pdf.ps

main.dvi: $(FILES)
	latex main
	bibtex main
	latex main
	latex main

clean:
	$(RM) *.aux *.bbl *.blg *.dvi *.log *.ps *.pdf *.toc *.lof


