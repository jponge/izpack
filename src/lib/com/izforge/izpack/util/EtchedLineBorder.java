/**
    @(#)enclosing_type.java
    © Copyright Servoy 2002
    All rights reserved 

    The copyright of the computer program(s) herein is 
    the property of Servoy. The program(s) may be used/copied
    only with the written permission of the owner or in 
    accordance with the terms and conditions stipulated in 
    the agreement/contract under which the program(s) have 
    been supplied. 
 */
package com.izforge.izpack.util;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.EtchedBorder;

/**
 * @author jblok
 * @version $Id$
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class EtchedLineBorder extends EtchedBorder
{
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
	int w = width;
	int h = height;
	
	g.translate(x, y);
	
	g.setColor(etchType == LOWERED? getShadowColor(c) : getHighlightColor(c));
	g.drawLine(10, 0, w-2, 0);
	
	g.setColor(etchType == LOWERED? getHighlightColor(c) : getShadowColor(c));
//	g.drawLine(1, h-3, 1, 1);
	g.drawLine(10, 1, w-2, 1);
	
//	g.drawLine(0, h-1, w-1, h-1);
//	g.drawLine(w-1, h-1, w-1, 0);
	
	g.translate(-x, -y);
    }

//    public Insets getBorderInsets(Component c)       {
//        return new Insets(10, 10, 10, 10);
//    }
//
}
