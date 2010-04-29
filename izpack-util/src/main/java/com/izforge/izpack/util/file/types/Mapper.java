/*
 * Copyright  2000-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.izforge.izpack.util.file.types;

import com.izforge.izpack.util.file.CompositeMapper;
import com.izforge.izpack.util.file.ContainerMapper;
import com.izforge.izpack.util.file.FileNameMapper;

import java.util.Properties;

/**
 * Element to define a FileNameMapper.
 */
public class Mapper extends DataType implements Cloneable
{

    protected MapperType type = null;
    protected String classname = null;
    protected Path classpath = null;
    protected String from = null;
    protected String to = null;

    private ContainerMapper container = null;

    /**
     * Set the type of <code>FileNameMapper</code> to use.
     *
     * @param type the <CODE>MapperType</CODE> enumerated attribute.
     */
    public void setType(MapperType type)
    {
        this.type = type;
    }

    /**
     * Add a nested <CODE>FileNameMapper</CODE>.
     *
     * @param fileNameMapper the <CODE>FileNameMapper</CODE> to add.
     */
    public void add(FileNameMapper fileNameMapper) throws Exception
    {
        if (container == null)
        {
            if (type == null && classname == null)
            {
                container = new CompositeMapper();
            }
            else
            {
                FileNameMapper m = getImplementation();
                if (m instanceof ContainerMapper)
                {
                    container = (ContainerMapper) m;
                }
                else
                {
                    throw new Exception(String.valueOf(m)
                            + " mapper implementation does not support nested mappers!");
                }
            }
        }
        container.add(fileNameMapper);
    }

    /**
     * Add a Mapper
     *
     * @param mapper the mapper to add
     */
    public void addConfiguredMapper(Mapper mapper) throws Exception
    {
        add(mapper.getImplementation());
    }

    /**
     * Set the class name of the FileNameMapper to use.
     */
    public void setClassname(String classname)
    {
        this.classname = classname;
    }

    /**
     * Set the argument to FileNameMapper.setFrom
     */
    public void setFrom(String from)
    {
        this.from = from;
    }

    /**
     * Set the argument to FileNameMapper.setTo
     */
    public void setTo(String to)
    {
        this.to = to;
    }

    /**
     * Returns a fully configured FileNameMapper implementation.
     */
    public FileNameMapper getImplementation() throws Exception
    {
        if (type == null && classname == null && container == null)
        {
            throw new Exception(
                    "nested mapper or "
                            + "one of the attributes type or classname is required");
        }

        if (container != null)
        {
            return container;
        }

        if (type != null && classname != null)
        {
            throw new Exception(
                    "must not specify both type and classname attribute");
        }

        try
        {
            FileNameMapper m
                    = (FileNameMapper) (getImplementationClass().newInstance());
            m.setFrom(from);
            m.setTo(to);

            return m;
        }
        catch (Exception be)
        {
            throw be;
        }
        catch (Throwable t)
        {
            throw new Exception(t);
        }
    }

    /**
     * Gets the Class object associated with the mapper implementation.
     *
     * @return <CODE>Class</CODE>.
     */
    protected Class getImplementationClass() throws ClassNotFoundException
    {

        String classname = this.classname;
        if (type != null)
        {
            classname = type.getImplementation();
        }

        ClassLoader loader = getClass().getClassLoader();

        return Class.forName(classname, true, loader);
    }

    /**
     * Class as Argument to FileNameMapper.setType.
     */
    public static class MapperType extends EnumeratedAttribute
    {
        private Properties implementations;

        public MapperType()
        {
            implementations = new Properties();
            implementations.put("identity",
                    "com.izforge.izpack.util.file.IdentityMapper");
            implementations.put("flatten",
                    "com.izforge.izpack.util.file.FlatFileNameMapper");
            implementations.put("glob",
                    "com.izforge.izpack.util.file.GlobPatternMapper");
            implementations.put("merge",
                    "com.izforge.izpack.util.file.MergingMapper");
            implementations.put("regexp",
                    "com.izforge.izpack.util.file.RegexpPatternMapper");
            implementations.put("package",
                    "com.izforge.izpack.util.file.PackageNameMapper");
            implementations.put("unpackage",
                    "com.izforge.izpack.util.file.UnPackageNameMapper");
        }

        public String[] getValues()
        {
            return new String[]{"identity", "flatten", "glob",
                    "merge", "regexp", "package", "unpackage"};
        }

        public String getImplementation()
        {
            return implementations.getProperty(getValue());
        }
    }

}
