/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2002 Jan Blok
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.izforge.izpack.Panel;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.SpecHelper;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * The user input panel console helper class.
 * 
 * @author Mounir El Hajj
 */
public class UserInputPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{

    protected int instanceNumber = 0;

    private static int instanceCount = 0;

    private static final String SPEC_FILE_NAME = "userInputSpec.xml";

    private static final String NODE_ID = "panel";

    private static final String INSTANCE_IDENTIFIER = "order";

    protected static final String PANEL_IDENTIFIER = "id";

    private static final String FIELD_NODE_ID = "field";

    protected static final String ATTRIBUTE_CONDITIONID_NAME = "conditionid";

    private static final String VARIABLE = "variable";

    private static final String SET = "set";

    private static final String TEXT = "txt";

    private static final String SPEC = "spec";

    private static final String TYPE_ATTRIBUTE = "type";

    private static final String TEXT_FIELD = "text";

    private static final String COMBO_FIELD = "combo";

    private static final String STATIC_TEXT = "staticText";

    private static final String CHOICE = "choice";

    private static final String VALUE = "value";

    private static final String RADIO_FIELD = "radio";

    private static final String DESCRIPTION = "description";

    private static final String TRUE = "true";

    public List<Input> listInputs;

    public UserInputPanelConsoleHelper()
    {
        instanceNumber = instanceCount++;
        listInputs = new ArrayList<Input>();
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        collectInputs(installData);
        Iterator<Input> inputIterator = listInputs.iterator();
        while (inputIterator.hasNext())
        {
            String strVariableName = ((Input) inputIterator.next()).strVariableName;
            String strVariableValue = p.getProperty(strVariableName);
            if (strVariableValue != null)
            {
                installData.setVariable(strVariableName, strVariableValue);
            }
        }
        return true;
    }

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
            PrintWriter printWriter)
    {
        collectInputs(installData);
        Iterator<Input> inputIterator = listInputs.iterator();
        while (inputIterator.hasNext())
        {
            printWriter.println(((Input) inputIterator.next()).strVariableName + "=");
        }
        return true;
    }

    public boolean runConsole(AutomatedInstallData idata)
    {
        collectInputs(idata);
        boolean status = true;
        Iterator<Input> inputsIterator = listInputs.iterator();
        while (inputsIterator.hasNext())
        {
            Input input = inputsIterator.next();
            String text = input.strText;
            if (text != null)
            {
                System.out.println(text);
            }
            if (TEXT_FIELD.equals(input.strFieldType))
            {
                status = status && processTextField(input, idata);
            }
            else if (COMBO_FIELD.equals(input.strFieldType)
                    || RADIO_FIELD.equals(input.strFieldType))
            {
                status = status && processComboRadioField(input, idata);
            }

        }

        int i = askEndOfConsolePanel();
        if (i == 1)
        {
            return true;
        }
        else if (i == 2)
        {
            return false;
        }
        else
        {
            return runConsole(idata);
        }

    }

    public boolean collectInputs(AutomatedInstallData idata)
    {
        listInputs.clear();
        IXMLElement data;
        IXMLElement spec = null;
        Vector<IXMLElement> specElements;
        String attribute;
        String dataID;
        String panelid = ((Panel) idata.panelsOrder.get(idata.curPanelNumber)).getPanelid();
        String instance = Integer.toString(instanceNumber);

        SpecHelper specHelper = new SpecHelper();
        try
        {
            specHelper.readSpec(specHelper.getResource(SPEC_FILE_NAME));
        }
        catch (Exception e1)
        {

            e1.printStackTrace();
        }

        specElements = specHelper.getSpec().getChildrenNamed(NODE_ID);
        for (int i = 0; i < specElements.size(); i++)
        {
            data = specElements.elementAt(i);
            attribute = data.getAttribute(INSTANCE_IDENTIFIER);
            dataID = data.getAttribute(PANEL_IDENTIFIER);
            if (((attribute != null) && instance.equals(attribute))
                    || ((dataID != null) && (panelid != null) && (panelid.equals(dataID))))
            {
                spec = data;
            }
        }
        Vector<IXMLElement> fields = spec.getChildrenNamed(FIELD_NODE_ID);
        for (int i = 0; i < fields.size(); i++)
        {
            IXMLElement field = fields.elementAt(i);
            String conditionid = field.getAttribute(ATTRIBUTE_CONDITIONID_NAME);
            if (conditionid != null)
            {
                // check if condition is fulfilled
                if (!idata.getRules().isConditionTrue(conditionid, idata.getVariables()))
                {
                    continue;
                }
            }
            listInputs.add(getInputFromField(field));
        }
        return true;
    }

    boolean processTextField(Input input, AutomatedInstallData idata)
    {
        String variable = input.strVariableName;
        String set;
        String fieldText;
        if ((variable == null) || (variable.length() == 0)) { return false; }

        if (input.listChoices.size() == 0)
        {
            Debug.trace("Error: no spec element defined in file field");
            return false;
        }
        set = input.strDefaultValue;
        if (set == null)
        {
            set = idata.getVariable(variable);
            if (set == null)
            {
                set = "";
            }
        }
        else
        {
            if (set != null && !"".equals(set))
            {

                VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                set = vs.substitute(set, null);
            }
        }

        fieldText = input.listChoices.get(0).strText;
        System.out.println(fieldText + " [" + set + "] ");
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String strIn = br.readLine();
            if (!strIn.trim().equals(""))
            {
                idata.setVariable(variable, strIn);
            }
            else
            {
                idata.setVariable(variable, set);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return true;

    }

    boolean processComboRadioField(Input input, AutomatedInstallData idata)
    {// TODO protection if selection not valid and no set value
        String variable = input.strVariableName;
        if ((variable == null) || (variable.length() == 0)) { return false; }
        String currentvariablevalue = idata.getVariable(variable);
        boolean userinput = false;
        List<Choice> lisChoices = input.listChoices;
        if (lisChoices.size() == 0)
        {
            Debug.trace("Error: no spec element defined in file field");
            return false;
        }
        if (currentvariablevalue != null)
        {
            userinput = true;
        }
        for (int i = 0; i < lisChoices.size(); i++)
        {
            Choice choice = lisChoices.get(i);
            String value = choice.strValue;
            if (userinput)
            {
                if ((value != null) && (value.length() > 0) && (currentvariablevalue.equals(value)))
                {
                    input.iSelectedChoice = i;
                }
            }
            else
            {
                String set = choice.strSet;
                if (set != null)
                {
                    if (set != null && !"".equals(set))
                    {
                        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                        set = vs.substitute(set, null);
                    }
                    if (set.equals(TRUE))
                    {
                        input.iSelectedChoice = i;
                    }
                }
            }
            System.out.println(i + "  [" + (input.iSelectedChoice == i ? "x" : " ") + "] "
                    + (choice.strText != null ? choice.strText : ""));
        }

        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            boolean bKeepAsking = true;

            while (bKeepAsking)
            {
                System.out.println("input selection:");
                String strIn = br.readLine();
                // take default value if default value exists and no user input
                if (strIn.trim().equals("") && input.iSelectedChoice != -1)
                {
                    bKeepAsking = false;
                }
                int j = -1;
                try
                {
                    j = Integer.valueOf(strIn).intValue();
                }
                catch (Exception ex)
                {}
                // take user input if user input is valid
                if (j >= 0 && j < lisChoices.size())
                {
                    input.iSelectedChoice = j;
                    bKeepAsking = false;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        idata.setVariable(variable, input.listChoices.get(input.iSelectedChoice).strValue);
        return true;

    }

    public Input getInputFromField(IXMLElement field)
    {
        String strVariableName = field.getAttribute(VARIABLE);
        String strFieldType = field.getAttribute(TYPE_ATTRIBUTE);
        if (STATIC_TEXT.equals(strFieldType))
        {
            String strText = null;
            strText = field.getAttribute(TEXT);
            return new Input(strVariableName, null, null, STATIC_TEXT, strText, 0);
        }
        if (TEXT_FIELD.equals(strFieldType))
        {
            List<Choice> choicesList = new ArrayList<Choice>();
            String strFieldText = null;
            String strSet = null;
            String strText = null;
            IXMLElement spec = field.getFirstChildNamed(SPEC);
            IXMLElement description = field.getFirstChildNamed(DESCRIPTION);
            if (spec != null)
            {
                strText = field.getFirstChildNamed(SPEC).getAttribute(TEXT);
                strSet = field.getFirstChildNamed(SPEC).getAttribute(SET);
            }
            if (description != null)
            {
                strFieldText = description.getAttribute(TEXT);
            }
            choicesList.add(new Choice(strText, null, strSet));
            return new Input(strVariableName, strSet, choicesList, TEXT_FIELD, strFieldText, 0);

        }
        else if (COMBO_FIELD.equals(strFieldType) || RADIO_FIELD.equals(strFieldType))
        {
            List<Choice> choicesList = new ArrayList<Choice>();
            String strFieldText = null;
            IXMLElement spec = field.getFirstChildNamed(SPEC);
            IXMLElement description = field.getFirstChildNamed(DESCRIPTION);
            Vector<IXMLElement> choices = null;
            if (spec != null)
            {
                choices = spec.getChildrenNamed(CHOICE);
            }
            if (description != null)
            {
                strFieldText = description.getAttribute(TEXT);
            }
            for (int i = 0; i < choices.size(); i++)
            {
                IXMLElement choice = choices.elementAt(i);
                choicesList.add(new Choice(choice.getAttribute(TEXT), choice.getAttribute(VALUE),
                        choice.getAttribute(SET)));
            }
            return new Input(strVariableName, null, choicesList, COMBO_FIELD, strFieldText, -1);
        }
        else
        {
            System.out.println(strFieldType + " field collection not implemented");

        }
        return null;
    }

    public class Input
    {

        public Input(String strVariableName, String strDefaultValue, List<Choice> listChoices,
                String strFieldType, String strText, int iSelectedChoice)
        {
            this.strVariableName = strVariableName;
            this.strDefaultValue = strDefaultValue;
            this.listChoices = listChoices;
            this.strFieldType = strFieldType;
            this.strText = strText;
            this.iSelectedChoice = iSelectedChoice;
        }

        String strVariableName;

        String strDefaultValue;

        List<Choice> listChoices;

        String strFieldType;

        String strText;

        int iSelectedChoice = -1;
    }

    public class Choice
    {

        public Choice(String strText, String strValue, String strSet)
        {
            this.strText = strText;
            this.strValue = strValue;
            this.strSet = strSet;
        }

        String strText;

        String strValue;

        String strSet;
    }
}
