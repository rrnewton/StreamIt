all:
	dot -Tps ex1.dot -o ex1.ps
	ps2epsi ex1.ps
	mv ex1.epsi ex1.eps
	latex paper.tex
	dvips paper.dvi -o paper.ps
