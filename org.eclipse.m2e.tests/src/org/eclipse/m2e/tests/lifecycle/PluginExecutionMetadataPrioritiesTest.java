
package org.eclipse.m2e.tests.lifecycle;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;

import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.internal.lifecyclemapping.LifecycleMappingFactory;
import org.eclipse.m2e.core.internal.lifecyclemapping.model.LifecycleMappingMetadataSource;
import org.eclipse.m2e.core.internal.markers.SourceLocation;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.ILifecycleMapping;
import org.eclipse.m2e.tests.common.AbstractLifecycleMappingTest;
import org.eclipse.m2e.tests.common.WorkspaceHelpers;
import org.eclipse.m2e.tests.configurators.TestLifecycleMapping;


@SuppressWarnings("restriction")
public class PluginExecutionMetadataPrioritiesTest extends AbstractLifecycleMappingTest {
  public void testDefaultMetadataSource() throws Exception {
    LifecycleMappingMetadataSource defaultMetadata = loadLifecycleMappingMetadataSource("projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionMetadataPrioritiesTest/defaultMetadata.xml");
    LifecycleMappingFactory.setDefaultLifecycleMappingMetadataSource(defaultMetadata);

    IMavenProjectFacade facade = importMavenProject(
        "projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionMetadataPrioritiesTest",
        "testDefaultMetadataSource/pom.xml");
    assertNotNull("Expected not null MavenProjectFacade", facade);
    IProject project = facade.getProject();
    List<IMarker> errorMarkers = WorkspaceHelpers.findErrorMarkers(project);
    assertNotNull(errorMarkers);
    assertEquals(WorkspaceHelpers.toString(errorMarkers), 2, errorMarkers.size());

    String expectedErrorMessage = "Plugin execution not covered by lifecycle configuration: org.eclipse.m2e.test.lifecyclemapping:test-lifecyclemapping-plugin:1.0.0:test-goal-1 (execution: default-test-goal-1, phase: process-resources)";
    WorkspaceHelpers.assertErrorMarker(IMavenConstants.MARKER_LIFECYCLEMAPPING_ID, expectedErrorMessage,
        11 /*lineNumber*/, project);

    expectedErrorMessage = "Project configurator \"missing default project configurator id for test-lifecyclemapping-plugin:test-goal-1\" is not available. To enable full functionality, install the project configurator and run Maven->Update Project Configuration.";
    IMarker marker = WorkspaceHelpers.assertErrorMarker(IMavenConstants.MARKER_LIFECYCLEMAPPING_ID,
        expectedErrorMessage, 11 /*lineNumber*/, project);
    WorkspaceHelpers.assertConfiguratorErrorMarkerAttributes(marker,
        "missing default project configurator id for test-lifecyclemapping-plugin:test-goal-1");
  }

  /**
   * This test verifies that pluginExecution mapping metadata contributed via eclipse extension point takes preference
   * over default metadata.
   */
  public void testEclipseExtension() throws Exception {
    LifecycleMappingMetadataSource defaultMetadata = loadLifecycleMappingMetadataSource("projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionMetadataPrioritiesTest/defaultMetadata.xml");
    LifecycleMappingFactory.setDefaultLifecycleMappingMetadataSource(defaultMetadata);

    IMavenProjectFacade facade = importMavenProject(
        "projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionMetadataPrioritiesTest",
        "testEclipseExtension/pom.xml");
    assertNotNull("Expected not null MavenProjectFacade", facade);
    IProject project = facade.getProject();
    WorkspaceHelpers.assertNoErrors(project);

    ILifecycleMapping lifecycleMapping = projectConfigurationManager.getLifecycleMapping(facade);
    assertNotNull(lifecycleMapping);
    assertTrue(lifecycleMapping.getClass().getCanonicalName(), lifecycleMapping instanceof TestLifecycleMapping);
  }

