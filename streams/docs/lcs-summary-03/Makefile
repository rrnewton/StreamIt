all: main.ps
main.dvi:	main.tex *.tex *.bib
		rm -f main.toc
		latex main
		run-bibtex
		latex main
		latex main

main.ps:	main.dvi
		dvips -o main.ps main

clean:
	rm -f *.aux *.bbl *.blg *.toc *.dvi *.log
	rm -rf main.ps