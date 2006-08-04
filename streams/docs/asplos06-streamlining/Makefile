asplos172-gordon.ps: clean asplos172-gordon.dvi
	dvips -t letter -P cmz asplos172-gordon.dvi -o asplos172-gordon.ps

asplos172-gordon.pdf: clean asplos172-gordon.dvi asplos172-gordon.ps
	ps2pdf asplos172-gordon.ps asplos172-gordon.pdf

asplos172-gordon.dvi: $(FILES)
	latex asplos172-gordon
	bibtex asplos172-gordon
	latex asplos172-gordon
# space bib entries closer together
	perl -pi -e 's/\\bibitem/\\vspace{-1pt}\\bibitem/g' asplos172-gordon.bbl
# but do not cramp the References section title
	perl -pi -e 's/\\begin\{thebibliography\}\{10\}/\\begin\{thebibliography\}\{10\}\\vspace\{-2pt\}/g' asplos172-gordon.bbl
	latex asplos172-gordon

clean:
	$(RM) *.aux *.bbl *.blg *.dvi *.log