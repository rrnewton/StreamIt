# Specify the top-level .tex file basename here.
document = main

SOURCES = $(wildcard *.tex) $(wildcard *.cls) $(wildcard *.bib)
FIGURES = $(wildcard figures/*)

LATEX = latex
DVIPS = dvips
DVIPSARGS = -t letter 
PS2PDF = ps2pdfwr

# it doesn't take much time to rebuild everything, so assume it's 
# all been touched.  This takes care of me updating the file on a 
# LaTeX error, quitting latex, and having the change go unnoticed
.PHONY : $(SOURCES)

# make will execute this as a default
all: dvi

# shortcuts
dvi: $(document).dvi
ps:  $(document).ps
pdf: $(document).pdf

# recreate the dvi using bakoma versions of CMR fonts (using kbarr's map)
$(document).pdf : $(document).dvi
	env DVIPSRC /u/kbarr/projects/latex/.dvipsrc $(DVIPS) $(DVIPSARGS) -j0 -f < $(document).dvi > $(document).ps
	$(PS2PDF) $(document).ps $(document).pdf

$(document).ps : $(document).dvi
	$(DVIPS) $(DVIPSARGS) -f < $(document).dvi > $(document).ps

$(document).dvi : $(SOURCES) $(FIGURES)
	$(LATEX) $(document)
	bibtex $(document)
	$(LATEX) $(document)
#	./balance.pl $(document)
	$(LATEX) $(document)

clean:
	rm -f *.dvi *.aux *.bbl *.log *.blg *.lot *.lof $(document).ps $(document).pdf *~


# Expected targets
twice:
	$(LATEX) $(document)
	$(LATEX) $(document)
bib:
	bibtex $(document)

ps2: $(document).ps2
$(document).ps2 : $(document).dvi
	$(DVIPS) $(DVIPSARGS) -o ps2.tmp -p 2 $(document).dvi
	mpage -2  ps2.tmp > $(document).ps2
	rm ps2.tmp


#----------------------------------------------------------------------
# Document previewers.
#----------------------------------------------------------------------

x:
	xdvi $(document) &

gv: $(document).ps
	ghostview $(document).ps &
