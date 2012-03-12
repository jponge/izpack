/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.panels.htmlinfo;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.base.InstallerFrame;
import com.izforge.izpack.installer.base.IzPanel;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.util.HyperlinkHandler;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.Document;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The HTML info panel.
 *
 * @author Julien Ponge
 */
public class HTMLInfoPanel extends IzPanel
{

    private static final long serialVersionUID = 3257008769514025270L;

    /**
     * Resource prefix for panel.
     */
    protected String panelResourcePrefixStr;

    /**
     * Resource name for panel content.
     */
    protected String panelResourceNameStr;

    /**
     * The text area.
     */
    private JEditorPane textArea;

    /**
     * Constructs an <tt>HTMLInfoPanel</tt>.
     *
     * @param parent          the parent window
     * @param installData     the installation data
     * @param resourceManager the resource manager
     * @param log             the log
     */
    public HTMLInfoPanel(InstallerFrame parent, GUIInstallData installData, ResourceManager resourceManager, Log log)
    {
        this(parent, installData, "HTMLInfoPanel", true, resourceManager, log);
    }

    /**
     * Constructs an <tt>HTMLInfoPanel</tt>.
     *
     * @param parent            the parent window
     * @param installData       the installation data
     * @param resPrefixStr      prefix string for content resource name.
     * @param showInfoLabelFlag true to show "please read..." label above content
     * @param resourceManager   the resource manager
     * @param log               the log
     */
    public HTMLInfoPanel(InstallerFrame parent, GUIInstallData installData,
                         String resPrefixStr, boolean showInfoLabelFlag, ResourceManager resourceManager,
                         Log log)
    {
        super(parent, installData, new IzPanelLayout(log), resourceManager);
        //setup given resource prefix and name:
        panelResourcePrefixStr = resPrefixStr;
        panelResourceNameStr = resPrefixStr + ".info";

        // We add the components

        if (showInfoLabelFlag)
        {  //flag is set; add label above content
            add(LabelFactory.create(installData.getLangpack().getString("InfoPanel.info"), parent.getIcons()
                    .get("edit"), LEADING), NEXT_LINE);
        }
        try
        {
            textArea = new JEditorPane()
            {       //override get-stream method to parse variable

                // declarations in HTML content:
                protected InputStream getStream(URL urlObj)
                        throws IOException
                {                  //get original stream contents:
                    final InputStream inStm = super.getStream(urlObj);
                    final ByteArrayOutputStream btArrOutStm =
                            new ByteArrayOutputStream();
                    int b;         //copy contents to output stream:
                    final byte[] buff = new byte[2048];
                    while ((b = inStm.read(buff, 0, buff.length)) > 0)
                    {
                        btArrOutStm.write(buff, 0, b);
                    }
                    //convert to string and parse variables:
                    final String parsedStr =
                            parseText(btArrOutStm.toString());
                    //return input stream to parsed string:
                    return new ByteArrayInputStream(
                            parsedStr.getBytes());
                }
            };
            textArea.setName(GuiId.HTML_INFO_PANEL_TEXT.id);
            textArea.setContentType("text/html; charset=utf-8");
            textArea.setEditable(false);
            textArea.addHyperlinkListener(new HyperlinkHandler());
            JScrollPane scroller = new JScrollPane(textArea);
            //textArea.setPage(loadHTMLInfoContent());
            //set caret so beginning of file is displayed:
            textArea.setCaretPosition(0);
            add(scroller, NEXT_LINE);
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }
        getLayoutHelper().completeLayout();
    }

    /*
    * loads the content of the info resource as text so that it can be parsed afterwards
    */

    private URL loadHTMLInfoContent()
    {
        if (getMetadata() != null && getMetadata().getPanelid() != null)
        {
            try
            {
                String panelSpecificResName = panelResourcePrefixStr + '.' + this.getMetadata().getPanelid();
                String panelspecificResContent = resourceManager.getTextResource(panelSpecificResName);
                if (panelspecificResContent != null)
                {
                    panelResourceNameStr = panelSpecificResName;
                }
            }
            catch (Exception e)
            {
                // Those ones can be skipped
            }
        }

        try
        {
            return resourceManager.getLocalizedURL(panelResourceNameStr);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Always true.
     */
    public boolean isValidated()
    {
        return true;
    }

    public void panelActivate()
    {
        // Clear this property to get the document to reload and perform variable substitution.
        // See JEditorPane.setPage javadoc.
        textArea.getDocument().putProperty(Document.StreamDescriptionProperty, null);
        try
        {
            textArea.setPage(loadHTMLInfoContent());
            textArea.setCaretPosition(0);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

