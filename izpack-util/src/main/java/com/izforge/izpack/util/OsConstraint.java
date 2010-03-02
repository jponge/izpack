/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Olexij Tkatchenko
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

package com.izforge.izpack.util;


import com.izforge.izpack.api.data.binding.OsModel;


/**
 * Encapsulates OS constraints specified on creation time and allows to check them against the
 * current OS.
 * <p/>
 * For example, this is used for &lt;executable&gt;s to check whether the executable is suitable for
 * the current OS.
 *
 * @author Olexij Tkatchenko <ot@parcs.de>
 */
public class OsConstraint
        implements java.io.Serializable
{
    //~ Static variables/initializers 

    /**
     *
     */
    private static final long serialVersionUID = 3762248660406450488L;

    //~ Instance variables 

    public OsModel osModel;

    public OsConstraint(OsModel osModel)
    {
        this.osModel = osModel;
    }

    public OsConstraint(String family,
                        String name,
                        String version,
                        String arch,
                        String jre)
    {
        new OsModel(arch, family, jre, name, version);
    }


    /**
     * Creates a new instance. Please remember, MacOSX belongs to Unix family.
     *
     * @param family  The OS family (unix, windows or mac).
     * @param name    The exact OS name.
     * @param version The exact OS version (check property <code>os.version</code> for values).
     * @param arch    The machine architecture (check property <code>os.arch</code> for values).
     */
    public OsConstraint(String family,
                        String name,
                        String version,
                        String arch)
    {
        this(family, name, version, arch, null);
    }

    public String getFamily()
    {
        return osModel.getFamily();
    }

    public String getName()
    {
        return osModel.getName();
    }


    public String toString()
    {
        return osModel.toString();
    }
}
