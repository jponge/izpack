/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package com.izforge.izpack.installer.panel;

import java.util.List;


/**
 * Manages navigation between panels, providing access to the view of each panel.
 *
 * @author Tim Anderson
 */
public interface PanelViews<T extends PanelView<V>, V> extends Panels
{

    /**
     * Returns the panel views.
     *
     * @return the panel views
     */
    List<T> getPanelViews();

    /**
     * Returns the current view.
     *
     * @return the current view, or {@code null} if there is none
     */
    V getView();

    /**
     * Returns the current panel view.
     *
     * @return the current panel view, or {@code null} if there is none
     */
    T getPanelView();

    /**
     * Returns the index of a visible panel, relative to other visible panels.
     *
     * @param panel the panel
     * @return the panel's visible index, or {@code -1} if the panel is not visible
     */
    int getVisibleIndex(T panel);

}
