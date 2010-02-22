package com.izforge.izpack.util.substitutor;

import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests of substitutor features
 *
 * @author Anthonin Bonnefoy
 */
public class VariableSubstitutorImplTest
{

    private VariableSubstitutor variableSubstitutor;

    @Before
    public void setupVariableSubstitutor()
    {
        Properties properties = new Properties(System.getProperties());
        properties.put("MY_PROP", "one");
        properties.put("MY_PROP2", "two");
        variableSubstitutor = new VariableSubstitutorImpl(properties);
    }

    @Test
    public void shouldNotSubstitute() throws Exception
    {
        String res = variableSubstitutor.substitute("string not substitute", SubstitutionType.TYPE_PLAIN);
        assertThat(res, Is.is("string not substitute"));
        res = variableSubstitutor.substitute("string not ${substitute}", SubstitutionType.TYPE_PLAIN);
        assertThat(res, Is.is("string not ${substitute}"));
    }

    @Test
    public void shouldSubstitutePlainText() throws Exception
    {
        assertThat(
                variableSubstitutor.substitute("${MY_PROP}${MY_PROP2}", SubstitutionType.TYPE_PLAIN),
                Is.is("onetwo"));
        assertThat(
                variableSubstitutor.substitute("$MY_PROP2$MY_PROP", SubstitutionType.TYPE_PLAIN),
                Is.is("twoone"));
    }

    @Test
    public void shouldSubstituteAntType() throws Exception
    {
        assertThat(
                variableSubstitutor.substitute("@MY_PROP@@MY_PROP2@", SubstitutionType.TYPE_ANT),
                Is.is("onetwo"));
    }

    @Test
    public void shouldSubstituteShellType() throws Exception
    {
        assertThat(
                variableSubstitutor.substitute("%MY_PROP%MY_PROP2", SubstitutionType.TYPE_SHELL),
                Is.is("onetwo"));
    }


}
