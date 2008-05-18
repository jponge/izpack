/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2004 Jan Blok
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
package com.izforge.izpack;

import com.izforge.izpack.util.OsConstraint;

import java.io.Serializable;
import java.util.List;

/**
 * @author Jan Blok
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class Panel implements Serializable
{

    static final long serialVersionUID = 8886445274940938809L;

    /**
     * The panel classname.
     */
    public String className;

    /**
     * The target operation system of this panel
     */
    public List<OsConstraint> osConstraints = null;

    /**
     * the unique id of this panel
     */
    protected String panelid;

    /**
     * condition for this panel
     */
    private String condition = null;

    public String getClassName()
    {
        return this.className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public List<OsConstraint> getOsConstraints()
    {
        return this.osConstraints;
    }

    public void setOsConstraints(List<OsConstraint> osConstraints)
    {
        this.osConstraints = osConstraints;
    }

    public String getPanelid()
    {
        if (this.panelid == null)
        {
            this.panelid = "UNKNOWN (" + className + ")";
        }
        return this.panelid;
    }

    public void setPanelid(String panelid)
    {
        this.panelid = panelid;
    }


    /**
     * @return the condition
     */
    public String getCondition()
    {
        return this.condition;
    }


    /**
     * @param condition the condition to set
     */
    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public boolean hasCondition()
    {
        return this.condition != null;
    }
}
