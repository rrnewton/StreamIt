paper.ps: clean paper.dvi
	dvips paper.dvi -o paper.ps
	gzip -f paper.ps
	cp paper.ps.gz ${HOME}/public_html/paper.ps

paper.pdf: clean paper.dvi
	dvipdf paper.dvi paper.pdf

paper.dvi: $(FILES)
	dot -Tps example.dot -o example.ps
	ps2epsi example.ps
	mv example.epsi example.eps
	latex paper
	bibtex paper
	latex paper
	latex paper

clean:
	$(RM) *.aux *.bbl *.blg *.dvi *.log
