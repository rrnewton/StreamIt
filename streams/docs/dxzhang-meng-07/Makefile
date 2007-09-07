.PHONY: all once view pdf clean cleanall

main.dvi: *.tex main.bib figs/*.eps mitthesis.cls *.sty
	echo all | latex main.tex
	bibtex main
	echo all | latex main.tex
	echo all | latex main.tex
	echo all | latex main.tex

main.pdf: main.dvi
	dvipdf main.dvi

all: main.dvi

once:
	echo all | latex main.tex

view: main.dvi
	xdvi main.dvi &

pdf: main.pdf

clean:
	@rm -f main.dvi
	@rm -f main.pdf
	@rm -f *.aux
	@rm -f main.bbl
	@rm -f main.blg
	@rm -f main.lof
	@rm -f main.log
	@rm -f main.toc

cleanall: clean
	@rm -f *~
