main.ps: clean main.dvi
	dvips paper.dvi -o paper.ps

main.pdf: clean main.dvi
	dvipdf paper.dvi paper.pdf

main.dvi: $(FILES)
	latex paper
	bibtex paper
	latex paper
	latex paper

clean:
	$(RM) *.aux *.bbl *.blg *.dvi *.log
