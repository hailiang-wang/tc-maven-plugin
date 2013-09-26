/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/**
 * 
 */
package org.terracotta.maven.plugins.tc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.tc.bundles.MavenToOSGi;

/**
 * Generate Manifest for Terracotta Integration Module (TIM)
 * 
 * @goal manifest
 * @author Eugene Kuleshov
 * @execute phase="generate-resources"
 * @phase generate-resources
 * @requiresDependencyResolution compile
 */
public class ManifestMojo extends AbstractMojo {

  /**
   * A special value used to turn of automatic generation of the required bundles
   */
  private static final String NONE = "NONE";
  
  /**
   * Location for the generated manifest file
   * 
   * @parameter expression="${manifest.file}" default-value="${project.build.directory}/MANIFEST.MF"
   */
  private File manifestFile;

  /**
   * Bundle-Category attribute
   * 
   * @parameter expression="${bundleCategory}"
   */
  private String bundleCategory;

  /**
   * Bundle-Copyright attribute
   * 
   * @parameter expression="${bundleCopyright}"
   */
  private String bundleCopyright;

  /**
   * Bundle-Description attribute
   * 
   * @parameter expression="${bundleDescription}" default-value="${project.description}"
   */
  private String bundleDescription;

  /**
   * Bundle-Name attribute
   * 
   * @parameter expression="${bundleName}" default-value="${project.name}"
   */
  private String bundleName;

  /**
   * Bundle-Vendor attribute
   * 
   * @parameter expression="${bundleVendor}" default-value="${project.organization.name}"
   */
  private String bundleVendor;

  /**
   * Bundle-Activator attribute
   * 
   * @parameter expression="${bundleActivator}"
   */
  private String bundleActivator;

  /**
   * Require-Bundle specification. For example,
   * org.terracotta.modules.modules-common;bundle-version:="1.0.0.SNAPSHOT",
   * org.terracotta.modules.clustered-cglib-2.1.3;bundle-version:="1.0.0.SNAPSHOT"
   * <br/><br/>
   * If not specified, value will be generated automatically based on the 
   * dependencies listed in the 
   * <a href="http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">dependencies</a>
   * section of the project pom.xml. 
   * Dependencies with scope "test" and "runtime" won't be included into the list.
   * <br/><br/>
   * A special value "NONE" can be used to turn off automatic Require-Bundle generation when it should be empty,
   * regardless of the actual dependencies.
   * 
   * @parameter expression="${requireBundle}"
   */
  private String requireBundle;

  /**
   * Import-Package attribute
   * 
   * @parameter expression="${importPackage}"
   */
  private String importPackage;

  /**
   * Export-Package attribute
   * 
   * @parameter expression="${exportPackage}"
   */
  private String exportPackage;

  /**
   * Bundle-SymbolicName attribute
   * 
   * @parameter expression="${bundleSymbolicName}"
   */
  private String bundleSymbolicName;

  /**
   * Bundle-Version attribute
   * 
   * @parameter expression="${bundleVersion}"
   */
  private String bundleVersion;

  // Bundle-ManifestVersion: 2

  /**
   * Bundle-RequiredExecutionEnvironment attribute
   *
   * @parameter expression="${bundleRequiredExecutionEnvironment}"
   */
  private String bundleRequiredExecutionEnvironment;

  /**
   * Terracotta-RequireVersion attribute
   *
   * @parameter expression="${terracottaRequireVersion}"
   */
  private String terracottaRequireVersion;
   
  /**
   * Terracotta-ProjectStatus attribute
   *
   * @parameter expression="${terracottaProjectStatus}"
   */
  private String terracottaProjectStatus;
  
  /**
   * Maven project
   * 
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;
  
  /**
   * DSO artifact resolver
   * 
   * @component role-hint="resolver"
   */
  protected DsoArtifactResolver resolver;

  /**
   * ArtifactRepository of the localRepository. To obtain the directory of localRepository in unit tests use
   * System.setProperty("localRepository").
   * 
   * @parameter expression="${localRepository}"
   * @required
   * @readonly
   */
  protected ArtifactRepository localRepository;

