all: main.pdf abstract-standalone.pdf
	cp main.pdf ~/public_html/ipdps.pdf
#	cp abstract-standalone.pdf ~/public_html/ipdps-ab.pdf

abstract-standalone.pdf:
	pdflatex abstract-standalone.tex
	pdflatex abstract-standalone.tex

main.pdf: main.pdf.ps
	ps2pdf main.pdf.ps main.pdf

main.ps: clean main.dvi
	dvips main.dvi -o main.ps

main.pdf.ps: clean main.dvi
	dvips main.dvi -P cmz -o main.pdf.ps

main.dvi: $(FILES)
	latex main
	bibtex main
	latex main
	latex main

clean:
	$(RM) *.aux *.bbl *.blg *.dvi *.log main.pdf main.pdf.ps abstract-standalone.pdf
