TEXFILES=*.tex

supporting.pdf: $(TEXFILES) Makefile
	pdflatex supporting.tex

clean:
	(rm -f *.dvi *.aux *.bbl *.blg *.ps *.log *.toc *~)

spotless: clean
	(rm -f main.pdf *.dvi *.aux *.bbl *.blg *.log *.toc *~)
