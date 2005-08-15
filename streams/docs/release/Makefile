DOCS := install news options readme
TEXTOUT := INSTALL NEWS OPTIONS README
RELEASEDATE := $(shell date +"%B %e, %Y")

TEX := $(DOCS:%=%.tex)

all: $(TEX) docs $(TEXTOUT)

include $(STREAMIT_HOME)/misc/Makefile.docs

%.tex: %.tex.in
	cp $< $@
	perl -pi -e 's/RELEASE\_DATE/$(RELEASEDATE)/g' $@

%.html: %.tex
	$(HEVEA) -nosymb $*.tex
	if [ -f $*.image.tex ]; then $(IMAGEN) -png $*; fi
	$(HEVEA) -nosymb $*.tex

options.html: options-list.tex

options.dvi: options-list.tex

INSTALL: install.txt
	cp $< $@

README: readme.txt
	cp $< $@

NEWS: news.txt
	cp $< $@

OPTIONS: options.txt
	cp $< $@

clean::
	rm -f $(TEXTOUT) $(TEX)
