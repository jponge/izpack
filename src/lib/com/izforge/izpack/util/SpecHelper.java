/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2004 Klaus Bartz
 *
 *  File :               SpecHelper.java
 *  Description :        Helper for XML specifications.
 *  Author's email :     klaus.bartz@coi.de
 *  Author's Website :   http://www.coi.de/
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

package com.izforge.izpack.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

import com.izforge.izpack.installer.InstallerException;
import com.izforge.izpack.installer.ResourceManager;

import net.n3.nanoxml.NonValidator;
import net.n3.nanoxml.StdXMLBuilder;
import net.n3.nanoxml.StdXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;

/**
 * This class contains some helper methods to simplify handling of 
 * xml specification files. 
 *
 * @author  Klaus Bartz
 *
 */
public class SpecHelper 
{
  private String specFilename;
  private XMLElement spec;
  private boolean _haveSpec;

  public static final String YES = "yes";
  public static final String NO = "no";
  private static final String PACK_KEY  = "pack";
  private static final String PACK_NAME = "name";


  /**
   *  The default constructor.
   */
  public SpecHelper()
  {
    super();
  }

   /*--------------------------------------------------------------------------*/
  /**
   * Reads the XML specification given by the file name. The result is
   * stored in spec.
   *
   * @exception Exception for any problems in reading the specification
   */
  /*--------------------------------------------------------------------------*/
   public void readSpec (String specFileName) throws Exception
   {
     readSpec( specFileName, null );
   }

