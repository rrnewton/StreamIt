all: paper
	xdvi paper.dvi

paper:
	latex paper.tex; bibtex paper; latex paper.tex; latex paper.tex

paper.ps: paper
	dvips paper.dvi -o paper.ps

