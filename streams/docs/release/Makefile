DOCS := install news options readme
TEXTOUT := INSTALL NEWS OPTIONS README

all: docs $(TEXTOUT)

include $(STREAMIT_HOME)/misc/Makefile.docs

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
	rm -f $(TEXTOUT)
