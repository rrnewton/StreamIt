TEXFILES=*.tex

main.pdf: $(TEXFILES) references.bib Makefile
	pdflatex main.tex
	bibtex main
	pdflatex main.tex
	pdflatex main.tex &> tex.output

clean:
	(rm -f *.dvi *.aux *.bbl *.blg *.ps *.log *.toc *~)

spotless: clean
	(rm -f main.pdf *.dvi *.aux *.bbl *.blg *.log *.toc *~)
