/*
 * $Id$
 * Copyright (C) 2002 Elmar Grom
 *
 * File :               RuleValidator.java
 * Description :        This interface defines the public interface for
 *                      classes that perform validation services for
 *                      the UsserInputPanel.
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
 * Interface for classes that provide rule validation services.
 *
 * @version  0.0.1 / 10/26/02
 * @author   Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public interface RuleValidator
{
 /*--------------------------------------------------------------------------*/
 /**
  * Validates the contend of a <code>RuleInputField</code>. 
  *
  * @param     client   the client object using the services of this validator.
  *
  * @return    <code>true</code> if the validation passes, otherwise <code>false</code>.
  */
 /*--------------------------------------------------------------------------*/
  public boolean validate (RuleInputField client);
}
/*---------------------------------------------------------------------------*/
