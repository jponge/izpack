/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               UninstallerFrame.java
 *  Description :        The uninstaller frame class.
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
package com.izforge.izpack.uninstaller;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.util.AbstractUIHandler;

/**
 *  The uninstaller frame class.
 *
 * @author     Julien Ponge
 */
public class UninstallerFrame extends JFrame
{
  /**  The icons database. */
  private IconsDatabase icons;

  /**  The language pack. */
  protected LocaleDatabase langpack;

  /**  The warning label. */
  private JLabel warningLabel;

  /**  The target destroy checkbox. */
  protected JCheckBox targetDestroyCheckbox;

  /**  The progress bar. */
  protected JProgressBar progressBar;

  /**  The destroy button. */
  protected JButton destroyButton;

  /**  The quit button. */
  protected JButton quitButton;

  /**  The layout. */
  private GridBagLayout layout;

  /**  the layout constraints. */
  private GridBagConstraints gbConstraints;

  /**  The buttons hover color. */
  private Color buttonsHColor = new Color(230, 230, 230);

  /**  The installation path. */
  protected String installPath;

  /**
   *  The constructor.
   *
   * @exception  Exception  Description of the Exception
   */
  public UninstallerFrame() throws Exception
  {
    super("IzPack - Uninstaller");

    // Initializations
    langpack =
      new LocaleDatabase(getClass().getResourceAsStream("/langpack.xml"));
    getInstallPath();
    icons = new IconsDatabase();
    loadIcons();
    UIManager.put(
      "OptionPane.yesButtonText",
      langpack.getString("installer.yes"));
    UIManager.put(
      "OptionPane.noButtonText",
      langpack.getString("installer.no"));
    UIManager.put(
      "OptionPane.cancelButtonText",
      langpack.getString("installer.cancel"));

    // Sets the frame icon
    setIconImage(icons.getImageIcon("JFrameIcon").getImage());

    // We build the GUI & show it
    buildGUI();
    addWindowListener(new WindowHandler());
    pack();
    centerFrame(this);
    setResizable(false);
    setVisible(true);
  }

  /**  Builds the GUI.  */
  private void buildGUI()
  {
    // We initialize our layout
    JPanel contentPane = (JPanel) getContentPane();
    layout = new GridBagLayout();
    contentPane.setLayout(layout);
    gbConstraints = new GridBagConstraints();
    gbConstraints.insets = new Insets(5, 5, 5, 5);

    // We prepare our action handler
    ActionsHandler handler = new ActionsHandler();

    // Prepares the glass pane to block gui interaction when needed
    JPanel glassPane = (JPanel) getGlassPane();
    glassPane.addMouseListener(new MouseAdapter()
    {
    });
    glassPane.addMouseMotionListener(new MouseMotionAdapter()
    {
    });
    glassPane.addKeyListener(new KeyAdapter()
    {
    });

    // We set-up the buttons factory
    ButtonFactory.useButtonIcons();
    ButtonFactory.useHighlightButtons();

    // We put our components

    warningLabel =
      new JLabel(
        langpack.getString("uninstaller.warning"),
        icons.getImageIcon("warning"),
        JLabel.TRAILING);
    buildConstraints(gbConstraints, 0, 0, 2, 1, 1.0, 0.0);
    gbConstraints.anchor = GridBagConstraints.WEST;
    gbConstraints.fill = GridBagConstraints.NONE;
    layout.addLayoutComponent(warningLabel, gbConstraints);
    contentPane.add(warningLabel);

    targetDestroyCheckbox =
      new JCheckBox(
        langpack.getString("uninstaller.destroytarget") + installPath,
        false);
    buildConstraints(gbConstraints, 0, 1, 2, 1, 1.0, 0.0);
    layout.addLayoutComponent(targetDestroyCheckbox, gbConstraints);
    contentPane.add(targetDestroyCheckbox);
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;

    progressBar = new JProgressBar();
    progressBar.setStringPainted(true);
    progressBar.setString(langpack.getString("InstallPanel.begin"));
    buildConstraints(gbConstraints, 0, 2, 2, 1, 1.0, 0.0);
    layout.addLayoutComponent(progressBar, gbConstraints);
    contentPane.add(progressBar);

    destroyButton =
      ButtonFactory.createButton(
        langpack.getString("uninstaller.uninstall"),
        icons.getImageIcon("delete"),
        buttonsHColor);
    destroyButton.addActionListener(handler);
    buildConstraints(gbConstraints, 0, 3, 1, 1, 0.5, 0.0);
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.WEST;
    layout.addLayoutComponent(destroyButton, gbConstraints);
    contentPane.add(destroyButton);

    quitButton =
      ButtonFactory.createButton(
        langpack.getString("installer.quit"),
        icons.getImageIcon("stop"),
        buttonsHColor);
    quitButton.addActionListener(handler);
    buildConstraints(gbConstraints, 1, 3, 1, 1, 0.5, 0.0);
    gbConstraints.anchor = GridBagConstraints.EAST;
    layout.addLayoutComponent(quitButton, gbConstraints);
    contentPane.add(quitButton);

  }

