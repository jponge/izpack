/*
* IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
*
* http://izpack.org/
* http://izpack.codehaus.org/
*
* Copyright (c) 2008, 2009 Anthonin Bonnefoy
* Copyright (c) 2008, 2009 David Duponchel
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

package com.izforge.izpack.adaptator.impl;

import com.izforge.izpack.adaptator.IXMLElement;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Implementation of the adaptator between nanoXml and javax
 *
 * @author Anthonin Bonnefoy
 * @author David Duponchel
 */
public class XMLElementImpl implements IXMLElement
{

    /**
     * The dom element embedded by the XMLElement
     */
    private Element element;

    /**
     * A flag to notice any changement made to the element.
     * It is used to generate the childrenVector.
     */
    private boolean hasChanged = true;

    /**
     * Vector of the children elements.
     * It is generated as it is called.
     */
    private Vector<IXMLElement> childrenVector;

    /**
     * Create a new root element in a new document.
     *
     * @param name Name of the root element
     */
    public XMLElementImpl(String name)
    {
        Document document;
        try
        {
            // Création d'un nouveau DOM
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder constructeur = documentFactory.newDocumentBuilder();
            document = constructeur.newDocument();
            // Propriétés du DOM
            document.setXmlVersion("1.0");
            element = document.createElement(name);
            document.appendChild(element);
        } catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Constructor which create a root element in the given document
     *
     * @param name       Name of the root element
     * @param inDocument The document in which to create the xml
     */
    public XMLElementImpl(String name, Document inDocument)
    {
        element = inDocument.createElement(name);
    }

    /**
     * Create a element in the same document of the given element
     *
     * @param name             Name of the element
     * @param elementReference Reference of an existing xml. It is used to generate an xmlElement on the same document.
     */
    public XMLElementImpl(String name, IXMLElement elementReference)
    {
        element = elementReference.getElement().getOwnerDocument().createElement(name);
    }

    /**
     * Constructor saving the passed node
     *
     * @param node Node to save inside the XMLElement
     */
    public XMLElementImpl(Node node)
    {
        if (!(node instanceof Element))
        {
            throw new IllegalArgumentException("The node should be an instance of Element");
        }
        this.element = (Element) node;
    }

    public String getName()
    {
        return element.getNodeName();
    }

    public void addChild(IXMLElement child)
    {
        hasChanged = true;
        element.appendChild(child.getElement());
    }

    public void removeChild(IXMLElement child)
    {
        hasChanged = true;
        element.removeChild(child.getElement());
    }

    public boolean hasChildren()
    {
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (child.getNodeType() == Node.ELEMENT_NODE)
            {
                return true;
            }
        }
        return false;
    }

    private void initChildrenVector()
    {
        if (hasChanged)
        {
            hasChanged = false;
            childrenVector = new Vector<IXMLElement>();
            for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling())
            {
                if (child.getNodeType() == Node.ELEMENT_NODE)
                {
                    childrenVector.add(new XMLElementImpl(child));
                }
            }
        }
    }

    public int getChildrenCount()
    {
        initChildrenVector();
        return childrenVector.size();
    }

    public Vector<IXMLElement> getChildren()
    {
        initChildrenVector();
        return childrenVector;
    }

    public IXMLElement getChildAtIndex(int index)
    {
        initChildrenVector();
        return childrenVector.get(index);
    }

    public IXMLElement getFirstChildNamed(String name)
    {
        XMLElementImpl res = null;
        NodeList nodeList = element.getElementsByTagName(name);
        if (nodeList.getLength() > 0)
        {
            res = new XMLElementImpl(nodeList.item(0));
        }
        return res;
    }

    public Vector<IXMLElement> getChildrenNamed(String name)
    {
        Vector<IXMLElement> res = new Vector<IXMLElement>();
        Vector<IXMLElement> children = getChildren();
        for (int i = 0; i < children.size(); i++)
        {
            IXMLElement child = children.elementAt(i);
            if (child.getName()!= null && child.getName().equals(name))
            {
                res.add(new XMLElementImpl(child.getElement()));
            }
        }
        return res;
    }

    public String getAttribute(String name)
    {
        return this.getAttribute(name, null);
    }

    public String getAttribute(String name, String defaultValue)
    {
        Node attribute = element.getAttributes().getNamedItem(name);
        if (attribute != null)
        {
            return attribute.getNodeValue();
        }
        return defaultValue;
    }

    public void setAttribute(String name, String value)
    {
        NamedNodeMap attributes = element.getAttributes();
        Attr attribute = element.getOwnerDocument().createAttribute(name);
        attribute.setValue(value);
        attributes.setNamedItem(attribute);
    }

    public void removeAttribute(String name)
    {
        this.element.getAttributes().removeNamedItem(name);
    }

    public Enumeration enumerateAttributeNames()
    {
        NamedNodeMap namedNodeMap = element.getAttributes();
        Properties properties = new Properties();
        for (int i = 0; i < namedNodeMap.getLength(); i++)
        {
            Node node = namedNodeMap.item(i);
            properties.put(node.getNodeName(), node.getNodeValue());
        }
        return properties.keys();
    }

    public boolean hasAttribute(String name)
    {
        return (this.element.getAttributes().getNamedItem(name) != null);
    }

    public Properties getAttributes()
    {
        Properties properties = new Properties();
        NamedNodeMap namedNodeMap = this.element.getAttributes();
        for (int i = 0; i < namedNodeMap.getLength(); ++i)
        {
            properties.put(namedNodeMap.item(i).getNodeName(), namedNodeMap.item(i).getNodeValue());
        }
        return properties;
    }

    public int getLineNr()
    {
        Object ln = element.getUserData("ln");
        if (ln == null)
        {
            return NO_LINE;
        }
        try
        {
            return (Integer) element.getUserData("ln");
        }
        catch (ClassCastException e)
        {
            return NO_LINE;
        }
    }

    public String getContent()
    {
        StringBuilder sb = new StringBuilder();
        String content;
        Node child = this.element.getFirstChild();

        // no error if there are children
        boolean err = (child == null);

        // pattern : only whitespace characters
        Pattern p = Pattern.compile("^\\s+$");

        while (!err && child != null)
        {
            content = child.getNodeValue();
            if (child.getNodeType() == Node.TEXT_NODE)
            {
                // text node : nanoXML ignores it if it's only whitespace characters.
                if (content != null && !p.matcher(content).matches())
                {
                    sb.append(content);
                }
            }
            else if (child.getNodeType() == Node.CDATA_SECTION_NODE)
            {
                sb.append(content);
            }
            // neither CDATA nor text : real nested element !
            else
            {
                err = true;
            }
            child = child.getNextSibling();
        }
        return (err) ? null : sb.toString().trim();
    }

    public void setContent(String content)
    {
        Node child;
        while ((child = this.element.getFirstChild()) != null)
        {
            this.element.removeChild(child);
        }
        element.appendChild(element.getOwnerDocument().createTextNode(content));
    }

    public Node getElement()
    {
        return element;
    }

    @Override
    public String toString()
    {
        return element.getNodeName() + " " + element.getNodeValue();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof IXMLElement)
        {
            IXMLElement o = (IXMLElement) obj;
            Element elem = (Element) o.getElement();
            Node child2 = elem.getFirstChild();
            for (Node child = element.getFirstChild(); child != null && child2 != null; child = child.getNextSibling())
            {
                if (!child.equals(child2))
                {
                    return false;
                }
                child2.getNextSibling();
            }
            return true;
        }
        return false;
    }
}
