DOCS := install news options readme
DVI := $(DOCS:%=%.dvi)
PS := $(DOCS:%=%.ps)
PDF := $(DOCS:%=%.pdf)
HTML := $(DOCS:%=%.html)
TEXT := $(DOCS:%=%.txt)
TEXTOUT := INSTALL NEWS OPTIONS README

MPOST := mpost
LATEX := latex
BIBTEX := bibtex
DVIPS := dvips
PS2PDF := ps2pdf
HEVEA := hevea
IMAGEN := imagen
PERL := perl

# RERUN := "(There were undefined references|Rerun to get (cross-references|the bars) right)"
RERUN := "Rerun to get (cross-references|the bars) right"
RERUNBIB := "No file.*\.bbl|Citation.*undefined"

all: $(DVI) $(PS) $(PDF) $(HTML) $(TEXTOUT)

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
	$(HEVEA) -nosymb $*.hva $*.tex
	if [ -f $*.image.tex ]; then imagen -png $*; fi
	$(HEVEA) -nosymb $*.hva $*.tex

%.txt: %.html
	$(PERL) htmlformat.pl $< > $@

INSTALL: install.txt
	cp $< $@

README: readme.txt
	cp $< $@

NEWS: news.txt
	cp $< $@

OPTIONS: options.txt
	cp $< $@

clean:
	rm -f $(DVI) $(PS) $(PDF) $(HTML) $(TEXT) $(TEXTOUT)
	rm -f *~ *.aux *.log *.bbl *.blg *.toc *.haux *.htoc *.png
	rm -f *.out *.image.tex *.[0-9]

cookbook.dvi: overview.tex minimal.tex moving-average.tex lpf.tex bpf.tex \
	equalizer.tex echo.tex fib.tex keywords.tex \
