/*
 * $Id$
 * Copyright (C) 2003 Elmar Grom
 *
 * File :               IPValidator.java
 * Description :        Sample implementation of a rule validator
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
 * This class represents a simple validator for IP addresses to demonstrate
 * the implementation of a rule validator that cooperates with the
 * <code>RuleInputField</code> used in the <code>UserInputPanel</code>
 *
 * @version  0.0.1 / 02/19/03
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class IPValidator implements Validator
{
 /*--------------------------------------------------------------------------*/
 /**
  * Validates the contend of a <code>RuleInputField</code>. The test is
  * intended for a rule input field composed of four sub-fields. The
  * combination of their individual content is assumed to represent an IP
  * address.
  *
  * @param     client   the client object using the services of this validator.
  *
  * @return    <code>true</code> if the validation passes, otherwise <code>false</code>.
  */
 /*--------------------------------------------------------------------------*/
  public boolean validate (ProcessingClient client)
  {
    // ----------------------------------------------------
    // verify that there are actually four sub-fields. A
    // different number would indicate that we are not
    // connected with the RuleInputField that we expect
    // ----------------------------------------------------
    if (client.getNumFields () != 4)
    {
      return (false);
    }
    
    // ----------------------------------------------------
    // test each field to make sure it actually contains
    // an integer and the value of the integer is beween
    // 0 and 255.
    // ----------------------------------------------------
    boolean isIP = true;
    
    for (int i = 0; i < 4; i++)
    {
      int value;
      
      try
      {
        value = Integer.parseInt (client.getFieldContents (i));
        if ((value < 0) || (value > 255))
        {
          isIP = false;
        }
      }
      catch (Throwable exception)
      {
        isIP = false;
      }
    }
    
    return (isIP);
  }
}
/*---------------------------------------------------------------------------*/
