/*
 * $Id$
 * Copyright (C) 2003 Elmar Grom
 *
 * File :               PWDValidator.java
 * Description :        Example implementation of a password validator
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

package   com.izforge.izpack.sample;

import    com.izforge.izpack.panels.*;

/*---------------------------------------------------------------------------*/
/**
 * This class represents a simple validator for passwords to demonstrate
 * the implementation of a password validator that cooperates with the
 * password field in the <code>UserInputPanel</code>
 *
 * @version  0.0.1 / 02/19/03
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class PWDValidator implements Validator
{
 /*--------------------------------------------------------------------------*/
 /**
  * Validates the contend of multiple password fields. The test 
  *
  * @param     client   the client object using the services of this validator.
  *
  * @return    <code>true</code> if the validation passes, otherwise <code>false</code>.
  */
 /*--------------------------------------------------------------------------*/
  public boolean validate (ProcessingClient client)
  {
    int numFields = client.getNumFields ();
    
    // ----------------------------------------------------
    // verify that there is more than one field. If there
    // is only one field we have to return true.
    // ----------------------------------------------------
    if (numFields < 2)
    {
      return (true);
    }
    
    boolean match   = true;
    String  content = client.getFieldContents (0);
    
    for (int i = 1; i < numFields; i++)
    {
      if (!content.equals (client.getFieldContents (i)))
      {
        match = false;
      }      
    }
    
    return (match);
  }
}
/*---------------------------------------------------------------------------*/
