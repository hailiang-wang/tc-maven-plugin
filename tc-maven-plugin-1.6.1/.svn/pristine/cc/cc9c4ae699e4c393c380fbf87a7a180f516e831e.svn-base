/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package org.terracotta.maven.plugins.tc;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Restart DSO server
 * 
 * @goal restart
 * @requiresDependencyResolution runtime
 * @requiresDependencyResolution runtime
 */
public class DsoRestartMojo extends AbstractDsoServerMojo {

  public DsoRestartMojo() {
    super();
  }
  
  public DsoRestartMojo(AbstractDsoMojo mojo) {
    super(mojo);
  }

  public void execute() throws MojoExecutionException, MojoFailureException {
      setSpawnServer(true);
      stop(true); 
      start();
  } 
}