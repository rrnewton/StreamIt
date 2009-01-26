# Add a list of home-made EPS figures here.
EPSFIGURES=

.SUFFIXES: .fig .eps .ps .dvi .bbl .bib .tex .pdf

FIGURES=${wildcard *.fig}
EPSFIGFILES=${FIGURES:.fig=.eps}
TEXFILES=*.tex

all: dvi

preprocess:

force: rmdone all

dvi: main.dvi

xdvi: main.dvi
	xdvi main.dvi

gv: main.ps
	gv main.ps

ps: main.ps 

pdf: main.pdf

dif:
	latexdiff --flatten old/main.tex main.tex > diff.tex
	latex diff.tex
	latex diff.tex
	bibtex diff
	latex diff.tex
	dvips -z -t letter -P cmz diff.dvi -o diff.ps
	ps2pdf diff.ps

rmdone:
	rm -f done.flag

done.flag:
	touch done.flag

printerrors:
	@echo "*****************************"; \
	echo Overview of typeset warnings: ; \
        echo "****************************"; \
	grep "Underfull" main.log; \
	grep "Overfull" main.log; \
	echo ; \
        echo "***************************"; \
	echo Overview of latex warnings: ; \
        echo "***************************"; \
	grep "LaTeX Warning" main.log | grep -v 'multiply defined'; \
        echo "***************************"; \
	echo ; \
        echo "***************************"; \
	echo Overview of bibtex warnings: ; \
        echo "***************************"; \
        grep ^Warning-- main.blg | grep -v 'missing pages'; \
	echo "***************************"; \
	echo ; \
        echo "*************************"; \
	echo Overview of latex errors: ; \
        echo "*************************"; \
	grep "^!" main.log; \
        echo "*************************"; \
	echo

main.bbl: references.bib zotero.bib messaging.bib faculty-app.bib $(TEXFILES) Makefile done.flag
	latex -interaction=batchmode main.tex || true
	rm -f $*.dvi
	bibtex -terse $*

main.dvi: preprocess $(TEXFILES) $(EPSFIGFILES) main.bbl $(EPSFIGURES) Makefile done.flag example-program.tex
	@echo Running latex once.; \
	while ( latex -interaction=nonstopmode main.tex; grep Rerun main.log ) ; do \
          echo; \
          echo "*********************************"; \
          echo "*********************************"; \
          echo Reruning to complete compilation.; \
          echo "*********************************"; \
          echo "*********************************"; \
	done; \
        echo "***************************"; \
	echo DVI file generated.; \
        echo "***************************"; \
	echo ; \
	make printerrors 

example-program.tex: example-program.in.tex
	lgrind -d lgrindef -lstreamit -e example-program.in.tex > example-program.tex

.dvi.pdf: Makefile
	dvips -z -q -t letter -P cmz -f $< | ps2pdf - $*.pdf

.fig.eps: Makefile
	fig2dev -L pstex $< $*.eps

.dvi.ps: Makefile
	dvips -z -q -t letter $< -o $*.ps

.eps.pdf: Makefile
	epstopdf $<

clean:
	rm -f *.lof *.lot *.pfg *~ *.aux *.dvi *.blg *.log *.bbl $(EPSFIGFILES) *.bak main.ps main.pdf *.toc *.swp done.flag diff.tex example-program.tex
