TEXFILES = thies1.tex

all:	${TEXFILES:.tex=.pdf}

%.pdf: %.tex *.bib
	pdflatex $*
	bibtex $*
	pdflatex $*
	pdflatex $*
