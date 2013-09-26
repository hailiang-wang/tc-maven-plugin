/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/*
 *
 */
package org.terracotta.maven.plugins.tc;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.deployer.DeployerWatchdog;
import org.codehaus.cargo.container.spi.util.ContainerUtils;
import org.codehaus.cargo.maven2.configuration.Configuration;
import org.codehaus.cargo.maven2.configuration.Container;
import org.codehaus.cargo.maven2.configuration.Deployable;
import org.codehaus.cargo.maven2.util.CargoProject;
import org.codehaus.cargo.util.internal.log.AbstractLogger;
import org.codehaus.cargo.util.log.FileLogger;
import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.terracotta.maven.plugins.tc.cl.CommandLineException;
import org.terracotta.maven.plugins.tc.cl.CommandLineUtils;
import org.terracotta.maven.plugins.tc.cl.Commandline;
import org.terracotta.maven.plugins.tc.cl.JavaShell;

import com.tc.config.schema.CommonL1Config;
import com.tc.config.schema.CommonL2Config;
import com.tc.config.schema.IllegalConfigurationChangeHandler;
import com.tc.config.schema.dynamic.ConfigItem;
import com.tc.config.schema.setup.ConfigurationSetupException;
import com.tc.config.schema.setup.ConfigurationSetupManagerFactory;
import com.tc.config.schema.setup.L1ConfigurationSetupManager;
import com.tc.config.schema.setup.L2ConfigurationSetupManager;
import com.tc.config.schema.setup.StandardConfigurationSetupManagerFactory;
import com.tc.logging.TCLogging;
import com.tc.management.beans.L2MBeanNames;
import com.tc.management.beans.TCServerInfoMBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 * @author Eugene Kuleshov
 */
public abstract class AbstractDsoMojo extends AbstractMojo {

  private static final List<String> SYSTEM_PROPS;

  static {
    List<String> props = new ArrayList<String>();
    props.add("SecretProvider.secret");
    props.add("com.tc.SecretProvider.impl");
    props.add("tc.ssl.trustAllCerts");
    props.add("tc.ssl.disableHostnameVerifier");
    SYSTEM_PROPS = Collections.unmodifiableList(props);
  }

  public static final String CONTEXT_KEY_STARTABLES = AbstractDsoMojo.class.getName() + "-Startables";

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
   * Project classpath.
   *
   * @parameter expression="${project.runtimeClasspathElements}"
   * @required
   * @readonly
   */
  protected List<String> classpathElements;

  /**
   * Plugin artifacts
   *
   * @parameter expression="${plugin.artifacts}"
   * @required
   */
  protected List<Artifact> pluginArtifacts;

  /**
   * Path to the java executable to use to spawned processes
   *
   * @parameter expression="${jvm}"
   * @optional
   */
  protected String jvm;

  /**
   * Arguments for the server java process
   *
   * @parameter expression="${jvmargs}"
   * @optional
   */
  protected String jvmargs;

  /**
   * Enable remote JVM debugging for the started process
   *
   * @parameter expression="${jvmdebug}" default-value="false"
   * @optional
   */
  protected boolean jvmdebug;

  /**
   * Enable passing input stream to the forked processes
   *
   * @parameter expression="${interactive}" default-value="false"
   * @optional
   */
  private boolean interactive;

  /**
   * Working directory for the spawned java processes
   *
   * @parameter expression="${workingDirectory}" default-value="${basedir}"
   */
  protected File workingDirectory;

  /**
   * Location of the DSO config (tc-config.xml)
   *
   * @parameter expression="${config}" default-value="${basedir}/tc-config.xml"
   */
  protected File config;

