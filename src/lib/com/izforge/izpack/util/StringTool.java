/*
 * Created on 11.11.2003
 * 
 * $Id$
 * IzPack
 * Copyright (C) 2002 by Marc Eppelmann
 *
 * File :               StringTool.java
 * Description :        Extended implemenation of Pythons string.replace and more...
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
package com.izforge.izpack.util;

import java.io.File;


/**
 * A extended Java Implementation of Pythons string.replace()
 *
 * @author marc.eppelmann&#064;gmx.de
 */
public class StringTool
{
  //~ Constructors *********************************************************************************

  /**
   * Default Constructor 
   */
  public StringTool(  )
  {
    super(  );
  }

  //~ Methods **************************************************************************************

  /** 
   * Test method  
   *
   * @param args Commandline Args
   */
  public static void main( String[] args )
  {
    System.out.println( "Test: string.replace(abc$defg,$de ,null ):" + StringTool.replace( "abc$defg", "$de", null, true ) );
  }

  /** 
   * Replaces <b>from</b> with <b>to</b> in given String: <b>value</b>  
   *
   * @param value original String 
   * @param from Search Pattern
   * @param to Replace with this
   *
   * @return the replaced String
   */
  public static String replace( String value, String from, String to )
  {
    return replace( value, from, to, true );
  }

  /** 
   * Replaces <b>from</b> with <b>to</b> in given String: <b>value</b>  
   *
   * @param value original String 
   * @param from Search Pattern
   * @param to Replace with this
   * @param If set to true be case sensitive.
   *
   * @return the replaced String
   */
  public static String replace( String value, String from, String to, boolean aCaseSensitiveFlag )
  {
    if( ( value == null ) || ( value.length(  ) == 0 ) || ( from == null ) || ( from.length(  ) == 0 ) )
    {
      return value;
    }

    if( to == null )
    {
      to = "";
    }

    String searchData = value;

    if( ! aCaseSensitiveFlag )
    {
      searchData = value.toLowerCase(  );
      from       = from.toLowerCase(  );
    }

    String result = value;
    int    lastIndex = 0;
    int    index = value.indexOf( from );

    if( index != -1 )
    {
      StringBuffer buffer = new StringBuffer(  );

      while( index != -1 )
      {
        buffer.append( value.substring( lastIndex, index ) ).append( to );
        lastIndex = index + from.length(  );
        index     = value.indexOf( from, lastIndex );
      }

      buffer.append( value.substring( lastIndex ) );
      result = buffer.toString(  );
    }

    return result;
  }

  /** 
   * Normalizes a Windows or Unix Path.
   * 
   * Reason: Javas File accepts / or \ for Pathes.
   * Batches or ShellScripts does it not!
   *
   * TODO: implement support for MAC < MacOSX
   *
   * @param destination
   * @param Force a target-system fileseparator
   *
   * @return
   */
  public static String normalizePath( String destination, String fileSeparator )
  {
    String FILESEP = (fileSeparator == null) ? File.separator : fileSeparator;
   
   destination = StringTool.replace( destination, "\\", "/" );
   
    //all occs of "//" by "/" 
    destination = StringTool.replace( destination, "//", "/" );
    
    destination = StringTool.replace( destination, ":", ";" );
    destination = StringTool.replace( destination, ";", ":" );
    
    destination = StringTool.replace( destination, "/",  FILESEP );
    
    if( "\\".equals( FILESEP ) )
    {
      destination = StringTool.replace( destination, ":", ";" );

      // results in "C;\" instead of "C:\"
      // so correct it:
      destination = StringTool.replace( destination, ";\\", ":\\" );
    }
    

    //  Convert the file separator characters
    return ( destination );
  }
  
  /** 
   * Normalizes a mixed Windows/Unix Path.
   * Does Only work for Windows or Unix Pathes
   * Reason: Java.File accepts / or \ for Pathes.
   * Batches or ShellScripts does it not!
   * 
   * @param A accepted mixed form by java.File like "C:/a/mixed\path\accepted/by\Java"
   *
   * @return the normalized Path
   */
  public static String normalizePath( String destination )
  {
   return( normalizePath( destination, null ) );
  }
}
