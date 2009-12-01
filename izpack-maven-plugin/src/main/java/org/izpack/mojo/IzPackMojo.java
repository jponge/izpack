package org.izpack.mojo;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenProjectValueSource;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.apache.maven.shared.filtering.PropertiesEscapingBackSlashValueSource;

import com.izforge.izpack.compiler.CompilerConfig;

/**
 * Build an IzPack installer
 * 
 * @goal izpack
 * @phase package
 * @requiresDependencyResolution test
 * @version $Id:  $
 * @author Dan T. Tran
 * @author Miguel Griffa
 */
public class IzPackMojo
    extends AbstractMojo
{

    /**
     * IzPack descriptor file.  This plugin interpolates and saves this file to
     * a new location at ${izpackBasedir} and feed it to IzPack compiler
     * @parameter  default-value="${basedir}/src/izpack/install.xml"
     * @since alpha 1
     */
    private File descriptor;

    /**
     * The descriptor's encoding format. Default is null to use the native system encoding
     * @parameter  
     * @since alpha 3
     */
    private String descriptorEncoding;
    
    /**
     * IzPack base directory.  This is the recommended place for staging
     * area as well.  Do not set it to any of the source tree directory
     * @parameter default-value="${project.build.directory}/izpack"
     * @since alpha 1
     */
    private File izpackBasedir;

    /**
     * IzPack's kind argument.
     * @parameter  default-value="standard"
     * @since alpha 1
     */
    private String kind;

    /**
     * Maven's file extension. 
     * @parameter default-value="jar"
     * @since alpha 1
     */
    private String fileExtension;

    /**
     * Location of external custom panel's jars which must be placed under sub directory bin/panels. ( ie ${customPanelDirectory/bin/panels )
     * @parameter default-value="${project.build.directory}/izpack"
     * @since alpha 1
     */
    private File customPanelDirectory;

    /**
     * Enable the built installer output to be attached to Maven project for install/deploy purposes.
     * @parameter default-value="true"
     * @since alpha 3
     */
    private boolean attach = true;

    /**
     * Internal Maven's project
     * @parameter expression="${project}"
     * @readonly
     * @since alpha 1
     */
    protected MavenProject project;

    /**
     * Maven component to install/deploy the installer(s)
     * 
     * @component
     * @readonly
     */
    private MavenProjectHelper projectHelper;

    /**
     * Internal Dependencies and ${project.build.directory}/classes directory
     * @parameter expression="${project.compileClasspathElements}"
     */
    private List classpathElements;

    /**
     * @component role="org.apache.maven.shared.filtering.MavenResourcesFiltering"
     *            role-hint="default"
     * @readonly
     * @since 1.0-alpha-3
     */
    private MavenResourcesFiltering mavenResourcesFiltering;

    /**
     * @parameter expression="${session}"
     * @readonly
     * @since 1.0-alpha-3
     */
    private MavenSession session;

    //////////////////////////////////////////////////////////////////////////////

    /**
     * Maven's classifier. Default to IzPack's kind when not given.  
     * Must be unique among Maven's executions
     */
    private String classifier;

    /**
     * The installer output file. Default to ${project.build.directory}/${project.build.finalName)-classifier.fileExtension
     * Must be unique among Maven's executions. 
     * @parameter
     */
    private File installerFile;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        init();

        buildInstaller();
    }

    private void init()
        throws MojoFailureException
    {
        classifier = this.kind;

        if ( installerFile == null )
        {
            installerFile = new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + "-"
                + classifier + "." + fileExtension );
        }

        File dir = installerFile.getParentFile();
        if ( !dir.exists() )
        {
            if ( !dir.mkdirs() )
            {
                throw new MojoFailureException( "Could not create directory " + dir );
            }
        }

        if ( this.attach )
        {
            checkForDuplicateAttachArtifact();
        }
    }

    /**
     * Interpolate ${} and @{} expressions
     * @return
     * @throws MojoExecutionException
     */
    private File interpolateDescriptorFile()
        throws MavenFilteringException
    {
        //TODO use the MavenFileFilter instead

        Resource resource = new Resource();
        resource.setFiltering( true );
        resource.setDirectory( descriptor.getAbsoluteFile().getParent() );

        String descriptorFileName = this.descriptor.getName();
        List includes = new ArrayList();
        includes.add( descriptorFileName );
        
        resource.setIncludes( includes );
       

        List resources = new ArrayList();
        resources.add( resource );

        MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution( resources, izpackBasedir,
                                                                                       project, this.descriptorEncoding, null, null,
                                                                                       session );

        mavenResourcesExecution.setUseDefaultFilterWrappers( true );
        
        // support @{} izpack ant format
        mavenResourcesExecution.addFilerWrapperWithEscaping( new PropertiesEscapingBackSlashValueSource( true, project
            .getProperties() ), "@{", "}", null );

        mavenResourcesExecution.addFilerWrapperWithEscaping( new MavenProjectValueSource( project, true ), "@{", "}",
                                                             null );

        mavenResourcesFiltering.filterResources( mavenResourcesExecution );

        return new File( izpackBasedir, descriptorFileName );
    }

    private void buildInstaller()
        throws MojoExecutionException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( getClassLoader( classLoader ) );

        try
        {
            String config = this.interpolateDescriptorFile().getAbsolutePath();
            String basedir = izpackBasedir.getAbsolutePath();

            CompilerConfig c = new CompilerConfig( config, basedir, kind, installerFile.getAbsolutePath() );
            CompilerConfig.setIzpackHome( basedir );

            c.executeCompiler();

            if ( !c.wasSuccessful() )
            {
                throw new MojoExecutionException( "IzPack compilation ERROR" );
            }
        }
        catch ( Exception ce )
        {
            throw new MojoExecutionException( "IzPack compilation ERROR", ce );
        }
        finally
        {
            if ( classLoader != null )
            {
                Thread.currentThread().setContextClassLoader( classLoader );
            }
        }

        if ( this.attach )
        {
            projectHelper.attachArtifact( project, fileExtension, classifier, installerFile );
        }
    }

    private void checkForDuplicateAttachArtifact()
        throws MojoFailureException
    {
        List attachedArtifacts = project.getAttachedArtifacts();

        Iterator iter = attachedArtifacts.iterator();

        while ( iter.hasNext() )
        {
            Artifact artifact = (Artifact) iter.next();
            if ( installerFile.equals( artifact.getFile() ) )
            {
                throw new MojoFailureException( "Duplicate installers found: " + installerFile );
            }
        }
    }

    private ClassLoader getClassLoader( ClassLoader classLoader )
        throws MojoExecutionException
    {
        List classpathURLs = new ArrayList();

        try
        {
            //make user's custom panel jar files available in the classpath
            URL customerPanelUrl = this.customPanelDirectory.toURI().toURL();
            classpathURLs.add( customerPanelUrl );
            getLog().debug( "Added to classpath " + customPanelDirectory );

            for ( int i = 0; i < classpathElements.size(); i++ )
            {
                String element = (String) classpathElements.get( i );
                File f = new File( element );
                URL newURL = f.toURI().toURL();
                classpathURLs.add( newURL );
                getLog().debug( "Added to classpath " + element );
            }
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error parsing classpath: " + e.getMessage() );
        }

        URL[] urls = (URL[]) classpathURLs.toArray( new URL[classpathURLs.size()] );

        return new URLClassLoader( urls, classLoader );
    }

}
