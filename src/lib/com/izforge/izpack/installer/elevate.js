/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

/*
 * This script can be used to elevate rights through the UAC on Windows Vista.
 * Run it as follows:
 *    elevate aplication.exe "argument1 argument2 argument3"
 *
 * Adapted from Aaron Margosis 'elevate.js' script, see
 * http://blogs.msdn.com/aaron_margosis/archive/2007/07/01/scripting-elevation-on-vista.aspx
 */

Application = WScript.Arguments(0);
Arguments = "";
for (Index = 1; Index < WScript.Arguments.Length; Index += 1)
{
    if (Index > 1)
    {
        Arguments += " ";
    }
    Arguments += "\"" + WScript.Arguments(Index) + "\"";
}
new ActiveXObject("Shell.Application").ShellExecute(Application, Arguments, "", "runas");
