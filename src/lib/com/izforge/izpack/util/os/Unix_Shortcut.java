/*
 * Created on 10.11.2003 by marc.eppelmann
 * 
 * 
 * This represents a Implementation of the KDE/GNOME DesktopEntry.
 * which is standard from
 * "Desktop Entry Standard"
 *  "The format of .desktop files, supported by KDE and GNOME."
 *  http://www.freedesktop.org/standards/desktop-entry-spec/
 * 
 *  [Desktop Entry]
//  Comment=$Comment
//  Comment[de]=
//  Encoding=$UTF-8
//  Exec=$'/home/marc/CPS/tomcat/bin/catalina.sh' run
//  GenericName=$
//  GenericName[de]=$
//  Icon=$inetd
//  MimeType=$
//  Name=$Start Tomcat
//  Name[de]=$Start Tomcat
//  Path=$/home/marc/CPS/tomcat/bin/
//  ServiceTypes=$
//  SwallowExec=$
//  SwallowTitle=$
//  Terminal=$true
//  TerminalOptions=$
//  Type=$Application
//  X-KDE-SubstituteUID=$false
//  X-KDE-Username=$
 *
 *
 * * File :             Unix_Shortcut.java
 * Description :        UNix-Implementaion of the shortcut API
 * Author's email :     marc.eppelmann@gmx.de
 * Website :            http://www.izforge.com
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
 *
 */
package com.izforge.izpack.util.os;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import com.izforge.izpack.util.StringTool;

/**
 * @author marc.eppelmann
 *
 * This is the Implementaion of the RFC-Based Desktop-Link.
 * Used in KDE and GNOME.
 */
