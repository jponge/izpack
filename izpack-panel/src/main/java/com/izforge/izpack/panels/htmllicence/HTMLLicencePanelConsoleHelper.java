/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels.htmllicence;

import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.panels.licence.AbstractLicensePanelConsole;
import com.izforge.izpack.util.Debug;


/**
 * HTML License Panel console helper
 */
public class HTMLLicencePanelConsoleHelper extends AbstractLicensePanelConsole
{

    /**
     * Constructs a <tt>HTMLLicencePanelConsoleHelper</tt>.
     *
     * @param resources the resources
     */
    public HTMLLicencePanelConsoleHelper(ResourceManager resources)
    {
        super(resources);
    }

    /**
     * Returns the text to display.
     *
     * @return the text. A <tt>null</tt> indicates failure
     */
    @Override
    protected String getText()
    {
        String text = getText("HTMLLicencePanel.licence");
        if (text != null)
        {
            text = removeHTML(text);
        }
        return text;
    }

    private String removeHTML(String source)
    {
        String result = "";
        try
        {
            // chose to keep newline (\n) instead of carriage return (\r) for line breaks.

            // Replace line breaks with space
            result = source.replaceAll("\r", " ");
            // Remove step-formatting
            result = result.replaceAll("\t", "");
            // Remove repeating spaces because browsers ignore them

            result = result.replaceAll("( )+", " ");


            result = result.replaceAll("<( )*head([^>])*>", "<head>");
            result = result.replaceAll("(<( )*(/)( )*head( )*>)", "</head>");
            result = result.replaceAll("(<head>).*(</head>)", "");
            result = result.replaceAll("<( )*script([^>])*>", "<script>");
            result = result.replaceAll("(<( )*(/)( )*script( )*>)", "</script>");
            result = result.replaceAll("(<script>).*(</script>)", "");

            // remove all styles (prepare first by clearing attributes)
            result = result.replaceAll("<( )*style([^>])*>", "<style>");
            result = result.replaceAll("(<( )*(/)( )*style( )*>)", "</style>");
            result = result.replaceAll("(<style>).*(</style>)", "");

            result = result.replaceAll("(<( )*(/)( )*sup( )*>)", "</sup>");
            result = result.replaceAll("<( )*sup([^>])*>", "<sup>");
            result = result.replaceAll("(<sup>).*(</sup>)", "");

            // insert tabs in spaces of <td> tags
            result = result.replaceAll("<( )*td([^>])*>", "\t");

            // insert line breaks in places of <BR> and <LI> tags
            result = result.replaceAll("<( )*br( )*>", "\r");
            result = result.replaceAll("<( )*li( )*>", "\r");

            // insert line paragraphs (double line breaks) in place
            // if <P>, <DIV> and <TR> tags
            result = result.replaceAll("<( )*div([^>])*>", "\r\r");
            result = result.replaceAll("<( )*tr([^>])*>", "\r\r");

            result = result.replaceAll("(<) h (\\w+) >", "\r");
            result = result.replaceAll("(\\b) (</) h (\\w+) (>) (\\b)", "");
            result = result.replaceAll("<( )*p([^>])*>", "\r\r");

            // Remove remaining tags like <a>, links, images,
            // comments etc - anything that's enclosed inside < >
            result = result.replaceAll("<[^>]*>", "");


            result = result.replaceAll("&bull;", " * ");
            result = result.replaceAll("&lsaquo;", "<");
            result = result.replaceAll("&rsaquo;", ">");
            result = result.replaceAll("&trade;", "(tm)");
            result = result.replaceAll("&frasl;", "/");
            result = result.replaceAll("&lt;", "<");
            result = result.replaceAll("&gt;", ">");

            result = result.replaceAll("&copy;", "(c)");
            result = result.replaceAll("&reg;", "(r)");
            result = result.replaceAll("&(.{2,6});", "");

            // Remove extra line breaks and tabs:
            // replace over 2 breaks with 2 and over 4 tabs with 4.
            // Prepare first to remove any whitespaces in between
            // the escaped characters and remove redundant tabs in between line breaks
            result = result.replaceAll("(\r)( )+(\r)", "\r\r");
            result = result.replaceAll("(\t)( )+(\t)", "\t\t");
            result = result.replaceAll("(\t)( )+(\r)", "\t\r");
            result = result.replaceAll("(\r)( )+(\t)", "\r\t");
            result = result.replaceAll("(\r)(\t)+(\\r)", "\r\r");
            result = result.replaceAll("(\r)(\t)+", "\r\t");

        }
        catch (Exception e)
        {
            Debug.error(e);
        }

        return result;
    }

}
