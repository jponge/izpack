/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2007 Dennis Reil
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
package com.izforge.izpack.installer;

import com.izforge.izpack.rules.RulesEngine;

public interface IUnpacker extends Runnable
{
    /**
     * Return the state of the operation.
     *
     * @return true if the operation was successful, false otherwise.
     */
    public abstract boolean getResult();

    /**
     * Called by the InstallerFrame to set a reference to the RulesEngine, which will
     * be used to check conditions.
     *
     * @param rules - an instantiated RulesEngine
     */
    public void setRules(RulesEngine rules);
}