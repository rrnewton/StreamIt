all: paper
	xdvi paper.dvi

paper:
	latex paper.tex; bibtex paper; latex paper.tex; latex paper.tex; dvips paper.dvi -o /u/thies/public_html/streamit.ps

paper.ps: paper
	dvips paper.dvi -o paper.ps

