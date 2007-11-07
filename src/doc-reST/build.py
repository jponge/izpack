#!/usr/bin/env python

import os
import shutil

REST_FILES = [
	'index',
	'introduction',
	'getting-started',
	'installation-files',
	'advanced-features',
	'desktop-shortcuts',
	'creating-panels',
	'user-input',
	'custom-actions',
	'apache-license',
	'cc-license',
	'cookbooks'
]

DIRS = ['html', 'pdf']

for d in DIRS:
	if not os.path.exists(d):
		os.makedirs(d)

for rest_file in REST_FILES:
	os.system('rst2html.py %s.txt html/%s.html' % (rest_file, rest_file))

os.system('rst2newlatex.py pdf-version.txt pdf/manual.tex')

for f in os.listdir('.'):
	if f.endswith('.png') or f.endswith('.jpg'):
		shutil.copyfile(f, 'html/' + f)
		shutil.copyfile(f, 'pdf/' + f)