/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001,2002 Marcus Stursberg
 *
 *  File :               ResourceManager.java
 *  Description :        Class to get resources from the installer
 *  Author's email :     marcus@emsty.de
 *  Author's Website :   http://www.emasty.de
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
package com.izforge.izpack.installer;

/**
 *  Describes that a resource could not be found
 *
 * @author     Marcus Stursberg
 */

public class ResourceNotFoundException extends Exception
{


 /**  creates a new ResourceNotFoundException */
 public ResourceNotFoundException()
 {
   super();
 }


 /**
  *  creates a new ResourceNotFoundException
  *
  * @param  s  description of the exception
  */
 public ResourceNotFoundException(String s)
 {
   super(s);
 }
}