  /**
   *  Centers a window on screen.
   *
   * @param  frame  The window to center.
   */
  private void centerFrame(Window frame)
  {
    Dimension frameSize = frame.getSize();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(
      (screenSize.width - frameSize.width) / 2,
      (screenSize.height - frameSize.height) / 2 - 10);
  }

  /**
   *  Sets the parameters of a GridBagConstraints object.
   *
   * @param  gbc  The constraints object.
   * @param  gx   The x coordinates.
   * @param  gy   The y coordinates.
   * @param  gw   The width.
   * @param  wx   The x wheight.
   * @param  wy   The y wheight.
   * @param  gh   Description of the Parameter
   */
  private void buildConstraints(
    GridBagConstraints gbc,
    int gx,
    int gy,
    int gw,
    int gh,
    double wx,
    double wy)
  {
    gbc.gridx = gx;
    gbc.gridy = gy;
    gbc.gridwidth = gw;
    gbc.gridheight = gh;
    gbc.weightx = wx;
    gbc.weighty = wy;
  }

  /**
   *  Gets the installation path from the log file.
   *
   * @exception  Exception  Description of the Exception
   */
  private void getInstallPath() throws Exception
  {
    InputStream in = getClass().getResourceAsStream("/install.log");
    InputStreamReader inReader = new InputStreamReader(in);
    BufferedReader reader = new BufferedReader(inReader);
    installPath = reader.readLine();
    reader.close();
  }

