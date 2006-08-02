/*
 * $Id:$
 * IzPack - Copyright 2001-2006 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2006 Klaus Bartz
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
package com.izforge.izpack.installer;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager2;

import com.izforge.izpack.gui.IzPanelConstraints;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LayoutConstants;
import com.izforge.izpack.installer.IzPanel.Filler;

/**
 * This class manages the layout for IzPanels. The layout related methods in IzPanel delegates the
 * work to this class. Use the layout helper directly because the delegating methods in IzPanel will
 * be removed in the future.<br>
 * This layout helper works with a GridBagLayout or a IzPanelLayout as layout manager. The layout
 * manager has to be set at calling the method <code>startLayout</code>. This method has to be
 * called before the first add of a component to the IzPanel.<br>
 * 
 * 
 * @author Klaus Bartz
 * 
 */
public class LayoutHelper implements LayoutConstants
{

    IzPanel parent;

    /** Indicates whether grid bag layout was started or not */
    protected boolean layoutStarted = false;

    /** The default grid bag constraint. */
    protected Object defaultConstraints;

    /** Current x position of grid. */
    protected int gridxCounter = -1;

    /** Current y position of grid. */
    protected int gridyCounter = -1;

    /** internal layout */
    protected LayoutManager2 izPanelLayout;

    /**
     * Layout anchor declared in the xml file with the guiprefs modifier "layoutAnchor"
     */
    protected static int ANCHOR = -1;

    protected static int X_STRETCH_TYPE = -1;

    /**
     * Look-up table for gap identifier to gap names. The gap names can be used in the XML
     * installation configuration file. Be aware that case sensitivity should be used.
     */
    public final static String[] GAP_NAME_LOOK_UP = { "noGap", "labelGap", "paragraphGap",
            "textGab", "controlGap", "labelToTextGap", "labelToControlGap", "textToLabelGap",
            "controlToLabelGap", "controlToTextGap", "textToControlGap", "topGap"};

    /**
     * Current defined gaps. Here are the defaults which can be overwritten at the first call to
     * method getGap. The gap type will be determined by the array index and has to be synchron to
     * the gap identifier and the indices of array GAP_NAME_LOOK_UP
     */
    protected static int[] GAPS = { 0, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, -1, 0, -1};

    public LayoutHelper(IzPanel parent)
    {
        this.parent = parent;
        izPanelLayout = new GridBagLayout();
        parent.setLayout(izPanelLayout);
        gridyCounter++;
    }

    private boolean isGridBag()
    {
        return (izPanelLayout instanceof GridBagLayout);
    }

    private boolean isIzPanel()
    {
        return (izPanelLayout instanceof IzPanelLayout);
    }

    // ----------------------------------------------------------------------
    public void add(Component comp)
    {

    }

    // ------------------- Common Layout stuff -------------------- START ---

    /**
     * Start layout determining. If it is needed, a dummy component will be created as first row.
     * This will be done, if the IzPack guiprefs modifier with the key "layoutAnchor" has the value
     * "SOUTH" or "SOUTHWEST". The earlier used value "BOTTOM" and the declaration via the IzPack
     * variable <code>IzPanel.LayoutType</code> are also supported.
     */
    public void startLayout(LayoutManager2 layout)
    {
        if (layoutStarted) return;
        izPanelLayout = layout;
        if (isGridBag())
        {
            startGridBagLayout();
            return;
        }
        // TODO: impl for IzPanelLayout
        if (isIzPanel()) startIzPanelLayout();
    }

    private void startIzPanelLayout()
    {
        IzPanelLayout.setAnchor(getAnchor());
        IzPanelLayout.setXStretchType(getXStretchType());
        parent.setLayout(izPanelLayout);

    }

    /**
     * Complete layout determining. If it is needed, a dummy component will be created as last row.
     * This will be done, if the IzPack guiprefs modifier with the key "layoutAnchor" has the value
     * "NORTH" or "NORTHWEST". The earlier used value "TOP" and the declaration via the IzPack
     * variable <code>IzPanel.LayoutType</code> are also supported.
     */
    public void completeLayout()
    {
        if (isGridBag())
        {
            completeGridBagLayout();
            return;
        }
        // TODO: impl for IzPanelLayout
    }

