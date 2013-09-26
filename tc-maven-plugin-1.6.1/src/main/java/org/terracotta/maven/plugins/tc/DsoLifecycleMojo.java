/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/*
 *
 */
package org.terracotta.maven.plugins.tc;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;


/**
 */
public abstract class DsoLifecycleMojo extends AbstractDsoMojo {

  // start/stop

  /**
   * Start DSO Server in a separate java process
   *
   * @parameter expression="${spawnServer}" default-value="true"
   */
  protected boolean spawnServer;

  /**
   * Name of the DSO Server to start
   *
   * @parameter expression="${serverName}"
   * @optional
   */
  protected String serverName;

  /**
   * Start DSO server before starting other goals, such as tC:run or tc:test
   *
   * @parameter expression="${startServer}" default-value="true"
   */
  protected boolean startServer;


  public void execute() throws MojoExecutionException, MojoFailureException {
    if(startDsoServer()) {
      DsoStartMojo dsoStartMojo = new DsoStartMojo(this);
      dsoStartMojo.setSpawnServer(spawnServer);
      dsoStartMojo.setServerName(serverName);
      dsoStartMojo.setStartServer(startServer);
      dsoStartMojo.setJvmargs(jvmargs);
      dsoStartMojo.setJvm(jvm);
      dsoStartMojo.execute();
    }

    try {
      onExecute();

    } finally {
      if(stopDsoServer()) {
        DsoStopMojo dsoStopMojo = new DsoStopMojo(this);
        dsoStopMojo.setSpawnServer(spawnServer);
        dsoStopMojo.setServerName(serverName);
        dsoStopMojo.setStartServer(startServer);
        dsoStopMojo.setJvmargs(jvmargs);
        dsoStopMojo.setJvm(jvm);
        dsoStopMojo.execute();
      }
    }
  }

  protected boolean startDsoServer() {
    return true;
  }

  protected boolean stopDsoServer() {
    return true;
  }

  protected abstract void onExecute()  throws MojoExecutionException, MojoFailureException;

}
