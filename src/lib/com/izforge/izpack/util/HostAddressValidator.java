/* 
*  Copyright (C) 2004 Thorsten Kamann
*
*  File :               HostAddressValidator.java
*  Description :        Validates a given host:port whether it is available or not
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

import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * A validator to check wheter a host:port is available (free).
 * 
 * This validator can be used for rule input fields in the UserInputPanel to make
 * sure that the port the user entered is not in use.
 * 
 * @author thorque
 */
public class HostAddressValidator implements Validator
{

	public boolean validate(ProcessingClient client){
		InetAddress inet = null;
		String host = "";
    	int port = 0;
		boolean retValue = false;
		int numfields = client.getNumFields();

		try {
			host = client.getFieldContents(0);
			port = Integer.parseInt(client.getFieldContents(1));
		}catch (Exception e) {
			return false;
		}
		
		try{
			inet = InetAddress.getByName(host);
			ServerSocket socket = new ServerSocket(port, 0, inet);
			if (socket.getLocalPort() > 0){
				retValue = true;
			}else{
				retValue = false;
			}
			socket.close();
		}catch (Exception ex){
			retValue = false;
		}
		return retValue;
    }

}