  /**
   * Configuration for the DSO-enabled processes to start. <br>
   * <br>
   * The following example shows plugin configuration that can be used to declare processes to start. Note that elements
   * container and configuration</a> are the same as for <a
   * href="http://cargo.codehaus.org/Maven2+Plugin+Reference+Guide">Cargo Maven plugin</a>.
   *
   * <pre>
   * &lt;processes&gt;
   *   &lt;process nodeName=&quot;master&quot; count=&quot;1&quot;
   *      arguments=&quot;someArgument&quot;
   *      jvmargs=&quot;-Xmx20m&quot;&gt;
   *     &lt;className&gt;org.terracotta.maven.plugins.tc.test.MasterProcess&lt;/className&gt;
   *   &lt;process/&gt;
   *   &lt;process nodeName=&quot;sample&quot; count=&quot;2&quot; jvmargs=&quot;-Xmx20m&quot;
   *     className=&quot;org.terracotta.maven.plugins.tc.test.SampleProcess&quot;&gt;
   *    &lt;systemProperty key=&quot;foo&quot; value=&quot;boo&quot;/&gt;
   *   &lt;/process&gt;
   *   &lt;process nodeName=&quot;tomcat&quot; count=&quot;2&quot; jvmargs=&quot;-Xmx20m&quot;&gt;
   *    &lt;container&gt;
   *     &lt;containerId&gt;tomcat5x&lt;/containerId&gt;
   *     &lt;zipUrlInstaller&gt;
   *      &lt;url&gt;http://www.apache.org/dist/tomcat/tomcat-5/v5.5.25/bin/apache-tomcat-5.5.25.zip&lt;/url&gt;
   *      &lt;installDir&gt;${project.build.directory}/install/tomcat5x&lt;/installDir&gt;
   *     &lt;/zipUrlInstaller&gt;
   *    &lt;/container&gt;
   *   &lt;/process&gt;
   * &lt;/processes&gt;
   * </pre>
   *
   * See <a href="launch.html">Launching</a> and <a href="cargo.html">Cargo</a> sections for more details.
   *
   * @parameter expression="${processes}"
   * @optional
   */
  protected ProcessConfiguration[] processes;

  /**
   * Main Java class name to start (not recommended, use processes element instead)
   *
   * @parameter expression="${className}"
   * @optional
   */
  private String className;

  /**
   * Arguments for the main Java class (not recommended, use processes element instead)
   *
   * @parameter expression="${arguments}"
   * @optional
   */
  private String arguments;

  /**
   * Number of nodes to start (not recommended, use processes element instead)
   *
   * @parameter expression="${numberOfNodes}" default-value="1"
   */
  protected int numberOfNodes;

  /**
   * Maven Project
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   * @see #getProject()
   */
  private MavenProject project;

  /**
   * Cargo Project
   *
   * @see org.codehaus.cargo.maven2.util.CargoProject
   */
  private CargoProject cargoProject;

  private int port = 8080;

  private int rmiPort = 9080;

  protected int debugPort = 5000;

  protected static final String SECURED_USER     = System.getProperty("tc.secured.user");
  protected static final String SECURED_PASSWORD = System.getProperty("tc.secured.password");

  static {
    if (isSecured()) {
      try {
        final Class<?> aClass = Class.forName("com.tc.net.core.ssl.SSLBufferManagerFactory");
        final Method createSSLContext = aClass.getDeclaredMethod("createSSLContext", KeyManager[].class);
        KeyManager[] keyManager = null;
        Object[] param = {keyManager}; // wrap the param to get passed into the invoke
        SSLContext sslContext = (SSLContext) createSSLContext.invoke(null, param);
        SSLContext.setDefault(sslContext);
      } catch (Exception e) {
        throw new RuntimeException("Couldn't create the SSLContext!", e);
      }
    }
  }

  public AbstractDsoMojo() {
  }

  public AbstractDsoMojo(AbstractDsoMojo mojo) {
    setLog(mojo.getLog());
    setPluginContext(mojo.getPluginContext());

    config = mojo.config;
    workingDirectory = mojo.workingDirectory;
    jvm = mojo.jvm;
    classpathElements = mojo.classpathElements;
    pluginArtifacts = mojo.pluginArtifacts;

    localRepository = mojo.localRepository;
    remoteRepositories = mojo.remoteRepositories;

    jvmdebug = mojo.jvmdebug;
    debugPort = mojo.debugPort;
  }

  protected Commandline createCommandLine() {
    String executable;
    if (jvm != null && jvm.length() > 0) {
      executable = jvm;
    } else {
      String os = System.getProperty("os.name");
      if (os.indexOf("Windows") > -1) {
        executable = System.getProperty("java.home") + "/bin/java.exe";
      } else {
        executable = System.getProperty("java.home") + "/bin/java";
      }
    }

    JavaShell javaShell = new JavaShell(executable);

    Commandline cmd = new Commandline(javaShell);

    return cmd;
  }

  protected String createProjectClasspath() {
    String classpath = "";
    for (String classpathElement : classpathElements) {
      classpath += classpathElement + File.pathSeparator;
    }
    return classpath;
  }

