/*
 * IzPack version 3.0.0 rc1 (build 2002.07.03)
 * Copyright (C) 2002 by Elmar Grom
 *
 * File :               CleanupClient.java
 * Description :        Interface for classes that use Housekeeper
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
 * Any class that wants to perform cleanup operations and to be notified by
 * <code>Hosekeeper</code> for this purpose must implement this interface.
 *
 * @version  0.0.1 / 2/9/02
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public interface CleanupClient
{
 /*--------------------------------------------------------------------------*/
 /**
  * Performs custom cleanup operations.
  */
 /*--------------------------------------------------------------------------*/
  public void cleanUp ();
}
/*---------------------------------------------------------------------------*/
