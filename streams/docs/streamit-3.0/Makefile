MAIN    = streamit-3.0

${MAIN}.pdf: ${MAIN}.tex Makefile tr.cls
	pdflatex ${MAIN}.tex
	pdflatex ${MAIN}.tex

again:
	pdflatex ${MAIN}.tex

view: ${MAIN}.pdf
	acroread ${MAIN}.pdf

clean:
	@rm -f *.dvi *.aux *.bbl *.blg *.ps *.log *.toc *~

spotless: clean
	@rm -f *.dvi *.aux *.bbl *.blg *.ps *.pdf *.log *.toc *~

