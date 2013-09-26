/*
 * All content copyright (c) Terracotta, Inc., except as may otherwise be noted in a separate copyright notice. All
 * rights reserved.
 */
package org.terracotta.maven.plugins.tc;

import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * List dependencies of current project that has group id starts with "org.terracotta"
 * 
 * @author hhuynh
 * 
 * @goal tc-dependencies
 */
public class TerracottaDependenciesMojo extends AbstractMojo {
  /**
   * @parameter expression="${project}"
   * @readonly
   */
  private MavenProject project;

  /**
   * Only print dependencies on other TIMs. Off by default
   * 
   * @parameter expression="${timOnly}" default-value=false
   */
  private boolean timOnly;

  /**
   * Output in simple format
   * 
   * @parameter expression="${simpleFormat}" default-value=false
   */
  private boolean simpleFormat;

  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException, MojoFailureException {
    System.out.println(PREFIX + "For " + project.getArtifactId() + ", " + project.getVersion());
    System.out.println(PREFIX + "------------------------------------------------------------------------");
    List<Dependency> dependencies = project.getDependencies();
    for (Dependency d : dependencies) {
      if (d.getGroupId().startsWith("org.terracotta")) {
        if (timOnly && !d.getGroupId().startsWith("org.terracotta.modules")) {
          continue;
        } else {
          printDependency(d);
        }
      }
    }
    System.out.println(PREFIX);
  }

  private static final String PREFIX = "TCDEP: ";

  private void printDependency(Dependency d) {
    if (simpleFormat) {
      System.out.println(PREFIX + d.getArtifactId() + ", " + d.getVersion());
    } else {
      System.out.println(PREFIX + d.getGroupId() + ", " + d.getArtifactId() + ", " + d.getVersion());
    }
  }
}
