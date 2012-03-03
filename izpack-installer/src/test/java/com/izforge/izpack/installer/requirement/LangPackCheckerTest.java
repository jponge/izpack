package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.data.ResourceManager;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link LangPackChecker}.
 *
 * @author Tim Anderson
 */
public class LangPackCheckerTest
{

    /**
     * Tests the {@link LangPackChecker}.
     */
    @Test
    public void testLangPackChecker()
    {
        final List<String> langPacks = new ArrayList<String>();
        ResourceManager resources = new ResourceManager(new Properties())
        {
            @Override
            public List<String> getAvailableLangPacks()
            {
                return langPacks;
            }
        };

        LangPackChecker checker = new LangPackChecker(resources);

        // no lang packs - should evaluate false
        assertFalse(checker.check());

        // add a lang pack - should evaluate true
        langPacks.add("eng");
        assertTrue(checker.check());
    }
}
