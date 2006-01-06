/*
 * IzPack - Copyright 2001-2006 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Klaus Bartz
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

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * <p>
 * A label factory which can handle modified look like to present icons or present it not.
 * </p>
 * 
 * @author Klaus Bartz
 * 
 */
public class LabelFactory implements SwingConstants
{

    private static boolean useLabelIcons = true;

    /**
     * Returns whether the factory creates labels with icons or without icons.
     * 
     * @return whether the factory creates labels with icons or without icons
     */
    public static boolean isUseLabelIcons()
    {
        return useLabelIcons;
    }

    /**
     * Sets the use icon state.
     * 
     * @param b flag for the icon state
     */
    public static void setUseLabelIcons(boolean b)
    {
        useLabelIcons = b;
    }

    /**
     * Returns a new JLabel with the horizontal alignment CENTER. If isUseLabelIcons is true, the
     * given image will be set to the label, else an empty label returns.
     * 
     * @param image the image to be used as label icon
     * @return new JLabel with the given parameters
     */
    public static JLabel create(Icon image)
    {
        return (create(image, CENTER));

    }

    /**
     * Returns a new JLabel with the given horizontal alignment. If isUseLabelIcons is true, the
     * given image will be set to the label, else an empty label returns.
     * 
     * @param image the image to be used as label icon
     * @param horizontalAlignment horizontal alignment of the label
     * @return new JLabel with the given parameters
     */
    public static JLabel create(Icon image, int horizontalAlignment)
    {
        return (create(null, image, horizontalAlignment));

    }

    /**
     * Returns a new JLabel with the horizontal alignment CENTER.
     * 
     * @param text the text to be set
     * @return new JLabel with the given parameters
     */
    public static JLabel create(String text)
    {
        return (create(text, CENTER));

    }

    /**
     * Returns a new JLabel with the given horizontal alignment.
     * 
     * @param text the text to be set
     * @param horizontalAlignment horizontal alignment of the label
     * @return new JLabel with the given parameters
     */
    public static JLabel create(String text, int horizontalAlignment)
    {
        return (create(text, null, horizontalAlignment));

    }

    /**
     * Returns a new JLabel with the given horizontal alignment. If isUseLabelIcons is true, the
     * given image will be set to the label. The given text will be set allways to the label. It is
     * allowed, that image and/or text are null.
     * 
     * @param text the text to be set
     * @param image the image to be used as label icon
     * @param horizontalAlignment horizontal alignment of the label
     * @return new JLabel with the given parameters
     */
    public static JLabel create(String text, Icon image, int horizontalAlignment)
    {
        JLabel retval = null;
        if (image != null && isUseLabelIcons())
        {
            retval = new JLabel(image);
        }
        else
        {
            retval = new JLabel();
        }
        if (text != null) retval.setText(text);
        retval.setHorizontalAlignment(horizontalAlignment);
        return (retval);
    }
}
