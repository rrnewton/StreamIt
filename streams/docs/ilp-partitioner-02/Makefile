all: clean
	latex paper
	latex paper
	dvips paper.dvi -o paper.ps
	gv paper.ps
	dvips -Ppdf paper.dvi -o paper.ps
	ps2pdf paper.ps

clean:
	$(RM) *.aux *.bbl *.blg *.dvi *.log
