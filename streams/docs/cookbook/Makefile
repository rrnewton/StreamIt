DOCS := cookbook
DVI := $(DOCS:%=%.dvi)
PS := $(DOCS:%=%.ps)
PDF := $(DOCS:%=%.pdf)

MPOST := mpost
LATEX := latex
BIBTEX := bibtex
DVIPS := dvips
PS2PDF := ps2pdf

# RERUN := "(There were undefined references|Rerun to get (cross-references|the bars) right)"
RERUN := "Rerun to get (cross-references|the bars) right"
RERUNBIB := "No file.*\.bbl|Citation.*undefined"

all: $(DVI) $(PS) $(PDF)

%.pdf: %.ps
	$(PS2PDF) $< $@

%.ps: %.dvi
	$(DVIPS) -o $@ $<

%.dvi %.aux %.log: %.tex %.mp
	$(MPOST) $*
	$(LATEX) $*
#	if egrep -q $(RERUNBIB) $*.log; then $(BIBTEX) $*; $(LATEX) $<; fi
	while egrep -q $(RERUN) $*.log; do $(LATEX) $<; done

clean:
	rm -f $(DVI) $(PS) $(PDF)
	rm -f *~ *.aux *.log *.bbl *.blg *.toc

cookbook.dvi: overview.tex minimal.tex moving-average.tex lpf.tex bpf.tex \
	equalizer.tex echo.tex fib.tex keywords.tex \
