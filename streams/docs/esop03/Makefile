paper.ps: clean paper.dvi
	dvips paper.dvi -o paper.ps
	gv paper.ps

paper.pdf: clean paper.dvi
	dvipdf paper.dvi paper.pdf

paper.dvi: $(FILES)
	dot -Tps equalizer.dot -o equalizer.ps
	ps2epsi equalizer.ps
	mv equalizer.epsi equalizer.eps
	latex paper
	bibtex paper
	latex paper
	latex paper

clean:
	$(RM) *.aux *.bbl *.blg *.dvi *.log
