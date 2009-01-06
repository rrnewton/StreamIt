all:
	pdflatex main.tex
	pdflatex main.tex
	bibtex main
	pdflatex main.tex
#	dvips -P cmz main.dvi -o main.ps
#	ps2pdf main.ps

clean:
	rm -f *~ *.aux *.log *.bbl *.blg *.toc *.haux *.htoc *.png main.pdf main.ps

