/*
 * $Id$
 * IzPack
 * Copyright (c) 2003, Julien Ponge <julien@izforge.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of IzPack nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package com.izforge.izpack.gui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.border.EtchedBorder;

/**
 * Draws an etched line border.
 * 
 * @author Julien Ponge
 */
public class EtchedLineBorder extends EtchedBorder
{

    private static final long serialVersionUID = 3256999956257649201L;

    /**
     * Paints the etched line.
     * 
     * @param c
     *            The component to draw the border on.
     * @param g
     *            The graphics object.
     * @param x
     *            The top-left x.
     * @param y
     *            The top-left y.
     * @param width
     *            The border width.
     * @param height
     *            The border height.
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
    {
        g.translate(x, y);

        g.setColor(etchType == LOWERED ? getShadowColor(c) : getHighlightColor(c));
        g.drawLine(10, 0, width - 2, 0);

        g.setColor(etchType == LOWERED ? getHighlightColor(c) : getShadowColor(c));
        g.drawLine(10, 1, width - 2, 1);

        g.translate(0 - x, 0 - y);
    }
}
