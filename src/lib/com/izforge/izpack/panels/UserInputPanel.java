/*
 * IzPack version 3.1.0 pre1 (build 2002.09.21)
 * Copyright (C) 2002 Elmar Grom
 *
 * File :               UserInputPanel.java
 * Description :        A panel to collect input form the end user.
 * Author's email :     elmar@grom.net
 * Author's Website :   http://www.izforge.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package   com.izforge.izpack.panels;

import    java.io.*;
import    java.util.*;

import    java.awt.*;
import    java.awt.event.*;

import    javax.swing.*;
import    javax.swing.border.*;

import    com.izforge.izpack.installer.*;
import    com.izforge.izpack.util.*;
import    com.izforge.izpack.gui.*;
import    com.izforge.izpack.*;

import    net.n3.nanoxml.*;

/*---------------------------------------------------------------------------*/
/**
 * This panel is designed to collect user input during the installation
 * process. The panel is initially blank and is populated with input elements
 * based on the XML specification in a resource file.
 *
 *
 * @version  0.0.1 / 10/19/02
 * @author   getDirectoryCreated
 */
/*---------------------------------------------------------------------------*/
/*$
 * @design
 *
 * Each field is specified in its own node, containing attributes and data.
 * When this class is instantiated, the specification is read and analyzed.
 * Each field node is processed based on its type. An specialized member
 * function is called for each field type that creates the necessary UI
 * elements. All UI elements are stored in the uiElements vector. Elements
 * are packaged in an object array that must follow this pattern:
 *
 * index 0 - a String object, that specifies the field type. This is
 *           identical to the string used to identify the field type
 *           in the XML file.
 * index 1 - a String object that contains the variable name for substitution.
 * index 2 - the constraints object that should be used for positioning the
 *           UI element
 * index 3 - the UI element itself
 * index 4 - a Vector containg a list of pack for which the item should be
 *           created. This is used by buildUI() to decide if the item should
 *           be added to the UI.
 *
 * In some cases additional entries are used. The use depends on the specific
 * needs of the type of input field.
 *
 * When the panel is activated, the method buildUI() walks the list of UI
 * elements adds them to the panel together with the matching constraint.
 *
 * When an attempt is made to move on to another panel, the method readInput()
 * walks the list of UI elements again and calls specialized methods that
 * know how to read the user input from each of the UI elemnts and set the
 * associated varaible.
 *
 * The actual variable substitution is not performed by this panel but by
 * the variable substitutor.
 *--------------------------------------------------------------------------*/
public class UserInputPanel extends IzPanel
{
  private static final int    POS_TYPE                      = 0;
  private static final int    POS_VARIABLE                  = 1;
  private static final int    POS_CONSTRAINTS               = 2;
  private static final int    POS_FIELD                     = 3;
  private static final int    POS_PACKS                     = 4;
  private static final int    POS_TRUE                      = 5;
  private static final int    POS_FALSE                     = 6;

  /** The name of the XML file that specifies the panel layout */
  private static final String SPEC_FILE_NAME                = "userInputSpec.xml";

  /** how the spec node for a specific panel is identified */
  private static final String NODE_ID                       = "panel";
  private static final String FIELD_NODE_ID                 = "field";
  private static final String INSTANCE_IDENTIFIER           = "order";
  private static final String TYPE                          = "type";
  private static final String DESCRIPTION                   = "description";
  private static final String VARIABLE                      = "variable";
  private static final String TEXT                          = "txt";
  private static final String KEY                           = "id";
  private static final String SPEC                          = "spec";
  private static final String SET                           = "set";
  private static final String TRUE                          = "true";
  private static final String FALSE                         = "false";
  private static final String ALIGNMENT                     = "align";
  private static final String LEFT                          = "left";
  private static final String CENTER                        = "center";
  private static final String RIGHT                         = "right";
  private static final String TOP                           = "top";
  private static final String BOTTOM                        = "bottom";
  private static final String ITALICS                       = "italic";
  private static final String BOLD                          = "bold";
  private static final String SIZE                          = "size";

  private static final String FIELD_LABEL                   = "label";

  private static final String TITLE_FIELD                   = "title";

  private static final String TEXT_FIELD                    = "text";
  private static final String TEXT_SIZE                     = "size";
  private static final String STATIC_TEXT                   = "staticText";

  private static final String COMBO_FIELD                   = "combo";
  private static final String COMBO_CHOICE                  = "choice";
  private static final String COMBO_VALUE                   = "value";

  private static final String RADIO_FIELD                   = "radio";
  private static final String RADIO_CHOICE                  = "choice";
  private static final String RADIO_VALUE                   = "value";

  private static final String SPACE_FIELD                   = "space";
  private static final String DIVIDER_FIELD                 = "divider";
  private static final String CHECK_FIELD                   = "check";

  private static final String RULE_FIELD                    = "rule";
  private static final String RULE_LAYOUT                   = "layout";
  private static final String RULE_SEPARATOR                = "separator";
  private static final String RULE_RESULT_FORMAT            = "resultFormat";
  private static final String RULE_PLAIN_STRING             = "plainString";
  private static final String RULE_DISPLAY_FORMAT           = "displayFormat";
  private static final String RULE_SPECIAL_SEPARATOR        = "specialSeparator";
  private static final String RULE_ENCRYPTED                = "processed";
  private static final String RULE_VALIDATOR                = "validator";
  private static final String RULE_PROCESSOR                = "processor";
  private static final String RULE_CLASS                    = "class";

