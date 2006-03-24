all: main.pdf

main.pdf: main.pdf.ps
	ps2pdf main.pdf.ps main.pdf

main.ps: clean main.dvi
	dvips main.dvi -o main.ps

main.pdf.ps: clean main.dvi
	dvips main.dvi -t letter -P cmz -o main.pdf.ps

main.dvi: $(FILES)
	latex main
	bibtex main
	latex main
	latex main

clean:
	$(RM) *.aux *.bbl *.blg *.dvi *.log main.pdf main.pdf.ps
