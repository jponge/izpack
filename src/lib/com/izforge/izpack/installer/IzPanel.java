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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.MultiLineLabel;

/**
 *  Defines the base class for the IzPack panels. Any panel should be a subclass
 *  of it and should belong to the <code>com.izforge.izpack.panels</code>
 *  package.
 *
 * @author     Julien Ponge
 */
public class IzPanel extends JPanel implements AbstractUIHandler
{
  /** Indicates whether grid bag layout was started or not */
  protected boolean gridBagLayoutStarted = false;
  
  /** The component which should get the focus at activation */
  protected Component initialFocus = null;
  /**
   *  The installer internal data (actually a melting-pot class with all-public
   *  fields.
   */
  protected InstallData idata;

  /**  The parent IzPack installer frame. */
  protected InstallerFrame parent;

  /** The default grid bag constraint. */
  protected GridBagConstraints defaultGridBagConstraints = new GridBagConstraints ();

  /** Current x position of grid. */
  protected int gridxCounter = -1;
  
  /** Current y position of grid. */
  protected int gridyCounter = -1;
  
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
     JOptionPane.showMessageDialog(
      this,message);
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

  //------------- Helper for common used components ----- START ---

  /**
   * Creates  a label via LabelFactory with the given ids and
   * the given horizontal alignment. If the icon id is null, the
   * label will be created also. The strings are the ids for the text
   * in langpack and the icon in icons of the installer frame.
   * @param textId id string for the text
   * @param iconId id string for the icon
   * @param pos horizontal alignment
   * @return the newly created label
   */
  public JLabel createLabel(String textId, String iconId, int pos)
  {
    ImageIcon ii = ( iconId != null ) ? parent.icons.getImageIcon(iconId) : null;
    JLabel label = LabelFactory.create(
      parent.langpack.getString(textId),
      ii, 
      pos);
    if(label != null)
      label.setFont(getControlTextFont() );
    return(label);
    
  }

  /**
   * Creates a multi line label with the language dependent text
   * given by the text id. The strings is the id for the text
   * in langpack of the installer frame. The horizontal alignment
   * will be LEFT.
   * @param textId id string for the text
   * @return the newly created multi line label
   */
  public MultiLineLabel createMultiLineLabelLang(String textId)
  {
    return( createMultiLineLabel(
      parent.langpack.getString( textId)  ));
  }

  /**
   * Creates a multi line label with the given text. 
   * The horizontal alignment will be LEFT.
   * @param text text to be used in the label
   * @return the newly created multi line label
   */
  public MultiLineLabel createMultiLineLabel(String text)
  {
    return( createMultiLineLabel(
      text, null , JLabel.LEFT ));
  }
  /**
   * Creates  a label via LabelFactory with the given text, the
   * given icon id  and the given horizontal alignment. 
   * If the icon id is null, the
   * label will be created also. The strings are the ids for the text
   * in langpack and the icon in icons of the installer frame.
   * @param text text to be used in the label
   * @param iconId id string for the icon
   * @param pos horizontal alignment
   * @return the created multi line label
   */
  public MultiLineLabel createMultiLineLabel(String text, 
    String iconId, int pos)
  {
    MultiLineLabel mll = null;
    mll = new MultiLineLabel( text, 0, 0);
    if( mll != null)
      mll.setFont(getControlTextFont() );
    return( mll );
  }

  /**
   * The Font of Labels in many cases
   */
  public Font getControlTextFont()
  {
    return( getLAF() != null ? MetalLookAndFeel.getControlTextFont() : getFont() );
  }

  protected static MetalLookAndFeel getLAF()
  {
    LookAndFeel laf = UIManager.getLookAndFeel();
    if( laf instanceof MetalLookAndFeel)
      return( (MetalLookAndFeel) laf);
    return( null );
  }

  //------------- Helper for common used components ----- END ---
  //------------------- Layout stuff -------------------- START ---
  /**
   * Returns the default GridBagConstraints of this panel.
   * @return the default GridBagConstraints of this panel
   */
  public GridBagConstraints getDefaultGridBagConstraints()
  {
    startGridBagLayout();
    return defaultGridBagConstraints;
  }

  /**
   * Sets the default GridBagConstraints of this panel to the given object.
   * @param constraints which should be set as default for this object
   */
  public void setDefaultGridBagConstraints(GridBagConstraints constraints)
  {
    startGridBagLayout();
    defaultGridBagConstraints = constraints;
  }
  /**
   * Resets the grid counters which are used at getNextXGridBagConstraints
   * and getNextYGridBagConstraints.
   */
  public void resetGridCounter()
  {
    gridxCounter = -1;
    gridyCounter = -1;
  }

  /**
   * Returns a newly created GridBagConstraints with the given values
   * and the values from the defaultGridBagConstraints for the other parameters.
   * @param gridx  value to be used for the new constraint
   * @param gridy  value to be used for the new constraint
   * @return newly created GridBagConstraints with the given values
   * and the values from the defaultGridBagConstraints for the other parameters
   */
  public GridBagConstraints getNewGridBagConstraints(int gridx, int gridy)
  {
    GridBagConstraints retval = (GridBagConstraints) getDefaultGridBagConstraints().clone();
    retval.gridx = gridx;
    retval.gridy = gridy;
    return(retval);
    
  }
  
  /**
   * Returns a newly created GridBagConstraints with the given values
   * and the values from the defaultGridBagConstraints for the other parameters.
   * @param gridx  value to be used for the new constraint
   * @param gridy  value to be used for the new constraint
   * @param gridwidth value to be used for the new constraint
   * @param gridheight value to be used for the new constraint
   * @return newly created GridBagConstraints with the given values
   * and the values from the defaultGridBagConstraints for the other parameters
   */
  public GridBagConstraints getNewGridBagConstraints(int gridx, int gridy,
    int gridwidth, int gridheight)
  {
    GridBagConstraints retval = getNewGridBagConstraints( gridx, gridy );
    retval.gridwidth = gridwidth;
    retval.gridheight = gridheight;
    return(retval);
  }
  