  // Referenced metadata has priority over eclipse extensions
  public void testReferencedFromPom() throws Exception {
    LifecycleMappingMetadataSource defaultMetadata = loadLifecycleMappingMetadataSource("projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionMetadataPrioritiesTest/defaultMetadata.xml");
    LifecycleMappingFactory.setDefaultLifecycleMappingMetadataSource(defaultMetadata);

    IMavenProjectFacade facade = importMavenProject(
        "projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionMetadataPrioritiesTest",
        "testReferencedFromPom/pom.xml");
    assertNotNull("Expected not null MavenProjectFacade", facade);
    IProject project = facade.getProject();
    List<IMarker> errorMarkers = WorkspaceHelpers.findErrorMarkers(project);
    assertNotNull(errorMarkers);
    assertEquals(WorkspaceHelpers.toString(errorMarkers), 2, errorMarkers.size());

    String expectedErrorMessage = "Plugin execution not covered by lifecycle configuration: org.eclipse.m2e.test.lifecyclemapping:test-lifecyclemapping-plugin:1.0.0:test-goal-for-eclipse-extension2 (execution: default-test-goal-for-eclipse-extension2, phase: compile)";
    WorkspaceHelpers.assertErrorMarker(IMavenConstants.MARKER_LIFECYCLEMAPPING_ID, expectedErrorMessage,
        11 /*lineNumber*/, project);

    expectedErrorMessage = "Project configurator \"no such project configurator id for test-lifecyclemapping-plugin:test-goal-for-eclipse-extension2 - referenced from pom\" is not available. To enable full functionality, install the project configurator and run Maven->Update Project Configuration.";
    IMarker marker = WorkspaceHelpers.assertErrorMarker(IMavenConstants.MARKER_LIFECYCLEMAPPING_ID,
        expectedErrorMessage, 11 /*lineNumber*/, project);
    WorkspaceHelpers
        .assertConfiguratorErrorMarkerAttributes(
            marker,
            "no such project configurator id for test-lifecyclemapping-plugin:test-goal-for-eclipse-extension2 - referenced from pom");
  }

  // Embedded metadata has priority over referenced metadata
  public void testEmbeddedInPom() throws Exception {
    LifecycleMappingMetadataSource defaultMetadata = loadLifecycleMappingMetadataSource("projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionMetadataPrioritiesTest/defaultMetadata.xml");
    LifecycleMappingFactory.setDefaultLifecycleMappingMetadataSource(defaultMetadata);

    IMavenProjectFacade facade = importMavenProject(
        "projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionMetadataPrioritiesTest",
        "testEmbeddedInPom/pom.xml");
    assertNotNull("Expected not null MavenProjectFacade", facade);
    IProject project = facade.getProject();
    List<IMarker> errorMarkers = WorkspaceHelpers.findErrorMarkers(project);
    assertNotNull(errorMarkers);
    assertEquals(WorkspaceHelpers.toString(errorMarkers), 2, errorMarkers.size());

    String expectedErrorMessage = "Plugin execution not covered by lifecycle configuration: org.eclipse.m2e.test.lifecyclemapping:test-lifecyclemapping-plugin:1.0.0:test-goal-for-eclipse-extension2 (execution: default-test-goal-for-eclipse-extension2, phase: compile)";
    WorkspaceHelpers.assertErrorMarker(IMavenConstants.MARKER_LIFECYCLEMAPPING_ID, expectedErrorMessage,
        11 /*lineNumber*/, project);

    expectedErrorMessage = "Project configurator \"no such project configurator id for test-lifecyclemapping-plugin:test-goal-for-eclipse-extension2 - embedded from pom\" is not available. To enable full functionality, install the project configurator and run Maven->Update Project Configuration.";
    IMarker marker = WorkspaceHelpers.assertErrorMarker(IMavenConstants.MARKER_LIFECYCLEMAPPING_ID,
        expectedErrorMessage, 11 /*lineNumber*/, project);
    WorkspaceHelpers
        .assertConfiguratorErrorMarkerAttributes(marker,
            "no such project configurator id for test-lifecyclemapping-plugin:test-goal-for-eclipse-extension2 - embedded from pom");
  }

