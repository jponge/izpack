# Variables
TEX  = latex
PS   = dvips
PDF  = dvipdfm
MAIN = izpack-doc
HTML = latex2html

# Default target - makes it all
all: dvi ps pdf html

# Makes the dvi
dvi: ${MAIN}.tex
	${TEX} ${MAIN}.tex
	${TEX} ${MAIN}.tex

# Makes the PostScript(tm) version
ps: dvi
	mkdir ps
	${PS} ${MAIN}.dvi -o ps/${MAIN}.ps

# Makes the PDF(tm) version
pdf: dvi
	mkdir pdf
	${PDF} -o pdf/${MAIN}.pdf ${MAIN}.dvi

# Makes the HTML version
html:
	mkdir html
	${HTML} -split 3 -noexternal_images -dir html -white -image_type png -local_icons ${MAIN}.tex

# Cleanup
clean:
	rm -f *.dvi *.aux *.log *.toc *~
	rm -rf pdf
	rm -rf ps
	rm -rf html
