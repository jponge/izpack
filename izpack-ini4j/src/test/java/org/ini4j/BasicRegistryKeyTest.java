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
package org.ini4j;

import org.ini4j.Registry.Type;

import static org.junit.Assert.*;

import org.junit.Test;

public class BasicRegistryKeyTest
{
    private static final String KEY = "key";
    private static final String DUMMY = "dummy";
    private static final String OPTION = "option";

    @Test public void testWrapped() throws Exception
    {
        BasicRegistry reg = new BasicRegistry();
        Registry.Key parent = reg.add(KEY);
        Registry.Key child = parent.addChild(DUMMY);

        assertSame(parent, child.getParent());
        assertSame(child, parent.getChild(DUMMY));
        Registry.Key kid = child.addChild(KEY);

        assertSame(kid, parent.lookup(DUMMY, KEY));
        parent.put(OPTION, DUMMY);
        parent.putType(OPTION, Type.REG_BINARY);
        assertEquals(Type.REG_BINARY, parent.getType(OPTION));
        parent.removeType(OPTION);
        assertNull(parent.getType(OPTION));
        parent.putType(OPTION, Type.REG_BINARY);
        parent.remove(OPTION);
        assertNull(parent.getType(OPTION));
    }
}
