/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               JDKPathPanel.java
 *  Description :        A panel to selct the JDK path.
 *  Author's email :     bartzkau@users.berlios.de
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
package com.izforge.izpack.panels;

import java.io.File;
import java.util.StringTokenizer;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.FileExecutor;

/**
 * Panel which asks for the JDK path.
 *
 * @author  Klaus Bartz
 *
 */
public class JDKPathPanel extends PathInputPanel
{
  private static final String[] testFiles = new String[] 
    {"lib" + File.separator + "tools.jar"};

  private String detectedVersion;
  private String minVersion = null;
  private String maxVersion = null;
  private String variableName;
  /**
   *  The constructor.
   *
   * @param  parent  The parent window.
   * @param  idata   The installation data.
   */
  public JDKPathPanel(InstallerFrame parent, InstallData idata)
  {
    super(parent, idata);
    setMustExist(true);
    setExistFiles(JDKPathPanel.testFiles);
    setMinVersion(idata.getVariable("JDKPathPanel.minVersion"));
    setMaxVersion(idata.getVariable("JDKPathPanel.maxVersion"));
    setVariableName("JDKPath");
  }
  /**
   *  Indicates wether the panel has been validated or not.
   *
   * @return    Wether the panel has been validated or not.
   */
  public boolean isValidated()
  {
    if( super.isValidated())
    {
      if(  verifyVersion() )
      {
        idata.setVariable(getVariableName(),pathSelectionPanel.getPath());
        return( true );
      }
      // Bad version detected.
      String min = getMinVersion();
      String max = getMaxVersion();
      StringBuffer message = new StringBuffer();
        message.append(parent.langpack.getString("JDKPathPanel.badVersion1")).
          append(getDetectedVersion()).
        append(parent.langpack.getString("JDKPathPanel.badVersion2"));
      if( min != null&& max != null )
        message.append( min ).append(" - ").append( max );
      else if( min != null )
        message.append( " >= " ).append(min);
      else if( max != null )
        message.append( " <= " ).append(max);
      
      message.append(parent.langpack.getString("JDKPathPanel.badVersion3"));
      if( askQuestion(parent.langpack.getString("installer.warning"),message.toString(),
        AbstractUIHandler.CHOICES_YES_NO, AbstractUIHandler.ANSWER_NO  ) 
        == AbstractUIHandler.ANSWER_YES)
        return( true);
    }
    return( false );
  }
  /**  Called when the panel becomes active.  */
  public void panelActivate()
  {
    // Resolve the default for chosenPath
    super.panelActivate();
    String chosenPath;
    // The variable will be exist if we enter this panel
    // second time. We would maintain the previos 
    // selected path.
    if( idata.getVariable(getVariableName()) != null )
      chosenPath = idata.getVariable(getVariableName());
    else
    // Try the JAVA_HOME as child dir of the jdk path
      chosenPath = (new File(idata.getVariable("JAVA_HOME"))).getParent();
    // Set the path for method pathIsValid ...
    pathSelectionPanel.setPath(chosenPath);
    
    if( ! pathIsValid() || ! verifyVersion())
      chosenPath = "";
    // Set the default to the path selection panel.
    pathSelectionPanel.setPath(chosenPath);
    String var = idata.getVariable("JDKPathPanel.skipIfValid");
    // Should we skip this panel?
    if( chosenPath.length() > 0 && 
      var != null &&  var.equalsIgnoreCase("yes") )
    {
      idata.setVariable(getVariableName(),chosenPath);
      parent.skipPanel();
    }
      
  }
  
