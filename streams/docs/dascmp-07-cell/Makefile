.PHONY: all once view pdf clean cleanall

all: main.dvi
#	cp main.pdf ~/public_html/main.pdf

main.dvi: *.tex main.bib figs/*.eps *.cls *.sty
	echo all | latex main.tex
	bibtex main
	echo all | latex main.tex
	echo all | latex main.tex
	echo all | latex main.tex

main.pdf: main.dvi
	dvips -Pdownload35 main.dvi  -t letter -P cmz -o main.ps
	ps2pdf main.ps

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
