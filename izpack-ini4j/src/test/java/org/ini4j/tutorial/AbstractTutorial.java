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
package org.ini4j.tutorial;

import java.io.File;

public abstract class AbstractTutorial
{
    public static final String FILENAME = "../sample/dwarfs.ini";

    protected abstract void run(File arg) throws Exception;

    protected static File filearg(String[] args)
    {
        return new File((args.length > 0) ? args[0] : FILENAME);
    }
}