  protected String createPluginClasspath() {
    String classpath = "";
    for (Artifact artifact : pluginArtifacts) {
      if (Artifact.SCOPE_COMPILE.equals(artifact.getScope()) || Artifact.SCOPE_RUNTIME.equals(artifact.getScope())) {
        String groupId = artifact.getGroupId();
        // XXX workaround to shorten the classpath
        if (!groupId.startsWith("org.apache.maven") //
            && !"org.codehaus.cargo".equals(groupId) //
            && !"org.springframework".equals(groupId)) {
          classpath += artifact.getFile().getAbsolutePath() + File.pathSeparator;
        }
      }
    }
    return classpath;
  }

  protected String createPluginClasspathAsFile() {
    FileOutputStream fos = null;
    File tempClasspath = null;
    try {
      tempClasspath = File.createTempFile("tc-classpath", ".tmp");
      tempClasspath.deleteOnExit();
      fos = new FileOutputStream(tempClasspath);
      IOUtils.write(createPluginClasspath(), fos);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create tc.classpath in " + System.getProperty("java.io.tmpdir"), e);
    } finally {
      IOUtils.closeQuietly(fos);
    }
    return tempClasspath.toURI().toString();
  }

  protected Artifact getTerracottaArtifact() {
    for (Artifact artifact : pluginArtifacts) {
      if ("terracotta".equals(artifact.getArtifactId())) {
        return artifact;
      }
    }
    return null;
  }

  protected String quoteIfNeeded(String path) {
    if (path.indexOf(" ") > 0) {
      return "\"" + path + "\"";
    }
    return path;
  }

  protected void appendSystemProps(Commandline cmd) {
    for (String prop : SYSTEM_PROPS) {
      final String val = System.getProperty(prop);
      if(val != null) {
        cmd.createArg().setValue("-D" + prop + "=" + val);
      }
    }
  }

  private static final class MavenLogger extends AbstractLogger {
    private final String nodeName;
    private transient final Log log;

    MavenLogger(Log log, String nodeName) {
      this.log = log;
      this.nodeName = nodeName;
    }

    @Override
    protected void doLog(LogLevel level, String message, String category) {
      String formattedMessage = "[" + nodeName + "] " + message;
      if (level == LogLevel.WARN) {
        log.warn(formattedMessage);
      } else if (level == LogLevel.DEBUG) {
        log.debug(formattedMessage);
      } else {
        log.info(formattedMessage);
      }
    }
  }

  class ForkedProcessStreamConsumer implements StreamConsumer {
    private final String prefix;

    public ForkedProcessStreamConsumer(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public void consumeLine(String msg) {
      getLog().info("[" + prefix + "] " + msg);
    }
  }

  public String getServerState(String jmxUrl) throws MalformedURLException, IOException {
    getLog().debug("Connecting to DSO server at " + jmxUrl);
    JMXServiceURL url = new JMXServiceURL(jmxUrl);
    JMXConnector jmxc = null;
    try {
      jmxc = getJmxConnector(url);
      MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
      TCServerInfoMBean serverBean = MBeanServerInvocationHandler.newProxyInstance(mbsc, L2MBeanNames.TC_SERVER_INFO, TCServerInfoMBean.class,
          false);
      return serverBean.getState();
    } finally {
      if (jmxc != null) {
        try {
          jmxc.close();
        } catch (IOException ex) {
          getLog().error("Error closing jmx connection", ex);
        }
      }
    }
  }

  protected static JMXConnector getJmxConnector(final JMXServiceURL url) throws IOException {
    final Map<String, Object> env = new HashMap<String, Object>();
    if (isSecured()) {
      SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
      SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();
      env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
      env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
      env.put("jmx.remote.credentials", new Object[] { SECURED_USER, SECURED_PASSWORD.toCharArray() });

      // Needed to avoid "non-JRMP server at remote endpoint" error
      env.put("com.sun.jndi.rmi.factory.socket", csf);
    }
    return JMXConnectorFactory.connect(url, env);
  }

  protected static boolean isSecured() {
    return SECURED_USER != null && SECURED_PASSWORD != null;
  }

  public boolean isServerFullyStarted(String jmxUrl) {
    getLog().debug("Connecting to DSO server at " + jmxUrl);
    JMXConnector jmxc = null;
    try {
      JMXServiceURL url = new JMXServiceURL(jmxUrl);
      jmxc = getJmxConnector(url);
      MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
      TCServerInfoMBean serverBean = MBeanServerInvocationHandler.newProxyInstance(mbsc, L2MBeanNames.TC_SERVER_INFO, TCServerInfoMBean.class,
          false);
      return serverBean.isActive() || serverBean.isPassiveStandby() || serverBean.isPassiveUninitialized();
    } catch (Exception e) {
      return false;
    }
    finally {
      if (jmxc != null) {
        try {
          jmxc.close();
        } catch (IOException ex) {
          getLog().error("Error closing jmx connection", ex);
        }
      }
    }
  }

