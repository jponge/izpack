/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Elmar Grom
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.panels;

import com.izforge.izpack.installer.InstallData;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JPasswordField;

/*---------------------------------------------------------------------------*/
/**
 * This class can be used to manage multiple related password fields. This is used in the
 * <code>UserInputPanel</code> to manage communication with the validator and processor for
 * password fields.
 * 
 * @see com.izforge.izpack.panels.UserInputPanel
 * 
 * @version 0.0.1 / 2/22/03
 * @author Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class PasswordGroup implements ProcessingClient
{

    // ------------------------------------------------------------------------
    // Variable Declarations
    // ------------------------------------------------------------------------
    private Vector fields = new Vector();

    private List validatorContainers = null;
//  private Validator validator = null;
//  private boolean hasParams = false;
//  private Map validatorParams = null;
    private Processor processor = null;

    private int currentValidator = 0;

    private InstallData idata;

    /*--------------------------------------------------------------------------*/
    /**
     * Creates a password group to manage one or more password fields.
     * 
     * @param idata the installation data
     * @param validatorContainers the validator containers
     * @param processor the processor
     */
    /*--------------------------------------------------------------------------*/
    public PasswordGroup(InstallData idata, List validatorContainers, String processor)
    {
        // ----------------------------------------------------
        // attempt to create an instance of the Validator
        // ----------------------------------------------------
        try
        {
            this.idata = idata;
//      this.validator = (Validator) Class.forName(validator).newInstance();
            this.validatorContainers = validatorContainers;
//      this.validatorParams = validatorParams;
//      if (validatorParams != null) {
//        if (validatorParams.size() > 0) {
//          hasParams = true;
//        }
//      }
        } catch (Throwable exception)
        {
            this.validatorContainers = null;
        }

        // ----------------------------------------------------
        // attempt to create an instance of the Processor
        // ----------------------------------------------------
        try
        {
            this.processor = (Processor) Class.forName(processor).newInstance();
        } catch (Throwable exception)
        {
            this.processor = null;
        }
    }

    public InstallData getIdata()
    {
        return idata;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the number of sub-fields.
     * 
     * @return the number of sub-fields
     */
    /*--------------------------------------------------------------------------*/
    public int getNumFields()
    {
        return (fields.size());
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the contents of the field indicated by <code>index</code>.
     * 
     * @param index the index of the sub-field from which the contents is requested.
     * 
     * @return the contents of the indicated sub-field.
     * 
     * @exception IndexOutOfBoundsException if the index is out of bounds.
     */
    /*--------------------------------------------------------------------------*/
    public String getFieldContents(int index) throws IndexOutOfBoundsException
    {
        if ((index < 0) || (index >= fields.size()))
        {
            throw (new IndexOutOfBoundsException());
        }

        String contents = new String(((JPasswordField) fields.elementAt(index)).getPassword());
        return (contents);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds a <code>JPasswordField</code> to the group of fields being managed by this object.
     * 
     * @param field <code>JPasswordField</code> to add
     */
    /*--------------------------------------------------------------------------*/
    public void addField(JPasswordField field)
    {
        if (field != null)
        {
            fields.add(field);
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * This method validates the group content. Validating is performed through a user supplied
     * service class that provides the validation rules.
     * 
     * @return <code>true</code> if the validation passes or no implementation of a validation
     * rule exists. Otherwise <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    public boolean validateContents(int i)
    {
        boolean returnValue = true;
        try
        {
            currentValidator = i;
            ValidatorContainer container = getValidatorContainer(i);
            Validator validator = container.getValidator();
            if (validator != null)
            {
                returnValue = validator.validate(this);
            }
        } catch (Exception e)
        {
            System.out.println("validateContents(" + i + ") failed: " + e);
        // just return true
        }
        return returnValue;
    }

    public String getValidatorMessage(int i)
    {
        String returnValue = null;
        try
        {
            ValidatorContainer container = getValidatorContainer(i);
            if (container != null)
            {
                returnValue = container.getMessage();
            }
        } catch (Exception e)
        {
            System.out.println("getValidatorMessage(" + i + ") failed: " + e);
        // just return true
        }
        return returnValue;
    }

    public int validatorSize()
    {
        int size = 0;
        if (validatorContainers != null)
        {
            size = validatorContainers.size();
        }
        return size;
    }

    public ValidatorContainer getValidatorContainer()
    {
        return getValidatorContainer(currentValidator);
    }

    public ValidatorContainer getValidatorContainer(int i)
    {
        ValidatorContainer container = null;
        try
        {
            container = (ValidatorContainer) validatorContainers.get(i);
        } catch (Exception e)
        {
            container = null;
        }
        return container;
    }

    public boolean hasParams()
    {
        return hasParams(currentValidator);
    }

    public boolean hasParams(int i)
    {
        boolean returnValue = false;
        try
        {
            ValidatorContainer container = getValidatorContainer(i);
            if (container != null)
            {
                returnValue = container.hasParams();
            }
        } catch (Exception e)
        {
            System.out.println("hasParams(" + i + ") failed: " + e);
        // just return true
        }
        return returnValue;
    }

    public Map getValidatorParams()
    {
        return getValidatorParams(currentValidator);
    }

    public Map getValidatorParams(int i)
    {
        Map returnValue = null;
        try
        {
            ValidatorContainer container = getValidatorContainer(i);
            if (container != null)
            {
                returnValue = container.getValidatorParams();
            }
        } catch (Exception e)
        {
            System.out.println("getValidatorParams(" + i + ") failed: " + e);
        // just return true
        }
        return returnValue;
    }

    // This method was added to support changes to ProcessingClient interface
    // it's use is non-deterministic in the newly implemented text validators.
    public String getText()
    {
        return getValidatorMessage(currentValidator);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the password. If a processing service class was supplied it will be used to process
     * the password before it is returned, otherwise the content of the first field will be
     * returned.
     * 
     * @return the password
     */
    /*--------------------------------------------------------------------------*/
    public String getPassword()
    {
        if (processor != null)
        {
            return (processor.process(this));
        } else
        {
            String contents = "";

            if (fields.size() > 0)
            {
                contents = new String(((JPasswordField) fields.elementAt(0)).getPassword());
            }

            return (contents);
        }
    }

}
/*---------------------------------------------------------------------------*/
