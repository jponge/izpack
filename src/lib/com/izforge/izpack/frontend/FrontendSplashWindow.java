/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               FrontendSplashWindow.java
 *  Description :        The Frontend splash window class.
 *  Author's email :     julien@izforge.com
 *  Author's Website :   http://www.izforge.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.izforge.izpack.frontend;

import com.izforge.izpack.*;
import com.izforge.izpack.gui.*;
import com.izforge.izpack.compiler.*;

import java.io.*;
import java.net.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

import net.n3.nanoxml.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import com.incors.plaf.kunststoff.*;

/**
 *  The frontend splash window class.
 *
 * @author     Julien Ponge
 * @created    October 27, 2002
 */
public class FrontendSplashWindow extends JWindow
{
  /**  The layout. */
  private GridBagLayout layout;

  /**  The layout constraints. */
  private GridBagConstraints gbConstraints;

  /**  The progress bar. */
  private JProgressBar progress;


  /**  The constructor.  */
  public FrontendSplashWindow()
  {
    super();

    buildGUI();
    pack();
    FrontendFrame.centerFrame(this);
    setVisible(true);
  }


  /**  Builds the GUI.  */
  private void buildGUI()
  {
    // We initialize our layout
    layout = new GridBagLayout();
    gbConstraints = new GridBagConstraints();
    gbConstraints.insets = new Insets(0, 0, 0, 0);
    JPanel contentPane = (JPanel) getContentPane();
    contentPane.setLayout(layout);

    // We put our components

    URL imgURL = getClass().getResource("/img/about_" + Frontend.random.nextInt(Frontend.MAX_SPLASHES_PICS) + ".jpg");
    JLabel label = new JLabel(new ImageIcon(imgURL));
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.CENTER;
    FrontendFrame.buildConstraints(gbConstraints, 0, 0, 1, 1, 0.0, 0.0);
    layout.addLayoutComponent(label, gbConstraints);
    contentPane.add(label);

    progress = new JProgressBar(0, 5);
    progress.setStringPainted(true);
    progress.setString("-");
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    FrontendFrame.buildConstraints(gbConstraints, 0, 1, 1, 1, 1.0, 0.0);
    layout.addLayoutComponent(progress, gbConstraints);
    contentPane.add(progress);

    // Sets the border
    contentPane.setBorder(BorderFactory.createEtchedBorder());
  }


  /**  Stops.  */
  public void stop()
  {
    dispose();
  }


  /**
   *  Updates the splash screen.
   *
   * @param  val  The new progress bar value.
   * @param  str  The new message to display.
   */
  public void update(int val, String str)
  {
    progress.setValue(val);
    progress.setString(str);
  }
}

