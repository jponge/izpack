/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Rene Krell
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

package com.izforge.izpack.core.variable;

import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsVersion;

import java.io.Serializable;


public class ExecValue extends ValueImpl implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = -6438593229737421526L;

    private String cmd[];

    public ExecValue(String[] command, boolean isShellCommand)
    {
        super();
        if (isShellCommand)
        {
            if (OsVersion.IS_WINDOWS)
            {
                this.cmd = new String[command.length + 2];
                this.cmd[0] = "cmd";
                this.cmd[1] = "/C";
                for (int i = 2; i < this.cmd.length; i++)
                {
                    this.cmd[i] = command[i - 2];
                }
            }
            else if (OsVersion.IS_UNIX)
            {
                this.cmd = new String[command.length + 1];
                this.cmd[0] = "sh";
                for (int i = 1; i < this.cmd.length; i++)
                {
                    this.cmd[i] = command[i - 1];
                }
            }
            else
            {
                this.cmd = command;
            }
        }
        else
        {
            this.cmd = command;
        }
    }

    public String[] getCmd()
    {
        return cmd;
    }

    public void setCmd(String[] cmd)
    {
        this.cmd = cmd;
    }

    @Override
    public void validate() throws Exception
    {
        if (this.cmd == null || this.cmd.length <= 0)
        {
            throw new IllegalArgumentException("Bad command line");
        }
    }

    @Override
    public String resolve()
    {
        return FileExecutor.getExecOutput(cmd);
    }

    @Override
    public String resolve(VariableSubstitutor... substitutors) throws Exception
    {
        String _cmd_[] = new String[cmd.length];
        for (int i = 0; i < cmd.length; i++)
        {
            String _cmdarg_ = cmd[i];
            for (VariableSubstitutor substitutor : substitutors)
            {
                _cmdarg_ = substitutor.substitute(_cmdarg_, (SubstitutionType) null);
            }
            _cmd_[i] = _cmdarg_;
        }
        return FileExecutor.getExecOutput(_cmd_);
    }
}
