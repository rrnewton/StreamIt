all: streamit-lang.pdf

streamit-lang.pdf: streamit-lang.dvi
	dvips -Ppdf streamit-lang.dvi -o streamit-lang.ps
	ps2pdf streamit-lang.ps

streamit-lang.dvi: streamit-lang.tex
	latex streamit-lang.tex	
	bibtex streamit-lang || echo
	latex streamit-lang.tex	
	latex streamit-lang.tex	
