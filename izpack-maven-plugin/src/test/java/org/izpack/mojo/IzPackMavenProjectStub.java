package org.izpack.mojo;

import org.apache.maven.project.MavenProject;

import java.util.Collections;
import java.util.List;

/**
 * Stub for maven project
 *
 * @author Anthonin Bonnefoy
 */
public class IzPackMavenProjectStub extends MavenProject {

    @Override
    public List getAttachedArtifacts() {
        return Collections.emptyList();
    }
}
