/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               FrontendAbout.java
 *  Description :        The Frontend about dialog class.
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

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.IconsDatabase;

/**
 *  The IzPack frontend about dialog class.
 *
 * @author     Julien Ponge
 */
public class FrontendAbout extends JDialog implements ActionListener
{
  /**  The language pack. */
  private LocaleDatabase langpack;

  /**  The icons. */
  private IconsDatabase icons;

  /**  The layout. */
  private GridBagLayout layout;

  /**  The layout constraints. */
  private GridBagConstraints gbConstraints;

  /**  The 'ok' button. */
  private JButton okButton;


  /**
   *  The constructor.
   *
   * @param  langpack  The language pack.
   * @param  icons     The icons database.
   * @param  owner     Description of the Parameter
   */
  public FrontendAbout(Frame owner, LocaleDatabase langpack, IconsDatabase icons)
  {
    super(owner, langpack.getString("menu.about"), true);

    this.langpack = langpack;
    this.icons = icons;

    buildGUI();
    pack();
    FrontendFrame.centerFrame(this);
    setResizable(false);
    setVisible(true);
  }


  /**  Builds the GUI.  */
  private void buildGUI()
  {
    // We initialize our layout
    layout = new GridBagLayout();
    gbConstraints = new GridBagConstraints();
    gbConstraints.insets = new Insets(2, 2, 2, 2);
    JPanel contentPane = (JPanel) getContentPane();
    contentPane.setLayout(layout);

    // We put our components

    JLabel label = new JLabel(icons.getImageIcon("about_" + Frontend.random.nextInt(Frontend.MAX_SPLASHES_PICS)));
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.CENTER;
    FrontendFrame.buildConstraints(gbConstraints, 0, 0, 1, 1, 1.0, 1.0);
    layout.addLayoutComponent(label, gbConstraints);
    contentPane.add(label);

    label = new JLabel("IzPack Version " + com.izforge.izpack.compiler.Compiler.IZPACK_VERSION);
    FrontendFrame.buildConstraints(gbConstraints, 0, 1, 1, 1, 1.0, 1.0);
    layout.addLayoutComponent(label, gbConstraints);
    contentPane.add(label);

    label = new JLabel(langpack.getString("frontend.about.copyright"));
    FrontendFrame.buildConstraints(gbConstraints, 0, 2, 1, 1, 1.0, 1.0);
    layout.addLayoutComponent(label, gbConstraints);
    contentPane.add(label);

    label = new JLabel(langpack.getString("frontend.about.url"));
    FrontendFrame.buildConstraints(gbConstraints, 0, 3, 1, 1, 1.0, 1.0);
    layout.addLayoutComponent(label, gbConstraints);
    contentPane.add(label);

    okButton = ButtonFactory.createButton(langpack.getString("frontend.licence.ok"),
      icons.getImageIcon("forward"),
      FrontendFrame.buttonsHColor);
    okButton.addActionListener(this);
    FrontendFrame.buildConstraints(gbConstraints, 0, 4, 1, 1, 1.0, 1.0);
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.SOUTH;
    layout.addLayoutComponent(okButton, gbConstraints);
    contentPane.add(okButton);
  }


  /**
   *  Action events handler.
   *
   * @param  e  The event.
   */
  public void actionPerformed(ActionEvent e)
  {
    dispose();
  }
}

