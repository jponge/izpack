# Variables
TEX  = latex
PS   = dvips
PDF  = dvipdfm
MAIN = izpack-doc

# Default target - makes it all
all: dvi ps pdf

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

# Cleanup
clean:
	rm -f *.dvi *.aux *.log *.toc
	rm -rf pdf
	rm -rf ps