  /**
   * Returns a newly created GridBagConstraints for the next column 
   * of the current layout row.
   * @return a newly created GridBagConstraints for the next column 
   * of the current layout row
   * 
   */
  public GridBagConstraints getNextXGridBagConstraints()
  {
    gridxCounter++;
    GridBagConstraints retval = getNewGridBagConstraints(gridxCounter,gridyCounter);
    return(retval);
  }
  
  /**
   * Returns a newly created GridBagConstraints for the next column 
   * of the current layout row using the given parameters.
   * @param gridwidth width for this constraint
   * @param gridheight height for this constraint
   * @return a newly created GridBagConstraints for the next column 
   * of the current layout row using the given parameters
   */
  private GridBagConstraints getNextXGridBagConstraints( int gridwidth, int gridheight)
  {
    GridBagConstraints retval = getNextXGridBagConstraints();
    retval.gridwidth = gridwidth;
    retval.gridheight = gridheight;
    return( retval );
  }
  /**
   * Returns a newly created GridBagConstraints with column 0 
   * for the next row.
   * @return a newly created GridBagConstraints with column 0 
   * for the next row
   * 
   */
  public GridBagConstraints getNextYGridBagConstraints()
  {
    gridyCounter++;
    gridxCounter = 0;
    GridBagConstraints retval = getNewGridBagConstraints(0,gridyCounter);
    return(retval);
  }
  
  /**
   * Returns a newly created GridBagConstraints with column 0 
   * for the next row using the given parameters.
   * @param gridwidth width for this constraint
   * @param gridheight height for this constraint
   * @return a newly created GridBagConstraints with column 0 
   * for the next row using the given parameters
   */
  public GridBagConstraints getNextYGridBagConstraints( int gridwidth, int gridheight)
  {
    startGridBagLayout();
    GridBagConstraints retval = getNextYGridBagConstraints();
    retval.gridwidth = gridwidth;
    retval.gridheight = gridheight;
    return( retval );
  }
  
  /**
   * Start layout determining. If it is needed, a dummy component 
   * will be created as first row. This will be done,
   * if the IzPack variable <code>IzPanel.LayoutType</code> has
   * the value "BOTTOM".
   */
  public void startGridBagLayout()
  {
    if( gridBagLayoutStarted  )
      return;
    gridBagLayoutStarted = true;
    GridBagLayout layout = new GridBagLayout();
    defaultGridBagConstraints.insets = new Insets( 0, 0, 20, 0);
    defaultGridBagConstraints.anchor = GridBagConstraints.WEST;   
    setLayout(layout);
    String todo = idata.getVariable("IzPanel.LayoutType");
    if( todo == null )  // No command, no work.
      return;
    if( todo.equals("BOTTOM"))
    { // Make a header to push the rest to the bottom.
      Filler dummy = new Filler();
      GridBagConstraints gbConstraint = getNextYGridBagConstraints();
      gbConstraint.weighty = 1.0;
      gbConstraint.fill = GridBagConstraints.BOTH;
      gbConstraint.anchor = GridBagConstraints.WEST;        
      this.add(dummy, gbConstraint );
    }
  
    // TODO: impl for layout type CENTER,  ...
  }
  

  /**
   * Complete layout determining. If it is needed, a dummy component 
   * will be created as last row. This will be done,
   * if the IzPack variable <code>IzPanel.LayoutType</code> has
   * the value "TOP".
   */
  public void completeGridBagLayout()
  {
    String todo = idata.getVariable("IzPanel.LayoutType");
    if( todo == null )  // No command, no work.
      return;
    if( todo.equals("TOP"))
    { // Make a footer to push the rest to the top.
       Filler dummy = new Filler();
      GridBagConstraints gbConstraint = getNextYGridBagConstraints();
      gbConstraint.weighty = 1.0;
      gbConstraint.fill = GridBagConstraints.BOTH;
      gbConstraint.anchor = GridBagConstraints.WEST;        
      this.add(dummy, gbConstraint );
    }
  }
    
  //------------------- Layout stuff -------------------- END ---

  //------------------- Summary stuff -------------------- START ---
  /**
   * This method will be called from the SummaryPanel
   * to get the summary of this class which should be
   * placed in the SummaryPanel. The returned text 
   * should not contain a caption of this item. The
   * caption will be requested from the method getCaption.
   * If <code>null</code> returns, no summary for this panel will be 
   * generated. 
   * Default behaviour is to return <code>null</code>.
   * @return the summary for this class
   */
  public String getSummaryBody()
  {
    return null;
  }
  
  /**
   * This method will be called from the SummaryPanel
   * to get the caption for this class which should be
   * placed in the SummaryPanel.
   * If <code>null</code> returns, no summary for this panel will be 
   * generated. 
   * Default behaviour is to return the string given by langpack
   * for the key <code>&lt;current class name>.summaryCaption&gt;</code> 
   * if exist, else the string &quot;summaryCaption.&lt;ClassName&gt;&quot;. 
    * @return the caption for this class
   */
  public String getSummaryCaption()
  {
    return(getI18nStringForClass("summaryCaption", this.getClass().getName()));
  }

  //------------------- Summary stuff -------------------- END ---


  //------------------- Inner classes ------------------- START ---
  public static class Filler extends JComponent 
  {
    
  }
  //------------------- Inner classes ------------------- END ---


}