public class Unix_Shortcut extends Shortcut implements Unix_ShortcutConstants  
{

private static String version = "$Id$";
private static String rev = "$Revision$";
private String createdDirectory; 
/** DESKTOP_EXT = ".desktop" */
private static String DESKTOP_EXT = ".desktop";
private int itsShow;
private int itsUserType;
private int itsType;
private int itsIconIndex;
private String itsWorkingDirectory;
private String itsGroupName;
private String itsTargetPath;
private String itsIconPath;
private String itsDescription;
private String itsArguments;
private String itsName;
private String itsFileName;

private static String template = "";

private Properties props;
/** N = "\n" */
private final static String N = "\n";
/** H = "#" */
private final static String H = "#";
/** S = " " */
private final static String S = " ";
/** C = Comment  = H+S = "# " */
private final static String C = H + S; 

 

public Unix_Shortcut()
{
  StringBuffer hlp = new StringBuffer();
  
  String userLanguage = System.getProperty("user.language", "en");
  
  hlp.append( "[Desktop Entry]"  + N  );
    
  hlp.append( "Comment="         + $Comment           + N );
  hlp.append( "Comment["+userLanguage+"]="     + $Comment           + N );
  hlp.append( "Encoding="        + $Encoding          + N );
  hlp.append( "Exec='"           + $Exec + "'" + S + $Arguments + N );
  hlp.append( "GenericName="     + $GenericName       + N );
  
  hlp.append( "GenericName["+userLanguage+"]=" + $GenericName + N );
  hlp.append( "Icon="            + $Icon + N );
  hlp.append( "MimeType="        + $MimeType          + N );
  hlp.append( "Name="            + $Name              + N );
  hlp.append( "Name["+userLanguage+"]="        + $Name        + N );
  
  hlp.append( "Path='"           + $Path + "'"        + N );  
  hlp.append( "ServiceTypes="    + $ServiceTypes    + N );
  hlp.append( "SwallowExec="     + $SwallowExec     + N );
  hlp.append( "SwallowTitle="    + $SwallowTitle    + N );  
  hlp.append( "Terminal="        + $Terminal        + N );
  
  hlp.append( "TerminalOptions=" + $Options_For_Terminal + N );
  hlp.append( "Type="                + $Type                + N );  
  hlp.append( "URL="                 + $URL + N );  
  hlp.append( "X-KDE-SubstituteUID=" + $X_KDE_SubstituteUID + N );
  hlp.append( "X-KDE-Username="      + $X_KDE_Username      + N );
    
  template = hlp.toString();
  
  props = new Properties();
  
  initProps(); 
}

/**
 * This initialisizes all Properties Values with null. 
 * 
 **/
private void initProps()
{
  String [] propsArray = { $Comment,              $$LANG_Comment,     $Encoding,    $Exec,         $Arguments,
                           $GenericName,          $$LANG_GenericName, $MimeType,    $Name,         $$LANG_Name,
                           $Path,                 $ServiceTypes,      $SwallowExec, $SwallowTitle, $Terminal,
                           $Options_For_Terminal, $Type,              $X_KDE_SubstituteUID,        $X_KDE_Username,
                           $Icon,                 $URL  
                         };
  
  for( int i = 0; i < propsArray.length; i++ ) 
  {  
    props.put( propsArray[i], "" );
  }    
}


/** Overridden Method 
 * @see com.izforge.izpack.util.os.Shortcut#initialize(int, java.lang.String)
 */
public void initialize( int aType, String aName ) throws Exception
{
  this.itsType=aType;
  this.itsName=aName;
  props.put( $Name, aName );    
}





/** 
 * This indicates that Unix will be supported.
 * 
 * @see com.izforge.izpack.util.os.Shortcut#supported()
 */
public boolean supported()
{
  return true;
}



/** Dummy
 * @see com.izforge.izpack.util.os.Shortcut#getDirectoryCreated()
 */
public String getDirectoryCreated()
{
  return this.createdDirectory; //while not stored...
}

/** Dummy
 * @see com.izforge.izpack.util.os.Shortcut#getFileName()
 */
public String getFileName()
{
  return( this.itsFileName );
}

/** Overridden Compatibility Method.  
 * 
 * returns all directories in $USER/.kde/share/applink
 * 
 * @see com.izforge.izpack.util.os.Shortcut#getProgramGroups(int)
 */
public Vector getProgramGroups( int userType )
{
  //if( userType > CURRENT_USER )
  //if( System.getProperty( "user.name" ).equals( "root" ) )
  String FS = File.separator;
  //System.out.println( "userType" + userType );
  Vector groups = new Vector();
  
  File homedir  = new File( System.getProperty( "user.home" ) );
    
  File kdeHomeShareApplnk;
  try
  {
    File kdeHome = new File( homedir.toString() + FS + ".kde" );
    if( ! kdeHome.exists() )
      return( groups );
    File kdeHomeShare = new File( kdeHome.toString()+ FS + "share" );
    if( ! kdeHomeShare.exists() )
      return( groups );
    kdeHomeShareApplnk = new File(kdeHomeShare.toString() + FS + "applnk");  
    if( ! kdeHomeShareApplnk.exists() )
      return( groups );
  }
  catch( Exception e1 )
  {
    // 
    e1.printStackTrace();
    return( groups );
  }
  
  try
  {
    File [] listing = kdeHomeShareApplnk.listFiles();
    
    for( int i = 0; i < listing.length; i++ )
    {
      if( listing[ i ].isDirectory() )
      {
        groups.add( listing[ i ].getName() );
      }
    } 
  }
  catch( Exception e )
  {
    // ignore and return an empty vector.
  }   
  return( groups );
}



/** overridden method 
 * @see com.izforge.izpack.util.os.Shortcut#multipleUsers()
 * @return true
 */
public boolean multipleUsers()
{
  // EVER true for UNIXes ;-)
  return (true);
}

/** Creates and stores the shortcut-files.
 *  
 * @see com.izforge.izpack.util.os.Shortcut#save()
 */
public void save() throws Exception
{
  String FS = File.separator;
  String target = null;
  
  String shortCutDef = this.replace();
   
  if( ( this.itsGroupName == null ) || ( "".equals( this.itsGroupName ) ) )
  {
    target = System.getProperty( "user.home" ) + FS + "Desktop" + FS + this.itsName + DESKTOP_EXT;
  }
  else
  {
    File homedir  = new File( System.getProperty( "user.home" ) );  
    File kdeHome = new File( homedir.toString() + FS + ".kde" );
    File kdeHomeShare = new File( kdeHome.toString()+ FS + "share" );
    File kdeHomeShareApplnk = new File( kdeHomeShare.toString()+ FS + "applnk" );
    target = kdeHomeShareApplnk.toString() + FS + this.itsGroupName + FS + this.itsName + DESKTOP_EXT;
  } 
  File targetPath = new File( target.toString().substring( 0, target.toString().lastIndexOf( File.separatorChar ) ) );
  if( ! targetPath.exists() )
  {
    targetPath.mkdirs();      
    this.createdDirectory = targetPath.toString();
  }
    
   
  this.itsFileName = new String( target );
  File targetFileName = new File( target );
  FileWriter fileWriter = null;
  try
  {
    fileWriter = new FileWriter( targetFileName );
  }
  catch (IOException e1)
  {
    e1.printStackTrace();
  }

  try
  {
    fileWriter.write( shortCutDef );
  }
  catch (IOException e)
  {
    e.printStackTrace();
  }
  try
  {
    fileWriter.close();
  }
  catch (IOException e2)
  {
    e2.printStackTrace();
  }
}

/** Set the Commandline Arguments
 * @see com.izforge.izpack.util.os.Shortcut#setArguments(java.lang.String)
 */
public void setArguments( String args )
{  
  this.itsArguments=args;  
  props.put( $Arguments, args );
}

/** Sets the Description
 * @see com.izforge.izpack.util.os.Shortcut#setDescription(java.lang.String)
 */
public void setDescription( String description )
{
  this.itsDescription=description;
  props.put( $Comment, description );
}

/** Sets The Icon Path
 * @see com.izforge.izpack.util.os.Shortcut#setIconLocation(java.lang.String, int)
 */
public void setIconLocation( String path, int index )
{
  this.itsIconPath  = path;
  this.itsIconIndex = index;
  props.put( $Icon, path );
  //
}

/** Sets the Name of this Shortcut 
 * @see com.izforge.izpack.util.os.Shortcut#setLinkName(java.lang.String)
 */
public void setLinkName( String aName )
{
  this.itsName = aName;
  props.put( $Name, aName );
}

/** Sets the type of this Shortcut
 * @see com.izforge.izpack.util.os.Shortcut#setLinkType(int)
 */
public void setLinkType(int aType) throws IllegalArgumentException
{
  this.itsType=aType;
}

/** Sets the ProgramGroup
 * @see com.izforge.izpack.util.os.Shortcut#setProgramGroup(java.lang.String)
 */
public void setProgramGroup(String aGroupName)
{
  this.itsGroupName=aGroupName;
}

/** Sets the ShowMode
 * @see com.izforge.izpack.util.os.Shortcut#setShowCommand(int)
 */
public void setShowCommand(int show)
{
  this.itsShow = show;
}

/** Sets The TargetPath
 * @see com.izforge.izpack.util.os.Shortcut#setTargetPath(java.lang.String)
 */
public void setTargetPath(String aPath)
{
  this.itsTargetPath = aPath;
  
  props.put($Exec, aPath);
}

/** Sets the usertype.
 * @see com.izforge.izpack.util.os.Shortcut#setUserType(int)
 */
public void setUserType(int aUserType)
{
  this.itsUserType = aUserType;  
}

/** Sets the working-directory
 * @see com.izforge.izpack.util.os.Shortcut#setWorkingDirectory(java.lang.String)
 */
public void setWorkingDirectory(String aDirectory)
{
  this.itsWorkingDirectory = aDirectory;
  
  props.put( $Path, aDirectory );
}

/** Dumps the Name to console.
 * @see java.lang.Object#toString()
 */
public String toString()
{
  return this.itsName + N + template;
}


/**
 * Creates the Shortcut String which will be stored as File.
 * @return the replaced String
 */
public String replace()
{
  String result = new String( template );
  Enumeration enum = props.keys();
  
  while( enum.hasMoreElements() )
  {
    String key = (String) enum.nextElement();
    //**DEBUG: System.out.println(key+"="+props.getProperty( key )); 
    result = StringTool.replace( result, key, props.getProperty( key ) );
  }
  //**DEBUG:  System.out.println(result);
  return result;
}


/**
 * Test Method 
 * @param args Commnandline-Args
 */
public static void main(String[] args) 
{
  Unix_Shortcut aSample = new Unix_Shortcut();
  try
  {
    aSample.initialize(APPLICATIONS, "Start Tomcat");
  }
  catch( Exception exc )
  {
    System.err.println("Could not init Unix_Shourtcut");
  }
  aSample.replace();
  System.out.println( aSample );
  
  File targetFileName = new File( System.getProperty("user.home") + File.separator + "Start Tomcat" + DESKTOP_EXT );
  FileWriter fileWriter = null;
  try
  {
    fileWriter = new FileWriter( targetFileName );
  }
  catch (IOException e1)
  {
    e1.printStackTrace();
  }

  try
  {
    fileWriter.write( template );
  }
  catch (IOException e)
  {
    e.printStackTrace();
  }
  try
  {
    fileWriter.close();
  }
  catch (IOException e2)
  {
    e2.printStackTrace();
  }
}
  /** Sets The Encoding
   * @see com.izforge.izpack.util.os.Shortcut#setEncoding(java.lang.String)
   */
  public void setEncoding(String aEncoding)
  {
    props.put( $Encoding, aEncoding );
  }

