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

import com.tc.util.ToolClassNames;

/**
 * Start DSO Admin UI
 * 
 * @author Eugene Kuleshov
 * 
 * @goal dev-console
 */
public class DsoDevConsoleMojo extends AbstractDsoMojo {

  public DsoDevConsoleMojo() {
  }
  
  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException {
    Commandline cmd = createCommandLine();

    cmd.createArgument().setValue("-cp");
    cmd.createArgument().setValue(quoteIfNeeded(createPluginClasspath()));

    cmd.createArgument().setValue(ToolClassNames.ADMIN_CONSOLE_CLASS_NAME);
    
    getLog().info(cmd.toString());

    try {
      ForkedProcessStreamConsumer streamConsumer = new ForkedProcessStreamConsumer("admin");

      getLog().info("------------------------------------------------------------------------");
      getLog().info("Starting Terracotta Developer Console");
      getLog().debug("cmd: " + cmd);
      
      CommandLineUtils.executeCommandLine(cmd, null, streamConsumer, streamConsumer, true);

      getLog().info("OK");
    } catch (Exception e) {
      getLog().error("Failed to execute bootjar tool", e);
    }
  }

}
