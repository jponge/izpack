/*
 *  $Id$
 *  IzPack
 *  Copyright (C) 2001-2004 Julien Ponge
 *
 *  File :               RegularExpressionValidator.java
 *  Description :        A validator to enforce non-empty fields.
 *  Author's email :     mike.cunneen@screwfix.com
 *  Author's Website :   N/A
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.izforge.izpack.util;

import java.util.Map;

import org.apache.regexp.RE;

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.RuleInputField;
import com.izforge.izpack.panels.Validator;

/**
 * A validator to enforce non-empty fields.
 *
 * This validator can be used for rule input fields in the UserInputPanel to make
 * sure that the user's entry matches a specified regular expression.
 *
 * @author Mike Cunneen <mike dot cunneen at screwfix dot com>
 */
public class RegularExpressionValidator implements Validator
{

  public static final String STR_PATTERN_DEFAULT = "[a-zA-Z0-9._-]{3,}@[a-zA-Z0-9._-]+([.][a-zA-Z0-9_-]+)*[.][a-zA-Z0-9._-]{2,4}";

  private static final String PATTERN_PARAM = "pattern";

  public boolean validate(ProcessingClient client)
  {

    String patternString;

    RuleInputField field = (RuleInputField) client;
    if (field.hasParams())
    {
      Map paramMap = field.getValidatorParams();
      patternString = (String) paramMap.get(PATTERN_PARAM);

    }
    else
    {
      patternString = STR_PATTERN_DEFAULT;
    }

    RE pattern = new RE(patternString);
    return pattern.match(((RuleInputField) client).getText());
  }

}