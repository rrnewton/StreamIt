all: mgordon.pdf
mgordon.pdf:	mgordon.tex *.tex *.bib
		rm -f mgordon.toc
		pdflatex mgordon
		run-bibtex
		pdflatex mgordon
		pdflatex mgordon