  public void testParent() throws Exception {
    IMavenProjectFacade parentFacade = importMavenProject(
        "projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionMetadataPrioritiesTest",
        "testParent/pom.xml");
    assertNotNull("Expected not null MavenProjectFacade", parentFacade);
    IProject project = parentFacade.getProject();
    WorkspaceHelpers.assertNoErrors(project);

    IMavenProjectFacade facade = importMavenProject(
        "projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionMetadataPrioritiesTest",
        "testParent/useParent/pom.xml");
    assertNotNull("Expected not null MavenProjectFacade", facade);
    project = facade.getProject();
    List<IMarker> errorMarkers = WorkspaceHelpers.findErrorMarkers(project);
    assertNotNull(errorMarkers);
    assertEquals(WorkspaceHelpers.toString(errorMarkers), 2, errorMarkers.size());
    String expectedErrorMessage = "Plugin execution not covered by lifecycle configuration: org.eclipse.m2e.test.lifecyclemapping:test-lifecyclemapping-plugin:1.0.0:test-goal-1 (execution: default-test-goal-1, phase: process-resources)";
    IMarker marker = WorkspaceHelpers.assertErrorMarker(IMavenConstants.MARKER_LIFECYCLEMAPPING_ID,
        expectedErrorMessage, null /*lineNumber*/, project);
    SourceLocation causeLocation = new SourceLocation(parentFacade.getMavenProject().getFile().getAbsolutePath(),
        WorkspaceHelpers.getModelId(parentFacade.getMavenProject()), 11, 7, 14);
    WorkspaceHelpers.assertMarkerLocation(new SourceLocation(5, 3, 10, causeLocation), marker);
    expectedErrorMessage = "Project configurator \"no such project configurator id for test-lifecyclemapping-plugin:test-goal-1 - parent\" is not available. To enable full functionality, install the project configurator and run Maven->Update Project Configuration.";
    marker = WorkspaceHelpers.assertErrorMarker(IMavenConstants.MARKER_LIFECYCLEMAPPING_ID, expectedErrorMessage,
        null /*lineNumber*/, project);
    WorkspaceHelpers.assertMarkerLocation(new SourceLocation(5, 3, 10, causeLocation), marker);

    facade = importMavenProject(
        "projects/lifecyclemapping/lifecycleMappingMetadata/PluginExecutionMetadataPrioritiesTest",
        "testParent/overrideParent/pom.xml");
    assertNotNull("Expected not null MavenProjectFacade", facade);
    project = facade.getProject();
    errorMarkers = WorkspaceHelpers.findErrorMarkers(project);
    assertNotNull(errorMarkers);
    assertEquals(WorkspaceHelpers.toString(errorMarkers), 2, errorMarkers.size());
    expectedErrorMessage = "Plugin execution not covered by lifecycle configuration: org.eclipse.m2e.test.lifecyclemapping:test-lifecyclemapping-plugin:1.0.0:test-goal-1 (execution: default-test-goal-1, phase: process-resources)";
    WorkspaceHelpers.assertErrorMarker(IMavenConstants.MARKER_LIFECYCLEMAPPING_ID, expectedErrorMessage,
        null /*lineNumber*/, project);
    WorkspaceHelpers.assertMarkerLocation(new SourceLocation(5, 3, 10), marker);
    expectedErrorMessage = "Project configurator \"no such project configurator id for test-lifecyclemapping-plugin:test-goal-1 - override\" is not available. To enable full functionality, install the project configurator and run Maven->Update Project Configuration.";
    WorkspaceHelpers.assertErrorMarker(IMavenConstants.MARKER_LIFECYCLEMAPPING_ID, expectedErrorMessage,
        null /*lineNumber*/, project);
  }
}
