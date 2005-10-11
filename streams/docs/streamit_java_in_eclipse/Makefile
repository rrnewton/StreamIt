# 
all: streamit_java_in_eclipse.pdf streamit_java_in_eclipse.ps

streamit_java_in_eclipse.ps: clean streamit_java_in_eclipse.dvi
	dvips streamit_java_in_eclipse.dvi -P cmz -o streamit_java_in_eclipse.ps

streamit_java_in_eclipse.pdf: streamit_java_in_eclipse.ps
	ps2pdf streamit_java_in_eclipse.ps streamit_java_in_eclipse.pdf

streamit_java_in_eclipse.dvi: $(FILES)
	latex streamit_java_in_eclipse
#	bibtex streamit_java_in_eclipse
	latex streamit_java_in_eclipse
	latex streamit_java_in_eclipse

clean:
	$(RM) *.aux *.bbl *.blg *.dvi *.log