    /**
     * Returns the default constraints of this panel.
     * 
     * @return the default constraints of this panel
     */
    public Object getDefaultConstraints()
    {
        startLayout(izPanelLayout);
        return defaultConstraints;
    }

    /**
     * Sets the default constraints of this panel to the given object.
     * 
     * @param constraints which should be set as default for this object
     */
    public void setDefaultConstraints(Object constraints)
    {

        startLayout(izPanelLayout);
        if ((isGridBag() && !(constraints instanceof GridBagConstraints))
                || (isIzPanel() && !(constraints instanceof IzPanelConstraints)))
            throw new IllegalArgumentException(
                    "Layout and constraints have to be from the same type.");
        defaultConstraints = constraints;
    }

    /**
     * Resets the grid counters which are used at getNextXConstraints and getNextYConstraints.
     */
    public void resetGridCounter()
    {
        gridxCounter = -1;
        gridyCounter = -1;
    }

    /**
     * Returns a newly created constraints with the given values and the values from the default
     * constraints for the other parameters.
     * 
     * @param gridx value to be used for the new constraint
     * @param gridy value to be used for the new constraint
     * @return newly created constraints with the given values and the values from the default
     * constraints for the other parameters
     */
    public Object getNewConstraints(int gridx, int gridy)
    {
        if (isGridBag())
        {
            GridBagConstraints retval = (GridBagConstraints) ((GridBagConstraints) getDefaultConstraints())
                    .clone();
            retval.gridx = gridx;
            retval.gridy = gridy;
            return (retval);
        }
        if (isIzPanel())
        {
            IzPanelConstraints retval = (IzPanelConstraints) ((IzPanelConstraints) getDefaultConstraints())
                    .clone();
            retval.setXPos(gridx);
            retval.setYPos(gridy);
            return (retval);
        }
        return (null);
    }

    /**
     * Returns a newly created constraints with the given values and the values from the
     * defaultGridBagConstraints for the other parameters.
     * 
     * @param gridx value to be used for the new constraint
     * @param gridy value to be used for the new constraint
     * @param gridwidth value to be used for the new constraint
     * @param gridheight value to be used for the new constraint
     * @return newly created constraints with the given values and the values from the default
     * constraints for the other parameters
     */
    public Object getNewConstraints(int gridx, int gridy, int gridwidth, int gridheight)
    {
        Object retval = getNewConstraints(gridx, gridy);
        if (isGridBag())
        {
            GridBagConstraints gbc = (GridBagConstraints) retval;
            gbc.gridwidth = gridwidth;
            gbc.gridheight = gridheight;
        }
        if (isIzPanel())
        {
            IzPanelConstraints gbc = (IzPanelConstraints) retval;
            gbc.setXWeight(gridwidth);
            gbc.setYWeight(gridheight);
        }
        return (retval);
    }

    /**
     * Returns a newly created constraints for the next column of the current layout row.
     * 
     * @return a newly created constraints for the next column of the current layout row
     * 
     */
    public Object getNextXConstraints()
    {
        gridxCounter++;
        return (getNewConstraints(gridxCounter, gridyCounter));
    }

    /**
     * Returns a newly created constraints with column 0 for the next row.
     * 
     * @return a newly created constraints with column 0 for the next row
     * 
     */
    public Object getNextYConstraints()
    {
        gridyCounter++;
        gridxCounter = 0;
        return (getNewConstraints(0, gridyCounter));
    }

    /**
     * Returns a newly created constraints with column 0 for the next row using the given
     * parameters.
     * 
     * @param gridwidth width for this constraint
     * @param gridheight height for this constraint
     * @return a newly created constraints with column 0 for the next row using the given parameters
     */
    public Object getNextYConstraints(int gridwidth, int gridheight)
    {
        gridyCounter++;
        gridxCounter = 0;
        return (getNewConstraints(0, gridyCounter, gridwidth, gridheight));
    }

