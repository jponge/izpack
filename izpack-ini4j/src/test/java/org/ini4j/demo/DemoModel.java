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

import bsh.ConsoleInterface;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.NameSpace;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Options;
import org.ini4j.Persistable;
import org.ini4j.Reg;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

public class DemoModel implements Runnable
{
    public static enum Mode
    {
        INI,
        REG,
        OPTIONS;
    }

    private Persistable _data;
    private Interpreter _interpreter;
    private Mode _mode = Mode.INI;

    public DemoModel(ConsoleInterface console)
    {
        _interpreter = new Interpreter(console);
        NameSpace namespace = _interpreter.getNameSpace();

        namespace.importPackage("org.ini4j.spi");
        namespace.importPackage("org.ini4j");
        namespace.importPackage("org.ini4j.sample");
    }

    public Object getData()
    {
        return _data;
    }

    public Mode getMode()
    {
        return _mode;
    }

    public void setMode(Mode mode)
    {
        _mode = mode;
    }

    public void clear() throws EvalError
    {
        _interpreter.unset("data");
    }

    public String help() throws IOException
    {
        return readResource("help.txt");
    }

    public String load() throws IOException
    {
        return readResource(_mode.name().toLowerCase() + "-data.txt");
    }

    public void parse(String text) throws IOException, EvalError
    {
        Persistable data = newData();

        data.load(new StringReader(text));
        _interpreter.set("data", data);
        _data = data;
    }

    @Override public void run()
    {
        _interpreter.setExitOnEOF(false);
        _interpreter.run();
    }

    public String tip() throws IOException
    {
        return readResource(_mode.name().toLowerCase() + "-tip.txt");
    }

    private Persistable newData()
    {
        Persistable ret = null;

        switch (_mode)
        {

            case INI:
                ret = new Ini();
                break;

            case REG:
                ret = new Reg();
                break;

            case OPTIONS:
                ret = new Options();
                break;
        }

        return ret;
    }

    private String readResource(String path) throws IOException
    {
        InputStream in = getClass().getResourceAsStream(path);
        Reader reader = new InputStreamReader(in, Config.DEFAULT_FILE_ENCODING);
        StringBuilder str = new StringBuilder();
        char[] buff = new char[8192];
        int n;

        while ((n = reader.read(buff)) >= 0)
        {
            str.append(buff, 0, n);
        }

        return str.toString();
    }
}
