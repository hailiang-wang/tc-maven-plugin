/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package org.terracotta.maven.plugins.tc;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.terracotta.maven.plugins.tc.cl.CommandLineException;
import org.terracotta.maven.plugins.tc.cl.CommandLineUtils;
import org.terracotta.maven.plugins.tc.cl.Commandline;

import com.tc.util.ToolClassNames;

import java.io.IOException;

public abstract class AbstractDsoServerMojo extends AbstractDsoMojo {
  private static final long DEFAULT_START_STOP_TIMEOUT = 30;

  private static final String DEFAULT_MAX_HEAP = "-Xmx512m";

  /**
   * Start DSO Server in a separate JVM
   * 
   * @parameter expression="${spawnServer}" default-value="true"
   */
  private boolean spawnServer;

  /**
   * DSO Server name to start
   * 
   * @parameter expression="${serverName}"
   * @optional
   */
  protected String serverName;

  /**
   * Server start timeout in seconds
   * 
   * @parameter expression="${startTimeoutInSeconds}"
   * @optional
   */
  protected long startTimeoutInSeconds = DEFAULT_START_STOP_TIMEOUT;

  /**
   * Server stop timeout in seconds
   * 
   * @parameter expression="${stopTimeoutInSeconds}"
   * @optional
   */
  protected long stopTimeoutInSeconds = DEFAULT_START_STOP_TIMEOUT;
  
  /**
   * Force stop a server, default is true
   * 
   * @parameter expression="${forceStop}" default-value="true"
   * @optional
   */
  protected boolean           forceStop                  = true;

  public AbstractDsoServerMojo() {
    //
  }

  public AbstractDsoServerMojo(AbstractDsoMojo mojo) {
    super(mojo);
  }

  protected void start() throws MojoExecutionException {
    String jmxUrl = null;

    try {
      jmxUrl = getJMXUrl(getServerConfig(serverName));
    } catch (Exception e) {
      getLog().error("Failed to verify DSO server status", e);
      return;
    }

    try {
      String state = getServerState(jmxUrl);
      getLog().info("Server already started and in current state: " + state);
      return;
    } catch (Exception e) {
      // ok - we expect to not connect
    }

    try {
      Commandline cmd = createCommandLine();

      if (jvmargs == null) {
        jvmargs = DEFAULT_MAX_HEAP;
      } else {
        if (!jvmargs.contains("-Xmx")) {
          jvmargs += " " + DEFAULT_MAX_HEAP;
        }
      }
      getLog().debug("jvmargs: " + jvmargs);


//      cmd.createArgument().setLine("-Xdebug -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y");
      cmd.createArgument().setLine(jvmargs);

      cmd.createArgument().setValue("-Dtc.classpath=" + createPluginClasspathAsFile());
      cmd.createArgument().setValue("-Dcom.tc.management.war=" + guessWarLocation());

      cmd.createArgument().setValue("-cp");
      cmd.createArgument().setValue(quoteIfNeeded(createPluginClasspath()));

      appendSystemProps(cmd);

      cmd.createArgument().setValue(ToolClassNames.TC_SERVER_CLASS_NAME);

      if (config.exists()) {
        getLog().debug("tc-config file " + config.getAbsolutePath());
        cmd.createArgument().setValue("-f");
        cmd.createArgument().setFile(config);
      } else {
        getLog().debug("tc-config file doesn't exists " + config.getAbsolutePath());
      }

      if (serverName != null && serverName.length() > 0) {
        cmd.createArgument().setValue("-n");
        cmd.createArgument().setValue(serverName);
        getLog().debug("server serverName = " + serverName);
      }

      ForkedProcessStreamConsumer streamConsumer = new ForkedProcessStreamConsumer("dso start");

      getLog().info("------------------------------------------------------------------------");
      getLog().info("Starting DSO Server");
      getLog().debug("cmd: " + cmd);

      Process p = CommandLineUtils.executeCommandLine(cmd, null, streamConsumer, streamConsumer, spawnServer);
      getLog().info("OK");

      if (jmxUrl != null) {
        getLog().info("Start timeout in seconds: " + startTimeoutInSeconds);
        final long timeout = startTimeoutInSeconds * 1000L + System.currentTimeMillis();
        while (System.currentTimeMillis() < timeout && !isServerFullyStarted(jmxUrl) && isRunning(p)) {
          getLog().info("server starting but not yet ready, waiting...");
          Thread.sleep(1000L);
        }
        getLog().info("DSO Server state: " + getServerState(jmxUrl));
      }

      if (!isRunning(p)) {
        int rc = p.exitValue();
        if (rc != 0) {
          String msg = "Failed to start DSO server. Process return code " + rc;
          getLog().error(msg);
          throw new MojoExecutionException(msg);
        }
      }

    } catch (CommandLineException e) {
      String msg = "Failed to start DSO server";
      getLog().error(msg, e);
      throw new MojoExecutionException(msg, e);
    } catch (InterruptedException e) {
      throw new MojoExecutionException("Interrupted", e);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to connect to server", e);
    }
  }

