//******************************************************************
// NotEmptyValidator
// Copyright © 2003 by Tino Schwarze
//******************************************************************
package com.izforge.izpack.util;

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;

/**
 * A validator to enforce non-empty fields.
 * 
 * This validator can be used for rule input fields in the UserInputPanel to make
 * sure that the user entered something.
 * 
 * @author tisc
 */
public class NotEmptyValidator implements Validator
{

  public boolean validate(ProcessingClient client)
  {
    int numfields = client.getNumFields();
    
    for (int i = 0; i < numfields; i++)
    {
      String value = client.getFieldContents(i);
      
      if ((value == null) || (value.length() == 0))
        return false;
    }
    
    return true;
  }

}
