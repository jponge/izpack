/*
 * $Id$
 * Copyright (C) 2003 Elmar Grom
 *
 * File :               PWDEncryptor.java
 * Description :        Example code for a password encryption service
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
 * connection with a password field, as used in a <code>UserInputPanel</code>.
 *
 * @version  0.0.1 / 02/19/03
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class PWDEncryptor implements Processor
{
 /*--------------------------------------------------------------------------*/
 /**
  * Encrypts the a password and returns the encrypted result. <br>
  * <b>Note:</b> this is not a real encryption algorithm. The code only
  * demonstrates the use of this interface in a real installation environment.
  * For a real application a proper encryption mechanism must be used. Though
  * Java 1.4.X provides such algorithms, you need to consider that not all
  * potential target environments have this version installed. It seems best
  * to include the necessary encryption library with the installer.
  *
  * @param     client   the client object using the services of this encryptor.
  *
  * @return    the encryption result.
  */
 /*--------------------------------------------------------------------------*/
  public String process (ProcessingClient client)
  {
    if (client.getNumFields () < 1)
    {
      return ("");
    }
    
    char [] password = client.getFieldContents (0).toCharArray ();
    char [] result   = new char [password.length];
    int  temp;
    
    for (int i = 0; i < password.length; i++)
    {
      temp = password [i] - 57;
      if (i > 0)
      {
        temp = temp + password [i - 1];
      }

      if ((temp % 3) == 0)
      {
        temp = temp + 13;
      }
      if (temp < 0)
      {
        temp = temp + 193;
      }
    
      result [i] = (char)temp;
    }

    return (new String (result));
  }
}
/*---------------------------------------------------------------------------*/
