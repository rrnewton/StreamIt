DOCS := streamit-lang semantic-checks
DVI := $(DOCS:%=%.dvi)
PS := $(DOCS:%=%.ps)
PDF := $(DOCS:%=%.pdf)
HTML := $(DOCS:%=%.html)
TEXT := $(DOCS:%=%.txt)

MPOST := mpost
LATEX := latex
BIBTEX := bibtex
DVIPS := dvips
PS2PDF := ps2pdf
HEVEA := hevea
IMAGEN := imagen

# RERUN := "(There were undefined references|Rerun to get (cross-references|the bars) right)"
RERUN := "Rerun to get (cross-references|the bars) right"
RERUNBIB := "No file.*\.bbl|Citation.*undefined"

all: $(DVI) $(PS) $(PDF) $(HTML) $(TEXT)

%.pdf: %.ps
	$(PS2PDF) $< $@

%.ps: %.dvi
	$(DVIPS) -P cmz -o $@ $<

#%.dvi %.aux %.log: %.tex %.mp
%.dvi %.aux %.log: %.tex
#	$(MPOST) $*
	$(LATEX) $*
#	if egrep -q $(RERUNBIB) $*.log; then $(BIBTEX) $*; $(LATEX) $<; fi
	while egrep -q $(RERUN) $*.log; do $(LATEX) $<; done

%.html: %.tex %.hva
	$(HEVEA) $*.hva $*.tex
#	imagen -png $*
	$(HEVEA) $*.hva $*.tex

%.txt: %.tex
	$(HEVEA) -text $*.hva $*.tex

clean:
	rm -f $(DVI) $(PS) $(PDF) $(HTML) $(TEXT)
	rm -f *~ *.aux *.log *.bbl *.blg *.toc *.haux *.htoc *.png
	rm -f *.out *.image.tex *.[0-9]

cookbook.dvi: overview.tex minimal.tex moving-average.tex lpf.tex bpf.tex \
	equalizer.tex echo.tex fib.tex keywords.tex \
