package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.installer.RequirementChecker;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link RequirementsChecker} class.
 *
 * @author Tim Anderson
 */
public class RequirementsCheckerTest
{
    /**
     * Tests the {@link RequirementsChecker#check()} method when all {@link RequirementChecker} implementations
     * return <tt>true</tt>.
     */
    @Test
    public void testCheckSuccess()
    {
        RequirementChecker checker = new RequirementsChecker(mock(LangPackChecker.class, true),
                mock(JavaVersionChecker.class, true), mock(JDKChecker.class, true),
                mock(LockFileChecker.class, true),
                mock(InstallerRequirementChecker.class, true));
        assertTrue(checker.check());
    }


    /**
     * Tests the {@link RequirementsChecker#check()} method when one of the {@link RequirementChecker} implementations
     * returns <tt>false</tt>.
     */
    @Test
    public void testCheckFailure()
    {
        for (int i = 0; i < 5; ++i)
        {
            LangPackChecker langChecker = mock(LangPackChecker.class, (i != 0));
            JavaVersionChecker javaChecker = mock(JavaVersionChecker.class, (i != 1));
            JDKChecker jdkChecker = mock(JDKChecker.class, (i != 2));
            LockFileChecker lockChecker = mock(LockFileChecker.class, (i != 3));
            InstallerRequirementChecker requirementChecker = mock(InstallerRequirementChecker.class, (i != 4));

            RequirementsChecker checker2 = new RequirementsChecker(langChecker, javaChecker, jdkChecker,
                    lockChecker, requirementChecker);
            assertFalse(checker2.check());
        }
    }

    /**
     * Mocks a RequirementChecker that returns the specified value.
     *
     * @param clazz  the class to mock
     * @param result the value to return
     * @return the mocked instance
     */
    private <T extends RequirementChecker> T mock(Class<T> clazz, boolean result)
    {
        return Mockito.mock(clazz, new Returns(result));
    }
}
