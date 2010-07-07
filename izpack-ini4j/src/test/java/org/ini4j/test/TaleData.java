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
package org.ini4j.test;

import org.ini4j.test.DwarfsData.DwarfData;

public final class TaleData
{
    public static final String PROP_DWARFS = "dwarfs";
    public static final char PATH_SEPARATOR = '/';
    public static final DwarfData bashful;
    public static final DwarfData doc;
    public static final DwarfData dopey;
    public static final DwarfData grumpy;
    public static final DwarfData happy;
    public static final DwarfData sleepy;
    public static final DwarfData sneezy;

    static
    {
        bashful = newDwarfData(DwarfsData.bashful);
        doc = newDwarfData(DwarfsData.doc);
        dopey = newDwarfData(DwarfsData.dopey);
        grumpy = newDwarfData(DwarfsData.grumpy);
        happy = newDwarfData(DwarfsData.happy);
        sleepy = newDwarfData(DwarfsData.sleepy);
        sneezy = newDwarfData(DwarfsData.sneezy);
    }

    private TaleData()
    {
    }

    private static DwarfData newDwarfData(DwarfData orig)
    {
        return new DwarfData(PROP_DWARFS + PATH_SEPARATOR + orig.name, orig.age, orig.fortuneNumber, orig.height, orig.homeDir, orig.homePage.toString(), orig.weight);
    }
}