  /** Sets The KDE Specific subst UID property
   * @see com.izforge.izpack.util.os.Shortcut#setKdeSubstUID(java.lang.String)
   */
  public void setKdeSubstUID(String aKDESubstUID)
  {
    props.put( $X_KDE_SubstituteUID, aKDESubstUID );
  }

  /** Sets the MimeType
   * @see com.izforge.izpack.util.os.Shortcut#setMimetype(java.lang.String)
   */
  public void setMimetype( String aMimetype )
  {
    props.put( $MimeType, aMimetype );
  }

  /** Sets the terminal  
   * @see com.izforge.izpack.util.os.Shortcut#setTerminal(java.lang.String)
   */
  public void setTerminal(String trueFalseOrNothing )
  {
    props.put( $Terminal, trueFalseOrNothing );    
  }

  /** Sets the terminal options 
   * @see com.izforge.izpack.util.os.Shortcut#setTerminalOptions(java.lang.String)
   */
  public void setTerminalOptions( String someTerminalOptions )
  {
    props.put( $Options_For_Terminal, someTerminalOptions );    
  }

  /** Sets the Shortcut type (one of Application, Link or Device)
   * @see com.izforge.izpack.util.os.Shortcut#setType(java.lang.String)
   */
  public void setType( String aType )
  {
    props.put( $Type, aType );
  }

  /** Sets the Url for type Link.
   * Can be also a apsolute file/path
   *  
   * @see com.izforge.izpack.util.os.Shortcut#setURL(java.lang.String)
   */
  public void setURL( String anUrl )
  {
    props.put( $URL, anUrl );    
  }

}
