paper.ps: clean paper.dvi
	dvips paper.dvi -o paper.ps

paper.pdf: clean paper.dvi
	dvipdf paper.dvi paper.pdf

paper.dvi: $(FILES)
	latex paper
	bibtex paper
	latex paper
	latex paper

clean:
	$(RM) *.aux *.bbl *.blg *.dvi *.log
