/* 
 *  Copyright (C) 2004 Thorsten Kamann
 *
 *  File :               IsPortValidator.java
 *  Description :        Checks whether the given port is a valid port in the range 
 * 								from 0 until 32000
 *  Author's email :     thorsten.kamann@planetes.de
 *  Author's Website :   http://www.izforge.com
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

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;

/**
 * A validator to check whether the field content is a port .
 * 
 * @author thorque
 */
public class IsPortValidator implements Validator
{

    public boolean validate(ProcessingClient client)
    {
        int port = 0;

        if (client.getFieldContents(0).equals("")) { return false; }

        try
        {
            port = Integer.parseInt(client.getFieldContents(0));
            if (port > 0 && port < 32000) { return true; }
        }
        catch (Exception ex)
        {
            return false;
        }

        return false;
    }

}
