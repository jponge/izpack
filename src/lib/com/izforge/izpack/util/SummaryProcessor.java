/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2005 Klaus Bartz
 *
 *  File :               SummaryProcessor.java
 *  Description :        A helper which creates a summary of all panels.
 *  Author's email :     bartzkau@users.berlios.de
 *  Website :            http://www.izforge.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.izforge.izpack.util;

import java.util.Iterator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.IzPanel;

/**
 * A helper class which creates a summary from all panels. This class calls all
 * declared panels for a summary To differ between caption and message, HTML is
 * used to draw caption in bold and indent messaged a little bit.
 * 
 * @author Klaus Bartz
 * 
 */
public class SummaryProcessor
{

    private static String HTML_HEADER;

    private static String HTML_FOOTER = "</body>\n</html>\n";

    private static String BODY_START = "<div class=\"body\">";

    private static String BODY_END = "</div>";

    private static String HEAD_START = "<h1>";

    private static String HEAD_END = "</h1>\n";

    private static String NULL_PAGE;
    static
    {
        // Initialize HTML header and footer.
        StringBuffer sb = new StringBuffer(256);
        sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n").append(
                "<html>\n<head>\n<STYLE TYPE=\"text/css\" media=screen,print>\n").append(
                "h1{\n  font-size: 100%;\n  margin: 1em 0 0 0;\n  padding: 0;\n}\n").append(
                "div.body {\n  font-size: 100%;\n  margin: 0mm 2mm 0  8mm;\n  padding: 0;\n}\n")
                .append("</STYLE>\n</head>\n<body>\n");
        HTML_HEADER = sb.toString();
        NULL_PAGE = HTML_HEADER + HTML_FOOTER;
    }

    /**
     * Returns a HTML formated string which contains the summary of all panels.
     * To get the summary, the methods * {@link IzPanel#getSummaryCaption} and
     * {@link IzPanel#getSummaryBody()} of all panels are called.
     * 
     * @param idata
     *            AutomatedInstallData which contains the panel references
     * @return a HTML formated string with the summary of all panels
     */
    public static String getSummary(AutomatedInstallData idata)
    {
        Iterator iter = idata.panels.iterator();
        StringBuffer sb = new StringBuffer(2048);
        sb.append(HTML_HEADER);
        while (iter.hasNext())
        {
            IzPanel panel = (IzPanel) iter.next();
            String caption = panel.getSummaryCaption();
            String msg = panel.getSummaryBody();
            // If no caption or/and message, ignore it.
            if (caption == null || msg == null)
            {
                continue;
            }
            sb.append(HEAD_START).append(caption).append(HEAD_END);
            sb.append(BODY_START).append(msg).append(BODY_END);
        }
        sb.append(HTML_FOOTER);
        return (sb.toString());
    }

}
