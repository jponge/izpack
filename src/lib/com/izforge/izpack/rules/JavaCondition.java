/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007-2009 Dennis Reil
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
package com.izforge.izpack.rules;

import com.izforge.izpack.util.Debug;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A condition based on the value of a static java field or static java method.
 *
 * @author Dennis Reil, <izpack@reil-online.de>
 */
public class JavaCondition extends Condition
{
    /**
     *
     */
    private static final long serialVersionUID = -7649870719815066537L;
    protected String classname;
    protected String methodname;
    protected String fieldname;
    protected boolean complete;
    protected String returnvalue;
    protected String returnvaluetype;


    protected Class usedclass;
    protected Field usedfield;
    protected Method usedmethod;

    public JavaCondition()
    {

    }

    public boolean isTrue()
    {
        if (!this.complete)
        {
            return false;
        }
        else
        {
            if (this.usedclass == null)
            {
                try
                {
                    this.usedclass = Class.forName(this.classname); 
                }
                catch (ClassNotFoundException e)
                {
                    Debug.log("Can't find class " + this.classname);
                    return false;
                }
            }
            if ((this.usedfield == null) && (this.fieldname != null))
            {
                try
                {
                    this.usedfield = this.usedclass.getField(this.fieldname);
                }
                catch (SecurityException e)
                {
                    Debug.log("No permission to access specified field: " + this.fieldname);
                    return false;
                }
                catch (NoSuchFieldException e)
                {
                    Debug.log("No such field: " + this.fieldname);
                    return false;
                }
            }
            if ((this.usedmethod == null) && (this.methodname != null))
            {
                Debug.log("not implemented yet.");
                return false;
            }

            if (this.usedfield != null)
            {
                // access field
                if ("boolean".equals(this.returnvaluetype))
                {
                    try
                    {
                        boolean returnval = this.usedfield.getBoolean(null);
                        boolean expectedreturnval = Boolean.valueOf(this.returnvalue);
                        return returnval == expectedreturnval;
                    }
                    catch (IllegalArgumentException e)
                    {
                        Debug.log("IllegalArgumentexeption " + this.fieldname);
                    }
                    catch (IllegalAccessException e)
                    {
                        Debug.log("IllegalAccessException " + this.fieldname);
                    }
                }
                else
                {
                    Debug.log("not implemented yet.");
                    return false;
                }
            }
            return false;
        }
    }

    public void readFromXML(IXMLElement xmlcondition)
    {
        if (xmlcondition.getChildrenCount() != 2)
        {
            Debug.log("Condition of type java needs (java,returnvalue)");
            return;
        }
        IXMLElement javael = xmlcondition.getFirstChildNamed("java");
        IXMLElement classel = javael.getFirstChildNamed("class");
        if (classel != null)
        {
            this.classname = classel.getContent();
        }
        else
        {
            Debug.log("Java-Element needs (class,method?,field?)");
            return;
        }
        IXMLElement methodel = javael.getFirstChildNamed("method");
        if (methodel != null)
        {
            this.methodname = methodel.getContent();
        }
        IXMLElement fieldel = javael.getFirstChildNamed("field");
        if (fieldel != null)
        {
            this.fieldname = fieldel.getContent();
        }
        if ((this.methodname == null) && (this.fieldname == null))
        {
            Debug.log("java element needs (class, method?,field?)");
            return;
        }
        IXMLElement returnvalel = xmlcondition.getFirstChildNamed("returnvalue");
        if (returnvalel != null)
        {
            this.returnvalue = returnvalel.getContent();
            this.returnvaluetype = returnvalel.getAttribute("type");
        }
        else
        {
            Debug.log("no returnvalue-element specified.");
            return;
        }
        this.complete = true;
    }
    
    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        XMLElementImpl javael = new XMLElementImpl("java",conditionRoot);
        conditionRoot.addChild(javael);
        XMLElementImpl classel = new XMLElementImpl("class",javael);
        classel.setContent(this.classname);
        javael.addChild(classel);
        if (this.methodname != null){
            XMLElementImpl methodel = new XMLElementImpl("method",javael);
            methodel.setContent(this.methodname);
            javael.addChild(methodel);    
        }
        if (this.fieldname != null){
            XMLElementImpl fieldel = new XMLElementImpl("field",javael);
            fieldel.setContent(this.fieldname);
            javael.addChild(fieldel);    
        }        
        XMLElementImpl returnvalel = new XMLElementImpl("returnvalue",javael);
        returnvalel.setContent(this.returnvalue);
        returnvalel.setAttribute("type", this.returnvaluetype);
        javael.addChild(returnvalel);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.rules.Condition#getDependenciesDetails()
     */
    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(this.id);
        details.append(" depends on the ");
        if (this.fieldname != null)
        {
            details.append("value of field <b>");
            details.append(this.fieldname);
            details.append("</b>");
        }
        else
        {
            details.append("return value of method <b>");
            details.append(this.methodname);
            details.append("</b>");
        }
        details.append(" on an instance of class <b>");
        details.append(this.classname);
        details.append("</b> which should be <b>");
        details.append(this.returnvalue);
        details.append("</b><br/>");
        return details.toString();
    }

    

}
