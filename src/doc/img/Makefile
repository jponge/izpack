# don't use eps2pdf, it's worse
EPSTOPDF=epstopdf

DIAGRAMS=\
	ch1-comparch.eps \
	ch1-compinside.eps \
	ch5-izpanel.eps

EPSPICTURES=\
	ch1-import.eps \
	userInput-IP.eps \
	userInput-email.eps \
	userInput-phone.eps \
	userInput-serial.eps

PNGPICTURES=\
	userInput-search.png \
	compilePanel.png

DESTFILES=\
	$(EPSPICTURES:.eps=.png) \
	$(PNGPICTURES:.png=.eps) \
	$(DIAGRAMS:.eps=.pdf)

.PHONY:	all
all:	$(DESTFILES)

.PHONY: clean
clean:
	rm -f $(DESTFILES)

%.png:	%.eps
	convert $< $@

%.eps:	%.png
	convert $< $@

%.pdf:	%.eps
	$(EPSTOPDF) $< --outfile=$@

