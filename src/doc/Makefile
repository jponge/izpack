# Variables
TEX  = latex
PS   = dvips
PDFLATEX  = pdflatex
MAIN = izpack-doc
HTML = latex2html
SUBDIRS = img

# Default target - makes it all
all: subdirs dvi pdf html

# Makes the dvi
.PHONY:	dvi
dvi: ${MAIN}.tex
	${TEX} ${MAIN}.tex
	${TEX} ${MAIN}.tex
	# three times to get references right
	${TEX} ${MAIN}.tex

# Makes the PostScript(tm) version
.PHONY:	ps
ps: dvi
	[ -d ps ] || mkdir ps
	${PS} ${MAIN}.dvi -o ps/${MAIN}.ps

# Makes the PDF(tm) version
.PHONY:	pdf
pdf:
	[ -d pdf ] || mkdir pdf
	${PDFLATEX} ${MAIN}
	${PDFLATEX} ${MAIN}
	${PDFLATEX} ${MAIN} && mv ${MAIN}.pdf pdf/

# Makes the HTML version
.PHONY: html
html:
	[ -d html ] || mkdir html
	${HTML} -split 3 -noexternal_images -dir html -white -image_type png -local_icons ${MAIN}.tex

.PHONY: subdirs
subdirs:
	for d in $(SUBDIRS) ; do \
		make -C $$d $(MAKECMDGOALS) ; \
	done

# Cleanup
.PHONY: clean
clean:	subdirs
	rm -f *.dvi *.aux *.log *.toc *.out *~
	rm -rf pdf
	rm -rf ps
	rm -rf html
	
# Uninstall
.PHONY: uninstall
uninstall:
	rm -rf ../../doc/izpack

# Install
.PHONY: install
install: dvi ps pdf html
	[ -d ../../doc/izpack ] || mkdir ../../doc/izpack
	cp -R pdf html ../../doc/izpack
