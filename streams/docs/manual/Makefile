all: clean ps pdf

dvi: 
	latex performance-metrics.tex
ps: dvi
	dvips performance-metrics.dvi -o performance-metrics.ps
pdf: 
	pdflatex performance-metrics.tex

clean:
	rm -f *.aux
	rm -f *.dvi
	rm -f *.log
	rm -f *.ps
	rm -f *.pdf