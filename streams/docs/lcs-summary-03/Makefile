main.dvi:	main.tex *.tex *.bib
		rm -f main.toc
		latex main
		run-bibtex
		latex main
		latex main

main.ps:	main.dvi
		dvips -o main.ps main