    // ------------------- Common Layout stuff -------------------- END ---

    // ------------------- GridBag Layout stuff -------------------- START ---
    /**
     * Start layout determining. If it is needed, a dummy component will be created as first row.
     * This will be done, if the IzPack guiprefs modifier with the key "layoutAnchor" has the value
     * "SOUTH" or "SOUTHWEST". The earlier used value "BOTTOM" and the declaration via the IzPack
     * variable <code>IzPanel.LayoutType</code> are also supported.
     */
    private void startGridBagLayout()
    {
        if (layoutStarted) return;
        layoutStarted = true;
        if (izPanelLayout == null || !(izPanelLayout instanceof GridBagLayout))
            izPanelLayout = new GridBagLayout();
        GridBagConstraints dgbc = new GridBagConstraints();
        dgbc.insets = new Insets(0, 0, getGap(LABEL_GAP), 0);
        dgbc.anchor = GridBagConstraints.WEST;
        defaultConstraints = dgbc;
        parent.setLayout(izPanelLayout);
        switch (getAnchor())
        {
        case SOUTH:
        case SOUTH_WEST:
            // Make a header to push the rest to the bottom.
            Filler dummy = new Filler();
            GridBagConstraints gbConstraint = (GridBagConstraints) getNextYConstraints();
            gbConstraint.weighty = 1.0;
            gbConstraint.fill = GridBagConstraints.BOTH;
            gbConstraint.anchor = GridBagConstraints.WEST;
            parent.add(dummy, gbConstraint);
            break;
        default:
            break;
        }
        // TODO: impl for layout type CENTER, ...
    }

    /**
     * Complete layout determining. If it is needed, a dummy component will be created as last row.
     * This will be done, if the IzPack guiprefs modifier with the key "layoutAnchor" has the value
     * "NORTH" or "NORTHWEST". The earlier used value "TOP" and the declaration via the IzPack
     * variable <code>IzPanel.LayoutType</code> are also supported.
     */
    private void completeGridBagLayout()
    {
        switch (getAnchor())
        {
        case NORTH:
        case NORTH_WEST:
            // Make a footer to push the rest to the top.
            Filler dummy = new Filler();
            GridBagConstraints gbConstraint = (GridBagConstraints) getNextYConstraints();
            gbConstraint.weighty = 1.0;
            gbConstraint.fill = GridBagConstraints.BOTH;
            gbConstraint.anchor = GridBagConstraints.WEST;
            parent.add(dummy, gbConstraint);
            break;
        default:
            break;
        }
    }

    /**
     * Returns the anchor as value declared in GridBagConstraints. Possible are NORTH, NORTHWEST,
     * SOUTH, SOUTHWEST and CENTER. The values can be configured in the xml description file with
     * the variable "IzPanel.LayoutType". The old values "TOP" and "BOTTOM" from the xml file are
     * mapped to NORTH and SOUTH.
     * 
     * @return the anchor defined in the IzPanel.LayoutType variable.
     */
    public static int getAnchor()
    {
        if (ANCHOR >= 0) return (ANCHOR);
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String todo;
        if (idata instanceof InstallData
                && ((InstallData) idata).guiPrefs.modifier.containsKey("layoutAnchor"))
            todo = (String) ((InstallData) idata).guiPrefs.modifier.get("layoutAnchor");
        else
            todo = idata.getVariable("IzPanel.LayoutType");
        if (todo == null) // No command, no work.
            ANCHOR = CENTER;
        else if ("EAST".equalsIgnoreCase(todo))
            ANCHOR = EAST;
        else if ("WEST".equalsIgnoreCase(todo))
            ANCHOR = WEST;
        else if ("TOP".equalsIgnoreCase(todo) || "NORTH".equalsIgnoreCase(todo))
            ANCHOR = NORTH;
        else if ("BOTTOM".equalsIgnoreCase(todo) || "SOUTH".equalsIgnoreCase(todo))
            ANCHOR = SOUTH;
        else if ("SOUTHWEST".equalsIgnoreCase(todo) || "SOUTH_WEST".equalsIgnoreCase(todo))
            ANCHOR = SOUTH_WEST;
        else if ("SOUTHEAST".equalsIgnoreCase(todo) || "SOUTH_EAST".equalsIgnoreCase(todo))
            ANCHOR = SOUTH_EAST;
        else if ("NORTHWEST".equalsIgnoreCase(todo) || "NORTH_WEST".equalsIgnoreCase(todo))
            ANCHOR = NORTH_WEST;
        else if ("NORTHEAST".equalsIgnoreCase(todo) || "NORTH_EAST".equalsIgnoreCase(todo))
            ANCHOR = NORTH_EAST;
        else if ("CENTER".equalsIgnoreCase(todo)) ANCHOR = CENTER;
        return (ANCHOR);
    }

