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

package com.izforge.izpack;

import com.izforge.izpack.compiler.PackInfo;
import com.izforge.izpack.util.OsConstraint;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a Pack.
 *
 * @author Julien Ponge
 */
public class Pack implements Serializable
{

    static final long serialVersionUID = -5458360562175088671L;

    private boolean hidden;
    
    /**
     * Flag for store files of this pack outside the installation jar file
     */
    public boolean loose;

    /**
     * If true, all files of the pack will be deleted during uninstallation, if
     * false they are only removed if uninstaller force delete option is
     * activated.
     */
    public boolean uninstall;

    /**
     * The pack name.
     */
    public String name;

    /**
     * The langpack id
     */
    public String id;

    /**
     * An association of this pack to zero or more installation groups. An
     * installation group is just a named collection of packs to allow for
     * different pack collections to be selected, for example: minimal,
     * default, all.
     */
    public Set<String> installGroups = new HashSet<String>();

    /**
     * All packs in the same excludeGroup are mutually exclusive. The excludeGroup
     * is a string and serves are key identifying each group of mutually
     * exclusive packs.
     */
    public String excludeGroup = "";

    /**
     * The group the pack is associated with. The pack group identifies
     * packs with common functionality to allow for grouping of packs in a
     * tree in the TargetPanel for example.
     */
    public String group;

    /**
     * The pack description.
     */
    public String description;

    /**
     * The target operation system of this pack
     */
    public List<OsConstraint> osConstraints = null;

    /**
     * Condition for this pack *
     */
    private String condition;

    /**
     * The list of packs this pack depends on
     */
    public List<String> dependencies = null;

    /**
     * Reverse dependencies(childs)
     */
    public List<String> revDependencies = null;

    /**
     * True if the pack is required.
     */
    public boolean required;

    /**
     * The number of bytes contained in the pack.
     */
    public long nbytes;

    /**
     * Whether this pack is suggested (preselected for installation).
     */
    public boolean preselected;

    /**
     * Parent pack name to display it in the TreePacksPanel (optional)
     */
    public String parent;

    /**
     * The color of the node. This is used for the dependency graph algorithms
     */
    public int color;

    /**
     * The id to use if we want to obtain this pack's image resource
     */
    public String packImgId;

    private List<String> validators = new ArrayList<String>();

    /**
     * white colour
     */
    public final static int WHITE = 0;

    /**
     * grey colour
     */
    public final static int GREY = 1;

    /**
     * black colour
     */
    public final static int BLACK = 2;

    /**
     * The constructor.
     *
     * @param name          The pack name.
     * @param id            The id of the pack which is used e.g. for I18N
     * @param description   The pack description.
     * @param osConstraints the OS constraint (or null for any OS)
     * @param dependencies  dependencies of this pack
     * @param required      Indicates wether the pack is required or not.
     * @param preselected   This pack will be selected automatically.
     * @param loose         Flag for store files of this pack outside the installation jar file
     * @param excludegroup  associated exclude group
     * @param uninstall     If true, pack must be uninstalled.
     */
    public Pack(String name, String id, String description, List<OsConstraint> osConstraints, List<String> dependencies,
                boolean required, boolean preselected, boolean loose, String excludegroup, boolean uninstall)
    {
        this.name = name;
        this.id = id;
        this.description = description;
        this.osConstraints = osConstraints;
        this.dependencies = dependencies;
        this.required = required;
        this.preselected = preselected;
        this.loose = loose;
        this.excludeGroup = excludegroup;
        this.uninstall = uninstall;
        this.packImgId = null;
        this.condition = null;
        nbytes = 0;
        color = PackInfo.WHITE;
    }

    /**
     * To a String (usefull for JLists).
     *
     * @return The String representation of the pack.
     */
    public String toString()
    {
        return name + " (" + description + ")";
    }

    /**
     * getter method for dependencies
     *
     * @return the dependencies list
     */
    public List<String> getDependencies()
    {
        return dependencies;
    }


    /**
     * This adds a reverse dependency. With a reverse dependency we imply a child dependency or the
     * dependents on this pack
     *
     * @param name0 The name of the pack that depents to this pack
     */
    public void addRevDep(String name0)
    {
        if (revDependencies == null)
        {
            revDependencies = new ArrayList<String>();
        }
        revDependencies.add(name0);
    }

    /**
     * Creates a text list of all the packs it depend on
     *
     * @return the created text
     */
    public String depString()
    {
        String text = "";
        if (dependencies == null)
        {
            return text;
        }
        String name0;
        for (int i = 0; i < dependencies.size() - 1; i++)
        {
            name0 = dependencies.get(i);
            text += name0 + ",";
        }
        name0 = dependencies.get(dependencies.size() - 1);
        text += name0;
        return text;

    }

    /**
     * Used of conversions.
     */
    private final static double KILOBYTES = 1024.0;

    /**
     * Used of conversions.
     */
    private final static double MEGABYTES = 1024.0 * 1024.0;

    /**
     * Used of conversions.
     */
    private final static double GIGABYTES = 1024.0 * 1024.0 * 1024.0;

    /**
     * Used of conversions.
     */
    private final static DecimalFormat formatter = new DecimalFormat("#,###.##");

    /**
     * Convert bytes into appropiate mesaurements.
     *
     * @param bytes A number of bytes to convert to a String.
     * @return The String-converted value.
     */
    public static String toByteUnitsString(long bytes)
    {
        if (bytes < KILOBYTES)
        {
            return String.valueOf(bytes) + " bytes";
        }
        else if (bytes < (MEGABYTES))
        {
            double value = bytes / KILOBYTES;
            return formatter.format(value) + " KB";
        }
        else if (bytes < (GIGABYTES))
        {
            double value = bytes / MEGABYTES;
            return formatter.format(value) + " MB";
        }
        else
        {
            double value = bytes / GIGABYTES;
            return formatter.format(value) + " GB";
        }
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
    
    public void addValidator(String validatorClassName)
    {
        validators.add(validatorClassName);
    }
    
    public List<String> getValidators()
    {
        return validators;
    }

    
    public boolean isHidden()
    {
        return hidden;
    }

    
    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }
}
