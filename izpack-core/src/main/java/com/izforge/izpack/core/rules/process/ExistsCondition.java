/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Dennis Reil
 * Copyright 2010 Rene Krell
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

package com.izforge.izpack.core.rules.process;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.core.substitutor.VariableSubstitutorBase;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.util.Debug;

/**
 * This condition checks if a certain variable has a value. If it is not
 * in the current list of variables it will evaluate to false.
 *
 * @author Dennis Reil,<izpack@reil-online.de>
 */
public class ExistsCondition extends Condition
{
    private static final long serialVersionUID = -7424383017678759732L;

    private ContentType contentType;
    private String content;

    public ExistsCondition() {}
    public ExistsCondition(ContentType contentType) { this.contentType = contentType; }

    @Override
    public boolean isTrue()
    {
        boolean result = false;
        switch (contentType)
        {
            case VARIABLE:
                if (this.content != null)
                {
                    String value = this.getInstalldata().getVariable(this.content);
                    if (value != null)
                    {
                        result = true;
                    }
                }
                break;

            case FILE:
                if (this.content != null)
                {
                    VariableSubstitutorBase subst = new VariableSubstitutorImpl(this.getInstalldata().getVariables());
                    File file = new File(subst.substitute(this.content));
                    if (file.exists())
                    {
                        result = true;
                    }
                }
                break;

            default:
                Debug.log("Illegal content type '"+contentType.getAttribute()+"' of ExistsCondition");
                break;
        }
        return result;
    }

    @Override
    public void readFromXML(IXMLElement xmlcondition)
    {
        if (xmlcondition != null)
        {
            if (xmlcondition.getChildrenCount() != 1)
            {
                Debug.error("ExistsCondition needs exactly one nested element");
            }
            IXMLElement child = xmlcondition.getChildAtIndex(0);
            this.contentType = ContentType.getFromAttribute(child.getName());
            if (this.contentType != null)
            {
                this.content = child.getContent();
            }
            else
            {
                Debug.error("Unknown nested element '"+child.getName()+"' to ExistsCondition");
            }
            if (this.content == null || this.content.length() == 0)
            {
                Debug.error("ExistsCondition has a nested element without valid contents");
            }
        }
    }

    public ContentType getContentType()
    {
        return contentType;
    }


    public void setContentType(ContentType contentType)
    {
        this.contentType = contentType;
    }


    public String getContent()
    {
        return content;
    }


    public void setContent(String content)
    {
        this.content = content;
    }

    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        XMLElementImpl el = new XMLElementImpl(this.contentType.getAttribute(), conditionRoot);
        el.setContent(this.content);
        conditionRoot.addChild(el);
    }

    public enum ContentType
    {
        VARIABLE("variable"), FILE("file");

        private static Map<String, ContentType> lookup;

        private String attribute;

        ContentType(String attribute)
        {
            this.attribute = attribute;
        }

        static
        {
            lookup = new HashMap<String, ContentType>();
            for (ContentType operation : EnumSet.allOf(ContentType.class))
            {
                lookup.put(operation.getAttribute(), operation);
            }
        }

        public String getAttribute()
        {
            return attribute;
        }

        public static ContentType getFromAttribute(String attribute)
        {
            if (attribute != null && lookup.containsKey(attribute))
            {
                return lookup.get(attribute);
            }
            return null;
        }
    }
}
