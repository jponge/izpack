

package com.izforge.izpack.panels;



import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;
import com.izforge.izpack.installer.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import net.n3.nanoxml.*;

import java.io.*;
import java.net.*;

public class EnrollPanel extends IzPanel implements ActionListener

{
    //.....................................................................

    // The fields

    private JLabel headerLable;

    private GridBagLayout layout;

    private GridBagConstraints gbConstraints;

    protected InstallData idata;

    private Map   textFieldMap = new HashMap();


    // The constructor

    public EnrollPanel(InstallerFrame parent, InstallData idata)  throws Exception {
        super(parent, idata);
        this.idata = idata;
        // We initialize our layout
        layout = new GridBagLayout();
        gbConstraints = new GridBagConstraints();
        setLayout(layout);

        GridBagConstraints gbConstraints = new GridBagConstraints();

        String headerText = parent.langpack.getString("EnrollPanel.header");
        headerLable = new JLabel(headerText);
        parent.buildConstraints(gbConstraints, 0, 0, 2, 1, 0.0, 0.0);
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.CENTER;
        layout.addLayoutComponent(headerLable, gbConstraints);
        add(headerLable);

        // Adds each pack enroll item
        int size = idata.enrollInfo.enrollItems.size();
        EnrollItem item = null;
        JLabel titleLabel = null;
        JTextField valueTextField = null;
        for (int i = 0; i < size; i++) {
            item = (EnrollItem)idata.enrollInfo.enrollItems.get(i);

            /* get the text of a EnrollItem*/
              String text = parent.langpack.getString("EnrollPanel."+item.name);
              if (item.required) {
                  text = text + "(*)";
              }
              titleLabel = new JLabel(text);
              valueTextField = new JTextField(null, 40);

            /* add JLabel and TextField for this EnrollItem */
            parent.buildConstraints(gbConstraints, 0, i+2, 1, 1, 0.0, 0.0);
            gbConstraints.insets = new Insets(5, 5, 5, 5);
            gbConstraints.fill = GridBagConstraints.NONE;
            gbConstraints.anchor = GridBagConstraints.EAST;
            layout.addLayoutComponent(titleLabel, gbConstraints);
            add(titleLabel);

            valueTextField = new JTextField(item.value, 40);
            valueTextField.addActionListener(this);
            parent.buildConstraints(gbConstraints, 1, i+2, 1, 1, 0.0, 0.0);
            gbConstraints.fill = GridBagConstraints.HORIZONTAL;
            gbConstraints.anchor = GridBagConstraints.WEST;
            layout.addLayoutComponent(valueTextField, gbConstraints);
            add(valueTextField);

            textFieldMap.put(valueTextField, item);
        }
    }


 public void panelActivate() {

    }



    //.....................................................................

    // The methods



    // Actions-handling method

    public void actionPerformed(ActionEvent e)   {
        Object source = e.getSource();
        EnrollItem item = (EnrollItem)textFieldMap.get(source);

        String value = null;
        if (source instanceof JTextField) {
            item.value = ((JTextField)source).getText();
        }
        else {
            System.err.println("unknown event source : " + e.toString());
            /** @todo  */
        }
    }

    // Indicates wether the panel has been validated or not

    public boolean isValidated() {
        boolean retVal = true;

        Iterator iter = textFieldMap.keySet().iterator();
        JTextField valueTextField = null;
        EnrollItem item = null;

        while (iter.hasNext()) {
            valueTextField = (JTextField)iter.next();
            item = (EnrollItem)textFieldMap.get(valueTextField);
            item.value = valueTextField.getText();
        }

        for (int i = 0; i < idata.enrollInfo.enrollItems.size(); i++) {
            item = (EnrollItem)idata.enrollInfo.enrollItems.get(i);
            if (item.required) {
                if ( null == item.value || item.value.trim().length() == 0) {
                    JOptionPane.showMessageDialog(this, parent.langpack.getString("EnrollPanel.itemsrequired") + item.name);
                    retVal = false;
                    return retVal;
                }
            }
        }

        Sender sender= new Sender(idata.enrollInfo.url);
        sender.setParentpanel(this);
        retVal = sender.send();

        return retVal;

    }



    // Asks to make the XML panel data

    public void makeXMLData(XMLElement panelRoot) {
        XMLElement enrollInfo = new XMLElement("enrollinfo");
        enrollInfo.setAttribute("url",idata.enrollInfo.url);

        XMLElement enrollItems = new XMLElement("enrollitems");
        enrollInfo.addChild(enrollItems);

        EnrollItem item = null;
        XMLElement itemElement = null;
        for (int i = 0; i < idata.enrollInfo.enrollItems.size(); i++)  {
            item = (EnrollItem)idata.enrollInfo.enrollItems.get(i);
            if (null == item.value) {
                continue;
            }
            itemElement = new XMLElement("item");
            itemElement.setAttribute("name", item.name);
            itemElement.setAttribute("value", item.value);

            enrollItems.addChild(itemElement);
        }
        panelRoot.addChild(enrollInfo);
    }



    // Asks to run in the automated mode

    public void runAutomated(XMLElement panelRoot)  {
        EnrollInfo enrollInfo = new EnrollInfo();

        XMLElement enrollInfoElement   = panelRoot.getFirstChildNamed("enrollinfo");
        enrollInfo.url = enrollInfoElement.getAttribute("url");

        Vector itemElements = enrollInfoElement.getChildren();

        Vector items = new Vector();
        EnrollItem item = null;
        XMLElement itemElement = null;

        if (itemElements != null) {
            for (int i = 0; i < itemElements.size(); i++) {
                itemElement = (XMLElement)itemElements.get(i);
                item = new EnrollItem();
                item.name = itemElement.getAttribute("name");
                item.value  = itemElement.getAttribute("value");
                items.add(item);
            }
        }

        idata.enrollInfo = enrollInfo;
    }


  class Sender {
    String urlString = null;
    IzPanel parent =null;
    Sender (String urlString)  {
        this.urlString = urlString;
    }

    void setParentpanel(IzPanel parent) {
        this.parent =parent;
    }
    boolean send() {
        boolean retVal = false;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = new sun.net.www.protocol.http.HttpURLConnection(url,null,0);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.connect();

            PrintWriter out = new PrintWriter( connection.getOutputStream());
            out.println(encode());
            connection.disconnect();

//        	BufferedReader in = new BufferedReader(
//				new InputStreamReader(url.openStream()));
//        	String inputLine = in.readLine();
//            if (null != inputLine) {
//                JOptionPane.showMessageDialog(parent,inputLine) ;
//            }

//            in.close();
            retVal = true;
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog( parent, ex.getMessage());
        }
        return retVal;
    }
    private String encode() {
        StringBuffer retVal = new StringBuffer();

        retVal.append(URLEncoder.encode("product"));
        retVal.append("=");
        retVal.append(URLEncoder.encode(idata.info.getAppName()));

        retVal.append("&");
        retVal.append(URLEncoder.encode("version"));
        retVal.append("=");
        retVal.append(URLEncoder.encode(idata.info.getAppVersion()));

        EnrollItem item = null;
        for (int i = 0; i < idata.enrollInfo.enrollItems.size(); i++) {
            item = (EnrollItem)idata.enrollInfo.enrollItems.get(i);
            retVal.append("&");
            retVal.append(URLEncoder.encode(item.name));
            retVal.append("=");
            retVal.append(URLEncoder.encode(item.value));
        }

        return retVal.toString();
    }

  }
    //.....................................................................

}

