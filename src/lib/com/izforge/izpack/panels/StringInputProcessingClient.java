package com.izforge.izpack.panels;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringInputProcessingClient implements ProcessingClient
{

    private String input;

    private List<ValidatorContainer> validators;

    private ValidatorContainer currentValidator;
    
    private String message;

    public StringInputProcessingClient(String input, List<ValidatorContainer> validators)
    {
        this.input = input;
        this.validators = validators;
    }

    public String getFieldContents(int index)
    {
        return input;
    }

    public int getNumFields()
    {
        return 1;
    }

    public String getText()
    {
        return this.input;
    }

    public Map<String, String> getValidatorParams()
    {        
        return (currentValidator != null) ? currentValidator.getValidatorParams() : new HashMap<String,String>();
    }

    public boolean hasParams()
    {
        return (currentValidator != null) ? currentValidator.hasParams() : false;
    }

    public boolean validate()
    {
        boolean success = true;
        
        if (validators != null){
            for (ValidatorContainer validator : validators)
            {
                currentValidator = validator;
                Validator validatorInstance = currentValidator.getValidator();
                if (validatorInstance != null){
                    success = validatorInstance.validate(this);
                    if (!success){
                        message = currentValidator.getMessage();
                        break;
                    }
                }
                
            }    
        }
        
        return success;
    }
    
    public String getValidationMessage(){
        return message;
    }
}
