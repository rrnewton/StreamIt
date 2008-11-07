.PHONY: all once view pdf clean cleanall

all: main.pdf

main.pdf: *.tex references.bib *.cls
	echo all | pdflatex main.tex
	bibtex main
	echo all | pdflatex main.tex
	echo all | pdflatex main.tex
	echo all | pdflatex main.tex

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
