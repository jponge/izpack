/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2003 Julien Ponge
 *
 *  File :               FrontendCompilerDialog.java
 *  Description :        The Frontend compiler frame class.
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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.compiler.PackagerListener;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.IconsDatabase;

/**
 *  A dialog echoing the compiler messages.
 *
 * @author     Julien Ponge
 */
public class FrontendCompilerDialog extends JDialog implements ActionListener, PackagerListener
{
  /**  The langauge pack. */
  private LocaleDatabase langpack;

  /**  The icons. */
  private IconsDatabase icons;

  /**  The layout. */
  private GridBagLayout layout;

  /**  The layout constraints. */
  private GridBagConstraints gbConstraints;

  /**  The 'ok' button. */
  private JButton okButton;

  /**  The text area. */
  private JTextArea textArea;

  /**  The vertical scrollbar. */
  private JScrollBar vertScrollBar;


  /**
   *  The constructor.
   *
   * @param  owner     The parent.
   * @param  langpack  The language pack.
   * @param  icons     The icons database.
   */
  public FrontendCompilerDialog(Frame owner, LocaleDatabase langpack, IconsDatabase icons)
  {
    super(owner, langpack.getString("frontend.comp_dlg.title"), false);

    this.langpack = langpack;
    this.icons = icons;

    buildGUI();
    pack();
    FrontendFrame.centerFrame(this);
    setVisible(true);
  }


  /**  Builds the GUI.  */
  private void buildGUI()
  {
    // Prepares the glass pane to block gui interaction when needed
    JPanel glassPane = (JPanel) getGlassPane();
    glassPane.addMouseListener(
      new MouseAdapter()
      {
      });
    glassPane.addMouseMotionListener(
      new MouseMotionAdapter()
      {
      });
    glassPane.addKeyListener(
      new KeyAdapter()
      {
      });

    // We initialize our layout
    layout = new GridBagLayout();
    gbConstraints = new GridBagConstraints();
    gbConstraints.insets = new Insets(5, 5, 5, 5);
    JPanel contentPane = (JPanel) getContentPane();
    contentPane.setLayout(layout);

    // We put our components

    JLabel label = new JLabel(langpack.getString("frontend.comp_dlg.msg"));
    FrontendFrame.buildConstraints(gbConstraints, 0, 0, 1, 1, 1.0, 0.0);
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
    layout.addLayoutComponent(label, gbConstraints);
    contentPane.add(label);

    textArea = new JTextArea();
    textArea.setEditable(false);
    JScrollPane scroller = new JScrollPane(textArea);
    FrontendFrame.buildConstraints(gbConstraints, 0, 1, 1, 1, 1.0, 1.0);
    scroller.setPreferredSize(new Dimension(500, 250));
    gbConstraints.fill = GridBagConstraints.BOTH;
    gbConstraints.anchor = GridBagConstraints.CENTER;
    layout.addLayoutComponent(scroller, gbConstraints);
    contentPane.add(scroller);
    vertScrollBar = scroller.getVerticalScrollBar();

    okButton = ButtonFactory.createButton(langpack.getString("frontend.comp_dlg.ok"),
      icons.getImageIcon("forward"),
      FrontendFrame.buttonsHColor);
    okButton.addActionListener(this);
    FrontendFrame.buildConstraints(gbConstraints, 0, 2, 1, 1, 1.0, 0.0);
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.SOUTH;
    layout.addLayoutComponent(okButton, gbConstraints);
    contentPane.add(okButton);
  }


  /**  Blocks GUI interaction.  */
  public void blockGUI()
  {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    getGlassPane().setVisible(true);
    getGlassPane().setEnabled(true);
  }


  /**  Releases GUI interaction.  */
  public void releaseGUI()
  {
    getGlassPane().setEnabled(false);
    getGlassPane().setVisible(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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


  /**
   *  Called as the packager sends messages.
   *
   * @param  info  The information send by the packager.
   */
  public void packagerMsg(String info)
  {
    textArea.append("\n" + info);
    vertScrollBar.setValue(vertScrollBar.getMaximum());
  }


  /**  Called when the packager starts.  */
  public void packagerStart()
  {
    blockGUI();
    textArea.setText("[ Begin ]\n");
  }


  /**  Called when the packager stops.  */
  public void packagerStop()
  {
    releaseGUI();
    textArea.append("\n\n[ End ]");
    vertScrollBar.setValue(vertScrollBar.getMaximum());
  }
}