  private final boolean verifyVersion()
  {
    String min = getMinVersion();
    String max = getMaxVersion();
    // No min and max, version always ok.
    if( min == null && max == null)
      return( true );
 
    if( ! pathIsValid())
      return( false );
    // No get the version ...
    // We cannot look to the version of this vm because we should
    // test the given JDK VM.
    String[] params = {pathSelectionPanel.getPath()  + File.separator + "bin"  
        + File.separator + "java", "-version"};
    String[] output = new String[2];
    FileExecutor fe = new FileExecutor();
    fe.executeCommand(params, output);
    // "My" VM writes the version on stderr :-(
    String vs = (output[0].length() > 0 ) ? output[0] : output[1];
    if( min != null )
    {
      if( ! compareVersions( vs, min, true, 3, 3, "__NO_NOT_IDENTIFIER_")) 
        return( false );
    }
    if( max != null )
    if( ! compareVersions( vs, max, false, 3, 3, "__NO_NOT_IDENTIFIER_")) 
      return( false );
    return( true );
  }

  private final boolean compareVersions(String in, String template,
    boolean isMin, int assumedPlace, int halfRange, String useNotIdentifier) 
  {
    StringTokenizer st = new StringTokenizer(in, " \t\n\r\f\"" );
    int length = st.countTokens();
    int i;
    int currentRange = 0;
    String[] interestedEntries = new String[halfRange + halfRange];
    int praeScan = 0;
    praeScan =  assumedPlace - halfRange;
    for( i = 0; i < assumedPlace - halfRange; ++i)
      if( st.hasMoreTokens())
        st.nextToken(); // Forget this entries.
    
    for( i = 0; i < halfRange + halfRange; ++i)
    { // Put the interesting Strings into an intermediaer array.
      if( st.hasMoreTokens())
      {
        interestedEntries[i] = st.nextToken();
        currentRange++;
      }
    }
   
    for( i = 0; i < currentRange; ++i)
    {
      if( useNotIdentifier != null && interestedEntries[i].indexOf (useNotIdentifier) > -1)
        continue;
      if( Character.getType(interestedEntries[i].charAt(0)) != Character.DECIMAL_DIGIT_NUMBER)
        continue;
      break;
    }
    if( i == currentRange)
    {
      detectedVersion = "<not found>";
      return(false);
    }
    detectedVersion = interestedEntries[i];
    StringTokenizer current = new StringTokenizer(interestedEntries[i], "._-" );
    StringTokenizer needed = new StringTokenizer(template, "._-" );
    while( needed.hasMoreTokens() && current.hasMoreTokens())
    {
      String cur = current.nextToken();
      String nee = needed.nextToken();
      if( Integer.parseInt(cur) < Integer.parseInt(nee))
        if(isMin ) 
          return( false ); 
        else 
          return( true );
      if( Integer.parseInt(cur) > Integer.parseInt(nee))
      if(isMin ) 
        return( true ); 
      else 
        return( false );
    }
    return(true); 
  }

  /**
   * Returns the current detected version.
   * @return the current detected version
   */
  public String getDetectedVersion()
  {
    return detectedVersion;
  }

  /**
   * Returns the current used maximum version.
   * @return the current used maximum version
   */
  public String getMaxVersion()
  {
    return maxVersion;
  }

  /**
   * Returns the current used minimum version.
   * @return the current used minimum version
   */
  public String getMinVersion()
  {
    return minVersion;
  }

  /**
   * Sets the given value as current detected version.
   * @param string version string to be used as detected version
   */
  protected void setDetectedVersion(String string)
  {
    detectedVersion = string;
  }

  /**
   * Sets the given value as maximum for version control.
   * @param string version string to be used as maximum
   */
  protected void setMaxVersion(String string)
  {
    maxVersion = string;
  }

  /**
   * Sets the given value as minimum for version control.
   * @param string version string to be used as minimum
   */
  protected void setMinVersion(String string)
  {
    minVersion = string;
  }

  /**
   * Returns the name of the variable which should be used for the path.
   * @return the name of the variable which should be used for the path
   */
  public String getVariableName()
  {
    return variableName;
  }

  /**
   * Sets the name for the variable which should be set with the path.
   * @param string variable name to be used
   */
  public void setVariableName(String string)
  {
    variableName = string;
  }
  /* (non-Javadoc)
   * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
   */
  public String getSummaryBody()
  {
    return(idata.getVariable(getVariableName()));
  }
}
