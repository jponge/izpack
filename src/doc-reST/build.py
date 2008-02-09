#!/usr/bin/env python
# ........................................................................... #
#
# This script generates the documentation using Python docutils.
#
# It requires the following softwares to be installed and made available from
# the invocation path context:
#
# 	- Python (of course!)
# 	- the docutils module (see http://docutils.sourceforge.net/)
#   - a LaTeX distribution to invoke 'pdflatex' (MikTeX, TeXLive, teTeX, ...)
#
# It should be noted that the pdflatex invocation can be skipped if necessary
# by using a special command-line flag.
#
# ........................................................................... #
#
# IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
#
# http://izpack.org/
# http://developer.berlios.de/projects/izpack/
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ........................................................................... #

import os
import shutil

from glob import glob
from optparse import OptionParser
from docutils.core import publish_cmdline as publish

def create_dirs():
	print('Creating output directories...')
	for d in ['html', 'pdf']:
		if not os.path.exists(d): os.makedirs(d)

def scan_files():
	print('Scanning files...')
	rest_files = [ f[0:(len(f) - 4)] for f in glob('*.txt') if f != 'pdf-version.txt' ]
	resources   = glob('*.jpg') + glob('*.png') + glob('*.css')
	return rest_files, resources

def copy_files(resources):
	print('Copying resources...')
	for pic in resources:
		print('    ' + pic)
		shutil.copyfile(pic, 'html/' + pic)
		shutil.copyfile(pic, 'pdf/' + pic)
		shutil.copyfile('picins.sty', 'pdf/picins.sty')

def generate_html(rest_files):
	print('Generating html...')
	shutil.copyfile('include-top', 'include-top.inc')
	shutil.copyfile('include-bottom', 'include-bottom.inc')
	for rest_file in rest_files:
		print('    ' + rest_file)
		args = [
		    '--link-stylesheet',
		    '--stylesheet-path=html/izpack.css',
		    '--cloak-email-addresses',
		    '%s.txt' % rest_file,
		    'html/%s.html' % rest_file
		]
		publish(writer_name='html', argv=args)

def generate_latex():
	print('Generating LaTeX for PDF output...')
	shutil.copyfile('include-empty', 'include-top.inc')
	shutil.copyfile('include-empty', 'include-bottom.inc')
	publish(writer_name='newlatex2e', argv=['pdf-version.txt', 'pdf/manual.tex'])

def compile_latex():
	print('Generating PDF from LaTeX...')
	os.chdir('pdf')
	for i in xrange(1,4):
		print('    pdflatex pass #%i' % i)
		os.system('pdflatex --interaction=batchmode manual.tex')
	os.chdir('..')

if __name__ == '__main__':
	parser = OptionParser()
	parser.add_option("-n", "--no-pdflatex", dest="pdflatex", action="store_false",
                  	default=True, help="don't call pdflatex")
	options, args = parser.parse_args()
	
	rest_files, resources = scan_files()
	create_dirs()
	copy_files(resources)
	generate_html(rest_files)
	if options.pdflatex:
	    generate_latex()
	    compile_latex()
	
	print('Done')
