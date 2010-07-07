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

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import java.net.URI;

//<editor-fold defaultstate="collapsed" desc="apt documentation">
//|
//|                ---------------
//|                Dwarf interface
//|
//|Dwarf interface
//|
//| This is a very simple bean interface with a few getter and setter. Some of
//| the properties are java primitive types. The <<<homePage>>> property has a
//| complex type (java.net.URI). It is not a problem for \[ini4j\] to do the
//| required type conversion automatically between java.lang.String and the tpye
//| of the given property. The <<<fortuneNumber>>> property is indexed, just to
//| show you may use indexed properties as well.
//|
//</editor-fold>
//{
public interface Dwarf
{
    String PROP_AGE = "age";
    String PROP_FORTUNE_NUMBER = "fortuneNumber";
    String PROP_HEIGHT = "height";
    String PROP_HOME_DIR = "homeDir";
    String PROP_HOME_PAGE = "homePage";
    String PROP_WEIGHT = "weight";

    int getAge();

    void setAge(int age);

    int[] getFortuneNumber();

    void setFortuneNumber(int[] value);

    double getHeight();

    void setHeight(double height) throws PropertyVetoException;

    String getHomeDir();

    void setHomeDir(String dir);

    URI getHomePage();

    void setHomePage(URI location);

    double getWeight();

    void setWeight(double weight);

    void addPropertyChangeListener(String property, PropertyChangeListener listener);

    void addVetoableChangeListener(String property, VetoableChangeListener listener);

    boolean hasAge();

    boolean hasHeight();

    boolean hasHomePage();

    boolean hasWeight();

    void removePropertyChangeListener(String property, PropertyChangeListener listener);

    void removeVetoableChangeListener(String property, VetoableChangeListener listener);
}
//}
