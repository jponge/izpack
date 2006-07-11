/*
 * $Id:$
 * IzPack - Copyright 2001-2006 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/
 * http://developer.berlios.de/projects/izpack/
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
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.MultiLineLabel;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * Defines the base class for the IzPack panels. Any panel should be a subclass of it and should
 * belong to the <code>com.izforge.izpack.panels</code> package.
 * 
 * @author Julien Ponge
 */
public class IzPanel extends JPanel implements AbstractUIHandler
{

    private static final long serialVersionUID = 3256442495255786038L;

    /** Indicates whether grid bag layout was started or not */
    protected boolean gridBagLayoutStarted = false;

    /** The component which should get the focus at activation */
    protected Component initialFocus = null;

    /**
     * The installer internal data (actually a melting-pot class with all-public fields.
     */
    protected InstallData idata;

    /** The parent IzPack installer frame. */
    protected InstallerFrame parent;

    /** The default grid bag constraint. */
    protected GridBagConstraints defaultGridBagConstraints = new GridBagConstraints();

    /** Current x position of grid. */
    protected int gridxCounter = -1;

    /** Current y position of grid. */
    protected int gridyCounter = -1;
    
    /** i.e. "com.izforge.izpack.panels.HelloPanel" */
    protected String myFullClassname;

    /** myClassname=i.e "FinishPanel" */
    protected String myClassname;

    /** i.e. "FinishPanel." useFull for getString() */
    protected String myPrefix;

    /** internal headline string */
    protected String headline;
    
    /** internal layout */
    protected GridBagLayout izPanelLayout;
    
    /** internal headline Label */
    protected JLabel headLineLabel;
    
    /** Is this panel general hidden or not */
    protected boolean hidden;
    
    /** Layout anchor declared in the xml file
     *  with the guiprefs modifier "layoutAnchor"
     */ 
    protected static int ANCHOR = -1;
    
    /** Identifier for using no gaps */
    public final static int NO_GAP = 0;

    /** Identifier for gaps between labels */
    public final static int LABEL_GAP = 1;

    /** Identifier for gaps between paragraphs */
    public final static int PARAGRAPH_GAP = 2;

    /** Identifier for gaps between labels and text fields */
    public final static int LABEL_TO_TEXT_GAP = 3;

    /** Identifier for gaps between labels and controls like radio buttons/groups */
    public final static int LABEL_TO_CONTROL_GAP = 4;

    /** Identifier for gaps between text fields and labels*/
    public final static int TEXT_TO_LABEL_GAP = 5;

    /** Identifier for gaps between controls like radio buttons/groups and labels*/
    public final static int CONTROL_TO_LABEL_GAP = 6;

    /**
     * Look-up table for gap identifier to gap names. The gap names can be used in the XML
     * installation configuration file. Be aware that case sensitivity should be used.
     */
    public final static String[] GAP_NAME_LOOK_UP = { "noGap", "labelGap", "paragraphGap",
            "labelToTextGap", "labelToControlGap", "textToLabelGap", "controlToLabelGap"};

    /**
     * Current defined gaps. Here are the defaults which can be overwritten at the first call to
     * method getGap. The gap type will be determined by the array index and has to be synchron to
     * the gap identifier and the indices of array GAP_NAME_LOOK_UP
     */
    protected static int[] GAPS = { 0, 5, 5, 5, 5, 5, 5, 5, -1};
    
    /** HEADLINE = "headline" */
    public final static String HEADLINE = "headline";
    
    /** X_ORIGIN = 0 */
    public final static int X_ORIGIN = 0;

    /** Y_ORIGIN = 0 */
    public final static int Y_ORIGIN = 0;
    /** D = "." ( dot ) */
    public final static String D = ".";

    /** d = D */
    public final static String d = D;
    
    /** COLS_1 = 1 */
    public final static int COLS_1 = 1;

    /** ROWS_1 = 1 */
    public final static int ROWS_1 = 1;


    /**
     * The constructor.
     * 
     * @param parent The parent IzPack installer frame.
     * @param idata The installer internal data.
     */
    public IzPanel(InstallerFrame parent, InstallData idata)
    {
      super();
      init( parent, idata );
    }
    
