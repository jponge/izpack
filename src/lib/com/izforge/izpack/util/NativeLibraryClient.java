/*
 * IzPack version 3.0.0 rc2 (build 2002.07.06)
 * Copyright (C) 2002 by Elmar Grom
 *
 * File :               NativeLibraryClient.java
 * Description :        Interface for classes that use native libraries
 * Author's email :     elmar@grom.net
 * Website :            http://www.izforge.com
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

package   com.izforge.izpack.util;

/*---------------------------------------------------------------------------*/
/**
 * Any class in IzPack that uses native libraries must implement this
 * interface. See the package documentation for more details on requirements
 * relating to the use of native libraries within IzPack. 
 *
 * @version  0.0.1 / 2/6/2002
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public interface NativeLibraryClient
{
 /*--------------------------------------------------------------------------*/
 /**
  * This method is used to free the library at the end of progam execution.
  * After this call, any instance of this calss will not be usable any more!
  * <b><i><u>This method is very likely NOT to return!</u></i></b>
  * <br><br>
  * <b>DO NOT CALL THIS METHOD DIRECTLY!</b><br>
  * It is used by the librarian to free a native library before physically
  * deleting it from its temporary loaction. A call to this method is
  * likely to irrecoverably freeze the application!
  * <br><br>
  * The contract for this method implementation is that a call will bring the
  * native library into a state where it can be deleted. This translates into
  * an operation to free the library. Since no libraries should be left
  * behind when the installer shuts down, it is necessary that each library
  * provides the means to free itself. For instance in a MS-Windows environment
  * the library must call <code>FreeLibraryAndExitThread()</code>. This will
  * result in a native fuction call that does not return.
  *
  * @param      name    the name of the library, without path but with extension
  */
 /*--------------------------------------------------------------------------*/
  public void freeLibrary (String name);
}
/*---------------------------------------------------------------------------*/
