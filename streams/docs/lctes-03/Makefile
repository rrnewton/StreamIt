all: main.dvi
	dvips -P cmz main.dvi -o main.ps

pseudocode2.eps: pseudocode2.tex
	enscript -Ejava -s 0.8 -f Courier2 -B -o pseudocode2.ps pseudocode2.tex || echo
	ps2epsi pseudocode2.ps
	mv pseudocode2.epsi pseudocode2.eps

main.dvi: *.tex pseudocode2.eps *.eps
	latex main.tex
	latex main.tex
	bibtex main
	latex main.tex