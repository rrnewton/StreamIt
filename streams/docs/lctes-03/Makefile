all: main.dvi
	dvips -P cmz main.dvi -o main.ps

pseudocode4.eps: pseudocode4.tex
	enscript -Ejava -s 0.8 -f Courier2 -B -o pseudocode4.ps pseudocode4.tex || echo
	ps2epsi pseudocode4.ps
	mv pseudocode4.epsi pseudocode4.eps

main.dvi: *.tex pseudocode4.eps *.eps
	latex main.tex
	latex main.tex
	bibtex main
	latex main.tex