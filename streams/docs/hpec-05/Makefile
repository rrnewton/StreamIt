main.pdf: main.tex Makefile tr.cls
	pdflatex main.tex
	bibtex main
	pdflatex main.tex
	pdflatex main.tex

view: main.pdf
	acroread main.pdf >& /dev/null

again:
	pdflatex main.tex

clean:
	(rm -f *.dvi *.aux *.bbl *.blg *.ps *.log *.toc *~)

spotless: clean
	(rm -f main.pdf *.dvi *.aux *.bbl *.blg *.ps *.pdf *.log *.toc *~)
