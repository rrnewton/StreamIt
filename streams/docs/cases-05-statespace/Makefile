all:
	latex main.tex
	latex main.tex
	bibtex main
	latex main.tex
	dvips -P cmz main.dvi -o main.ps
	cp main.ps /u/thies/public_html