  protected void stop() {
    stop(false);
  }

  protected void stop(boolean wait) {
    try {
      Commandline cmd = createCommandLine();
      cmd.createArgument().setValue("-Dtc.classpath=" + createPluginClasspathAsFile());

      cmd.createArgument().setValue("-cp");
      cmd.createArgument().setValue(quoteIfNeeded(createPluginClasspath()));
      appendSystemProps(cmd);

      cmd.createArgument().setValue(ToolClassNames.TC_STOP_CLASS_NAME);

      if (config.exists()) {
        cmd.createArgument().setValue("-f");
        cmd.createArgument().setFile(config);
        getLog().debug("tc-config file  = " + config.getAbsolutePath());
      }

      if(isSecured()) {
        cmd.createArgument().setValue("-s");
        cmd.createArgument().setValue("-u");
        cmd.createArgument().setValue(SECURED_USER);
        cmd.createArgument().setValue("-w");
        cmd.createArgument().setValue(SECURED_PASSWORD);
      }

      if (serverName != null && serverName.length() > 0) {
        cmd.createArgument().setValue("-n");
        cmd.createArgument().setValue(serverName);
        getLog().debug("server name = " + serverName);
      }

      if (forceStop) {
        cmd.createArgument().setValue("-force");
      }

      ForkedProcessStreamConsumer streamConsumer = new ForkedProcessStreamConsumer("dso stop");

      getLog().info("------------------------------------------------------------------------");
      getLog().info("Stopping DSO Server");
      getLog().debug("cmd: " + cmd);

      CommandLineUtils.executeCommandLine(cmd, null, streamConsumer, streamConsumer, spawnServer);

      if (wait) {
        getLog().info("Stop timeout in seconds: " + stopTimeoutInSeconds);
        getLog().info("Waiting for server to stop...");
        String jmxUrl = getJMXUrl(getServerConfig(serverName));
        String state = null;
        final long timeout = stopTimeoutInSeconds * 1000L + System.currentTimeMillis();
        do {
          try {
            state = getServerState(jmxUrl);
          } catch (Throwable ex) {
            state = null;
          }
        } while (System.currentTimeMillis() < timeout && state != null);
      }
      getLog().info("Stop command issued. Server might take some time to stop.");
    } catch (Exception e) {
      getLog().error("Error stopping DSO server", e);
    }
  }

  private boolean isRunning(Process p) {
    try {
      p.exitValue();
      return false;
    } catch (IllegalThreadStateException e) {
      return true;
    }
  }

  //setters for the lifecycle simulation
  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public void setSpawnServer(boolean spawn) {
    this.spawnServer = spawn;
  }

  private String guessWarLocation() {
    for (Artifact pluginArtifact : pluginArtifacts) {
      if(pluginArtifact.getGroupId().equals("org.terracotta") && pluginArtifact.getArtifactId().equals("management-tsa-war")) {
        return pluginArtifact.getFile().getAbsolutePath();
      }
    }
    return null;
  }
}
