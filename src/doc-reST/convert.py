#!/usr/bin/env python

import os

for i in xrange(1, 14):
	os.system('rst2html.py node%i.html.txt node%i.html' % (i, i))