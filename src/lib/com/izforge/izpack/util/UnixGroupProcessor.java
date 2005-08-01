/* 
*  Copyright (C) 2004 Thorsten Kamann
*
*  File :               UnixGroupProcessor.java
*  Description :       Retrieves a list of the current groups 
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

import java.io.BufferedReader;
import java.io.FileReader;

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Processor;

/**
 * @author thorsten-kamann
 */
public class UnixGroupProcessor implements Processor {

	public String process (ProcessingClient client){
		String retValue = "";
		String filepath = "/etc/group";
		BufferedReader reader = null;
		String line = "";
				
		try{
			reader = new BufferedReader(new FileReader(filepath));
			while ((line = reader.readLine()) != null){
				retValue += line.substring(0, line.indexOf(":"))+":";
			}
			if (retValue.endsWith(":")){
				retValue = retValue.substring(0, retValue.length()-1);
			}			
		}catch (Exception ex){
			retValue = "";
		}
		
		return retValue;
	}

}
