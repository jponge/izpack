package com.izforge.izpack.util.xml;

import net.n3.nanoxml.XMLElement;


/**
 * A Collection of convenient XML-Helper Methods and Constants
 *
 * @author marc.eppelmann&#064;gmx.de
 * @version $Revision: 1.1 $
 */
public class XMLHelper
{
  //~ Static fields/initializers *********************************************************

  /** YES   = "YES" */
  public final static String YES   = "YES";

  /** NO = "NO" */
  public final static String NO = "NO";

  /** TRUE = "TRUE" */
  public final static String TRUE = "TRUE";

  /** FALSE = "FALSE" */
  public final static String FALSE = "FALSE";

  /** ON = "ON" */
  public final static String ON = "ON";

  /** OFF = "OFF" */
  public final static String OFF = "OFF";

  /** _1 = "1" */
  public final static String _1 = "1";

  /** _0 = "0" */
  public final static String _0 = "0";

  //~ Constructors ***********************************************************************

  /**
   * Creates a new XMLHelper object.
   */
  public XMLHelper(  )
  {
    super(  );
  }

  //~ Methods ****************************************************************************

  /** 
   * Determines if the named attribute in true. True is represented by any of the
   * following strings and is not case sensitive. <br>
   * 
   * <ul>
   * <li>
   * yes
   * </li>
   * <li>
   * 1
   * </li>
   * <li>
   * true
   * </li>
   * <li>
   * on
   * </li>
   * </ul>
   * 
   * <br> Every other string, including the empty string as well as the non-existence of
   * the attribute will cuase <code>false</code> to be returned.
   *
   * @param element the <code>XMLElement</code> to search for the attribute.
   * @param name the name of the attribute to test.
   *
   * @return <code>true</code> if the attribute value equals one of the pre-defined
   *         strings, <code>false</code> otherwise.
   */

  /*--------------------------------------------------------------------------*/
  public static boolean attributeIsTrue( XMLElement element, String name )
  {
    String value = element.getAttribute( name, "" ).toUpperCase(  );

    if( value.equals( YES ) )
    {
      return ( true );
    }
    else if( value.equals( TRUE ) )
    {
      return ( true );
    }
    else if( value.equals( ON ) )
    {
      return ( true );
    }
    else if( value.equals( _1 ) )
    {
      return ( true );
    }

    return ( false );
  }

  /** 
   * The Opposit of AttributeIsTrue
   *
   * @param element
   * @param name
   *
   * @return
   */
  public static boolean attributeIsFalse( XMLElement element, String name )
  {
    String value = element.getAttribute( name, "" ).toUpperCase(  );

    if( value.equals( "NO" ) )
    {
      return ( true );
    }
    else if( value.equals( "FALSE" ) )
    {
      return ( true );
    }
    else if( value.equals( "OFF" ) )
    {
      return ( true );
    }
    else if( value.equals( "0" ) )
    {
      return ( true );
    }

    return ( false );
  }
}
