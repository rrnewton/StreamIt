all: paper paperpdf
	xdvi paper.dvi

paper:
	latex paper.tex; bibtex paper; latex paper.tex; latex paper.tex

paperpdf:
	pdflatex paper-pdf.tex; bibtex paper-pdf; pdflatex paper-pdf.tex; pdflatex paper-pdf.tex

paper.ps: paper
	dvips paper.dvi -o paper.ps

