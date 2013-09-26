/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package org.terracotta.maven.plugins.tc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.plugin.testing.stubs.StubArtifactRepository;
import org.apache.maven.project.MavenProject;

import com.tc.bundles.MavenToOSGi;

/**
 * @author Eugene Kuleshov
 */
public class ManifestMojoTest extends AbstractMojoTestCase {

  public void testCustomManifestCreation() throws Exception {
    ManifestMojo manifestMojo = new ManifestMojo();
    
    setVariableValueToObject(manifestMojo, "bundleSymbolicName", "some.bundle.name");
    setVariableValueToObject(manifestMojo, "bundleVersion", "1.0.0");
    setVariableValueToObject(manifestMojo, "terracottaRequireVersion", "2.6.4");
    setVariableValueToObject(manifestMojo, "bundleName", "Some bundle name");
    setVariableValueToObject(manifestMojo, "bundleCategory", "Some bundle category");
    setVariableValueToObject(manifestMojo, "bundleDescription", "Some bundle description");
    setVariableValueToObject(manifestMojo, "bundleCopyright", "Some bundle copyright");
    setVariableValueToObject(manifestMojo, "bundleVendor", "Some bundle vendor");
    setVariableValueToObject(manifestMojo, "bundleActivator", "some.bundle.Activator");
    setVariableValueToObject(manifestMojo, "bundleRequiredExecutionEnvironment", "J2SE-1.4");
    setVariableValueToObject(manifestMojo, "exportPackage", "some.bundle.foo");
    setVariableValueToObject(manifestMojo, "importPackage", "some.other.bundle.bar");
    setVariableValueToObject(manifestMojo, "requireBundle", "some.other.bundle");
    
    Manifest manifest = manifestMojo.createManifest();

    assertManifest(manifest, "Bundle-SymbolicName", "some.bundle.name");
    assertManifest(manifest, "Bundle-Version", "1.0.0");
    assertManifest(manifest, "Terracotta-RequireVersion", "2.6.4");
    assertManifest(manifest, "Bundle-Name", "Some bundle name");
    assertManifest(manifest, "Bundle-Category", "Some bundle category");
    assertManifest(manifest, "Bundle-Description", "Some bundle description");
    assertManifest(manifest, "Bundle-Copyright", "Some bundle copyright");
    assertManifest(manifest, "Bundle-Vendor", "Some bundle vendor");
    assertManifest(manifest, "Bundle-Activator", "some.bundle.Activator");
    assertManifest(manifest, "Bundle-RequiredExecutionEnvironment", "J2SE-1.4");
    assertManifest(manifest, "Export-Package", "some.bundle.foo");
    assertManifest(manifest, "Import-Package", "some.other.bundle.bar");
    assertManifest(manifest, "Require-Bundle", "some.other.bundle");
  }
  
  public void testSymbolicNameConversion() throws Exception {
    ManifestMojo manifestMojo = new ManifestMojo();
    
    setVariableValueToObject(manifestMojo, "requireBundle", "NONE");
    setVariableValueToObject(manifestMojo, "bundleVersion", "1.0.0");
    
    MavenProjectStub project = new MavenProjectStub();
    project.setGroupId("some.bundle");
    project.setArtifactId("name");
    setVariableValueToObject(manifestMojo, "project", project);
    
    Manifest manifest = manifestMojo.createManifest();
    assertManifest(manifest, "Bundle-SymbolicName", "some.bundle.name");
    assertManifest(manifest, "Require-Bundle", null);
    
    project.setGroupId("some.bundle");
    project.setArtifactId("another&name");
    
    manifest = manifestMojo.createManifest();
    assertManifest(manifest, "Bundle-SymbolicName", "some.bundle.another_name");
  }
  
