/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               IzPanel.java
 *  Description :        The class for the panels.
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
package com.izforge.izpack.installer;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.util.AbstractUIHandler;

/**
 *  Defines the base class for the IzPack panels. Any panel should be a subclass
 *  of it and should belong to the <code>com.izforge.izpack.panels</code>
 *  package.
 *
 * @author     Julien Ponge
 */
public class IzPanel extends JPanel implements AbstractUIHandler
{
  /** The component which should get the focus at activation */
  protected Component initialFocus = null;
  /**
   *  The installer internal data (actually a melting-pot class with all-public
   *  fields.
   */
  protected InstallData idata;

  /**  The parent IzPack installer frame. */
  protected InstallerFrame parent;

  /**
   *  The constructor.
   *
   * @param  parent  The parent IzPack installer frame.
   * @param  idata   The installer internal data.
   */
  public IzPanel(InstallerFrame parent, InstallData idata)
  {
    super();

    this.idata = idata;
    this.parent = parent;
  }

  /**
   *  Indicates wether the panel has been validated or not. The installer won't
   *  let the user go further through the installation process until the panel
   *  is validated. Default behaviour is to return <code>true</code>.
   *
   * @return    A boolean stating wether the panel has been validated or not.
   */
  public boolean isValidated()
  {
    return true;
  }

  /**
   *  This method is called when the panel becomes active. Default is to do
   *  nothing : feel free to implement what you need in your subclasses. A panel
   *  becomes active when the user reaches it during the installation process.
   */
  public void panelActivate()
  {
  }

  /**
   *  This method is called when the panel gets desactivated, when the user
   *  switches to the next panel. By default it doesn't do anything.
   */
  public void panelDeactivate()
  {
  }

  /**
   *  Asks the panel to set its own XML data that can be brought back for an
   *  automated installation process. Use it as a blackbox if your panel needs
   *  to do something even in automated mode.
   *
   * @param  panelRoot  The XML root element of the panels blackbox tree.
   */
  public void makeXMLData(XMLElement panelRoot)
  {
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
        this,
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

  /**
   * Notify the user about something.
   * 
   * @param message The notification.
   */
  public void emitNotification(String message)
  {
    // ignore it
  }

  /**
   * Warn the user about something.
   * 
   * @param message The warning message.
   */
  public boolean emitWarning(String title, String message)
  {
    return (
      JOptionPane.showConfirmDialog(
        this,
        message,
        title,
        JOptionPane.WARNING_MESSAGE,
        JOptionPane.OK_CANCEL_OPTION)
        == JOptionPane.OK_OPTION);

  }

  /**
   * Notify the user of some error.
   * 
   * @param message The error message.
   */
  public void emitError(String title, String message)
  {
    JOptionPane.showMessageDialog(
      this,
      message,
      title,
      JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Returns the component which should be get the
   * focus at activation of this panel.
   * @return the component which should be get the
   * focus at activation of this panel
   */
  public Component getInitialFocus()
  {
    return initialFocus;
  }

  /**
   * Sets the component which should be get the
   * focus at activation of this panel.
   * @param component which should be get the
   * focus at activation of this panel
   */
  public void setInitialFocus(Component component)
  {
    initialFocus = component;
  }

  /**
   * Calls the langpack of parent InstallerFrame for the String 
   * <tt>RuntimeClassName.subkey</tt>. Do not add a point infront
   * of subkey, it is always added in this method.
   * @param subkey the subkey for the string which should be returned
   * @param alternateClass the short name of the class which should be used 
   * if no string is present with the runtime class name 
   * @return the founded string
   */
  public String getI18nStringForClass(String subkey, String alternateClass )
  {
    String curClassName = this.getClass().getName();
    int     nameStart     = curClassName.lastIndexOf ('.') + 1;
    curClassName          = curClassName.substring (nameStart, curClassName.length ());
    StringBuffer buf = new StringBuffer();
    buf.append(curClassName).append(".").append(subkey);
    String fullkey = buf.toString();
    String retval = parent.langpack.getString( fullkey);
    if( retval == null || retval.equals(fullkey) )
    {
      buf.delete(0, buf.length());
      buf.append(alternateClass).append(".").append(subkey);
      retval = parent.langpack.getString( buf.toString());
    }
    return( retval);
  }

  /**
   * Returns the parent of this IzPanel (which is a InstallerFrame).
   * @return the parent of this IzPanel
   */
  public InstallerFrame getInstallerFrame()
  {
    return(parent);
  }
}
