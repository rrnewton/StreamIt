DOCS := cookbook

all: docs

include $(STREAMIT_HOME)/misc/Makefile.docs

# I can't seem to get latex to include the file from ../release, so just copy it over
options-list.tex: ../release/options-list.tex
	cp ../release/options-list.tex .

cookbook.dvi: overview.tex minimal.tex moving-average.tex lpf.tex bpf.tex \
	equalizer.tex echo.tex fib.tex keywords.tex compiler.tex options.tex options-list.tex *.eps fm-bloodgraph.eps \

fm-bloodgraph.eps: fm-bloodgraph.eps.gz
	gunzip -c fm-bloodgraph.eps.gz > fm-bloodgraph.eps
