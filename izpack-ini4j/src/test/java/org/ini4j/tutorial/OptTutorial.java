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

import org.ini4j.Options;

import org.ini4j.sample.Dwarf;

import org.ini4j.test.DwarfsData;
import org.ini4j.test.Helper;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.Set;

//<editor-fold defaultstate="collapsed" desc="apt documentation">
//|
//|                ----------------
//|                Options Tutorial
//|
//|Options Tutorial - java.util.Properties replacement
//|
//| Options (org.ini4j.Options) is a java.util.Properties replacement with
//| several useful features, like:
//|
//|  * variable/macro substitution. You may refer to other property's value with
//|  $\{NAME\} expression, where NAME is the name of the referred property.
//|  ofcourse you can use more than one property reference per property, and
//|  you can mix constant text and property references:
//|
//|+-------------------+
//|player.name = Joe
//|player.greetings = Hi ${player.name}!
//|player.domain = foo.bar
//|player.email = ${player.name}@${player.domain}
//|+-------------------+
//|
//|  * multiply property values. You can refer to multi value properties with
//| integer indexes. Ofcource it is also works in macro/variable substitutions:
//| $\{user.fortuneNumber\[2\]\}
//|
//|+-------------------+
//|player.fortuneNumber = 33
//|player.fortuneNumber = 44
//|player.fortuneNumber = 55
//|player.fortuneNumber = 66
//|
//|magicNumber = ${player.foruneNumber[1]}
//|+--------------------+
//|
//|  The magicNumber property will have value: <<<44>>>
//|
//|  * as Java class, Options is basicly map of Strings indexed with Strings. It
//|  is standard Collection API (ok, it is a bit enhanced to deal with multi
//|  values, but in general it is a Map\<String,String\>).
//|
//|  * Java Beans api. You can read/write properties in type safe way. To do it
//|  you just define an interface, call Options#as() method. This method will
//|  provide an implementation of given interface on top of Options. Property
//|  types are mapped automatically between Java type and String.
//|
//|* Why need Options
//|
//| With standard Properties class there is several small problem. Most of them
//| came from backward compatibility.
//|
//|  * not implements Map\<String,String\>, but Map\<Object,Object\>. If you
//|    want to use Collections api, it is a bit unconfortable.
//|
//|  * only single property values allowed. Probably you already see ugly
//|    workarounds: index number in property names, like: file.1, file.2 ...
//|
//|  * no macro/variable substitution. In some environment, like
//|    Apache Ant, you can use ${name} like references, but with standard
//|    java.util.Properties you can't.
//|
//| As side effect of \[ini4j\] development, there is a solution for aboves.
//| This is the org.ini4j.Options class, which is basicly a feature rich
//| replacement for java.util.Properties.
//|
//| Code sniplets in this tutorial tested with the following .opt file:
//| {{{../sample/dwarfs.opt.html}dwarfs.opt}}
//|
//</editor-fold>
public class OptTutorial extends AbstractTutorial
{
    public static final String FILENAME = "../sample/dwarfs.opt";

    public static void main(String[] args) throws Exception
    {
        new OptTutorial().run(filearg(args));
    }

    protected void run(File arg) throws Exception
    {
        Options opt = new Options(arg.toURI().toURL());

        sample01(arg);
        sample02(opt);
    }

//|
//|* Instantiating
//|
//| There is nothing special with instantiating Options object, but there is a
//| few constructor, to simplify loading data. These constructors simply call
//| the <<<load()>>> method on newly created instance. Ofcource these
//| constructors are throws IOException.
//{
    void sample01(File file) throws IOException
    {
        Options opt = new Options();

        //
        // or instantiate and load data:
        //
        opt = new Options(new FileReader(file));

//}
        assertFalse(opt.keySet().isEmpty());
    }

//|
//|* Map of String
//{
    void sample02(Options opt)
    {
        Set<String> optionNames = opt.keySet();

        //
        String age = opt.get("age");
        String weight = opt.fetch("weight");
        String height = opt.fetch("height");

//}
//|
//| The Options is a MultiMap\<String,String\>, that is, a map that assigns
//| String values to String keys. So the <<<get>>> method is used to get values
//| inside the options. To get a value, besides <<<get()>>> you can also
//| use <<<fetch()>>> which resolves any occurrent $\{option\} format
//| variable references in the needed value.
        Helper.assertEquals(DwarfsData.dopey, opt.as(Dwarf.class));
    }
}
