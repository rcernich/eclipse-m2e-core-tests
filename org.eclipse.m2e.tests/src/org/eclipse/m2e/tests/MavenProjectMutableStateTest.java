/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Sonatype, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.m2e.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;

import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;


public class MavenProjectMutableStateTest extends AbstractMavenProjectTestCase {

  public void testImport() throws Exception {
    IProject project = importProject("projects/projectmodelchanges/projectimport/pom.xml");
    waitForJobsToComplete();
    assertNoErrors(project);

    IJavaProject javaProject = JavaCore.create(project);
    IClasspathEntry[] cp = javaProject.getRawClasspath();

    assertEquals(6, cp.length);

    assertEquals(project.getFolder("src/main/java").getFullPath(), cp[0].getPath());
    assertEquals(project.getFolder("src/main/avaj").getFullPath(), cp[1].getPath());
    assertEquals(project.getFolder("src/main/secruoser").getFullPath(), cp[2].getPath());
    assertEquals(project.getFolder("src/test/java").getFullPath(), cp[3].getPath());
    assertEquals("org.eclipse.jdt.launching.JRE_CONTAINER", cp[4].getPath().segment(0));
    assertEquals("org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER", cp[5].getPath().toOSString());
  }

  public void testMavenBuilder() throws Exception {
    IProject project = importProject("projects/projectmodelchanges/workspacebuild/pom.xml");
    waitForJobsToComplete();
    assertNoErrors(project);

    // assert MavenProject changes are propagated through mojo executions
    project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
    assertNoErrors(project);

    // assert MavenProject changes are reverted after the build
    assertMutableState(project);
  }

  private void assertMutableState(IProject project) throws CoreException {
    MavenProject mavenProject = MavenPlugin.getMavenProjectRegistry().create(project, monitor).getMavenProject(monitor);
    assertEquals(Arrays.asList(location(project, "src/main/java")), mavenProject.getCompileSourceRoots());
    assertEquals(Arrays.asList(location(project, "src/test/java")), mavenProject.getTestCompileSourceRoots());
    assertResourceDirectorues(Arrays.asList(location(project, "src/main/resources")), mavenProject.getResources());
    assertResourceDirectorues(Arrays.asList(location(project, "src/test/resources")), mavenProject.getTestResources());
  }

  public void testExecuteMojo() throws Exception {
    IProject project = importProject("projects/projectmodelchanges/workspacebuild/pom.xml");
    waitForJobsToComplete();
    assertNoErrors(project);

    IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().create(project, monitor);
    MavenProject mavenProject = facade.getMavenProject(monitor);

    IMaven maven = MavenPlugin.getMaven();

    MavenSession session = maven.createSession(maven.createExecutionRequest(monitor), mavenProject);
    MojoExecution execution = facade.getMojoExecutions("org.eclipse.m2e.test.lifecyclemapping",
        "test-buildhelper-plugin", monitor, "publish").get(0);
    maven.execute(session, execution, monitor);

    assertMutableState(project);

  }

  private String location(IProject project, String relpath) {
    return project.getLocation().append(relpath).toOSString();
  }

  private void assertResourceDirectorues(List<String> expected, List<Resource> actual) {
    List<String> directories = new ArrayList<String>();
    for(Resource resource : actual) {
      directories.add(resource.getDirectory());
    }
    assertEquals(expected, directories);
  }

}
