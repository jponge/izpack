/*
 * Copyright 2005,2009 Ivan SZKIBA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ini4j.demo;

import bsh.util.JConsole;

import org.ini4j.demo.DemoModel.Mode;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public class Demo
{
    private enum Command
    {
        MODE_INI,
        MODE_REG,
        MODE_OPTIONS,
        LOAD_TEST_DATA,
        PARSE_DATA,
        CLEAR_DATA
    }

    private JConsole _console;
    private final Container _container;
    private JTextArea _dataTextArea;
    private JTextArea _helpTextArea;
    private DemoModel _model;
    private JTextArea _tipTextArea;
    private ActionListener _actionListener = new ActionListener()
    {
        public void actionPerformed(ActionEvent event)
        {
            Command cmd = Command.valueOf(event.getActionCommand());

            switch (cmd)
            {

                case MODE_INI:
                    doMode(Mode.INI);
                    break;

                case MODE_REG:
                    doMode(Mode.REG);
                    break;

                case MODE_OPTIONS:
                    doMode(Mode.OPTIONS);
                    break;

                case LOAD_TEST_DATA:
                    doLoad();
                    break;

                case PARSE_DATA:
                    doParse();
                    break;

                case CLEAR_DATA:
                    doClear();
                    break;
            }
        }
    };

    public Demo(Container container)
    {
        _container = container;
    }

    public void init()
    {
        _container.setBackground(Color.WHITE);
        _container.setLayout(new BoxLayout(_container, BoxLayout.PAGE_AXIS));
        initInputPane();
        initButtonsPane();
        initOutputPane();

        //
        new Thread(_model).start();
        doMode(Mode.INI);
    }

    private void addButton(JPanel panel, String label, Command command)
    {
        JButton button = new JButton();

        button.setText(label);
        button.setActionCommand(command.name());
        button.addActionListener(_actionListener);
        panel.add(button);
    }

    private void addModeButton(ButtonGroup group, JPanel panel, Mode mode)
    {
        String label = mode.name().charAt(0) + mode.name().toLowerCase().substring(1);
        JRadioButton button = new JRadioButton(label);

        button.setActionCommand("MODE_" + mode.name());
        button.setSelected(mode == Mode.INI);
        panel.add(button);
        button.addActionListener(_actionListener);
        group.add(button);
    }

    private void doClear()
    {
        try
        {
            _dataTextArea.setText("");
            _model.clear();
        }
        catch (Exception x)
        {
            exceptionThrown(x);
        }
    }

    private void doLoad()
    {
        try
        {
            _dataTextArea.setText(_model.load());
            _console.println("Test data loaded");
        }
        catch (Exception x)
        {
            exceptionThrown(x);
        }
    }

    private void doMode(Mode mode)
    {
        _model.setMode(mode);
        try
        {
            _tipTextArea.setText(_model.tip());
        }
        catch (Exception x)
        {
            exceptionThrown(x);
        }
    }

    private void doParse()
    {
        try
        {
            _model.parse(_dataTextArea.getText());
            _console.println("Parse ready");
        }
        catch (Exception x)
        {
            exceptionThrown(x);
        }
    }

    private void exceptionThrown(Exception exception)
    {
        _console.error(exception);
        _console.error("\n");
        exception.printStackTrace();
    }

    private void initButtonsPane()
    {
        JPanel buttons = new JPanel();

        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.setBackground(Color.WHITE);
        buttons.add(new JLabel("Mode: "));
        ButtonGroup group = new ButtonGroup();

        addModeButton(group, buttons, Mode.INI);
        addModeButton(group, buttons, Mode.REG);
        addModeButton(group, buttons, Mode.OPTIONS);
        buttons.add(Box.createHorizontalGlue());
        addButton(buttons, " C L E A R ", Command.CLEAR_DATA);
        addButton(buttons, " L O A D ", Command.LOAD_TEST_DATA);
        addButton(buttons, " P A R S E ", Command.PARSE_DATA);
        _container.add(buttons);
    }

    private void initInputPane()
    {
        JTabbedPane inputPane = new JTabbedPane(JTabbedPane.TOP);

        inputPane.setPreferredSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        inputPane.setBackground(Color.WHITE);
        _dataTextArea = new JTextArea();
        JScrollPane sp = new JScrollPane(_dataTextArea);

        inputPane.addTab("data", sp);
        _tipTextArea = new JTextArea();
        _tipTextArea.setEditable(false);
        sp = new JScrollPane(_tipTextArea);
        inputPane.addTab("tip", sp);
        _helpTextArea = new JTextArea();
        _helpTextArea.setEditable(false);
        sp = new JScrollPane(_helpTextArea);
        inputPane.addTab("help", sp);
//
        _container.add(inputPane);
    }

    private void initOutputPane()
    {
        JTabbedPane output = new JTabbedPane(JTabbedPane.BOTTOM);
        JConsole console = new JConsole();

        console.setBackground(Color.WHITE);
        _model = new DemoModel(console);
        _console = new JConsole();

        output.addTab("Console", _console);
        output.setBackground(Color.WHITE);
        output.setPreferredSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        output.addTab("Interpreter", console);
        try
        {

            //
            _helpTextArea.setText(_model.help());
        }
        catch (IOException x)
        {
            exceptionThrown(x);
        }

        //
        _container.add(output);
    }
}
