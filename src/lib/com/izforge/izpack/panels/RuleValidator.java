/*---------------------------------------------------------------------------*
 *                      (C) Copyright 2002 by Elmar Grom
 *                                       
 *                           - All Rights Reserved -             
 *                                       
 *                  THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE        
 *                                       
 * This copyright notice does not evidence any actual or intended publication
 *---------------------------------------------------------------------------*/

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
