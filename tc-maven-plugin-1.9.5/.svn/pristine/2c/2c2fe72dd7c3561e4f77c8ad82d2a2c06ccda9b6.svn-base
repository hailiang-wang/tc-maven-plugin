/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/**
 *
 */
package org.terracotta.maven.plugins.tc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import com.tc.bundles.MavenToOSGi;

/**
 * Generate Manifest for Terracotta Integration Module (TIM)
 *
 * @author Eugene Kuleshov
 *
 * @goal manifest
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
   * Require-Bundle specification. For example, org.terracotta.modules.foo;bundle-version:="1.0.0.SNAPSHOT",
   * org.terracotta.modules.bar;bundle-version:="1.0.0.SNAPSHOT" <br/>
   * <br/>
   * If not specified, value will be generated automatically based on the dependencies listed in the <a
   * href="http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">dependencies</a>
   * section of the project pom.xml. Dependencies with scope "test" and "runtime" won't be included into the list. <br/>
   * <br/>
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
   * Terracotta-Kit attribute
   *
   * @parameter expression="${terracottaKit}"
   */
  private String terracottaKit;

  /**
   * Terracotta-Tim-Api attribute
   *
   * @parameter expression="${terracottaTimApi}"
   */
  private String terracottaTimApi;

  /**
   * Presentation-Factory attribute
   *
   * @parameter expression="${presentationFactory}"
   */
  private String presentationFactory;

  /**
   * Terracotta-ProjectStatus attribute
   *
   * @parameter expression="${terracottaProjectStatus}"
   */
  private String terracottaProjectStatus;

  /**
   * Terracotta-InternalTIM attribute
   *
   * @parameter expression="false"
   */
  private boolean terracottaInternalTIM;

  /**
   * Terracotta-Configuration attribute
   *
   * @parameter expression="${terracottaConfiguration}"
   */
  private String terracottaConfiguration;

  /**
   * Terracotta-TunneledMBeanDomains attribute
   *
   * @parameter expression="${tunneledMBeanDomains}"
   */
  private String[] tunneledMBeanDomains;

  /**
   * Maven project
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  /**
   * Project root directory
   *
   * @parameter expression="${rootPath}"
   */
  protected String rootPath;

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
  protected List<ArtifactRepository> remoteRepositories;

  /**
   * Property to tell the build to fail if svn command failed to execute for any reason
   *
   * @parameter expression="${buildinfo.failOnError}" default-value=false
   */
  private final boolean failOnError = false;

  private static ThreadLocal<DateFormat> sdf = new ThreadLocal<DateFormat>() {
    @Override
    protected DateFormat initialValue() {
      return new SimpleDateFormat("yyyyMMdd-HHmmss");
    }
  };

  public void execute() throws MojoExecutionException, MojoFailureException {
    Manifest manifest = createManifest();

    Attributes attributes = manifest.getMainAttributes();

    for (Object name : attributes.keySet()) {
      Attributes.Name key = (Attributes.Name) name;
      getLog().debug("  " + key + ": " + attributes.getValue(key));
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
      if (!NONE.equals(requireBundle)) {
        attributes.putValue("Require-Bundle", trimWhitespace(requireBundle));
      }
    } else {
      String bundles = generateRequiredBundles();
      if (bundles.length() > 0) {
        attributes.putValue("Require-Bundle", bundles);
      }
    }

    if (importPackage != null) {
      attributes.putValue("Import-Package", trimWhitespace(importPackage));
    }

    if (exportPackage != null) {
      attributes.putValue("Export-Package", trimWhitespace(exportPackage));
    }

    if (bundleActivator != null) {
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
      String osgiVersion = MavenToOSGi.projectVersionToBundleVersion(v.getMajorVersion(), v.getMinorVersion(),
          v.getIncrementalVersion(), v.getQualifier());
      attributes.putValue("Bundle-Version", osgiVersion);
    }

    if (terracottaRequireVersion != null) {
      attributes.putValue("Terracotta-RequireVersion", terracottaRequireVersion);
    }

    if (terracottaKit != null) {
      attributes.putValue("Terracotta-Kit", terracottaKit);
    }

    if (terracottaTimApi != null) {
      attributes.putValue("Terracotta-TIM-API", terracottaTimApi);
    }

    if (presentationFactory != null) {
      attributes.putValue("Presentation-Factory", presentationFactory);
    }

    if (terracottaProjectStatus != null) {
      attributes.putValue("Terracotta-ProjectStatus", terracottaProjectStatus);
    }

    if (terracottaConfiguration != null) {
      attributes.putValue("Terracotta-Configuration", terracottaConfiguration);
    }

    if (tunneledMBeanDomains != null && tunneledMBeanDomains.length > 0) {
      attributes.putValue("Tunneled-MBean-Domains", StringUtils.join(tunneledMBeanDomains, ":"));
    }

    attributes.putValue("Terracotta-InternalTIM", String.valueOf(terracottaInternalTIM));
    addBuildAttributes(attributes);

    return manifest;
  }

  private void addBuildAttributes(Attributes attributes) throws MojoExecutionException {
    final String UNKNOWN = "unknown";
    final String BUILDINFO = "BuildInfo-";
    String host = UNKNOWN;
    String svnUrl = UNKNOWN;
    String revision = UNKNOWN;

    String user = System.getProperty("user.name") == null ? UNKNOWN : System.getProperty("user.name");
    String timestamp = ((SimpleDateFormat) sdf.get()).format(new Date());
    try {
      host = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      if (getLog().isErrorEnabled())
        getLog().error("Exception while finding host name", e);
    }

    attributes.putValue(BUILDINFO + "User", user);
    attributes.putValue(BUILDINFO + "Host", host);
    attributes.putValue(BUILDINFO + "Timestamp", timestamp);

    try {
      String svnInfo = getSvnInfo();
      BufferedReader br = new BufferedReader(new StringReader(svnInfo));
      String line = null;
      final String urlKey = "URL: ";
      final String revisionKey = "Last Changed Rev: ";
      while ((line = br.readLine()) != null) {
        if (line.startsWith(urlKey)) {
          svnUrl = line.substring(urlKey.length());
        }
        if (line.startsWith(revisionKey)) {
          revision = line.substring(revisionKey.length());
        }
      }
      if (svnUrl.equals(UNKNOWN)) {
        throw new MojoExecutionException("Expecting key '" + urlKey + "' not found");
      }
      if (revision.equals(UNKNOWN)) {
        throw new MojoExecutionException("Expecting key '" + revisionKey + "' not found");
      }
    } catch (IOException ioe) {
      throw new MojoExecutionException("Exception reading svn info", ioe);
    } catch (MojoExecutionException e1) {
      if (failOnError) {
        throw e1;
      } else {
        getLog().warn("svn info failed: " + e1.getMessage());
        if (getLog().isDebugEnabled()) {
          getLog().debug(e1);
        }
      }
    }

    attributes.putValue(BUILDINFO + "URL", svnUrl);
    attributes.putValue(BUILDINFO + "Revision", revision);
  }

  private String generateRequiredBundles() throws MojoExecutionException {
    getLog().debug("Generating required bundles");

    try {
      StringBuffer sb = new StringBuffer();

      String sep = "";

      @SuppressWarnings("unchecked")
      List<Dependency> dependencies = project.getDependencies();

      for (Dependency d : dependencies) {
        getLog().debug("  Dependency: " + d);
        getLog().debug("required bundle original version: " + d.getVersion());

        String scope = d.getScope();
        if ("test".equals(scope) || "provided".equals(scope)) {
          continue;
        }

        File file = null;

        if (isRange(d.getVersion())) {
          file = resolver.resolveArtifactInRange(d.getGroupId(), d.getArtifactId(), d.getVersion(), localRepository,
              remoteRepositories);
        } else {
          file = resolver.resolveArtifact(d.getGroupId(), d.getArtifactId(), d.getVersion(), localRepository,
              remoteRepositories);
        }

        if (file != null && file.isFile()) {
          JarFile bundle = new JarFile(file);
          Manifest manifest = bundle.getManifest();

          Attributes attributes = manifest.getMainAttributes();
          String symbolicName = attributes.getValue("Bundle-SymbolicName");
          String version = attributes.getValue("Bundle-Version");

          // If original dependency listed a version range, respect that by using a
          // translated version of the range instead.
          if (isRange(d.getVersion())) {
            version = d.getVersion().replaceAll("-SNAPSHOT", ".SNAPSHOT");

            if (version.endsWith(",]") || version.endsWith(",)")) {
              if (version.startsWith("(")) {
                // XXX: I don't know how to translate this to an OSGi range right now
                throw new AssertionError("Cannot handle non-inclusive min with unbounded max");
              }

              version = version.substring(1, version.indexOf(','));
            }

            version = "\"" + version + "\"";
          } else {
            version = "\"[" + version + "," + version + "]\"";
          }

          // Allows have the version range completely open for toolkit dependency
          if (d.getGroupId().equals("org.terracotta.toolkit")) {
            getLog().info("Setting open range for " + d.getArtifactId() + " dependency in " + project.getArtifactId());
            version = "\"1.0.0\"";
          }

          getLog().debug("    symbolicName: " + symbolicName);
          getLog().debug("    version: " + version);

          if (symbolicName != null && symbolicName.startsWith("org.terracotta")) {
            // org.terracotta.modules.clustered-commons-collections-3.1;bundle-version:=2.8.0.SNAPSHOT
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

  private boolean isRange(String version) {
    return version.startsWith("[") || version.startsWith("(");
  }

  private String trimWhitespace(String s) {
    return s.replaceAll("\\s", "");
  }

  private String getSvnInfo() throws MojoExecutionException, IOException {
    try {
      File baseDir = project.getBasedir();
      if (baseDir == null) // in tests
        baseDir = new File(".");
      if (rootPath == null) {
        rootPath = baseDir.getAbsolutePath();
      }

      Commandline cmd = new Commandline();
      cmd.setExecutable("svn");
      cmd.addArguments(new String[] { "info", new File(rootPath).getCanonicalPath() });

      StringBuilderStreamConsumer streamConsumer = new StringBuilderStreamConsumer();
      if (getLog().isDebugEnabled()) {
        getLog().debug("BuildInfo: executing " + cmd);
      }

      int exitCode = CommandLineUtils.executeCommandLine(cmd, streamConsumer, streamConsumer);
      if (exitCode == 0) {
        return streamConsumer.toString();
      } else {
        throw new MojoExecutionException(streamConsumer.toString());
      }
    } catch (CommandLineException e) {
      throw new MojoExecutionException("svn command failed", e);
    }
  }

  static class StringBuilderStreamConsumer implements StreamConsumer {
    StringBuilder sb = new StringBuilder();

    public void consumeLine(String s) {
      sb.append(s).append("\n");
    }

    @Override
    public String toString() {
      return sb.toString();
    }
  }
}