  /**
   *  Loads the icons.
   *
   * @exception  Exception  Description of the Exception
   */
  private void loadIcons() throws Exception
  {
    // Initialisations
    icons = new IconsDatabase();
    URL url;
    ImageIcon img;

    // We load it
    url = getClass().getResource("/img/trash.png");
    img = new ImageIcon(url);
    icons.put("delete", img);

    url = getClass().getResource("/img/stop.png");
    img = new ImageIcon(url);
    icons.put("stop", img);

    url = getClass().getResource("/img/flag.png");
    img = new ImageIcon(url);
    icons.put("warning", img);

    url = getClass().getResource("/img/JFrameIcon.png");
    img = new ImageIcon(url);
    icons.put("JFrameIcon", img);
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
   *  The window events handler.
   *
   * @author     Julien Ponge
   */
  class WindowHandler extends WindowAdapter
  {
    /**
     *  We can't avoid the exit here ... so don't call exit.
     *
     * @param  e  The event.
     */
    public void windowClosing(WindowEvent e)
    {
      System.exit(0);
    }
  }

  /**
   * The destroyer handler.
   *
   * This class also implements the InstallListener because the FileExecutor needs it.
   * TODO: get rid of the InstallListener - implement generic Listener
   * 
   * @author     Julien Ponge
   * @author     Tino Schwarze
   */
  class DestroyerHandler
    implements com.izforge.izpack.util.AbstractUIProgressHandler
  {
    /**
     *  The destroyer starts.
     *
     * @param name The name of the overall action. Not used here. 
     * @param max  The maximum value of the progress.
     */
    public void startAction(String name, int max)
    {
      progressBar.setMinimum(0);
      progressBar.setMaximum(max);
      blockGUI();
    }

    /**  The destroyer stops.  */
    public void stopAction()
    {
      progressBar.setString(langpack.getString("InstallPanel.finished"));
      targetDestroyCheckbox.setEnabled(false);
      destroyButton.setEnabled(false);
      releaseGUI();
    }

    /**
     *  The destroyer progresses.
     *
     * @param  pos      The actual position.
     * @param  message  The message.
     */
    public void progress(int pos, String message)
    {
      progressBar.setValue(pos);
      progressBar.setString(message);
    }

    public void nextStep(String step_name, int step_no, int no_of_substeps)
    {
    }

    /**
     *  Output a notification.
     * 
     * Does nothing here.
     * 
     * @param text
     */
    public void emitNotification(String text)
    {
    }

    /**
     *  Output a warning.
     * 
     * @param text
     */
    public boolean emitWarning(String title, String text)
    {
      return (
        JOptionPane.showConfirmDialog(
          null,
          text,
          title,
          JOptionPane.OK_CANCEL_OPTION,
          JOptionPane.WARNING_MESSAGE)
          == JOptionPane.OK_OPTION);
    }

    /**
     *  The destroyer encountered an error.
     *
     * @param  error  The error message.
     */
    public void emitError(String title, String error)
    {
      progressBar.setString(error);
      JOptionPane.showMessageDialog(
        null,
        error,
        title,
        JOptionPane.OK_CANCEL_OPTION);
    }

    /**
    * Ask the user a question.
    * 
    * @param title Message title.
    * @param question The question.
    * @param choices The set of choices to present.
    * 
    * @return The user's choice.
    * 
    * @see AbstractUIHandler#askQuestion(String, String, int)
    */
    public int askQuestion(String title, String question, int choices)
    {
      return askQuestion(title, question, choices, -1);
    }

    /**
     * Ask the user a question.
     * 
     * @param title Message title.
     * @param question The question.
     * @param choices The set of choices to present.
     * @param default_choice The default choice. (-1 = no default choice)
     * 
     * @return The user's choice.
     * @see AbstractUIHandler#askQuestion(String, String, int, int)
     */
    public int askQuestion(
      String title,
      String question,
      int choices,
      int default_choice)
    {
      int jo_choices = 0;

      if (choices == AbstractUIHandler.CHOICES_YES_NO)
        jo_choices = JOptionPane.YES_NO_OPTION;
      else if (choices == AbstractUIHandler.CHOICES_YES_NO_CANCEL)
        jo_choices = JOptionPane.YES_NO_CANCEL_OPTION;

      int user_choice =
        JOptionPane.showConfirmDialog(
          null,
          (Object) question,
          title,
          jo_choices,
          JOptionPane.QUESTION_MESSAGE);

      if (user_choice == JOptionPane.CANCEL_OPTION)
        return AbstractUIHandler.ANSWER_CANCEL;

      if (user_choice == JOptionPane.YES_OPTION)
        return AbstractUIHandler.ANSWER_YES;

      if (user_choice == JOptionPane.NO_OPTION)
        return AbstractUIHandler.ANSWER_NO;

      return default_choice;
    }

  }

  /**
   *  The actions events handler.
   *
   * @author     Julien Ponge
   */
  class ActionsHandler implements ActionListener
  {
    /**
     *  Action handling method.
     *
     * @param  e  The event.
     */
    public void actionPerformed(ActionEvent e)
    {
      Object src = e.getSource();
      if (src == quitButton)
        System.exit(0);
      else if (src == destroyButton)
      {
        Destroyer destroyer =
          new Destroyer(
            installPath,
            targetDestroyCheckbox.isSelected(),
            new DestroyerHandler());
        destroyer.start();
      }
    }
  }
}
