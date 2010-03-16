#!/usr/bin/env python

import string

from glob import glob
from xml.etree.ElementTree import ElementTree, tostring

def warn_missing(element):
    if element.tag not in action: print '============( %s )============' % element.tag
    pass

action = {
    'title': lambda element, context: ('\nh%s. %s\n\n' % (context['header_depth'], decode_inline(element, context)), False),
    'paragraph': lambda element, context: ('%s%s' % (decode_inline(element, context), context['paragraph_newlines']), False),
    'list_item': lambda element, context: ('%s ' % context['list_prefix'], True),
    'reference': lambda element, context: ('[%s|%s]' % (element.text, element.get('refuri', '')), True),
    'literal_block': lambda element, context: ('\n{code}\n%s\n{code}\n\n' % element.text, False),
    'block_quote': lambda element, context: ('\n{quote}\n%s\n{quote}\n\n' % element.text, True),
    'strong': lambda element, context: ('*%s*' % element.text, True),
    'literal': lambda element, context: ('{{%s}}' % element.text, True),
    'emphasis': lambda element, context: ('_%s_' % element.text, True),
    'image': lambda element, context: ('\n{note:title=There was an image}%s{note}\n\n' % element.attrib['uri'], False),
    'term': lambda element, context: ('%s ' % context['list_prefix'], True),
    'table': lambda element, context: ('\n{note:title=Table to format}{code}%s{code}{note}\n\n' % tostring(element), False)
}

initial_context = {
    'header_depth': 1,
    'list_prefix' : '',
    'paragraph_newlines': '\n\n'
}

def decode_inline(element, context):    
    
    warn_missing(element)
    
    text = element.text
    if text is None: text = ''
        
    tail = element.tail
    if tail is None: tail = ''    
    
    subs = []
    for child in element.getchildren():
        if child.tag in action:
            output = action[child.tag](child, context)[0]
            subs.append(output)
        else:
            subs.append(decode_inline(child, context))
    sub = string.join(subs, '')
    
    return '%s%s%s' % (text, sub, tail)

def convert(source, target):
        
    tree = ElementTree()
    tree.parse(source)
    
    out = open(target, "w")
    
    def walk(element, context):
        
        warn_missing(element)
        
        if element.tag in action:
            output, walk_children = action[element.tag](element, context)
            if output is not None:
                print output
                out.write(output)
            if not walk_children:
                return
        
        for child in element.getchildren():
            new_context = dict(context)
            if element.tag == 'section':
                new_context['header_depth'] = new_context['header_depth'] + 1
            elif element.tag == 'bullet_list':
                new_context['list_prefix'] = new_context['list_prefix'] + '*'
                new_context['paragraph_newlines'] = '\n'
            elif element.tag == 'enumerated_list':
                new_context['list_prefix'] = new_context['list_prefix'] + '#'
                new_context['paragraph_newlines'] = '\n'
            elif element.tag == 'definition_list':
                new_context['list_prefix'] = new_context['list_prefix'] + '*'
                new_context['paragraph_newlines'] = '\n'
            walk(child, new_context)
    
    for child in tree.getroot().getchildren():
        walk(child, initial_context)
    
    out.close()
    
if __name__ == '__main__':

    for xml_file in glob('xml/*.xml'):
        convert(xml_file, xml_file[0:-4] + '.txt')
    