    /**
     * Creates a new IzPanel object.
     *
     * @param parent the Parent Frame
     * @param idata Installers Runtime Data Set
     * @param iconName The Headline IconName
     */
    public IzPanel( InstallerFrame parent, InstallData idata, String iconName )
    {
      this( parent, idata, iconName, -1 );
    }    
    
    /**
     * The constructor with Icon.
     *
     * @param parent The parent IzPack installer frame.
     * @param idata The installer internal data.
     * @param iconName A iconname to show as left oriented headline-leading Icon.
     * @param instance An instance counter
     */
    public IzPanel( InstallerFrame parent, InstallData idata, String iconName, int instance )
    {
      super(  );
      init( parent, idata );

      setLayout(  );
      buildHeadline( iconName, instance );
      gridyCounter++;
    }
    
    /** 
     * Build the IzPanel internal Headline. If an external headline#
     * is used, this method returns immediately with false.
     * Allows also to display a leading Icon for the PanelHeadline.
     * This Icon can also be different if the panel has more than one Instances. 
     * The UserInputPanel is one of these Candidates.
     * 
     * by marc.eppelmann&#064;gmx.de
     *
     * @param imageIconName an Iconname
     * @param instanceNumber an panel instance
     *
     * @return true if successful build
     */
    protected boolean buildHeadline( String imageIconName, int instanceNumber )
    {
      boolean result = false;
      if( parent.isHeading(this))
          return(false);

      // TODO: proteced instancenumber
      // TODO: is to be validated
      // TODO: 
      // TODO: first Test if a Resource for your protected Instance exists.
      String headline;
      String headlineSearchBaseKey = myClassname + d + "headline"; // Results for example in "ShortcutPanel.headline" : 

      if( instanceNumber > -1 )  // Search for Results for example in "ShortcutPanel.headline.1, 2, 3 etc." :
      {
        String instanceSearchKey = headlineSearchBaseKey + d +
                                   Integer.toString( instanceNumber );

        String instanceHeadline = getString( instanceSearchKey );

        if( Debug.isLOG() ) 
        { 
          System.out.println( "found headline: " + instanceHeadline  +  d + " for instance # " +  instanceNumber ); 
        }
        if( ! instanceSearchKey.equals( instanceHeadline ) )
        {
          headline = instanceHeadline;
        }
        else
        {
          headline = getString( headlineSearchBaseKey );
        }
      }
      else
      {
        headline = getString( headlineSearchBaseKey );
      }

      if( headline != null )
      {
        if( ( imageIconName != null ) && ! "".equals( imageIconName ) )
        {
          headLineLabel = new JLabel( headline, getImageIcon( imageIconName ),
                                      JLabel.LEADING );
        }
        else
        {
          headLineLabel = new JLabel( headline );
        }

        Font  font  = headLineLabel.getFont(  );
        float size  = font.getSize(  );
        int   style = 0;
        font = font.deriveFont( style, ( size * 1.5f ) );
        headLineLabel.setFont( font );

        GridBagConstraints gbc = new GridBagConstraints(  );

        gbc.gridx      = 0;
        gbc.gridy      = 0;
        gbc.gridwidth  = 1;
        gbc.gridheight = 1;

        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        headLineLabel.setName( HEADLINE );
        izPanelLayout.setConstraints( headLineLabel, gbc );

        add( headLineLabel );
      }

      return result;
    }
    
    /** 
     * Gets a language Resource String from the parent, which  holds these global resource.
     *
     * @param key The Search key
     *
     * @return The Languageresource or the key if not found.
     */
    public String getString( String key )
    {
      return parent.langpack.getString( key );
    }
    
    /** 
     * Gets a named image icon
     *
     * @param iconName a valid image icon
     *
     * @return the icon
     */
    public ImageIcon getImageIcon( String iconName )
    {
      return parent.icons.getImageIcon( iconName );
    }


    
    /** 
     * Inits and sets teh internal LayoutObjects.
     *
     * @return true if finshed.
     */
    protected boolean setLayout(  )
    {
      izPanelLayout        = new GridBagLayout(  );
      defaultGridBagConstraints = new GridBagConstraints(  );

      setLayout( izPanelLayout );

      return true;
    }
    

