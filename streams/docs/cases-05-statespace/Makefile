all:
	latex main.tex
	latex main.tex
	bibtex main
	latex main.tex
	dvips -P cmz main.dvi -o main.ps
	ps2pdf main.ps
	cp main.ps /u/thies/public_html
	cp main.pdf /u/thies/public_html
