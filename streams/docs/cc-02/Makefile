all:
	latex paper.tex; bibtex paper; latex paper.tex; latex paper.tex; dvips paper.dvi -o paper.ps; gv paper.ps

clean:
	rm -f *~ paper.aux paper.bbl paper.log paper.blg paper.ps paper.pdf paper.dvi