    /** 
     * Gets and fills the classname fields
     */
    protected void getClassName(  )
    {
      myFullClassname = getClass(  ).getName(  );
      myClassname     = myFullClassname.substring( myFullClassname.lastIndexOf( "." ) + 1 );
      myPrefix        = myClassname + ".";
    }
    
    /** 
     * Internal init method
     *
     * @param parent the parent frame
     * @param idata installers runtime dataset
     */
    protected void init( InstallerFrame parent, InstallData idata )
    { 
      getClassName(  );
      
      this.idata           = idata;
      this.parent          = parent;
      
      gridyCounter = -1;
    }

    /**
     * Indicates wether the panel has been validated or not. The installer won't let the user go
     * further through the installation process until the panel is validated. Default behaviour is
     * to return <code>true</code>.
     * 
     * @return A boolean stating wether the panel has been validated or not.
     */
    public boolean isValidated()
    {
        return true;
    }

    /**
     * This method is called when the panel becomes active. Default is to do nothing : feel free to
     * implement what you need in your subclasses. A panel becomes active when the user reaches it
     * during the installation process.
     */
    public void panelActivate()
    {
    }

    /**
     * This method is called when the panel gets desactivated, when the user switches to the next
     * panel. By default it doesn't do anything.
     */
    public void panelDeactivate()
    {
    }

    /**
     * Asks the panel to set its own XML data that can be brought back for an automated installation
     * process. Use it as a blackbox if your panel needs to do something even in automated mode.
     * 
     * @param panelRoot The XML root element of the panels blackbox tree.
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
    public int askQuestion(String title, String question, int choices, int default_choice)
    {
        int jo_choices = 0;

        if (choices == AbstractUIHandler.CHOICES_YES_NO)
            jo_choices = JOptionPane.YES_NO_OPTION;
        else if (choices == AbstractUIHandler.CHOICES_YES_NO_CANCEL)
            jo_choices = JOptionPane.YES_NO_CANCEL_OPTION;

        int user_choice = JOptionPane.showConfirmDialog(this, (Object) question, title, jo_choices,
                JOptionPane.QUESTION_MESSAGE);

        if (user_choice == JOptionPane.CANCEL_OPTION) return AbstractUIHandler.ANSWER_CANCEL;

        if (user_choice == JOptionPane.YES_OPTION) return AbstractUIHandler.ANSWER_YES;

        if (user_choice == JOptionPane.NO_OPTION) return AbstractUIHandler.ANSWER_NO;

        return default_choice;
    }

    /**
     * Notify the user about something.
     * 
     * @param message The notification.
     */
    public void emitNotification(String message)
    {
        JOptionPane.showMessageDialog(this, message);
    }