  /**
   * Remote repositories declared in the project pom
   * 
   * @parameter expression="${project.pluginArtifactRepositories}"
   */
  protected List remoteRepositories;

  
  public void execute() throws MojoExecutionException, MojoFailureException {
    Manifest manifest = createManifest();
    
    Attributes attributes = manifest.getMainAttributes(); 

    for (Iterator it = attributes.keySet().iterator(); it.hasNext();) {
      Attributes.Name key = (Attributes.Name) it.next();
      getLog().info("  " + key + ": " + attributes.getValue(key));
    }
    
    FileOutputStream fos = null;
    try {
      manifestFile.getAbsoluteFile().getParentFile().mkdirs();
      
      fos = new FileOutputStream(manifestFile);
      manifest.write(fos);
      fos.flush();
    } catch (IOException ex) {
      getLog().error("Unable to write manifest file " + manifestFile.getAbsolutePath(), ex);
      throw new MojoFailureException(ex.getMessage());
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException ex) {
          getLog().error("Unable to close stream", ex);
        }
      }
    }
  }

  private static String sanitize(String s) {
     return s.replaceAll("[\\n\\t\\r\\f]", " ").trim();
  }

  protected Manifest createManifest() throws MojoExecutionException {
    Manifest manifest = new Manifest();
    
    Attributes attributes = manifest.getMainAttributes();

    attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    
    attributes.putValue("Bundle-ManifestVersion", "2");
    if (bundleCategory != null) {
      attributes.putValue("Bundle-Category", bundleCategory);
    }
    if (bundleDescription != null) {
      attributes.putValue("Bundle-Description", sanitize(bundleDescription));
    }
    if (bundleCopyright != null) {
      attributes.putValue("Bundle-Copyright", bundleCopyright);
    }
    if (bundleVendor != null) {
      attributes.putValue("Bundle-Vendor", bundleVendor);
    }
    if (bundleName != null) {
      attributes.putValue("Bundle-Name", bundleName);
    }

    if (bundleRequiredExecutionEnvironment != null) {
      attributes.putValue("Bundle-RequiredExecutionEnvironment", bundleRequiredExecutionEnvironment);
    }

    if (requireBundle != null) {
      if(!NONE.equals(requireBundle)) {
        attributes.putValue("Require-Bundle", trimWhitespace(requireBundle));
      }
    } else {
      String bundles = generateRequiredBundles();
      if(bundles.length()>0) {
        attributes.putValue("Require-Bundle", bundles);
      }
    }

    if (importPackage != null) {
      attributes.putValue("Import-Package", trimWhitespace(importPackage));
    }

    if (exportPackage != null) {
      attributes.putValue("Export-Package", trimWhitespace(exportPackage));
    }

    if(bundleActivator!=null) {
      attributes.putValue("Bundle-Activator", bundleActivator);
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
    
    if (terracottaRequireVersion != null) {
       attributes.putValue("Terracotta-RequireVersion", terracottaRequireVersion);
    }

    if (terracottaProjectStatus != null) {
       attributes.putValue("Terracotta-ProjectStatus", terracottaProjectStatus);
    }
    
    return manifest;
  }

  private String generateRequiredBundles() throws MojoExecutionException {
    getLog().debug("Generating required bundles");

    try {
      StringBuffer sb = new StringBuffer();
      
      String sep = "";
      for (Iterator it = project.getDependencies().iterator(); it.hasNext();) {
        Dependency d = (Dependency) it.next();
        getLog().debug("  Dependency: " + d);
  
        String scope = d.getScope();
        if("test".equals(scope) || "provided".equals(scope)) {
          continue;
        }
        
        File file = resolver.resolveArtifact(d.getGroupId(), d.getArtifactId(), d.getVersion(), localRepository, remoteRepositories);
        if(file!=null) {
          JarFile bundle = new JarFile(file);
          Manifest manifest = bundle.getManifest();
          
          Attributes attributes = manifest.getMainAttributes();
          String symbolicName = attributes.getValue("Bundle-SymbolicName");
          String version = attributes.getValue("Bundle-Version");
          
          getLog().debug("    symbolicName: " + symbolicName);
          getLog().debug("    version: " + version);
          
          if(version!=null && symbolicName!=null) {
            // org.terracotta.modules.clustered-commons-collections-3.1;bundle-version:=2.6.4
            sb.append(sep).append(symbolicName).append(";bundle-version:=").append(version);
            sep = ",";
          }
        }
      }
      
      return sb.toString();
      
    } catch (IOException ex) {
      throw new MojoExecutionException("Unable to calculate the 'Require-Bundle' attribute", ex);
    }
  }

  private String trimWhitespace(String s) {
    return s.replaceAll("\\s", "");
  }
  
}
