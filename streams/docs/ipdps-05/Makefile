all: main.pdf
	cp main.pdf ~/public_html/ipdps.pdf

main.pdf: main.tex main.bib
	pdflatex main.tex
	bibtex main
	pdflatex main.tex
	pdflatex main.tex

main.ps: clean main.dvi
	dvips main.dvi -o main.ps

#main.pdf: main.pdf.ps
	ps2pdf main.pdf.ps main.pdf

main.pdf.ps: clean main.dvi
	dvips main.dvi -P cmz -o main.pdf.ps

main.dvi: $(FILES)
	latex main
	bibtex main
	latex main
	latex main

clean:
	$(RM) *.aux *.bbl *.blg *.dvi *.log main.pdf