    /**
     * Warn the user about something.
     * 
     * @param message The warning message.
     */
    public boolean emitWarning(String title, String message)
    {
        return (JOptionPane.showConfirmDialog(this, message, title, JOptionPane.WARNING_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);

    }

    /**
     * Notify the user of some error.
     * 
     * @param message The error message.
     */
    public void emitError(String title, String message)
    {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Returns the component which should be get the focus at activation of this panel.
     * 
     * @return the component which should be get the focus at activation of this panel
     */
    public Component getInitialFocus()
    {
        return initialFocus;
    }

    /**
     * Sets the component which should be get the focus at activation of this panel.
     * 
     * @param component which should be get the focus at activation of this panel
     */
    public void setInitialFocus(Component component)
    {
        initialFocus = component;
    }

    /**
     * Calls the langpack of parent InstallerFrame for the String <tt>RuntimeClassName.subkey</tt>.
     * Do not add a point infront of subkey, it is always added in this method.
     * If <tt>RuntimeClassName.subkey</tt> is not found, the super class name will be used
     * until it is <tt>IzPanel</tt>. If no key will be found, null returns.
     * 
     * @param subkey the subkey for the string which should be returned
     * @return the founded string
     */
    public String getI18nStringForClass(String subkey)
    {
        String retval = null;
        Class clazz = this.getClass();
        while (retval == null && !clazz.getName().endsWith(".IzPanel"))
        {
            retval = getI18nStringForClass(clazz.getName(), subkey, null);
            clazz = clazz.getSuperclass();
        }
        return (retval);
    }

    /**
     * Calls the langpack of parent InstallerFrame for the String <tt>RuntimeClassName.subkey</tt>.
     * Do not add a point infront of subkey, it is always added in this method.
     * If no key will be found the key or - if alternate class is null - null returns.
     * 
     * @param subkey the subkey for the string which should be returned
     * @param alternateClass the short name of the class which should be used if no string is
     * present with the runtime class name
     * @return the founded string
     */
    public String getI18nStringForClass(String subkey, String alternateClass)
    {
        return( getI18nStringForClass(getClass().getName(), subkey, alternateClass));

    }

    private String getI18nStringForClass(String curClassName, String subkey, String alternateClass)
    {

        int nameStart = curClassName.lastIndexOf('.') + 1;
        curClassName = curClassName.substring(nameStart, curClassName.length());
        StringBuffer buf = new StringBuffer();
        buf.append(curClassName).append(".").append(subkey);
        String fullkey = buf.toString();
        String retval = parent.langpack.getString(fullkey);
        if (retval == null || retval.startsWith(fullkey))
        {
            if (alternateClass == null) return (null);
            buf.delete(0, buf.length());
            buf.append(alternateClass).append(".").append(subkey);
            retval = parent.langpack.getString(buf.toString());
        }
        if (retval != null && retval.indexOf('$') > -1)
        {
            VariableSubstitutor substitutor = new VariableSubstitutor(idata.getVariables());
            retval = substitutor.substitute(retval, null);
        }
        return (retval);
    }
    
        
    /**
     * Returns the parent of this IzPanel (which is a InstallerFrame).
     * 
     * @return the parent of this IzPanel
     */
    public InstallerFrame getInstallerFrame()
    {
        return (parent);
    }

    // ------------- Helper for common used components ----- START ---

    /**
     * Creates a label via LabelFactory using iconId, pos and method getI18nStringForClass for
     * resolving the text to be used. If the icon id is null, the label will be created also.
     * 
     * @param subkey the subkey which should be used for resolving the text
     * @param alternateClass the short name of the class which should be used if no string is
     * present with the runtime class name
     * @param iconId id string for the icon
     * @param pos horizontal alignment
     * @return the newly created label
     */
    public JLabel createLabel(String subkey, String alternateClass, String iconId, int pos)
    {
        ImageIcon ii = (iconId != null) ? parent.icons.getImageIcon(iconId) : null;
        String msg = getI18nStringForClass(subkey, alternateClass);
        JLabel label = LabelFactory.create(msg, ii, pos);
        if (label != null) label.setFont(getControlTextFont());
        return (label);

    }

    /**
     * Creates a label via LabelFactory with the given ids and the given horizontal alignment. If
     * the icon id is null, the label will be created also. The strings are the ids for the text in
     * langpack and the icon in icons of the installer frame.
     * 
     * @param textId id string for the text
     * @param iconId id string for the icon
     * @param pos horizontal alignment
     * @return the newly created label
     */
    public JLabel createLabel(String textId, String iconId, int pos)
    {
        ImageIcon ii = (iconId != null) ? parent.icons.getImageIcon(iconId) : null;
        JLabel label = LabelFactory.create(parent.langpack.getString(textId),ii, pos);
        if (label != null) label.setFont(getControlTextFont());
        return (label);

    }
    /**
     * Creates a multi line label with the language dependent text given by the text id. The strings
     * is the id for the text in langpack of the installer frame. The horizontal alignment will be
     * LEFT.
     * 
     * @param textId id string for the text
     * @return the newly created multi line label
     */
    public MultiLineLabel createMultiLineLabelLang(String textId)
    {
        return (createMultiLineLabel(parent.langpack.getString(textId)));
    }

    /**
     * Creates a multi line label with the given text. The horizontal alignment will be LEFT.
     * 
     * @param text text to be used in the label
     * @return the newly created multi line label
     */
    public MultiLineLabel createMultiLineLabel(String text)
    {
        return (createMultiLineLabel(text, null, JLabel.LEFT));
    }

    /**
     * Creates a label via LabelFactory with the given text, the given icon id and the given
     * horizontal alignment. If the icon id is null, the label will be created also. The strings are
     * the ids for the text in langpack and the icon in icons of the installer frame.
     * 
     * @param text text to be used in the label
     * @param iconId id string for the icon
     * @param pos horizontal alignment
     * @return the created multi line label
     */
    public MultiLineLabel createMultiLineLabel(String text, String iconId, int pos)
    {
        MultiLineLabel mll = null;
        mll = new MultiLineLabel(text, 0, 0);
        if (mll != null) mll.setFont(getControlTextFont());
        return (mll);
    }

    /**
     * The Font of Labels in many cases
     */
    public Font getControlTextFont()
    {
        return (getLAF() != null ? MetalLookAndFeel.getControlTextFont() : getFont());
    }

    protected static MetalLookAndFeel getLAF()
    {
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf instanceof MetalLookAndFeel) return ((MetalLookAndFeel) laf);
        return (null);
    }

    // ------------- Helper for common used components ----- END ---
    // ------------------- Layout stuff -------------------- START ---
    /**
     * Returns the default GridBagConstraints of this panel.
     * 
     * @return the default GridBagConstraints of this panel
     */
    public GridBagConstraints getDefaultGridBagConstraints()
    {
        startGridBagLayout();
        return defaultGridBagConstraints;
    }

    /**
     * Sets the default GridBagConstraints of this panel to the given object.
     * 
     * @param constraints which should be set as default for this object
     */
    public void setDefaultGridBagConstraints(GridBagConstraints constraints)
    {
        startGridBagLayout();
        defaultGridBagConstraints = constraints;
    }

    /**
     * Resets the grid counters which are used at getNextXGridBagConstraints and
     * getNextYGridBagConstraints.
     */
    public void resetGridCounter()
    {
        gridxCounter = -1;
        gridyCounter = -1;
    }

    /**
     * Returns a newly created GridBagConstraints with the given values and the values from the
     * defaultGridBagConstraints for the other parameters.
     * 
     * @param gridx value to be used for the new constraint
     * @param gridy value to be used for the new constraint
     * @return newly created GridBagConstraints with the given values and the values from the
     * defaultGridBagConstraints for the other parameters
     */
    public GridBagConstraints getNewGridBagConstraints(int gridx, int gridy)
    {
        GridBagConstraints retval = (GridBagConstraints) getDefaultGridBagConstraints().clone();
        retval.gridx = gridx;
        retval.gridy = gridy;
        return (retval);

    }

    /**
     * Returns a newly created GridBagConstraints with the given values and the values from the
     * defaultGridBagConstraints for the other parameters.
     * 
     * @param gridx value to be used for the new constraint
     * @param gridy value to be used for the new constraint
     * @param gridwidth value to be used for the new constraint
     * @param gridheight value to be used for the new constraint
     * @return newly created GridBagConstraints with the given values and the values from the
     * defaultGridBagConstraints for the other parameters
     */
    public GridBagConstraints getNewGridBagConstraints(int gridx, int gridy, int gridwidth,
            int gridheight)
    {
        GridBagConstraints retval = getNewGridBagConstraints(gridx, gridy);
        retval.gridwidth = gridwidth;
        retval.gridheight = gridheight;
        return (retval);
    }

    /**
     * Returns a newly created GridBagConstraints for the next column of the current layout row.
     * 
     * @return a newly created GridBagConstraints for the next column of the current layout row
     * 
     */
    public GridBagConstraints getNextXGridBagConstraints()
    {
        gridxCounter++;
        GridBagConstraints retval = getNewGridBagConstraints(gridxCounter, gridyCounter);
        return (retval);
    }

    /**
     * Returns a newly created GridBagConstraints for the next column of the current layout row
     * using the given parameters.
     * 
     * @param gridwidth width for this constraint
     * @param gridheight height for this constraint
     * @return a newly created GridBagConstraints for the next column of the current layout row
     * using the given parameters
     */
//    private GridBagConstraints getNextXGridBagConstraints(int gridwidth, int gridheight)
//    {
//        GridBagConstraints retval = getNextXGridBagConstraints();
//        retval.gridwidth = gridwidth;
//        retval.gridheight = gridheight;
//        return (retval);
//    }

    /**
     * Returns a newly created GridBagConstraints with column 0 for the next row.
     * 
     * @return a newly created GridBagConstraints with column 0 for the next row
     * 
     */
    public GridBagConstraints getNextYGridBagConstraints()
    {
        gridyCounter++;
        gridxCounter = 0;
        GridBagConstraints retval = getNewGridBagConstraints(0, gridyCounter);
        return (retval);
    }

    /**
     * Returns a newly created GridBagConstraints with column 0 for the next row using the given
     * parameters.
     * 
     * @param gridwidth width for this constraint
     * @param gridheight height for this constraint
     * @return a newly created GridBagConstraints with column 0 for the next row using the given
     * parameters
     */
    public GridBagConstraints getNextYGridBagConstraints(int gridwidth, int gridheight)
    {
        startGridBagLayout();
        GridBagConstraints retval = getNextYGridBagConstraints();
        retval.gridwidth = gridwidth;
        retval.gridheight = gridheight;
        return (retval);
    }

    /**
     * Start layout determining. If it is needed, a dummy component will be created as first row.
     * This will be done, if the IzPack guiprefs modifier with the key "layoutAnchor" has the value
     * "SOUTH" or "SOUTHWEST". The earlier used value "BOTTOM" and the declaration via the IzPack
     * variable <code>IzPanel.LayoutType</code> are also supported.
     */
    public void startGridBagLayout()
    {
        if (gridBagLayoutStarted) return;
        gridBagLayoutStarted = true;
        GridBagLayout layout = new GridBagLayout();
        defaultGridBagConstraints.insets = new Insets(0, 0, getLabelGap(), 0);
        defaultGridBagConstraints.anchor = GridBagConstraints.WEST;
        setLayout(layout);
        switch (getAnchor())
        {
        case GridBagConstraints.SOUTH:
        case GridBagConstraints.SOUTHWEST:
            // Make a header to push the rest to the bottom.
            Filler dummy = new Filler();
            GridBagConstraints gbConstraint = getNextYGridBagConstraints();
            gbConstraint.weighty = 1.0;
            gbConstraint.fill = GridBagConstraints.BOTH;
            gbConstraint.anchor = GridBagConstraints.WEST;
            this.add(dummy, gbConstraint);
            break;
        default:
            break;
        }
        // TODO: impl for layout type CENTER, ...
    }

    /**
     * Complete layout determining. If it is needed, a dummy component will be created as last row.
     * This will be done, if the IzPack guiprefs modifier with the key "layoutAnchor" has the value
     * "NORTH" or "NORTHWEST". The earlier used value "TOP" and the declaration via the IzPack
     * variable <code>IzPanel.LayoutType</code> are also supported.
     */
    public void completeGridBagLayout()
    {
        switch (getAnchor())
        {
        case GridBagConstraints.NORTH:
        case GridBagConstraints.NORTHWEST:
            // Make a footer to push the rest to the top.
            Filler dummy = new Filler();
            GridBagConstraints gbConstraint = getNextYGridBagConstraints();
            gbConstraint.weighty = 1.0;
            gbConstraint.fill = GridBagConstraints.BOTH;
            gbConstraint.anchor = GridBagConstraints.WEST;
            add(dummy, gbConstraint);
            break;
        default:
            break;
        }
    }

    /**
     * Returns the anchor as value declared in GridBagConstraints. Possible are NORTH,
     * NORTHWEST, SOUTH, SOUTHWEST and CENTER. The values can be configured in the
     * xml description file with the variable "IzPanel.LayoutType". The old values
     * "TOP" and "BOTTOM" from the xml file are mapped to NORTH and SOUTH.
     *  
     * @return the anchor defined in the IzPanel.LayoutType variable.
     */
    public static int getAnchor()
    {
        if( ANCHOR >= 0)
            return(ANCHOR);
        AutomatedInstallData idata = AutomatedInstallData.getInstance();  
        String todo;
        if (idata instanceof InstallData
                && ((InstallData) idata).guiPrefs.modifier.containsKey("layoutAnchor"))
            todo = (String) ((InstallData) idata).guiPrefs.modifier.get("layoutAnchor");
        else
            todo = idata.getVariable("IzPanel.LayoutType");
        if (todo == null) // No command, no work.
            ANCHOR = GridBagConstraints.NONE;
        else if("TOP".equals(todo) || "NORTH".equals(todo))
            ANCHOR = GridBagConstraints.NORTH;
        else if("BOTTOM".equals(todo) || "SOUTH".equals(todo))
            ANCHOR = GridBagConstraints.SOUTH;
        else if("SOUTHWEST".equals(todo))
            ANCHOR = GridBagConstraints.SOUTHWEST;
        else if("NORTHWEST".equals(todo))
            ANCHOR = GridBagConstraints.NORTHWEST;
        else if("CENTER".equals(todo))
            ANCHOR = GridBagConstraints.CENTER;
        return(ANCHOR);
    }
    
    /**
     * Returns the gap which should be used for (multiline) labels 
     *  to create a consistent view. The value will
     *  be configurable by the guiprefs modifier "labelGap".
     * @return the label gap depend on the xml-configurable guiprefs modifier "labelGap" 
     */
    public static int getLabelGap()
    {
        return (getGap(LABEL_GAP));
    }

    /**
     * Returns the gap which should be used between the given gui objects. The
     * value will be configurable by guiprefs modifiers. Valid values are all
     * entries in the static String array GAP_NAME_LOOK_UP of this class.
     * There are constant ints for the indexes of this array.
     * 
     * @param gapId index in array GAP_NAME_LOOK_UP for the needed gap
     * 
     * @return the gap depend on the xml-configurable guiprefs modifier
     */
    public static int getGap(int gapId)
    {
        if (gapId < 0 || gapId >= GAPS.length - 2)
            throw new IllegalArgumentException("gapId out of range.");
        if (GAPS[GAPS.length - 1] >= 0) return (GAPS[gapId]);
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        if (!(idata instanceof InstallData)) return (GAPS[gapId]);
        String var = null;
        for (int i = 0; i < GAP_NAME_LOOK_UP.length; ++i)
        {
            if (((InstallData) idata).guiPrefs.modifier.containsKey(GAP_NAME_LOOK_UP[i]))
            {
                var = (String) ((InstallData) idata).guiPrefs.modifier.get(GAP_NAME_LOOK_UP[i]);
                if (var != null)
                {
                    try
                    {
                        GAPS[i] = Integer.parseInt(var);
                    }
                    catch (NumberFormatException nfe)
                    {
                        // Do nothing else use the default value.
                        // Need to set it again at this position??
                    }
                }
            }

        }
        return (GAPS[gapId]);
    }
    // ------------------- Layout stuff -------------------- END ---

    // ------------------- Summary stuff -------------------- START ---
    /**
     * This method will be called from the SummaryPanel to get the summary of this class which
     * should be placed in the SummaryPanel. The returned text should not contain a caption of this
     * item. The caption will be requested from the method getCaption. If <code>null</code>
     * returns, no summary for this panel will be generated. Default behaviour is to return
     * <code>null</code>.
     * 
     * @return the summary for this class
     */
    public String getSummaryBody()
    {
        return null;
    }

    /**
     * This method will be called from the SummaryPanel to get the caption for this class which
     * should be placed in the SummaryPanel. If <code>null</code> returns, no summary for this
     * panel will be generated. Default behaviour is to return the string given by langpack for the
     * key <code>&lt;current class name>.summaryCaption&gt;</code> if exist, else the string
     * &quot;summaryCaption.&lt;ClassName&gt;&quot;.
     * 
     * @return the caption for this class
     */
    public String getSummaryCaption()
    {
        return (getI18nStringForClass("summaryCaption", this.getClass().getName()));
    }

    // ------------------- Summary stuff -------------------- END ---

    // ------------------- Inner classes ------------------- START ---
    public static class Filler extends JComponent
    {

        private static final long serialVersionUID = 3258416144414095153L;

    }
    // ------------------- Inner classes ------------------- END ---

    
    /**
     * Returns whether this panel will be hidden general or not.
     * A hidden panel will be not counted  in the step counter and
     * for panel icons.
     * @return whether this panel will be hidden general or not
     */
    public boolean isHidden()
    {
        return hidden;
    }

    
    /**
     * Set whether this panel should be hidden or not.
     * A hidden panel will be not counted  in the step counter and
     * for panel icons.
     * @param hidden flag to be set
     */
    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

}
