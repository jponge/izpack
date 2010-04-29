package com.izforge.izpack.core.variable;

import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;

import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;


public class JarEntryConfigValue extends ZipEntryConfigFileValue
{

    public JarEntryConfigValue(String filename, String entryname, int type, String section, String key)
    {
        super(filename, entryname, type, section, key);
    }

    @Override
    public String resolve() throws Exception
    {
        return super.resolve(getJarEntryInputStream(getFilename(), getEntryname()));
    }

    @Override
    public String resolve(VariableSubstitutor... substitutors)
            throws Exception
    {
        String _filename_ = getFilename(), _entryname_ = getEntryname();
        for (VariableSubstitutor substitutor : substitutors)
        {
            _filename_ = substitutor.substitute(_filename_, (SubstitutionType) null);
        }
        for (VariableSubstitutor substitutor : substitutors)
        {
            _entryname_ = substitutor.substitute(_entryname_, (SubstitutionType) null);
        }
        return super.resolve(getJarEntryInputStream(_filename_, _entryname_), substitutors);
    }

    private InputStream getJarEntryInputStream(String filename, String entryname) throws Exception
    {
        JarFile jarfile;
        try
        {
            jarfile = new JarFile(filename);
            JarEntry entry = jarfile.getJarEntry(entryname);
            if (entry == null)
            {
                throw new Exception("Jar file entry " + entryname + " not found in " + jarfile.getName());
            }
            return jarfile.getInputStream(entry);
        }
        catch (ZipException ze)
        {
            throw new Exception("Error opening jar file " + filename, ze);
        }
    }

}