  private static final String PACKS                         = "createForPack";
  private static final String NAME                          = "name";

  /** specifies the percentage of the total panel width to use for the space
      buffer on the right and left side. */
  private static final int    SIDE_BUFFER_RATIO             = 5;
  /** the margin to use to the left of all input fields, except the
      RuleInputField. The RuleInputField has a natural margin to the left side
      because the FlowLayout which is used for laying out the internal fields
      uses the horizontal gap setting also at the very left and right margin.
      The margin defined here is used with all other fields to compensate for
      the layout offset between fields that would otherwise occur. */
  private static final int    LEFT_FIELD_MARGIN             = 5;

  private static int          instanceCount   = 0;
  private        int          instanceNumber  = 0;
  private        boolean      uiBuilt         = false;

  /** If there is a possibility that some UI elements will not get added we
      can not allow to go back to the PacksPanel, because the process of
      building the UI is not reversable. This variable keeps track if any
      packs have been defined and will be used to make a decision for
      locking the 'previous' button. */
  private         boolean     packsDefined    = false;

  private InstallerFrame      parent;
  /** The parsed result from reading the XML specification from the file */
  private XMLElement          spec;
  private boolean             haveSpec = false;

  /** Holds the references to all of the UI elements */
  private Vector              uiElements      = new Vector ();
  /** Holds the references to all radio button groups */
  private Vector              buttonGroups    = new Vector ();

  private TwoColumnLayout     layout;
  private LocaleDatabase      langpack        = null;


 /*--------------------------------------------------------------------------*/
 // This method can be used to search for layout problems. If this class is
 // compiled with this method uncommented, the layout guides will be shown
 // on the panel, making it possible to see iff all components are placed
 // correctly.
 /*--------------------------------------------------------------------------*/
//  public void paint (Graphics graphics)
//  {
//    super.paint (graphics);
//    layout.showRules ((Graphics2D)graphics, Color.red);
//  }
 /*--------------------------------------------------------------------------*/
 /**
  * Constructs a <code>UserInputPanel</code>.
  *
  * @param     parent       reference to the application frame
  * @param     installData  shared information about the installation
  */
 /*--------------------------------------------------------------------------*/
  public UserInputPanel (InstallerFrame parent,
                         InstallData    installData)

  {
    super (parent, installData);

    instanceNumber = instanceCount++;
    this.parent = parent;

    // ----------------------------------------------------
    // ----------------------------------------------------
    layout = new TwoColumnLayout (10, 5, 30, 25, TwoColumnLayout.LEFT);
    setLayout (layout);

    // ----------------------------------------------------
    // get a locale database
    // ----------------------------------------------------
    ResourceManager resources = new ResourceManager (installData);
    try
    {
      langpack = new LocaleDatabase (resources.getInputStream (SPEC_FILE_NAME));
    }
    catch (Throwable exception)
    {}

    // ----------------------------------------------------
    // read the specifications
    // ----------------------------------------------------
    try
    {
      readSpec ();
    }
    catch (Throwable exception)
    {
      // log the problem
      exception.printStackTrace ();
    }

    // ----------------------------------------------------
    // process all field nodes. Each field node is analyzed
    // for its type, then an appropriate memeber function
    // is called that will create the appropriate UI elements.
    // ----------------------------------------------------
    Vector fields = spec.getChildrenNamed (FIELD_NODE_ID);

    for (int i = 0; i < fields.size (); i++)
    {
      XMLElement  field     = (XMLElement)fields.elementAt (i);
      String      attribute = field.getAttribute (TYPE);

      if (attribute != null)
      {
        if (attribute.equals (RULE_FIELD))
        {
          addRuleField (field);
        }
        else if (attribute.equals (TEXT_FIELD))
        {
          addTextField (field);
        }
        else if (attribute.equals (COMBO_FIELD))
        {
          addComboBox (field);
        }
        else if (attribute.equals (RADIO_FIELD))
        {
          addRadioButton (field);
        }
        else if (attribute.equals (SPACE_FIELD))
        {
          addSpace (field);
        }
        else if (attribute.equals (DIVIDER_FIELD))
        {
          addDivider (field);
        }
        else if (attribute.equals (CHECK_FIELD))
        {
          addCheckBox (field);
        }
        else if (attribute.equals (STATIC_TEXT))
        {
          addText (field);
        }
        else if (attribute.equals (TITLE_FIELD))
        {
          addTitle (field);
        }
      }
    }
  }

//  public boolean panelDeactivate ()
  public void panelDesactivate ()
  {
//    number.validate ();
//    idata.getVariableValueMap ().setVariable ("serialKey", number.getText ());
//    return (true);
  }

