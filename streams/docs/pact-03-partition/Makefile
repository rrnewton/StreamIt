all: 
	latex paper
	bibtex paper
	latex paper
	latex paper
	dvips paper.dvi -o paper.ps

clean:
	rm -f paper.pdf paper.ps paper.dvi paper.log paper.aux paper.bbl paper.blg
