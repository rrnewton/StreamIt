all: report.dvi

report.dvi : report.tex \
#	block.ps \
#	feedback.ps \
#	split-join.ps \
#	ceiling.ps \
#	pipe.ps \
#   stream-select.ps \


%.dvi : %.tex
	latex $<
	latex $<

%.ps : %.dot
	dot -Tps $< > $@