  protected String getJMXUrl(CommonL2Config config) {
    String host = config.host();
    int port;
    if (host == null) {
      host = "localhost";
      port = 9520;
    } else {
      port = config.jmxPort().getIntValue();
    }

    if (isSecured()) {
      return "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
    } else {
      return "service:jmx:jmxmp://" + host + ":" + port;
    }
  }

  protected CommonL2Config getServerConfig(String serverName) throws ConfigurationSetupException {
    TCLogging.disableLocking(); // required, so L1 and L2 won't complain about locked log directory

    String[] args = {};
    if (config.exists()) {
      if (serverName == null) {
        args = new String[] { "-f", config.getAbsolutePath() };
      } else {
        args = new String[] { "-f", config.getAbsolutePath(), "-n", serverName };
      }
    }

    ConfigurationSetupManagerFactory factory = new StandardConfigurationSetupManagerFactory(args,
        StandardConfigurationSetupManagerFactory.ConfigMode.L2, new MavenIllegalConfigurationChangeHandler(), null);

    L2ConfigurationSetupManager manager = factory.createL2TVSConfigurationSetupManager(serverName);

    return manager.commonL2ConfigFor(serverName);
  }

  protected CommonL1Config getClientConfig() throws ConfigurationSetupException {
    ConfigurationSetupManagerFactory factory = new StandardConfigurationSetupManagerFactory(
        StandardConfigurationSetupManagerFactory.ConfigMode.CUSTOM_L1, new MavenIllegalConfigurationChangeHandler(), null);
    L1ConfigurationSetupManager manager = factory.getL1TVSConfigurationSetupManager();
    return manager.commonL1Config();
  }

  void log(String msg, Exception ex) {
    if (getLog().isDebugEnabled()) {
      getLog().error(msg, ex);
    } else {
      getLog().error(msg);
    }
  }

  // startables

  @SuppressWarnings("unchecked")
  protected List<Startable> getStartables() throws MojoExecutionException {
    List<Startable> startables = null;

    Map<Object, Object> context = getPluginContext();
    if (context != null) {
      startables = (List<Startable>) context.get(CONTEXT_KEY_STARTABLES);
    }

    if (startables == null) {
      startables = createStartables();
      if (context != null) {
        context.put(CONTEXT_KEY_STARTABLES, startables);
      }
    }

    return startables;
  }

  private List<Startable> createStartables() throws MojoExecutionException {
    getLog().debug("Creating Startables");

    List<ProcessConfiguration> processList = new ArrayList<ProcessConfiguration>();
    if (className != null) {
      ProcessConfiguration process = new ProcessConfiguration();
      process.setNodeName("node");
      process.setClassName(className);
      process.setArgs(arguments);
      process.setJvmArgs(jvmargs);
      process.setCount(numberOfNodes);
      processList.add(process);
    }

    processList.addAll(Arrays.asList(processes));
    getLog().debug("Processes: " + processList);

    int totalNumberOfNodes = 0;
    for (ProcessConfiguration process : processList) {
      totalNumberOfNodes += process.getCount();
    }

    List<Startable> startables = new ArrayList<Startable>();
    for (ProcessConfiguration process : processList) {
      getLog().debug("Process: " + process.getNodeName() + " " + process.getCount());

      int startNode = process.getStartNode();
      if (startNode > 0) {
        port += startNode;
        rmiPort += startNode;
      }
      for (int n = 0; n < process.getCount(); n++) {
        String nodeName = process.getCount() > 1 ? process.getNodeName() + (n + startNode) : process.getNodeName();

        Container container = process.getContainer();
        Startable startable;
        if (container == null) {
          startable = createCmdStartable(process, nodeName, totalNumberOfNodes);
        } else {
          startable = createCargoStartable(process, nodeName, totalNumberOfNodes, container);
        }
        startables.add(startable);
        getLog().debug("Created Startable: " + startable);
      }
    }

    return startables;
  }

