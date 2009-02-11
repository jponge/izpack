package com.izforge.izpack.rules;

import com.izforge.izpack.util.Debug;

import com.izforge.izpack.adaptator.IXMLElement;

/**
 * Checks to see whether the user who is running the installer is the same as the user who should be
 * running the installer.
 * 
 * @author J. Chris Folsom <jchrisfolsom@gmail.com>
 * @author Dennis Reil <izpack@reil-online.de>
 *
 */
public class UserCondition extends Condition
{    
    private static final long serialVersionUID = -2076347348048202718L;
    private String requiredUsername;
    
    @Override
    public boolean isTrue()
    {
        boolean result = false;
        if (this.requiredUsername == null)
        {
            Debug.log("Expected user name not set in user condition. Condition will return false.");            
        }
        else
        {
            String actualUsername = System.getProperty("user.name");
            if ((actualUsername != null) || (actualUsername.length() >= 0)){
                result = this.requiredUsername.equals(actualUsername);
            }            
            else {
                Debug.log("No user.name found in system properties. Condition will return false.");
            }
        }
        return result;
    }

    @Override
    public void readFromXML(IXMLElement xmlcondition)
    {
        IXMLElement userElement = xmlcondition.getFirstChildNamed("requiredusername");

        if (userElement == null)
        {
            Debug.log("Condition or type \"user\" requires child element: user");
        }
        else
        {
            this.requiredUsername = userElement.getContent();
        }
    }

}
