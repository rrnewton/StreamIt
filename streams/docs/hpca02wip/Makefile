all: paper
	xdvi paper.dvi

paper: diagram
	latex paper.tex; bibtex paper; latex paper.tex; latex paper.tex

diagram:
	dot -Tps radio.dot -o radio.ps

paper.ps: paper
	dvips paper.dvi -o paper.ps

