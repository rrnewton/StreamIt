all: ps

dvi: *.tex
	latex manual.tex
	latex manual.tex
	latex manual.tex
ps: dvi
	dvips manual.dvi -o manual.ps
pdf: ps
	ps2pdf manual.ps
view: ps
	gv manual.ps &

clean:
	rm -f *.aux
	rm -f *.dvi
	rm -f *.log
	rm -f *.ps
	rm -f *.pdf