  private Startable createCmdStartable(ProcessConfiguration process, String nodeName, int totalNumberOfNodes) {
    Commandline cmd = createCommandLine();

    if (workingDirectory != null) {
      cmd.setWorkingDirectory(workingDirectory);
    }

    cmd.createArgument().setLine(createJvmArguments(process, nodeName, totalNumberOfNodes));

    cmd.createArgument().setValue("-cp");
    cmd.createArgument().setValue(quoteIfNeeded(createProjectClasspath()));

    appendSystemProps(cmd);

    cmd.createArgument().setValue(process.getClassName());
    if (process.getArgs() != null) {
      cmd.createArgument().setValue(process.getArgs());
    }

    return new CmdStartable(nodeName, cmd, interactive);
  }

  private Startable createCargoStartable(ProcessConfiguration process, String nodeName, int totalNumberOfNodes,
      Container container) throws MojoExecutionException {

    LocalConfiguration cargoConfiguration = createCargoConfiguration(container, process, nodeName, totalNumberOfNodes);

    InstalledLocalContainer cargoContainer = (InstalledLocalContainer) container.createContainer( //
        cargoConfiguration, //
        createCargoLogger(container, nodeName), getCargoProject());

    // cargoContainer.setSystemProperties(process.getProperties());

    // deploy auto-deployable
    if (getCargoProject().getPackaging() != null && getCargoProject().isJ2EEPackaging()) {
      // Has the auto-deployable already been specified as part of the <deployables> config element?
      getLog().info("cargoConfiguration " + cargoConfiguration);
      if (process.getConfiguration() == null //
          || process.getConfiguration().getDeployables() == null
          || !containsAutoDeployable(process.getConfiguration().getDeployables())) {
        LocalConfiguration configuration = cargoContainer.getConfiguration();
        configuration.addDeployable(new Deployable().createDeployable(cargoContainer.getId(), getCargoProject()));
      }
    }

    getLog().debug(nodeName + " home:" + cargoContainer.getHome());
    getLog().debug(nodeName + " conf:" + cargoContainer.getConfiguration());
    getLog().debug(nodeName + " props:" + cargoConfiguration.getProperties());

    return new CargoStartable(nodeName, cargoContainer);
  }

  private boolean containsAutoDeployable(Deployable[] deployableElements) {
    for (Deployable deployableElement : deployableElements) {
      if (deployableElement.getGroupId().equals(getCargoProject().getGroupId())
          && deployableElement.getArtifactId().equals(getCargoProject().getArtifactId())) {
        return true;
      }
    }
    return false;
  }

  private CargoProject getCargoProject() {
    if (cargoProject == null) {
      cargoProject = new CargoProject(project, getLog());
    }
    return cargoProject;
  }

  private Logger createCargoLogger(Container container, String nodeName) {
    Logger logger;
    if (container.getLog() != null) {
      container.getLog().getParentFile().mkdirs();
      logger = new FileLogger(container.getLog(), false);
    } else {
      logger = new MavenLogger(getLog(), nodeName);
    }

    if (container.getLogLevel() != null) {
      logger.setLevel(container.getLogLevel());
    }

    return logger;
  }

  private LocalConfiguration createCargoConfiguration(Container container, ProcessConfiguration process,
      String nodeName, int totalNumberOfNodes) throws MojoExecutionException {
    Configuration configuration = process.getConfiguration();
    if (configuration == null) {
      configuration = new Configuration();
    }

    // If no configuration element has been specified create one with default values.
    //    if (getConfigurationElement() == null) {
    //      Configuration configurationElement = new Configuration();
    //
    //      if (getContainerElement().getType().isLocal()) {
    //        configurationElement.setType(ConfigurationType.STANDALONE);
    //        configurationElement.setHome(new File(getCargoProject().getBuildDirectory(), getContainerElement()
    //            .getContainerId()).getPath());
    //      } else {
    //        configurationElement.setType(ConfigurationType.RUNTIME);
    //      }
    //
    //      setConfigurationElement(configurationElement);
    //    }

    // XXX
    //    configuration.setHome(home);
    //    configuration.setProperties(properties);
    //    configuration.setDeployables(deployables);

    configuration.setHome("target/" + nodeName);

    LocalConfiguration localConfiguration = (LocalConfiguration) configuration.createConfiguration( //
        container.getContainerId(), container.getType(), getCargoProject());

    localConfiguration.setProperty(ServletPropertySet.PORT, "" + port++);
    localConfiguration.setProperty(GeneralPropertySet.RMI_PORT, "" + rmiPort++);
    localConfiguration.setProperty(GeneralPropertySet.JVMARGS,
        createJvmArguments(process, nodeName, totalNumberOfNodes));

    if (getLog().isDebugEnabled()) {
      localConfiguration.setProperty(GeneralPropertySet.LOGGING, "high");
    } else {
      localConfiguration.setProperty(GeneralPropertySet.LOGGING, "medium");
    }

    return localConfiguration;
  }

