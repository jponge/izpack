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
package com.izforge.izpack.gui;

import javax.swing.SwingConstants;


public interface LayoutConstants extends SwingConstants
{
    public final static int NO_GAP = -13;

    /** Identifier for gaps between labels */
    public final static int LABEL_GAP = -1;

     /** Identifier for gaps between labels and text fields */
    public final static int TEXT_GAP = -2;

    /** Identifier for gaps between labels and controls like radio buttons/groups */
    public final static int CONTROL_GAP = -3;

    /** Identifier for gaps between paragraphs */
    public final static int PARAGRAPH_GAP = -4;

    /** Identifier for gaps between labels and text fields */
    public final static int LABEL_TO_TEXT_GAP = -5;

    /** Identifier for gaps between labels and controls like radio buttons/groups */
    public final static int LABEL_TO_CONTROL_GAP = -6;

   /** Identifier for gaps between text fields and labels */
    public final static int TEXT_TO_LABEL_GAP = -7;

    /** Identifier for gaps between controls like radio buttons/groups and labels */
    public final static int CONTROL_TO_LABEL_GAP = -8;

    /** Identifier for gaps between controls like radio buttons/groups and labels */
    public final static int CONTROL_TO_TEXT_GAP = -9;

    /** Identifier for gaps between controls like radio buttons/groups and labels */
    public final static int TEXT_TO_CONTROL_GAP = -10;

    /** Identifier for gaps between panel top and the first control. */
    public final static int TOP_GAP = -11;

    /** Identifier for gaps to be evaluated automatically at a late time. */
    public final static int AUTOMATIC_GAP = -12;

    /** Identifier for relative row positioning (next). */
    public static final int NEXT_ROW = -1;

    /** Identifier for relative row positioning (current). */
    public static final int CURRENT_ROW = -2;
    
    /** Identifier for relative column positioning (next). */
    public static final int NEXT_COLUMN = -1;

    /** Identifier for relative column positioning (current). */
    public static final int CURRENT_COLUMN = -2;
    
    /** Identifier for using the default alignment defined for labels. The
     *  value will be resolved at layouting, therefore it is possible to change
     *  the default values in </code>IzPanelConstraints</code>. 
     */
    public static final int DEFAULT_LABEL_ALIGNMENT = -1;

    /** Identifier for using the default alignment defined for text fields. The
     *  value will be resolved at layouting, therefore it is possible to change
     *  the default values in </code>IzPanelConstraints</code>. 
     */
    public static final int DEFAULT_TEXT_ALIGNMENT = -2;

    /** Identifier for using the default alignment defined for other controls. The
     *  value will be resolved at layouting, therefore it is possible to change
     *  the default values in </code>IzPanelConstraints</code>. 
     */
    public static final int DEFAULT_CONTROL_ALIGNMENT = -3;
    
    public static final int LABEL_CONSTRAINT = 0;

    public static final int TEXT_CONSTRAINT = 1;

    public static final int CONTROL_CONSTRAINT = 2;

    public static final int MULTILINE_LABEL_CONSTRAINT = 3;

    public static final int XDUMMY_CONSTRAINT = 4;

    public static final int YDUMMY_CONSTRAINT = 5;

    /** Constant used to specify that no action should be done. Useable for X_STRETCH. */
    public static final int NO_STRETCH = 0;

    /** X_STRETCH constant used to specify relative weighting of stretch factors. */
    public static final int RELATIVE_STRETCH = 1;

    /** X_STRETCH constant used to specify absolute weighting of stretch factors. */
    public static final int ABSOLUTE_STRETCH = 2;

    public static final String NEXT_LINE = "nextLine";
}