  public void testVersionConversion() throws Exception {
    ManifestMojo manifestMojo = new ManifestMojo();
    
    setVariableValueToObject(manifestMojo, "requireBundle", "NONE");
    setVariableValueToObject(manifestMojo, "bundleSymbolicName", "some.bundle.name");

    MavenProjectStub project = new MavenProjectStub();
    setVariableValueToObject(manifestMojo, "project", project);
    
    project.setVersion("1.0.0");
    Manifest manifest = manifestMojo.createManifest();
    assertManifest(manifest, "Bundle-Version", "1.0.0");
    
    project.setVersion("2.0");
    manifest = manifestMojo.createManifest();
    assertManifest(manifest, "Bundle-Version", "2.0.0");
    
    project.setVersion("2.1-SNAPSHOT");
    manifest = manifestMojo.createManifest();
    assertManifest(manifest, "Bundle-Version", "2.1.0.SNAPSHOT");
  }
  
  public void testGenerateNoRequiredBundles() throws Exception {
    ManifestMojo manifestMojo = new ManifestMojo();
    
    setVariableValueToObject(manifestMojo, "bundleSymbolicName", "some.bundle.name");
    setVariableValueToObject(manifestMojo, "bundleVersion", "1.0.0");
    
    setVariableValueToObject(manifestMojo, "requireBundle", "NONE");
    Manifest manifest = manifestMojo.createManifest();
    assertManifest(manifest, "Require-Bundle", null);
  }
  
  public void testGenerateRequiredBundles() throws Exception {
    ManifestMojo manifestMojo = new ManifestMojo();
    
    setVariableValueToObject(manifestMojo, "bundleSymbolicName", "some.bundle.name");
    setVariableValueToObject(manifestMojo, "bundleVersion", "1.0.0");
    
    DsoArtifactResolver resolver = new DsoArtifactResolverStub();
    setVariableValueToObject(manifestMojo, "resolver", resolver);
    
    MavenProject project = new MavenProject();
    setVariableValueToObject(manifestMojo, "project", project);

    List dependencies = new ArrayList();
    project.setDependencies(dependencies);
    
    dependencies.add(dependency("some.group", "dependency2", "2.0.0-SNAPSHOT", null));
    dependencies.add(dependency("some.group", "dependency3", "3.0.0-SNAPSHOT", "test"));
    dependencies.add(dependency("some.group", "dependency4", "4.0.0-SNAPSHOT", "provided"));
    Manifest manifest = manifestMojo.createManifest();
    assertManifest(manifest, "Require-Bundle", "some.group.dependency2;bundle-version:=2.0.0.SNAPSHOT");
    
    dependencies.clear();
    dependencies.add(dependency("some.group", "dependency2", "2.0.0-SNAPSHOT", null));
    dependencies.add(dependency("some.group", "dependency3", "3.0.0-SNAPSHOT", null));
    manifest = manifestMojo.createManifest();
    assertManifest(manifest, "Require-Bundle",
        "some.group.dependency2;bundle-version:=2.0.0.SNAPSHOT,some.group.dependency3;bundle-version:=3.0.0.SNAPSHOT");
  }

  private Dependency dependency(String groupId, String artifactId, String version, String scope) {
    Dependency dependency = new Dependency();
    dependency.setGroupId(groupId);
    dependency.setArtifactId(artifactId);
    dependency.setVersion(version);
    dependency.setScope(scope);
    return dependency;
  }

  private void assertManifest(Manifest manifest, String attr, String expectedValue) {
    assertEquals(expectedValue, manifest.getMainAttributes().getValue(attr));
  }

