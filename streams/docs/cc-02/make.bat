del paper.aux
latex paper.tex
bibtex paper
latex paper.tex
latex paper.tex
dvips paper.dvi -o paper.ps
start paper.ps
