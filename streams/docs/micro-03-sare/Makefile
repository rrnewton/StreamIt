paper.ps: clean paper.dvi
	dvips paper.dvi -o paper.ps
	gv paper.ps

paper.pdf: clean paper.dvi
	dvipdf paper.dvi paper.pdf

paper.dvi:
	latex paper
	bibtex paper
	latex paper
	latex paper

clean:
	$(RM) *.aux *.bbl *.blg *.dvi *.log