  private ManifestMojo createManifestMojo() throws Exception {
    // Xpp3Dom xpp3dom = Xpp3DomBuilder.build(new StringReader(config));
    // PlexusConfiguration pluginConfiguration = extractPluginConfiguration("tc-maven-plugin", xpp3dom);

    ManifestMojo manifestMojo = new ManifestMojo();
    // BootjarMojo bootjarMojo = (BootjarMojo) lookupMojo("bootjar", "src/test/java/org/terracotta/maven/plugins/tc/bootjarMojo.xml");

    setVariableValueToObject(manifestMojo, "localRepository", new StubArtifactRepository("repo"));

//    ArrayList pluginArtifacts = new ArrayList();
//    pluginArtifacts.add(new DefaultArtifact("org.terracotta", "terracotta", //
//        VersionRange.createFromVersion("2.6.4"), null, "jar", null, new DefaultArtifactHandlerStub("jar")));
//    setVariableValueToObject(manifestMojo, "pluginArtifacts", pluginArtifacts);

/*

      attributes.putValue("Bundle-Category", bundleCategory);
      attributes.putValue("Bundle-Description", bundleDescription);
      attributes.putValue("Bundle-Copyright", bundleCopyright);
      attributes.putValue("Bundle-Vendor", bundleVendor);
      attributes.putValue("Bundle-Name", bundleName);
      attributes.putValue("Bundle-RequiredExecutionEnvironment", bundleRequiredExecutionEnvironment);
      attributes.putValue("Import-Package", trimWhitespace(importPackage));
      attributes.putValue("Export-Package", trimWhitespace(exportPackage));
      attributes.putValue("Bundle-Activator", bundleActivator);


    if (requireBundle != null) {
      attributes.putValue("Require-Bundle", trimWhitespace(requireBundle));
    } else {
      String bundles = generateRequiredBundles();
      getLog().debug("  Require-Bundle: " + bundles);
      if(bundles.length()>0) {
        attributes.putValue("Require-Bundle", bundles);
      }
    }


    if (bundleSymbolicName != null) {
      attributes.putValue("Bundle-SymbolicName", trimWhitespace(bundleSymbolicName));
    } else {
      String groupId = project.getGroupId();
      String artifactId = project.getArtifactId();
      String symbolicName = MavenToOSGi.artifactIdToSymbolicName(groupId, artifactId);
      attributes.putValue("Bundle-SymbolicName", trimWhitespace(symbolicName));
    }

    if (bundleVersion != null) {
      attributes.putValue("Bundle-Version", bundleVersion);
    } else {
      DefaultArtifactVersion v = new DefaultArtifactVersion(project.getVersion());
      String osgiVersion = MavenToOSGi.projectVersionToBundleVersion(v.getMajorVersion(), v.getMinorVersion(), v.getIncrementalVersion(), v.getQualifier());      
      attributes.putValue("Bundle-Version", osgiVersion);
    }

 */
  
    
    return manifestMojo;
  }

  
  public class DsoArtifactResolverStub implements DsoArtifactResolver {

    public File resolveArtifact(String groupId, String artifactId, String version, ArtifactRepository localRepository,
        List remoteRepositories) {
      
      String id = groupId + "." + artifactId + "-" + version;

      try {
        File tempFile = File.createTempFile("temp-" + id, "jar");
        tempFile.deleteOnExit();
  
        Manifest manifest = new Manifest();

        DefaultArtifactVersion v = new DefaultArtifactVersion(version);
        
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.putValue("Bundle-ManifestVersion", "2");
        attributes.putValue("Bundle-SymbolicName", MavenToOSGi.artifactIdToSymbolicName(groupId, artifactId));
        attributes.putValue("Bundle-Version", MavenToOSGi.projectVersionToBundleVersion(v.getMajorVersion(), v.getMinorVersion(), v.getIncrementalVersion(), v.getQualifier()));
        
        JarOutputStream os = new JarOutputStream(new FileOutputStream(tempFile), manifest);
        os.flush();
        os.close();
        
        return tempFile;

      } catch (IOException ex) {
        throw new RuntimeException("Can't create artifact bundle for " + id);
      
      }
    }

    public ArtifactResolutionResult resolveArtifact(Artifact filteredArtifact, Artifact providerArtifact,
        ArtifactRepository localRepository, List remoteRepositories) throws ArtifactResolutionException,
        ArtifactNotFoundException {

      return null;
    }

  }

  
}
