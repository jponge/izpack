/*
 * $Id$
 * Copyright (C) 2003 Elmar Grom
 *
 * File :               ProcessingClient.java
 * Description :        This interface must be implemented by any
 *                      class that wants to use processing or
 *                      validation services.
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

package   com.izforge.izpack.panels;

/*---------------------------------------------------------------------------*/
/**
 * Implement this interface in any class that wants to use processing or
 * validation services.
 *
 * @see      com.izforge.izpack.panels.Processor
 * @see      com.izforge.izpack.panels.Validator
 *
 * @version  0.0.1 / 2/22/03
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public interface ProcessingClient
{
 /*--------------------------------------------------------------------------*/
 /**
  * Returns the number of sub-fields.
  *
  * @return    the number of sub-fields
  */
 /*--------------------------------------------------------------------------*/
  public int getNumFields ();
 /*--------------------------------------------------------------------------*/
 /**
  * Returns the contents of the field indicated by <code>index</code>.
  *
  * @param     index  the index of the sub-field from which the contents
  *                   is requested.
  *
  * @return    the contents of the indicated sub-field.
  *
  * @exception IndexOutOfBoundsException if the index is out of bounds.
  */
 /*--------------------------------------------------------------------------*/
  public String getFieldContents (int index);
}
/*---------------------------------------------------------------------------*/
