all: mgordon.ps
mgordon.dvi:	mgordon.tex *.tex *.bib
		rm -f mgordon.toc
		latex mgordon
		run-bibtex
		latex mgordon
		latex mgordon

mgordon.ps:	mgordon.dvi
		dvips -o mgordon.ps mgordon
