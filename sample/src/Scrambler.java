/*
 * $Id$
 * Copyright (C) 2003 Elmar Grom
 *
 * File :               Scramble.java
 * Description :        Example code for an encryption service
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
 * This class provides a demonstration for using an encryption service in
 * connection with a <code>RuleInputField</code>, as used in a
 * <code>UserInputPanel</code>.
 *
 * @version  0.0.1 / 02/19/03
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class Scrambler implements Encryptor
{
 /*--------------------------------------------------------------------------*/
 /**
  * Rearranges the input fields and concatenates the result, separating
  * individual fields with a '*'.
  *
  * @param     client   the client object using the services of this encryptor.
  *
  * @return    the encryption result.
  */
 /*--------------------------------------------------------------------------*/
  public String encrypt (RuleInputField client)
  {
    StringBuffer buffer = new StringBuffer ();
    
    for (int i = client.getNumFields () - 1; i > -1; i--)
    {
      buffer.append (client.getFieldContents (i));
      if (i > 0)
      {
        buffer.append ('*');
      }
    }
    
    return (buffer.toString ());
  }
}
/*---------------------------------------------------------------------------*/
