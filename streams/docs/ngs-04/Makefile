DOCS   := NGSabstract NGSpaper
DVI    := $(DOCS:%=%.dvi)
PS     := $(DOCS:%=%.ps)
PDF    := $(DOCS:%=%.pdf)

LATEX  := latex
BIBTEX := bibtex
DVIPS  := dvips
PS2PDF := ps2pdf

PAPER_EPS := code-fm.eps echo.eps fib-streamit.eps fir-object.eps \
	     fir-proc.eps fir-streamit.eps fusion-horiz.eps fusion-vert.eps

# RERUN := "(There were undefined references|Rerun to get (cross-references|the bars) right)"
RERUN    := "Rerun to get (cross-references|the bars) right"
RERUNBIB := "No file.*\.bbl|Citation.*undefined"

all: $(PAPER_EPS) $(DVI) $(PS) $(PDF)

%.pdf: %.ps
	$(PS2PDF) $< $@

%.ps: %.dvi
	$(DVIPS) -o $@ $<

%.dvi %.aux %.log: %.tex 
	$(LATEX) $<
	if egrep -q $(RERUNBIB) $*.log; then $(BIBTEX) $*; $(LATEX) $<; fi
	while egrep -q $(RERUN) $*.log; do $(LATEX) $<; done

#%.eps: %.obj
#	tgif -print -eps $<

clean:
	rm -f *~ *.aux *.bbl *.log *.blg *.ps *.pdf *.dvi
#	rm -f $(PAPER_EPS)
