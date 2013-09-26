/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/**
 * 
 */
package org.terracotta.maven.plugins.tc;

import org.apache.maven.plugin.MojoExecutionException;
import org.terracotta.maven.plugins.tc.cl.CommandLineUtils;
import org.terracotta.maven.plugins.tc.cl.Commandline;

import com.tc.admin.AdminClient;

/**
 * Start DSO Admin UI
 * 
 * @author Eugene Kuleshov
 * 
 * @goal admin
 */
public class DsoAdminMojo extends AbstractDsoMojo {

  public DsoAdminMojo() {
  }
  
  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException {
    Commandline cmd = createCommandLine();

    cmd.createArgument().setValue("-cp");
    cmd.createArgument().setValue(quoteIfNeeded(createPluginClasspath()));

    cmd.createArgument().setValue(AdminClient.class.getName());
    
    getLog().info(cmd.toString());

    try {
      ForkedProcessStreamConsumer streamConsumer = new ForkedProcessStreamConsumer("admin");

      getLog().info("------------------------------------------------------------------------");
      getLog().info("Starting Terracotta Admin");
      getLog().debug("cmd: " + cmd);
      
      CommandLineUtils.executeCommandLine(cmd, null, streamConsumer, streamConsumer, true);

      getLog().info("OK");
    } catch (Exception e) {
      getLog().error("Failed to execute bootjar tool", e);
    }
  }

}