    /**
     * Returns the gap which should be used between the given gui objects. The value will be
     * configurable by guiprefs modifiers. Valid values are all entries in the static String array
     * GAP_NAME_LOOK_UP of this class. There are constant ints for the indexes of this array.
     * 
     * @param gapId index in array GAP_NAME_LOOK_UP for the needed gap
     * 
     * @return the gap depend on the xml-configurable guiprefs modifier
     */
    public static int getGap(int gapId)
    {
        if (gapId < 0) gapId = -gapId;
        if (gapId >= GAPS.length - 2) throw new IllegalArgumentException("gapId out of range.");
        if (GAPS[GAPS.length - 1] >= 0) return (GAPS[gapId]);
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        if (!(idata instanceof InstallData)) return (GAPS[gapId]);
        String var = null;
        for (int i = 0; i < GAP_NAME_LOOK_UP.length; ++i)
        {
            if (((InstallData) idata).guiPrefs.modifier.containsKey(GAP_NAME_LOOK_UP[i]))
            {
                var = (String) ((InstallData) idata).guiPrefs.modifier.get(GAP_NAME_LOOK_UP[i]);
                if (var != null)
                {
                    try
                    {
                        GAPS[i] = Integer.parseInt(var);
                    }
                    catch (NumberFormatException nfe)
                    {
                        // Do nothing else use the default value.
                        // Need to set it again at this position??
                    }
                }
            }

        }
        GAPS[GAPS.length - 1] = 0; // Mark external settings allready loaded.
        return (GAPS[gapId]);
    }

    public static int getXStretchType()
    {
        X_STRETCH_TYPE = ABSOLUTE_STRETCH;
        if (X_STRETCH_TYPE > -1) return (X_STRETCH_TYPE);
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        if (!(idata instanceof InstallData)) return (RELATIVE_STRETCH);
        String var = null;
        if (((InstallData) idata).guiPrefs.modifier.containsKey("layoutXStretchType"))
        {
            var = (String) ((InstallData) idata).guiPrefs.modifier.get("layoutXStretchType");
            if (var != null)
            {
                if ("RELATIVE_STRETCH".equalsIgnoreCase(var) || "RELATIVE".equalsIgnoreCase(var))
                    X_STRETCH_TYPE = RELATIVE_STRETCH;
                else if ("ABSOLUTE_STRETCH".equalsIgnoreCase(var)
                        || "ABSOLUTE".equalsIgnoreCase(var))
                    X_STRETCH_TYPE = ABSOLUTE_STRETCH;
                else if ("NO_STRETCH".equalsIgnoreCase(var) || "NO".equalsIgnoreCase(var))
                    X_STRETCH_TYPE = NO_STRETCH;
            }
        }
        return (X_STRETCH_TYPE);

    }

    /**
     * Returns the layout manager which current used by this layout helper. The layout manager
     * implements LayoutManager2. It can be a GridBagLayout or a IzPanelLayout.
     * 
     * @return current used layout manager
     */
    public LayoutManager2 getLayout()
    {
        return izPanelLayout;
    }

    /**
     * Sets the given layout manager for this layout helper to be used.
     * 
     * @param izPanelLayout layout manager to be used
     */
    public void setLayout(LayoutManager2 izPanelLayout)
    {
        this.izPanelLayout = izPanelLayout;
    }

}
