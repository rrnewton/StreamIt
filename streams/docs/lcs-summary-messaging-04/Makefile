TEXFILES = thies1.tex

all:	${TEXFILES:.tex=.pdf}
# 	latex thies1.tex
# 	bibtex thies1
# 	latex thies1.tex
# 	latex thies1.tex
# 	dvips -P cmz thies1.dvi -o thies1.ps
# 	ps2pdf thies1.ps

%.pdf: %.tex *.bib
	pdflatex $*
	bibtex $*
	pdflatex $*
	pdflatex $*