   /*--------------------------------------------------------------------------*/
   /**
    * Reads the XML specification given by the file name. The result is
    * stored in spec.
    *
    * @exception Exception for any problems in reading the specification
    */
   /*--------------------------------------------------------------------------*/
    public void readSpec (String specFileName, VariableSubstitutor substitutor) throws Exception
    {
      // open an input stream
      InputStream input = null;
      try
      {
        input = getResource (specFileName);
      }
      catch (Exception exception)
      {
        _haveSpec = false;
        return;
      }
      if (input == null)
      {
        _haveSpec = false;
        return;
      }
        
      readSpec( input, substitutor );
         
      // close the stream
      input.close ();
      this.specFilename = specFileName;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Reads the XML specification given by the input stream. The result is
     * stored in spec.
     *
     * @exception Exception for any problems in reading the specification
     */
    /*--------------------------------------------------------------------------*/
     public void readSpec (InputStream input) throws Exception
     {
       readSpec( input, null );
     }
    
   /*--------------------------------------------------------------------------*/
   /**
    * Reads the XML specification given by the input stream. The result is
    * stored in spec.
    *
    * @exception Exception for any problems in reading the specification
    */
   /*--------------------------------------------------------------------------*/
    public void readSpec (InputStream input, VariableSubstitutor substitutor) throws Exception
    {
      // first try to substitute the variables
      if (substitutor != null)
      {
        input = substituteVariables( input, substitutor );
      }

      // initialize the parser
      StdXMLParser parser = new StdXMLParser ();
      parser.setBuilder   (new StdXMLBuilder ());
      parser.setValidator (new NonValidator ());
      parser.setReader    (new StdXMLReader (input));
         
      // get the data
      spec = (XMLElement) parser.parse ();
      _haveSpec = true;
    }

   /**
   *  Gets the stream to a resource.
   *
   * @param  res            The resource id.
   * @return                The resource value, null if not found
   */
  public InputStream getResource(String res)
  {
    try
    {
      //System.out.println ("retrieving resource " + res);
      return ResourceManager.getInstance().getInputStream(res);
    } catch (Exception e)
    { // Cannot catch ResourceNotFoundException because it is not public.
      return null;
    }
  }

  /**
   * Returns a XML element which represents the pack for the given name.
   * @param packDestName name of the pack which should be returned
   * @return a XML element which represents the pack for the given name
   */
  public XMLElement getPackForName(String packDestName )
  {
    Vector packs = getSpec().getChildrenNamed(PACK_KEY);
    Iterator iter = null;
    if( packs == null )
      return(null);
    iter = packs.iterator();
    while (iter.hasNext())
    {
      
      XMLElement pack = (XMLElement) iter.next();
      String packName = pack.getAttribute(PACK_NAME); 
      if( packName.equals(packDestName))
        return( pack );
    }
    return(null);
  
  }

  /**
   * Create parse error with consistent messages. Includes file name and line #
   * of parent. It is an error for 'parent' to be null.
   *
   * @param parent  The element in which the error occured
   * @param message Brief message explaining error
   */
  public void parseError(XMLElement parent, String message)
    throws InstallerException
  {
    throw new InstallerException(
      specFilename + ":" + parent.getLineNr() + ": " + message);
  }

  /**
   * Returns true if a specification exist, else false.
   * @return true if a specification exist, else false
   */
  public boolean haveSpec()
  {
    return _haveSpec;
  }

  /**
   * Returns the specification.
   * @return the specification
   */
  public XMLElement getSpec()
  {
    return spec;
  }

  /**
   * Sets the specifaction to the given XML element.
   * @param element
   */
  public void setSpec(XMLElement element)
  {
    spec = element;
  }

  /**
   * Returns a Vector with all leafs of the tree which is described
   * with childdef.
   * 
   * @param root the XMLElement which is the current root for the search
   * @param childdef a String array which describes the tree; the last element
   * contains the leaf name
   * @return a Vector of XMLElements of all leafs founded under root
   */
	public Vector getAllSubChildren( XMLElement root, String [] childdef)
  {
    return( getSubChildren( root, childdef, 0 ));
  }

  /**
   * Returns a Vector with all leafs of the tree which is described
   * with childdef beginning at the given depth.
   * 
   * @param root the XMLElement which is the current root for the search
   * @param childdef a String array which describes the tree; the last element
   * contains the leaf name
   * @param depth depth to start in childdef
   * @return a Vector of XMLElements of all leafs founded under root
   */
  private Vector getSubChildren( XMLElement root, String [] childdef, int depth)
  {
    Vector retval = null;
    Vector retval2 = null;
    Vector children = root != null ? root.getChildrenNamed(childdef[depth]) : null;
    if( children == null )
      return( null );
    if( depth < childdef.length - 1)
    {
      Iterator iter = children.iterator();
      while( iter.hasNext() )
      {
        retval2 = getSubChildren((XMLElement) iter.next(), childdef, depth + 1 );
        if( retval2 != null)
        {
          if( retval == null )
            retval = new Vector();
          retval.addAll(retval2);
        }
      }
    }
    else
      return( children);
    return( retval);
  }


  /**
   * Creates an temp file in to the substitutor the
   * substituted contents of input writes; close it and
   * (re)open it as FileInputStream.
   * The file will be deleted on exit.
   * @param input the opened input stream which contents should be substituted
   * @param substitutor substitutor which should substitute the contents of input
   * @return a file input stream of the created temporary file
   * @throws Exception
   */
  public InputStream substituteVariables( InputStream input, VariableSubstitutor substitutor)
    throws Exception
  {
    File tempFile = File.createTempFile("izpacksubs", "");
    FileOutputStream fos = null;
    tempFile.deleteOnExit();
    try
    {
      fos = new FileOutputStream( tempFile );
      substitutor.substitute(input,fos,null,null);
    }
    finally
    {
      if( fos != null )
        fos.close();
    }
    return new FileInputStream( tempFile );
  }

  /**
   * Returns whether the value to the given attribute is
   * "yes" or not. If the attribute does not exist, or the
   * value is not "yes" and not "no", the default value is
   * returned.
   * @param element the XML element which contains the attribute
   * @param attribute the name of the attribute
   * @param defaultValue the default value
   * @return whether the value to the given attribute is
   * "yes" or not
   */
  public boolean isAttributeYes( XMLElement element,
    String attribute, boolean defaultValue )
  {
    String value =
      element.getAttribute(attribute, (defaultValue ? YES : NO));
    if (value.equalsIgnoreCase(YES))
      return true;
    if (value.equalsIgnoreCase(NO))
      return false;

    return defaultValue;
  }

  /**
   * Returns the attribute for the given attribute name.
   * If no attribute exist, an InstallerException with
   * a detail message is thrown.
   * @param element XML element which should contain the attribute
   * @param attrName key of the attribute
   * @return the attribute as string 
   * @throws Exception
   */
  public String getRequiredAttribute(XMLElement element,String attrName ) 
    throws InstallerException
  {
    String attr = element.getAttribute(attrName);
    if( attr == null )
    {
      parseError(element, "<" + element.getName() + 
        "> requires attribute '" + attrName + "'.");
    }
    return( attr );
  }
}
