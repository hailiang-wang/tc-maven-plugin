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
 * Stop DSO server
 * 
 * @author Eugene Kuleshov
 * 
 * @goal stop
 * @requiresDependencyResolution runtime
 */
public class DsoStopMojo extends AbstractDsoServerMojo {

  private boolean startServer = true;

  public DsoStopMojo() {
  }
  
  public DsoStopMojo(AbstractDsoMojo mojo) {
    super(mojo);
  }
  
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!startServer) {
      getLog().info("Skipping stopping DSO Server");
      return;
    }

    stop(); 
  }
    
  public void setStartServer(boolean startServer) {
    this.startServer = startServer;
  }
}
