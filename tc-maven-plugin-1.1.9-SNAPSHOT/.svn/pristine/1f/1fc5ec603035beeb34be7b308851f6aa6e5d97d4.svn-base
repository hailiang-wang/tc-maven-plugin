/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/**
 * 
 */
package org.terracotta.maven.plugins.tc;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.terracotta.maven.plugins.tc.cl.CommandLineException;
import org.terracotta.maven.plugins.tc.cl.CommandLineUtils;
import org.terracotta.maven.plugins.tc.cl.Commandline;

import com.tc.object.tools.BootJarTool;

/**
 * Create DSO bootjar
 * 
 * @author Eugene Kuleshov
 * 
 * @goal bootjar
 * @execute phase="validate"
 * @phase integration-test
 * @requiresDependencyResolution runtime
 */
public class BootjarMojo extends AbstractDsoMojo {

  /**
   * Show verbose output from the boot jar tool
   * 
   * @parameter expression="${verbose}" default-value="false"
   */
  private boolean verbose;

  /**
   * Overwrite boot jar if already exist  
   * 
   * @parameter expression="${overwriteBootjar}" default-value="true"
   */
  private boolean overwriteBootjar;

  /**
   * Location of the generated DSO boot jar
   * 
   * @parameter expression="${bootjar}" default-value="${project.build.directory}/dso-boot.jar"
   */
  private File bootJar;

  public BootjarMojo() {
  }

  public BootjarMojo(AbstractDsoMojo mojo) {
    super(mojo);
  }
  
  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException {
    resolveModuleArtifacts(getAdditionalModules());
    
    if (!overwriteBootjar && bootJar.exists()) {
      getLog().info("BootJar already exists: " + bootJar.getAbsolutePath());
      return;
    }

    Commandline cmd = createBootjarCommandLine();

    getLog().info("------------------------------------------------------------------------");
    getLog().info("Starting bootjar tool");
    getLog().debug("cmd: " + cmd);
    
    ForkedProcessStreamConsumer streamConsumer = new ForkedProcessStreamConsumer("bootjar");
    
    try {
      Process process = CommandLineUtils.executeCommandLine(cmd, null, streamConsumer, streamConsumer, false);
      int rc = process.exitValue();
      if(rc==0) {
        getLog().info("OK");
      } else {
        String msg = "Failed to execute bootjar tool. Process return code is " + rc;
        getLog().error(msg);
        throw new MojoExecutionException(msg);
      }
    } catch (CommandLineException e) {
      String msg = "Failed to execute bootjar tool";
      getLog().error(msg, e);
      throw new MojoExecutionException(msg, e);
    }
  }

  protected Commandline createBootjarCommandLine() {
    Commandline cmd = createCommandLine();
    
    if(jvmargs!=null) {
      cmd.createArgument().setLine(jvmargs);
    }

    cmd.createArgument().setValue("-Dtc.classpath=" + createPluginClasspathAsFile());
    
    cmd.createArgument().setValue("-cp");
    cmd.createArgument().setValue(quoteIfNeeded(createPluginClasspath()));
    
    cmd.createArgument().setValue(BootJarTool.class.getName());
    
    if (verbose) {
      cmd.createArgument().setValue("-v");
    }
    
    if (overwriteBootjar) {
      cmd.createArgument().setValue("-w");
    }
    
    cmd.createArgument().setValue("-o");
    cmd.createArgument().setFile(bootJar);
    getLog().debug("bootjar file  = " + bootJar.getAbsolutePath());

    cmd.createArgument().setValue("-f");
    cmd.createArgument().setFile(config);
    getLog().debug("tc-config file  = " + config.getAbsolutePath());
    return cmd;
  }

  
  // setters for the lifecycle simulation 
  
  public void setBootJar(File bootJar) {
    this.bootJar = bootJar;
  }
  
  public void setOverwriteBootjar(boolean overwriteBootjar) {
    this.overwriteBootjar = overwriteBootjar;
  }
  
  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }
  
}
