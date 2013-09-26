/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/**
 * 
 */
package org.terracotta.maven.plugins.tc;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Start DSO server
 * 
 * @author Eugene Kuleshov
 * 
 * @goal start
 * @requiresDependencyResolution runtime
 */
public class DsoStartMojo extends AbstractDsoServerMojo 
{
  /**
   * Only set by DsoLifecycleMojo
   */
  private boolean startServer = true;

  public DsoStartMojo() {
  }

  public DsoStartMojo(AbstractDsoMojo mojo) {
    super(mojo);
  }

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!startServer) {
      getLog().info("Skipping starting DSO Server");
      return;
    }

    start();
  }
  
  public void setStartServer(boolean startServer) {
    this.startServer = startServer;
  }

}