  public boolean isValidated ()
  {
    return (readInput ());
  }
 /*--------------------------------------------------------------------------*/
 /**
  * This method is called when the panel becomes active.
  */
 /*--------------------------------------------------------------------------*/
  public void panelActivate ()
  {
    Vector forPacks = spec.getChildrenNamed (PACKS);
    if (!itemRequiredFor (forPacks))
    {
      parent.skipPanel ();
      return;
    }
    if (!haveSpec)
    {
      parent.skipPanel ();
      return;
    }
    if (uiBuilt)
    {
      return;
    }

    buildUI ();
    uiBuilt = true;

    if (packsDefined)
    {
      parent.lockPrevButton ();
    }
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Builds the UI and makes it ready for display
  */
 /*--------------------------------------------------------------------------*/
  private void buildUI ()
  {
    Object [] uiElement;

    for (int i = 0; i < uiElements.size (); i++)
    {
      uiElement = (Object [])uiElements.elementAt (i);

      if (itemRequiredFor ((Vector)uiElement [POS_PACKS]))
      {
        try
        {
          add ((JComponent)uiElement [POS_FIELD], uiElement [POS_CONSTRAINTS]);
        }
        catch (Throwable exception)
        {
          System.out.println ("Internal format error in field: " + uiElement [0].toString ());  // !!! logging
        }
      }
    }
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Reads the input data from all UI elements and sets the associated variables.
  *
  * @return    <code>true</code> if the operation is successdul, otherwise
  *            <code>false</code>.
  */
 /*--------------------------------------------------------------------------*/
  private boolean readInput ()
  {
    boolean     success;
    String      fieldType = null;
    Object []   field     = null;

    for (int i = 0; i < uiElements.size (); i++)
    {
      field     = (Object [])uiElements.elementAt (i);

      if (field != null)
      {
        fieldType = (String)(field [POS_TYPE]);

        // ------------------------------------------------
        if (fieldType.equals (RULE_FIELD))
        {
          success = readRuleField (field);
          if (!success)
          {
            return (false);
          }
        }

        // ------------------------------------------------
        else if (fieldType.equals (TEXT_FIELD))
        {
          success = readTextField (field);
          if (!success)
          {
            return (false);
          }
        }

        // ------------------------------------------------
        else if (fieldType.equals (COMBO_FIELD))
        {
          success = readComboBox (field);
          if (!success)
          {
            return (false);
          }
        }

        // ------------------------------------------------
        else if (fieldType.equals (RADIO_FIELD))
        {
          success = readRadioButton (field);
          if (!success)
          {
            return (false);
          }
        }

        // ------------------------------------------------
        else if (fieldType.equals (CHECK_FIELD))
        {
          success = readCheckBox (field);
          if (!success)
          {
            return (false);
          }
        }
      }
    }
    return (true);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Reads the XML specification for the panel layout. The result is
  * stored in spec.
  *
  * @exception Exception for any problems in reading the specification
  */
 /*--------------------------------------------------------------------------*/
  private void readSpec () throws Exception
  {
    InputStream input = null;
    XMLElement  data;
    Vector      specElements;
    String      attribute;
    String      instance = Integer.toString (instanceNumber);

    try
    {
      input = parent.getResource (SPEC_FILE_NAME);
    }
    catch (Exception exception)
    {
      haveSpec = false;
      return;
    }
    if (input == null)
    {
      haveSpec = false;
      return;
    }

    // initialize the parser
    StdXMLParser parser = new StdXMLParser ();
    parser.setBuilder   (new StdXMLBuilder ());
    parser.setValidator (new NonValidator ());
    parser.setReader    (new StdXMLReader (input));

    // get the data
    data = (XMLElement) parser.parse ();

    // extract the spec to this specific panel instance
    if (data.hasChildren ())
    {
      specElements = data.getChildrenNamed (NODE_ID);
      for (int i = 0; i < specElements.size (); i++)
      {
        data      = (XMLElement)specElements.elementAt (i);
        attribute = data.getAttribute (INSTANCE_IDENTIFIER);

        if (instance.equals (attribute))
        {
          // use the current element as spec
          spec = data;
          // close the stream
          input.close ();
          haveSpec = true;
          return;
        }
      }

      haveSpec = false;
      return;
    }

    haveSpec = false;
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Adds the title to the panel. There can only be one title, if mutiple
  * titles are defined, they keep overwriting what has already be defined,
  * so that the last definition is the one that prevails.
  *
  * @param     spec  a <code>XMLElement</code> containing the specification
  *                  for the title.
  */
 /*--------------------------------------------------------------------------*/
  private void addTitle (XMLElement spec)
  {
    String  title       = getText       (spec);
    boolean italic      = getBoolean    (spec, ITALICS, false);
    boolean bold        = getBoolean    (spec, BOLD, false);
    float   multiplier  = getFloat      (spec, SIZE, 2.0f);
    int     justify     = getAlignment  (spec);

    if (title != null)
    {
      JLabel label = new JLabel (title);
      Font   font  = label.getFont ();
      float  size  = font.getSize ();
      int    style = 0;
      
      if (bold)
      {
        style = style + Font.BOLD;
      }
      if (italic)
      {
        style = style + Font.ITALIC;
      }

      font = font.deriveFont (style, (size * multiplier));
      label.setFont (font);
      label.setAlignmentX (0);

      TwoColumnConstraints constraints = new TwoColumnConstraints ();
      constraints.align     = justify;
      constraints.position  = constraints.NORTH;

      add (label, constraints);
    }
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Adds a rule field to the list of UI elements.
  *
  * @param     spec  a <code>XMLElement</code> containing the specification
  *                  for the rule field.
  */
 /*--------------------------------------------------------------------------*/
  private void addRuleField (XMLElement spec)
  {
    Vector forPacks = spec.getChildrenNamed (PACKS);
    XMLElement      element       = spec.getFirstChildNamed (SPEC);
    String          variable      = spec.getAttribute (VARIABLE);
    RuleInputField  field         = null;
    JLabel          label;
    String          layout;
    String          set;
    String          separator;
    String          format;
    String          description   = null;
    String          validator     = null;
    String          processor     = null;
    int             resultFormat  = RuleInputField.DISPLAY_FORMAT;

    // ----------------------------------------------------
    // extract the specification details
    // ----------------------------------------------------
    if (element != null)
    {
      label     = new JLabel (getText (element));
      layout    = element.getAttribute (RULE_LAYOUT);
      set       = element.getAttribute (SET);
      separator = element.getAttribute (RULE_SEPARATOR);
      format    = element.getAttribute (RULE_RESULT_FORMAT);

      if (format != null)
      {
        if (format.equals (RULE_PLAIN_STRING))
        {
          resultFormat = RuleInputField.PLAIN_STRING;
        }
        else if (format.equals (RULE_DISPLAY_FORMAT))
        {
          resultFormat = RuleInputField.DISPLAY_FORMAT;
        }
        else if (format.equals (RULE_SPECIAL_SEPARATOR))
        {
          resultFormat = RuleInputField.SPECIAL_SEPARATOR;
        }
        else if (format.equals (RULE_ENCRYPTED))
        {
          resultFormat = RuleInputField.ENCRYPTED;
        }
      }
    }
    // ----------------------------------------------------
    // if there is no specification element, return without
    // doing anything.
    // ----------------------------------------------------
    else
    {
      return;
    }

    // ----------------------------------------------------
    // get the description and add it to the list UI
    // elements if it exists.
    // ----------------------------------------------------
    element = spec.getFirstChildNamed (DESCRIPTION);
    addDescription (element, forPacks);

    // ----------------------------------------------------
    // get the validator and processor if they are defined
    // ----------------------------------------------------
    element = spec.getFirstChildNamed (RULE_VALIDATOR);
    if (element != null)
    {
      validator = element.getAttribute (RULE_CLASS);
    }

    element = spec.getFirstChildNamed (RULE_PROCESSOR);
    if (element != null)
    {
      processor = element.getAttribute (RULE_CLASS);
    }

    // ----------------------------------------------------
    // create an instance of RuleInputField based on the
    // extracted specifications, then add it to the list
    // of UI elements.
    // ----------------------------------------------------
    field = new RuleInputField (layout,
                                set,
                                separator,
                                validator,
                                processor,
                                resultFormat,
                                getToolkit ());

    TwoColumnConstraints constraints = new TwoColumnConstraints ();
    constraints.position              = constraints.WEST;

    uiElements.add (new Object [] {FIELD_LABEL, null, constraints, label, forPacks});

    TwoColumnConstraints constraints2 = new TwoColumnConstraints ();
    constraints2.position             = constraints2.EAST;

    uiElements.add (new Object [] {RULE_FIELD, variable, constraints2, field, forPacks});
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Reads the data from the rule input field and sets the associated variable.
  *
  * @param     field  the object array that holds the details of the field.
  *
  * @return    <code>true</code> if there was no problem reading the data or
  *            if there was an irrecovarable problem. If there was a problem
  *            that can be corrected by the operator, an error dialog is
  *            popped up and <code>false</code> is returned.
  */
 /*--------------------------------------------------------------------------*/
  private boolean readRuleField (Object [] field)
  {
    RuleInputField  ruleField = null;
    String          variable  = null;

    try
    {
      ruleField = (RuleInputField)field [POS_FIELD];
      variable  = (String)field [POS_VARIABLE];
    }
    catch (Throwable exception)
    {
      return (true);
    }
    if ((variable == null) || (ruleField == null))
    {
      return (true);
    }

/*    boolean success = ruleField.validate ()
    if (!success)
    {
      // !!! pop up a dialog!
      return (false);
    }

    ruleField.processInput ();*/

    idata.getVariableValueMap ().setVariable (variable, ruleField.getText ());
    return (true);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Adds a text field to the list of UI elements
  *
  * @param     spec  a <code>XMLElement</code> containing the specification
  *                  for the text field.
  */
 /*--------------------------------------------------------------------------*/
  private void addTextField (XMLElement spec)
  {
    Vector      forPacks = spec.getChildrenNamed (PACKS);
    XMLElement  element  = spec.getFirstChildNamed (SPEC);
    JLabel      label;
    String      set;
    int         size;

    String      variable = spec.getAttribute (VARIABLE);
    if ((variable == null) || (variable.length () == 0))
    {
      return;
    }

    // ----------------------------------------------------
    // extract the specification details
    // ----------------------------------------------------
    if (element != null)
    {
      label = new JLabel (getText (element));
      set   = element.getAttribute (SET);
      if (set == null)
      {
        set = "";
      }
      try
      {
        size = Integer.parseInt (element.getAttribute (TEXT_SIZE));
      }
      catch (Throwable exception)
      {
        size = 1;
      }
    }
    // ----------------------------------------------------
    // if there is no specification element, return without
    // doing anything.
    // ----------------------------------------------------
    else
    {
      return;
    }

    // ----------------------------------------------------
    // get the description and add it to the list UI
    // elements if it exists.
    // ----------------------------------------------------
    element = spec.getFirstChildNamed (DESCRIPTION);
    addDescription (element, forPacks);

    // ----------------------------------------------------
    // construct the UI element and add it to the list
    // ----------------------------------------------------
    JTextField field = new JTextField (set, size);
    field.setCaretPosition (0);

    TwoColumnConstraints constraints = new TwoColumnConstraints ();
    constraints.position  = constraints.WEST;

    uiElements.add (new Object [] {FIELD_LABEL, null, constraints, label, forPacks});

    TwoColumnConstraints constraints2 = new TwoColumnConstraints ();
    constraints2.position  = constraints2.EAST;

    uiElements.add (new Object [] {TEXT_FIELD, variable, constraints2, field, forPacks});
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Reads data from the text field and sets the associated variable.
  *
  * @param     field  the object array that holds the details of the field.
  *
  * @return    <code>true</code> if there was no problem reading the data or
  *            if there was an irrecovarable problem. If there was a problem
  *            that can be corrected by the operator, an error dialog is
  *            popped up and <code>false</code> is returned.
  */
 /*--------------------------------------------------------------------------*/
  private boolean readTextField (Object [] field)
  {
    JTextField  textField = null;
    String      variable  = null;
    String      value     = null;

    try
    {
      textField = (JTextField)field [POS_FIELD];
      variable  = (String)field [POS_VARIABLE];
      value     = textField.getText ();
    }
    catch (Throwable exception)
    {
      return (true);
    }
    if ((variable == null) || (value == null))
    {
      return (true);
    }

    idata.getVariableValueMap ().setVariable (variable, value);
    return (true);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Adds a combo box to the list of UI elements.<br>
  * This is a complete example of a valid XML specification
  * <pre>
  * <field type="combo" variable="testVariable">
  *   <description text="Description for the combo box" key="a key for translated text"/>
  *   <spec text="label" key="key for the label"/>
  *     <choice text="choice 1" key="" value="combo box 1"/>
  *     <choice text="choice 2" key="" value="combo box 2" set="true"/>
  *     <choice text="choice 3" key="" value="combo box 3"/>
  *     <choice text="choice 4" key="" value="combo box 4"/>
  *   </spec>
  * </field>
  * </pre>
  * @param     spec  a <code>XMLElement</code> containing the specification
  *                  for the combo box.
  */
 /*--------------------------------------------------------------------------*/
  private void addComboBox (XMLElement spec)
  {
    Vector      forPacks  = spec.getChildrenNamed (PACKS);
    XMLElement  element   = spec.getFirstChildNamed (SPEC);
    String      variable  = spec.getAttribute (VARIABLE);
    String      value     = null;
    String      text      = null;
    JComboBox   field     = new JComboBox ();
    JLabel      label;

    // ----------------------------------------------------
    // extract the specification details
    // ----------------------------------------------------
    if (element != null)
    {
      label = new JLabel (getText (element));

      Vector choices = element.getChildrenNamed (COMBO_CHOICE);

      if (choices == null)
      {
        return;
      }

      for (int i = 0; i < choices.size (); i++)
      {
        text   = getText ((XMLElement)choices.elementAt (i));
        value  = ((XMLElement)choices.elementAt (i)).getAttribute (COMBO_VALUE);

        field.addItem (text);

        String set    = ((XMLElement)choices.elementAt (i)).getAttribute (SET);
        if (set != null)
        {
          if (set.equals (TRUE))
          {
            field.setSelectedIndex (i);
          }
        }
      }
    }
    // ----------------------------------------------------
    // if there is no specification element, return without
    // doing anything.
    // ----------------------------------------------------
    else
    {
      return;
    }

    // ----------------------------------------------------
    // get the description and add it to the list of UI
    // elements if it exists.
    // ----------------------------------------------------
    element = spec.getFirstChildNamed (DESCRIPTION);
    addDescription (element, forPacks);

    TwoColumnConstraints constraints = new TwoColumnConstraints ();
    constraints.position  = constraints.WEST;

    uiElements.add (new Object [] {FIELD_LABEL, null, constraints, label, forPacks});

    TwoColumnConstraints constraints2 = new TwoColumnConstraints ();
    constraints2.position  = constraints2.EAST;

    uiElements.add (new Object [] {COMBO_FIELD, variable, constraints2, field, forPacks, value});
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Enter description synopsis here. More detailed description after the first period.
  *
  * @param     -
  *
  * @return    -
  *
  * @see       -
  *
  * @exception -
  */
 /*--------------------------------------------------------------------------*/
  private boolean readComboBox (Object [] field)
  {
    String variable = null;
    String value    = null;

    try
    {
      variable = (String)field [POS_VARIABLE];
      value    = (String)field [POS_TRUE];
    }
    catch (Throwable exception)
    {
      return (true);
    }
    if ((variable == null) || (value == null))
    {
      return (true);
    }

    idata.getVariableValueMap ().setVariable (variable, value);
    return (true);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Adds a radio button set to the list of UI elements.<br>
  * This is a complete example of a valid XML specification
  * <pre>
  * <field type="radio" variable="testVariable">
  *   <description text="Description for the radio buttons" key="a key for translated text"/>
  *   <spec text="label" key="key for the label"/>
  *     <choice text="radio 1" key="" value=""/>
  *     <choice text="radio 2" key="" value="" set="true"/>
  *     <choice text="radio 3" key="" value=""/>
  *     <choice text="radio 4" key="" value=""/>
  *     <choice text="radio 5" key="" value=""/>
  *   </spec>
  * </field>
  * </pre>
  * @param     spec  a <code>XMLElement</code> containing the specification
  *                  for the radio button set.
  */
 /*--------------------------------------------------------------------------*/
  private void addRadioButton (XMLElement spec)
  {
    Vector forPacks = spec.getChildrenNamed (PACKS);
    String                variable  = spec.getAttribute (VARIABLE);
    String                value     = null;

    XMLElement            element   = null;
    JLabel                label;

    ButtonGroup           group     = new ButtonGroup ();
    buttonGroups.add (group);

    TwoColumnConstraints  constraints = new TwoColumnConstraints ();
    constraints.position  = constraints.BOTH;
    constraints.indent    = true;
    constraints.stretch   = true;

    // ----------------------------------------------------
    // get the description and add it to the list of UI
    // elements if it exists.
    // ----------------------------------------------------
    element = spec.getFirstChildNamed (DESCRIPTION);
    addDescription (element, forPacks);

    // ----------------------------------------------------
    // extract the specification details
    // ----------------------------------------------------
    element = spec.getFirstChildNamed (SPEC);

    if (element != null)
    {
      label = new JLabel (getText (element));

      Vector choices = element.getChildrenNamed (RADIO_CHOICE);

      if (choices == null)
      {
        return;
      }

      // --------------------------------------------------
      // process each choice element
      // --------------------------------------------------
      for (int i = 0; i < choices.size (); i++)
      {
        JRadioButton choice   = new JRadioButton ();
        choice.setText          (getText ((XMLElement)choices.elementAt (i)));
        value                 = (((XMLElement)choices.elementAt (i)).getAttribute (RADIO_VALUE));

        group.add (choice);

        String set    = ((XMLElement)choices.elementAt (i)).getAttribute (SET);
        if (set != null)
        {
          if (set.equals (TRUE))
          {
            choice.setSelected (true);
          }
        }

        uiElements.add (new Object [] {RADIO_FIELD, variable, constraints, choice, forPacks, value});
      }
    }
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Enter description synopsis here. More detailed description after the first period.
  *
  * @param     -
  *
  * @return    -
  *
  * @see       -
  *
  * @exception -
  */
 /*--------------------------------------------------------------------------*/
  private boolean readRadioButton (Object [] field)
  {
    String variable     = null;
    String value        = null;
    JRadioButton button = null;

    try
    {
      button   = (JRadioButton)field [POS_FIELD];

      if (!button.isSelected ())
      {
        return (true);
      }

      variable = (String)field [POS_VARIABLE];
      value    = (String)field [POS_TRUE];
    }
    catch (Throwable exception)
    {
      return (true);
    }

    if ((variable == null) || (value == null))
    {
    }

    idata.getVariableValueMap ().setVariable (variable, value);
    return (true);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Adds a chackbox to the list of UI elements.
  *
  * @param     spec  a <code>XMLElement</code> containing the specification
  *                  for the checkbox.
  */
 /*--------------------------------------------------------------------------*/
  private void addCheckBox (XMLElement spec)
  {
    Vector      forPacks    = spec.getChildrenNamed (PACKS);
    String      label       = "";
    String      set         = null;
    String      trueValue   = null;
    String      falseValue  = null;
    String      variable    = spec.getAttribute (VARIABLE);
    XMLElement  detail      = spec.getFirstChildNamed (SPEC);

    if (variable == null)
    {
      return;
    }

    if (detail != null)
    {
      label       = getText (detail);
      set         = detail.getAttribute (SET);
      trueValue   = detail.getAttribute (TRUE);
      falseValue  = detail.getAttribute (FALSE);
    }

    JCheckBox   checkbox  = new JCheckBox (label);

    if (set != null)
    {
      if (set.equals (FALSE))
      {
        checkbox.setSelected (false);
      }
      if (set.equals (TRUE))
      {
        checkbox.setSelected (true);
      }
    }

    // ----------------------------------------------------
    // get the description and add it to the list of UI
    // elements if it exists.
    // ----------------------------------------------------
    XMLElement element = spec.getFirstChildNamed (DESCRIPTION);
    addDescription (element, forPacks);

    TwoColumnConstraints constraints = new TwoColumnConstraints ();
    constraints.position  = constraints.BOTH;
    constraints.stretch   = true;
    constraints.indent    = true;

    uiElements.add (new Object [] {CHECK_FIELD, variable, constraints, checkbox, forPacks, trueValue, falseValue});
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Enter description synopsis here. More detailed description after the first period.
  *
  * @param     -
  *
  * @return    -
  *
  * @see       -
  *
  * @exception -
  */
 /*--------------------------------------------------------------------------*/
  private boolean readCheckBox (Object [] field)
  {
    String    variable    = null;
    String    trueValue   = null;
    String    falseValue  = null;
    JCheckBox box         = null;

    try
    {
      box         = (JCheckBox)field [POS_FIELD];
      variable    = (String)field [POS_VARIABLE];
      trueValue   = (String)field [POS_TRUE];
      if (trueValue == null)
      {
        trueValue = "";
      }

      falseValue  = (String)field [POS_FALSE];
      if (falseValue == null)
      {
        falseValue = "";
      }
    }
    catch (Throwable exception)
    {
      return (true);
    }

    if (box.isSelected ())
    {
      idata.getVariableValueMap ().setVariable (variable, trueValue);
    }
    else
    {
      idata.getVariableValueMap ().setVariable (variable, falseValue);
    }

    return (true);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Adds text to the list of UI elements
  *
  * @param     spec  a <code>XMLElement</code> containing the specification
  *                  for the text.
  */
 /*--------------------------------------------------------------------------*/
  private void addText (XMLElement spec)
  {
    Vector forPacks = spec.getChildrenNamed (PACKS);

    addDescription (spec, forPacks);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Adds a dummy field to the list of UI elements to act as spacer.
  *
  * @param     spec  a <code>XMLElement</code> containing other specifications.
  *                  At present this information is not used but might be in
  *                  future versions.
  */
 /*--------------------------------------------------------------------------*/
  private void addSpace (XMLElement spec)
  {
    Vector forPacks = spec.getChildrenNamed (PACKS);
    JPanel panel    = new JPanel ();

    TwoColumnConstraints constraints = new TwoColumnConstraints ();
    constraints.position  = constraints.BOTH;
    constraints.stretch   = true;

    uiElements.add (new Object [] {SPACE_FIELD, null, constraints, panel, forPacks});
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Adds a dividing line to the list of UI elements act as separator.
  *
  * @param     spec  a <code>XMLElement</code> containing additional
  *                  specifications.
  */
 /*--------------------------------------------------------------------------*/
  private void addDivider (XMLElement spec)
  {
    Vector forPacks   = spec.getChildrenNamed (PACKS);
    JPanel panel      = new JPanel ();
    String alignment  = spec.getAttribute (ALIGNMENT);

    if (alignment != null)
    {
      if (alignment.equals (TOP))
      {
        panel.setBorder (BorderFactory.createMatteBorder (1, 0, 0, 0, Color.gray));
      }
      else
      {
        panel.setBorder (BorderFactory.createMatteBorder (0, 0, 1, 0, Color.gray));
      }
    }
    else
    {
      panel.setBorder (BorderFactory.createMatteBorder (0, 0, 1, 0, Color.gray));
    }

    TwoColumnConstraints constraints = new TwoColumnConstraints ();
    constraints.position  = constraints.BOTH;
    constraints.stretch   = true;

    uiElements.add (new Object [] {DIVIDER_FIELD, null, constraints, panel, forPacks});
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Adds a description to the list of UI elements.
  *
  * @param     spec  a <code>XMLElement</code> containing the specification
  *                  for the description.
  */
 /*--------------------------------------------------------------------------*/
  private void addDescription (XMLElement spec,
                               Vector     forPacks)
  {
    String               description;
    TwoColumnConstraints constraints = new TwoColumnConstraints ();
    constraints.position  = constraints.BOTH;
    constraints.stretch   = true;

    if (spec != null)
    {
      description = getText (spec);

      // if we have a description, add it to the UI elements
      if (description != null)
      {
        String  alignment = spec.getAttribute (ALIGNMENT);
        int     justify   = MultiLineLabel.LEFT;

        if (alignment != null)
        {
          if (alignment.equals (LEFT))
          {
            justify     = MultiLineLabel.LEFT;
          }
          else if (alignment.equals (CENTER))
          {
            justify     = MultiLineLabel.CENTER;
          }
          else if (alignment.equals (RIGHT))
          {
            justify     = MultiLineLabel.RIGHT;
          }
        }

        MultiLineLabel label = new MultiLineLabel (description, justify);

        uiElements.add (new Object [] {DESCRIPTION, null, constraints, label, forPacks});
      }
    }
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Retrieves the value of a boolean attribute. If the attribute is found and
  * the values equals the value of the constant <code>TRUE</code> then true
  * is returned. If it equals <code>FALSE</code> the false is returned. In
  * all other cases, including when the attribute is not found, the default
  * value is returned.
  *
  * @param     element        the <code>XMLElement</code> to search for the
  *                           attribute.
  * @param     attribute      the attribute to search for
  * @param     defaultValue   the default value to use if the attribute does
  *                           not exist or a illegal value was discovered.
  *
  * @return    <code>true</code> if the attribute is found and the value
  *            equals the the constant <code>TRUE</code>. <<code> if the
  *            attribute is <code>FALSE</code>. In all other cases the
  *            default value is returned.
  */
 /*--------------------------------------------------------------------------*/
  private boolean getBoolean (XMLElement element,
                              String     attribute,
                              boolean    defaultValue)
  {
    boolean result = defaultValue;
    
    if ((attribute != null) && (attribute.length () > 0))
    {
      String value = element.getAttribute (attribute);
      
      if (value != null)
      {
        if (value.equals (TRUE))
        {
          result = true;
        }
        else if (value.equals (FALSE))
        {
          result = false;
        }
      }
    }
    
    return (result);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Retrieves the value of an integer attribute. If the attribute is not
  * found or the value is non-numeric then the default value is returned.
  *
  * @param     element      the <code>XMLElement</code> to search for the
  *                         attribute.
  * @param     attribute    the attribute to search for
  * @param     defaultValue the default value to use in case the attribute
  *                         does not exist.
  *
  * @return    the value of the attribute. If the attribute is not found or
  *            the content is not a legal integer, then the default value is
  *            returned.
  */
 /*--------------------------------------------------------------------------*/
  private int getInt (XMLElement  element, 
                      String      attribute,
                      int         defaultValue)
  {
    int result = defaultValue;
    
    if ((attribute != null) && (attribute.length () > 0))
    {
      try
      {
        result = Integer.parseInt (element.getAttribute (attribute));
      }
      catch (Throwable exception)
      {}
    }
    
    return (result);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Retrieves the value of a floating point attribute. If the attribute is not
  * found or the value is non-numeric then the default value is returned.
  *
  * @param     element      the <code>XMLElement</code> to search for the
  *                         attribute.
  * @param     attribute    the attribute to search for
  * @param     defaultValue the default value to use in case the attribute
  *                         does not exist.
  *
  * @return    the value of the attribute. If the attribute is not found or
  *            the content is not a legal integer, then the default value is
  *            returned.
  */
 /*--------------------------------------------------------------------------*/
  private float getFloat (XMLElement  element, 
                          String      attribute, 
                          float       defaultValue)
  {
    float result = defaultValue;
    
    if ((attribute != null) && (attribute.length () > 0))
    {
      try
      {
        result = Float.parseFloat (element.getAttribute (attribute));
      }
      catch (Throwable exception)
      {}
    }
    
    return (result);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Extracts the text from an <code>XMLElement</code>. The text must be
  * defined in the resource file under the key defined in the <code>key</code>
  * attribute or as value of the attribute <code>text</code>.
  *
  * @param     element  the <code>XMLElement</code> from which to extract
  *                     the text.
  *
  * @return    The text defined in the <code>XMLElement</code>. If no text
  *            can be located, <code>null</code> is returned.
  */
 /*--------------------------------------------------------------------------*/
  private String getText (XMLElement element)
  {
    if (element == null)
    {
      return (null);
    }

    String key  = element.getAttribute (KEY);
    String text = null;

    if ((key != null) && (langpack != null))
    {
      try
      {
        text = langpack.getString (key);
      }
      catch (Throwable exception)
      {
        text = null;
      }
    }

    // if there is no text in the description, then
    // we were unable to retrieve it form the resource.
    // In this case try to get the text directly from
    // the XMLElement
    if (text == null)
    {
      text = element.getAttribute (TEXT);
    }

    return (text);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Retreives the alignment setting for the <code>XMLElement</code>. The
  * default value in case the <code>ALIGNMENT</code> attribute is not
  * found or the value is illegal is <code>TwoColumnConstraints.LEFT</code>.
  *
  * @param     element  the <code>XMLElement</code> from which to extract
  *                     the alignment setting.
  *
  * @return    the alignement setting for the <code>XMLElement</code>. The
  *            value is either <code>TwoColumnConstraints.LEFT</code>,
  *            <code>TwoColumnConstraints.CENTER</code> or
  *            <code>TwoColumnConstraints.RIGHT</code>.
  *
  * @see       com.izforge.izpack.gui.TwoColumnConstraints
  */
 /*--------------------------------------------------------------------------*/
  private int getAlignment (XMLElement element)
  {
    int result = TwoColumnConstraints.LEFT;

    String value = element.getAttribute (ALIGNMENT);
    
    if (value != null)
    {
      if (value.equals (LEFT))
      {
        result = TwoColumnConstraints.LEFT;
      }
      else if (value.equals (CENTER))
      {
        result = TwoColumnConstraints.CENTER;
      }
      else if (value.equals (RIGHT))
      {
        result = TwoColumnConstraints.RIGHT;
      }
    }
    
    return (result);
  }
 /*--------------------------------------------------------------------------*/
 /**
  * Verifies if an item is required for any of the packs listed. An item is
  * required for a pack in the list if that pack is actually selected for
  * installation.
  * <br><br>
  * <b>Note:</b><br>
  * If the list of selected packs is empty then <code>true</code> is always
  * returnd. The same is true if the <code>packs</code> list is empty.
  *
  * @param     packs  a <code>Vector</code> of <code>String</code>s. Each of
  *                   the strings denotes a pack for which an item
  *                   should be created if the pack is actually installed.
  *
  * @return    <code>true</code> if the item is required for at least
  *            one pack in the list, otherwise returns <code>false</code>.
  */
 /*--------------------------------------------------------------------------*/
 /*$
  * @design
  *
  * The information about the installed packs comes from
  * InstallData.selectedPacks. This assumes that this panel is presented to
  * the user AFTER the PacksPanel.
  *--------------------------------------------------------------------------*/
  private boolean itemRequiredFor (Vector packs)
  {
    String selected;
    String required;

    if (packs.size () == 0)
    {
      return (true);
    }

    // ----------------------------------------------------
    // We are getting to this point if any packs have been
    // specified. This means that there is a possibility
    // that some UI elements will not get added. This
    // means that we can not allow to go back to the
    // PacksPanel, because the process of building the
    // UI is not reversable.
    // ----------------------------------------------------
    packsDefined = true;

    // ----------------------------------------------------
    // analyze if the any of the packs for which the item
    // is required have been selected for installation.
    // ----------------------------------------------------
    for (int i = 0; i < idata.selectedPacks.size (); i++)
    {
      selected = ((Pack)idata.selectedPacks.get (i)).name;

      for (int k = 0; k < packs.size (); k++)
      {
        required = (String)((XMLElement)packs.elementAt (k)).getAttribute (NAME, "");
        if (selected.equals (required))
        {
          return (true);
        }
      }
    }

    return (false);
  }
}
/*---------------------------------------------------------------------------*/
