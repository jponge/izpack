/*
 * IzPack - Copyright 2001-2006 Julien Ponge, All Rights Reserved.
 * 
 * http://www.izforge.com/izpack/
 * http://developer.berlios.de/projects/izpack/
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

package com.izforge.izpack;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import junit.framework.TestCase;

/**
 * A JUnit TestCase to check completeness of the all the language packs
 * 
 * @author Hans Aikema
 *
 */
public class Bin_Langpacks_InstallerTest extends TestCase
{
    private final static String referencePack = "eng.xml";
    private final static String basePath= "." + File.separator + 
                                "bin" + File.separator +
                                "langpacks" + File.separator +
                                "installer" + File.separator;
    private static LocaleDatabase reference;
    private LocaleDatabase check;

    public Bin_Langpacks_InstallerTest() throws Exception
    {
        this("");
    }
    
    public Bin_Langpacks_InstallerTest(String arg0) throws Exception
    {
        super(arg0);
        Bin_Langpacks_InstallerTest.reference = new LocaleDatabase(new FileInputStream(basePath + referencePack));
    }
    
    private void checkLangpack(String langpack) throws Exception{
        check = new LocaleDatabase(new FileInputStream(basePath + langpack));
        // all keys in the English langpack should be present in the foreign langpack
        for (Iterator i = reference.keySet().iterator();i.hasNext();) {
            // Locale Database uses the id strings as keys
            String id = (String) i.next();
            assertTrue("Missing translation for id:"+id,check.containsKey(id));
        }
        // there should be no keys in the foreign langpack which don't exist in the 
        // english langpack
        for (Iterator i = check.keySet().iterator();i.hasNext();) {
            // LocaleDatabase uses the id strings as keys
            String id = (String) i.next();
            assertTrue("Superfluous translation for id:"+id,reference.containsKey(id));
        }
    }
    public void testCat() throws Exception{
        this.checkLangpack("cat.xml");
    }
    public void testChn() throws Exception{
        this.checkLangpack("chn.xml");
    }
    public void testCze() throws Exception{
        this.checkLangpack("cze.xml");
    }
    public void testDan() throws Exception{
        this.checkLangpack("dan.xml");
    }
    public void testDeu() throws Exception{
        this.checkLangpack("deu.xml");
    }
    public void testEll() throws Exception{
        this.checkLangpack("ell.xml");
    }
    public void testEng() throws Exception{
        this.checkLangpack("eng.xml");
    }
    public void testFin() throws Exception{
        this.checkLangpack("fin.xml");
    }
    public void testFra() throws Exception{
        this.checkLangpack("fra.xml");
    }
    public void testHun() throws Exception{
        this.checkLangpack("hun.xml");
    }
    public void testIta() throws Exception{
        this.checkLangpack("ita.xml");
    }
    public void testJpn() throws Exception{
        this.checkLangpack("jpn.xml");
    }
    public void testKor() throws Exception{
        this.checkLangpack("kor.xml");
    }
    public void testMys() throws Exception{
        this.checkLangpack("mys.xml");
    }
    public void testNed() throws Exception{
        this.checkLangpack("ned.xml");
    }
    public void testNor() throws Exception{
        this.checkLangpack("nor.xml");
    }
    public void testPol() throws Exception{
        this.checkLangpack("pol.xml");
    }
    public void testPor() throws Exception{
        this.checkLangpack("por.xml");
    }
    public void testRom() throws Exception{
        this.checkLangpack("rom.xml");
    }
    public void testRus() throws Exception{
        this.checkLangpack("rus.xml");
    }
    public void testScg() throws Exception{
        this.checkLangpack("scg.xml");
    }
    public void testSpa() throws Exception{
        this.checkLangpack("spa.xml");
    }
    public void testSvk() throws Exception{
        this.checkLangpack("svk.xml");
    }
    public void testSwe() throws Exception{
        this.checkLangpack("swe.xml");
    }
    public void testTur() throws Exception{
        this.checkLangpack("tur.xml");
    }
    public void testUkr() throws Exception{
        this.checkLangpack("ukr.xml");
    }
}
