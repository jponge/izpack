/* 
*  Copyright (C) 2004 Thorsten Kamann
*
*  File :               PortProcessor.java
*  Description :       Arises the port until it is free
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

import java.net.InetAddress;
import java.net.ServerSocket;

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Processor;

/**
 * Checks whether the value of the field comtemt is a port and is free. If 
 * false the next free port will be searched.
 * @author Thorsten Kamann <thorsten.kamann@planetes.de>
 */
public class PortProcessor implements Processor {

	public String process (ProcessingClient client){
		String retValue = "";
		String host = "localhost";
		int port = 0;
		int oPort = 0;
		boolean found = false;
		InetAddress inet = null;
		ServerSocket socket = null;
				
		try{
			if (client.getNumFields()>1){
				host = client.getFieldContents(0);
				oPort = Integer.parseInt(client.getFieldContents(1));
			}else{
				oPort = Integer.parseInt(client.getFieldContents(0));
			}
		}catch (Exception ex){
			return getReturnValue(client, null, null);
		}
		
		port = oPort;
		while (!found){
			try{
				inet = InetAddress.getByName(host);
				socket = new ServerSocket(port, 0, inet);
				if (socket.getLocalPort() > 0){
					found = true;
					retValue = getReturnValue(client, null, String.valueOf(port));
				}else{
					port++;					
				}
			}catch (java.net.BindException ex){
				port++;
			}catch (Exception ex){
				return getReturnValue(client, null, null);
			}finally{
				try{
					socket.close();
				}catch (Exception ex){}
			}
		}
		return retValue;
	}
	
	/**
	 * Creates the return value
	 * @param client The ProcessingClient
	 */
	private String getReturnValue(ProcessingClient client, String host, String port){
		String retValue = "";
		String _host = "";
		String _port = "";
		
		if (client.getNumFields()>1){
			_host = (host == null)? client.getFieldContents(0): host;
			_port = (port == null)? client.getFieldContents(1): port;
			retValue = _host+"*"+_port;
		}else{
			_port = (port == null)? client.getFieldContents(0): port;
			retValue = _port;
		}
		
		return retValue;
	}
}
