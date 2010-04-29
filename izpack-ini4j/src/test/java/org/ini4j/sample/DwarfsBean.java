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
package org.ini4j.sample;

//<editor-fold defaultstate="collapsed" desc="apt documentation">
//|
//|                ----------------
//|                DwarfsBean class
//|
//|DwarfsBean class
//|
//</editor-fold>
//{
public class DwarfsBean implements Dwarfs
{
    private Dwarf _bashful;
    private Dwarf _doc;
    private Dwarf _dopey;
    private Dwarf _grumpy;
    private Dwarf _happy;
    private Dwarf _sleepy;
    private Dwarf _sneezy;

    @Override public Dwarf getBashful()
    {
        return _bashful;
    }

    public void setBashful(Dwarf value)
    {
        _bashful = value;
    }

    @Override public Dwarf getDoc()
    {
        return _doc;
    }

    public void setDoc(Dwarf value)
    {
        _doc = value;
    }

    @Override public Dwarf getDopey()
    {
        return _dopey;
    }

    public void setDopey(Dwarf value)
    {
        _dopey = value;
    }

    @Override public Dwarf getGrumpy()
    {
        return _grumpy;
    }

    public void setGrumpy(Dwarf value)
    {
        _grumpy = value;
    }

    @Override public Dwarf getHappy()
    {
        return _happy;
    }

    public void setHappy(Dwarf value)
    {
        _happy = value;
    }

    @Override public Dwarf getSleepy()
    {
        return _sleepy;
    }

    public void setSleepy(Dwarf value)
    {
        _sleepy = value;
    }

    @Override public Dwarf getSneezy()
    {
        return _sneezy;
    }

    public void setSneezy(Dwarf value)
    {
        _sneezy = value;
    }
}
//}