  protected String createJvmArguments(ProcessConfiguration process, String nodeName, int totalNumberOfNodes) {
    StringBuffer sb = new StringBuffer();

    sb.append("-Dtc.nodeName=" + nodeName);
    sb.append(" -Dtc.numberOfNodes=" + totalNumberOfNodes);

    // system properties
    for (Entry<String, String> e : process.getProperties().entrySet()) {
      sb.append(" -D" + e.getKey() + "=" + e.getValue());
    }

    // DSO debugging
    if (jvmdebug) {
      int port = ++debugPort;
      sb.append(" -Xdebug");
      sb.append(" -Xnoagent");
      // sb.append("-Xrunjdwp:transport=dt_socket,server=y,address=" + port);
      sb.append(" -Xrunjdwp:transport=dt_socket,suspend=y,server=y,address=" + port);

      sb.append(" -Dtc.classloader.writeToDisk=true");

      getLog().info("[" + nodeName + "] debug port " + port);
    }

    if (process.getJvmArgs() != null) {
      sb.append(' ').append(process.getJvmArgs());
    }

    return sb.toString();
  }

  // setters for the lifecycle simulation

  public void setJvmargs(String jvmargs) {
    this.jvmargs = jvmargs;
  }

  public void setJvm(String jvm) {
    this.jvm = jvm;
  }

  final class MavenIllegalConfigurationChangeHandler implements IllegalConfigurationChangeHandler {
    @Override
    public void changeFailed(ConfigItem item, Object oldValue, Object newValue) {
      String text = "Inconsistent Terracotta configuration.\n\n"
          + "The configuration that this client is using is different from the one used by\n"
          + "the connected production server.\n\n" + "Specific information: " + item + " has changed.\n"
          + "   Old value: " + describe(oldValue) + "\n" + "   New value: " + describe(newValue) + "\n";
      getLog().error(text);
    }

    private String describe(Object o) {
      if (o == null) {
        return "<null>";
      } else if (o.getClass().isArray()) {
        return ArrayUtils.toString(o);
      } else {
        return o.toString();
      }
    }
  }

  public static interface Startable {
    public void start(boolean wait);

    public void stop();

    public String getNodeName();
  }

  public class CmdStartable implements Startable {
    private final String nodeName;
    private final Commandline cmd;
    private final boolean interactive;

    public CmdStartable(String nodeName, Commandline cmd, boolean interactive) {
      this.nodeName = nodeName;
      this.cmd = cmd;
      this.interactive = interactive;
    }

    @Override
    public void start(boolean wait) {
      try {
        StreamConsumer streamConsumer = new ForkedProcessStreamConsumer(nodeName);
        InputStream systemIn = interactive ? System.in : null;
        CommandLineUtils.executeCommandLine(cmd, systemIn, streamConsumer, streamConsumer, !wait);
      } catch (CommandLineException e) {
        getLog().error("Failed to start node " + nodeName, e);
      }
    }

    @Override
    public void stop() {
      // ignore
    }

    @Override
    public String getNodeName() {
      return nodeName;
    }

    @Override
    public String toString() {
      return cmd.toString();
    }

  }

  public class CargoStartable implements Startable {
    private final String nodeName;
    private final InstalledLocalContainer container;

    public CargoStartable(String nodeName, InstalledLocalContainer container) {
      this.nodeName = nodeName;
      this.container = container;
    }

    @Override
    public void start(boolean wait) {
      container.start();

      URL cpcUrl = ContainerUtils.getCPCURL(container.getConfiguration());

      DeployerWatchdog watchdog1 = new DeployerWatchdog(new URLDeployableMonitor(cpcUrl, container.getTimeout()));
      watchdog1.watchForAvailability();
      getLog().info("[" + nodeName + "] started");

      try {
        Thread.sleep(2000L);
      } catch (InterruptedException ex) {
        // ignore
      }

      if (wait) {
        ContainerUtils.waitTillContainerIsStopped(container);
        getLog().info("[" + nodeName + "] stopped");
      }
    }

    @Override
    public void stop() {
      container.stop();
    }

    @Override
    public String getNodeName() {
      return nodeName;
    }

    @Override
    public String toString() {
      return container.toString();
    }

  }

}
