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
import com.izforge.izpack.installer.VariableSubstitutor;

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
   * @return
   */
  public boolean haveSpec()
  {
    return _haveSpec;
  }

  /**
   * @return
   */
  public XMLElement getSpec()
  {
    return spec;
  }

  /**
   * @param element
   */
  public void setSpec(XMLElement element)
  {
    spec = element;
  }

  public String getRequiredStringAttribute(XMLElement element,String attrName ) throws Exception
  {
    String attr = element.getAttribute(attrName);
    if( attr == null )
    {
      // Oops, this is an error.
      // throw, or throw not, that is the question ...
      parseError(element, "Missing required key '" + attrName + "'.");
    }
    return( attr );
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


  public InputStream substituteVariables( InputStream input, VariableSubstitutor substitutor)
    throws Exception
  {
    File tempFile = File.createTempFile("izpacksubs", "");
    tempFile.deleteOnExit();
    FileOutputStream fos = new FileOutputStream( tempFile );
    substitutor.substitute(input,fos,null,null);
    fos.close();
    return new FileInputStream( tempFile );
  }
}
