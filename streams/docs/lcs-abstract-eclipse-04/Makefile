MAIN    = kuo

${MAIN}.pdf: ${MAIN}.tex Makefile
	pdflatex ${MAIN}.tex
	bibtex   ${MAIN}
	pdflatex ${MAIN}.tex
	pdflatex ${MAIN}.tex

again:
	pdflatex ${MAIN}.tex

view: ${MAIN}.pdf
	acroread ${MAIN}.pdf 

clean:
	rm -f *.dvi *.aux *.bbl *.blg *.ps *.log *~

spotless:
	rm -f *.dvi *.aux *.bbl *.blg *.ps ${MAIN}.pdf *.log *~
