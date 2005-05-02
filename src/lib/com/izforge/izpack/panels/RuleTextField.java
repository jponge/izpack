/*
 * $Id$
 * Copyright (C) 2002 Elmar Grom
 *
 * File :               RuleInputField.java
 * Description :        A Java component that serves as a text input field
 *                      with the abilty to impose limitations on the type
 *                      of data that can be entered.
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

package com.izforge.izpack.panels;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/*---------------------------------------------------------------------------*/
/**
 * One line synopsis. <BR>
 * <BR>
 * Enter detailed class description here.
 * 
 * @see UserInputPanel
 * 
 * @version 0.0.1 / 10/20/02
 * @author Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class RuleTextField extends JTextField
{

    /**
     * 
     */
    private static final long serialVersionUID = 3976731454594365493L;

    /** Used to specify numeric input only */
    public static final int N = 1;

    /** Used to specify hexadecimal input only */
    public static final int H = 2;

    /** Used to specify alphabetic input only */
    public static final int A = 3;

    /** Used to specify open input (no restrictions) */
    public static final int O = 4;

    /** Used to specify alpha-numeric input only */
    public static final int AN = 5;

    private int columns;

    private int editLength;

    private int type;

    private boolean unlimitedEdit;

    private Toolkit toolkit;

    private Rule rule;

    public RuleTextField(int digits, int editLength, int type, boolean unlimitedEdit,
            Toolkit toolkit)
    {
        super(digits + 1);

        columns = digits;
        this.toolkit = toolkit;
        this.type = type;
        this.editLength = editLength;
        this.unlimitedEdit = unlimitedEdit;
        rule = new Rule();
        rule.setRuleType(type, editLength, unlimitedEdit);
        setDocument(rule);
    }

    protected Document createDefaultModel()
    {
        rule = new Rule();
        return (rule);
    }

    public int getColumns()
    {
        return (columns);
    }

    public int getEditLength()
    {
        return (editLength);
    }

    public boolean unlimitedEdit()
    {
        return (unlimitedEdit);
    }

    public void setColumns(int columns)
    {
        super.setColumns(columns + 1);
        this.columns = columns;
    }

    // --------------------------------------------------------------------------
    //
    // --------------------------------------------------------------------------

    class Rule extends PlainDocument
    {

        /**
         * 
         */
        private static final long serialVersionUID = 3258134643651063862L;

        private int editLength;

        private int type;

        private boolean unlimitedEdit;

        public void setRuleType(int type, int editLength, boolean unlimitedEdit)
        {
            this.type = type;
            this.editLength = editLength;
            this.unlimitedEdit = unlimitedEdit;
        }

        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
        {
            // --------------------------------------------------
            // don't process if we get a null reference
            // --------------------------------------------------
            if (str == null) { return; }

            // --------------------------------------------------
            // Compute the total length the string would become
            // if the insert request were be honored. If this
            // size is within the specified limits, apply further
            // rules, otherwise give an error signal and return.
            // --------------------------------------------------
            int totalSize = getLength() + str.length();

            if ((totalSize <= editLength) || (unlimitedEdit))
            {
                boolean error = false;

                // test for numeric type
                if (type == N)
                {
                    for (int i = 0; i < str.length(); i++)
                    {
                        if (!Character.isDigit(str.charAt(i)))
                        {
                            error = true;
                        }
                    }
                }
                // test for hex type
                else if (type == H)
                {
                    for (int i = 0; i < str.length(); i++)
                    {
                        char focusChar = Character.toUpperCase(str.charAt(i));
                        if (!Character.isDigit(focusChar) && (focusChar != 'A')
                                && (focusChar != 'B') && (focusChar != 'C') && (focusChar != 'D')
                                && (focusChar != 'E') && (focusChar != 'F'))
                        {
                            error = true;
                        }
                    }
                }
                // test for alpha type
                else if (type == A)
                {
                    for (int i = 0; i < str.length(); i++)
                    {
                        if (!Character.isLetter(str.charAt(i)))
                        {
                            error = true;
                        }
                    }
                }
                // test for alpha-numeric type
                else if (type == AN)
                {
                    for (int i = 0; i < str.length(); i++)
                    {
                        if (!Character.isLetterOrDigit(str.charAt(i)))
                        {
                            error = true;
                        }
                    }
                }
                // test for 'open' -> no limiting rule at all
                else if (type == O)
                {
                    // let it slide...
                }
                else
                {
                    System.out.println("type = " + type);
                }

                // ------------------------------------------------
                // if we had no error when applying the rules, we
                // are ready to insert the string, otherwise give
                // an error signal.
                // ------------------------------------------------
                if (!error)
                {
                    super.insertString(offs, str, a);
                }
                else
                {
                    toolkit.beep();
                }
            }
            else
            {
                toolkit.beep();
            }
        }
    }
}
/*---------------------------------------------------------------------------*/
