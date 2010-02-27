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


import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.binding.OsModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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

    private final OsModel osModel = new OsModel();

    //~ Constructors 

    /**
     * Constructs a new instance. Please remember, MacOSX belongs to Unix family.
     *
     * @param family  The OS family (unix, windows or mac).
     * @param name    The exact OS name.
     * @param version The exact OS version (check property <code>os.version</code> for values).
     * @param arch    The machine architecture (check property <code>os.arch</code> for values).
     * @param jre     The Java version used for installation (check property <code>java.version</code> for values).
     */
    public OsConstraint(String family,
                        String name,
                        String version,
                        String arch,
                        String jre)
    {
        this.osModel.setFamily((family != null)
                ? family.toLowerCase()
                : null);
        this.osModel.setName((name != null)
                ? name.toLowerCase()
                : null);
        this.osModel.setVersion((version != null)
                ? version.toLowerCase()
                : null);
        this.osModel.setArch((arch != null)
                ? arch.toLowerCase()
                : null);
        this.osModel.setJre((jre != null)
                ? jre.toLowerCase()
                : null);
    }    // end OsConstraint()


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
    }    // end OsConstraint()

    //~ Methods 

    /**
     * Matches OS specification in this class against current system properties.
     *
     * @return Description of the Return Value
     */
    public boolean matchCurrentSystem()
    {
        boolean match = true;
        String osName = System.getProperty("os.name").toLowerCase();


        if ((osModel.getArch() != null) && (osModel.getArch().length() != 0))
        {
            match = System.getProperty("os.arch").toLowerCase().equals(osModel.getArch());
        }    // end if

        if (match && (osModel.getVersion() != null) && (osModel.getVersion().length() != 0))
        {
            match = System.getProperty("os.version").toLowerCase().equals(osModel.getVersion());
        }    // end if

        if (match && (osModel.getName() != null) && (osModel.getName().length() != 0))
        {
            match = osName.equals(osModel.getName());
        }    // end if

        if (match && (osModel.getFamily() != null))
        {
            if ("windows".equals(osModel.getFamily()))
            {
                match = OsVersion.IS_WINDOWS;
            }    // end if
            else if ("mac".equals(osModel.getFamily()) || "osx".equals(osModel.getFamily()))
            {
                match = OsVersion.IS_OSX;
            }    // end else if
            else if ("unix".equals(osModel.getFamily()))
            {
                match = OsVersion.IS_UNIX;
            }    // end else if
        }    // end if

        if (match && (osModel.getJre() != null) && (osModel.getJre().length() > 0))
        {
            match = System.getProperty("java.version").toLowerCase().startsWith(osModel.getJre());
        }    // end if

        return match
                && ((osModel.getFamily() != null) || (osModel.getName() != null) || (osModel.getVersion() != null) || (osModel.getArch() != null) || (osModel.getJre() != null));
    }    // end matchCurrentSystem()


    /**
     * Extract a list of OS constraints from given element.
     *
     * @param element parent IXMLElement
     * @return List of OsConstraint (or empty List if no constraints found)
     */
    public static List<OsConstraint> getOsList(IXMLElement element)
    {
        // get os info on this executable
        ArrayList<OsConstraint> osList = new ArrayList<OsConstraint>();
        for (IXMLElement osElement : element.getChildrenNamed("os"))
        {
            osList.add(new OsConstraint(osElement.getAttribute("family",
                    null),
                    osElement.getAttribute("name",
                            null),
                    osElement.getAttribute("version",
                            null),
                    osElement.getAttribute("arch",
                            null),
                    osElement.getAttribute("jre",
                            null)));
        }
        // backward compatibility: still support os attribute
        String osattr = element.getAttribute("os");
        if ((osattr != null) && (osattr.length() > 0))
        {
            // add the "os" attribute as a family constraint
            osList.add(new OsConstraint(osattr,
                    null,
                    null,
                    null,
                    null));
        }    // end if

        return osList;
    }    // end getOsList()


    /**
     * Helper function: Scan a list of OsConstraints for a match.
     *
     * @param constraint_list List of OsConstraint to check
     * @return true if one of the OsConstraints matched the current system or constraint_list is
     *         null (no constraints), false if none of the OsConstraints matched
     */
    public static boolean oneMatchesCurrentSystem(List<OsConstraint> constraint_list)
    {
        if (constraint_list == null)
        {
            return true;
        }    // end if

        Iterator<OsConstraint> constraint_it = constraint_list.iterator();

        // no constraints at all - matches!
        if (!constraint_it.hasNext())
        {
            return true;
        }    // end if

        while (constraint_it.hasNext())
        {
            OsConstraint osc = constraint_it.next();


            Debug.trace("checking if os constraints " + osc + " match current OS");

            // check for match
            if (osc.matchCurrentSystem())
            {
                Debug.trace("matched current OS.");

                return true;    // bail out on first match
            }    // end if
        }    // end while

        Debug.trace("no match with current OS!");

        // no match found
        return false;
    }    // end oneMatchesCurrentSystem()


    /**
     * Helper function: Check whether the given IXMLElement is "suitable" for the current OS.
     *
     * @param el The IXMLElement to check for OS constraints.
     * @return true if there were no OS constraints or the constraints matched the current OS.
     */
    public static boolean oneMatchesCurrentSystem(IXMLElement el)
    {
        return oneMatchesCurrentSystem(getOsList(el));
    }    // end oneMatchesCurrentSystem()


    public String getFamily()
    {
        return osModel.getFamily();
    }    // end getFamily()


    public void setName(String n)
    {
        osModel.setName(n);
    }    // end setName()


    public String getName()
    {
        return osModel.getName();
    }    // end getName()


    public String toString()
    {


        return osModel.toString();
    }    // end toString()
}    // end OsConstraint
