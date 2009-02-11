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


import com.izforge.izpack.adaptator.IXMLElement;

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

    /**
     * OS architecture from java system properties
     */
    private String arch;

    /**
     * The OS family
     */
    private String family;

    /**
     * JRE version used for installation
     */
    private String jre;

    /**
     * OS name from java system properties
     */
    private String name;

    /**
     * OS version from java system properties
     */
    private String version;

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
        this.family = (family != null)
                ? family.toLowerCase()
                : null;
        this.name = (name != null)
                ? name.toLowerCase()
                : null;
        this.version = (version != null)
                ? version.toLowerCase()
                : null;
        this.arch = (arch != null)
                ? arch.toLowerCase()
                : null;
        this.jre = (jre != null)
                ? jre.toLowerCase()
                : null;
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


        if ((arch != null) && (arch.length() != 0))
        {
            match = System.getProperty("os.arch").toLowerCase().equals(arch);
        }    // end if

        if (match && (version != null) && (version.length() != 0))
        {
            match = System.getProperty("os.version").toLowerCase().equals(version);
        }    // end if

        if (match && (name != null) && (name.length() != 0))
        {
            match = osName.equals(name);
        }    // end if

        if (match && (family != null))
        {
            if ("windows".equals(family))
            {
                match = OsVersion.IS_WINDOWS;
            }    // end if
            else if ("mac".equals(family) || "osx".equals(family))
            {
                match = OsVersion.IS_OSX;
            }    // end else if
            else if ("unix".equals(family))
            {
                match = OsVersion.IS_UNIX;
            }    // end else if
        }    // end if

        if (match && (jre != null) && (jre.length() > 0))
        {
            match = System.getProperty("java.version").toLowerCase().startsWith(jre);
        }    // end if

        return match
                && ((family != null) || (name != null) || (version != null) || (arch != null) || (jre != null));
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
        Iterator<IXMLElement> osIterator = element.getChildrenNamed("os").iterator();


        while (osIterator.hasNext())
        {
            IXMLElement os = osIterator.next();


            osList.add(new OsConstraint(os.getAttribute("family",
                    null),
                    os.getAttribute("name",
                            null),
                    os.getAttribute("version",
                            null),
                    os.getAttribute("arch",
                            null),
                    os.getAttribute("jre",
                            null)));
        }    // end while

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


    public void setFamily(String f)
    {
        family = f.toLowerCase();
    }    // end setFamily()


    public String getFamily()
    {
        return family;
    }    // end getFamily()


    public void setName(String n)
    {
        name = n.toLowerCase();
    }    // end setName()


    public String getName()
    {
        return name;
    }    // end getName()

    public void setVersion(String v)
    {
        version = v.toLowerCase();
    }    // end setVersion()


    public String getVersion()
    {
        return version;
    }    // end getVersion()


    public void setArch(String a)
    {
        arch = a.toLowerCase();
    }    // end setArch()


    public String getArch()
    {
        return arch;
    }    // end getArch()

    public String toString()
    {
        StringBuffer retval = new StringBuffer();


        retval.append("[Os ");
        retval.append(" family ").append(family);
        retval.append(" name ").append(name);
        retval.append(" version ").append(version);
        retval.append(" arch ").append(arch);
        retval.append(" jre ").append(jre);
        retval.append(" ]");

        return retval.toString();
    }    // end toString()
}    // end OsConstraint
