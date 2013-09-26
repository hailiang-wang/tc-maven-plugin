/*
 * All content copyright (c) Terracotta, Inc., except as may otherwise be noted in a separate copyright notice. All
 * rights reserved.
 */
package org.terracotta.maven.plugins.tc;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginManager;
import org.apache.maven.plugins.help.DescribeMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.settings.Settings;

/**
 * Print help for all known goals
 * 
 * @author hhuynh
 * 
 * @goal help
 */
public class HelpMojo extends AbstractMojo {
  /**
   * @component
   */
  private ArtifactFactory artifactFactory;

  /**
   * @component role="org.apache.maven.plugin.PluginManager"
   */
  private PluginManager pluginManager;

  /**
   * @component role="org.apache.maven.project.MavenProjectBuilder"
   */
  private MavenProjectBuilder projectBuilder;

  /**
   * @parameter expression="${project}"
   * @readonly
   */
  private MavenProject project;

  /**
   * @parameter expression="${settings}"
   * @required
   * @readonly
   */
  private Settings settings;

  /**
   * @parameter expression="${session}"
   * @required
   * @readonly
   */
  private MavenSession session;

  /**
   * @parameter expression="${localRepository}"
   * @required
   * @readonly
   */
  private ArtifactRepository localRepository;

  /**
   * @parameter expression="${project.remoteArtifactRepositories}"
   * @required
   * @readonly
   */
  private List<?> remoteRepositories;

  /**
   * The goal you want to see help. By default help prints for all goals
   * 
   * @parameter expression="${goal}"
   */
  private String goal;

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!project.isExecutionRoot()) {
      return;
    }

    DescribeMojo describeMojo = new DescribeMojo();

    setValue(describeMojo, "artifactFactory", artifactFactory);
    setValue(describeMojo, "pluginManager", pluginManager);
    setValue(describeMojo, "projectBuilder", projectBuilder);
    setValue(describeMojo, "project", project);
    setValue(describeMojo, "settings", settings);
    setValue(describeMojo, "session", session);
    setValue(describeMojo, "localRepository", localRepository);
    setValue(describeMojo, "remoteRepositories", remoteRepositories);

    setValue(describeMojo, "plugin", "org.terracotta.maven.plugins:tc-maven-plugin");
    setValue(describeMojo, "detail", true);
    setValue(describeMojo, "goal", goal);

    describeMojo.execute();
  }

  private void setValue(Object o, String field, Object value) throws MojoFailureException {
    Class<?> c = o.getClass();
    Field _field;
    try {
      _field = c.getDeclaredField(field);
      _field.setAccessible(true);
      _field.set(o, value);
    } catch (Exception e) {
      throw new MojoFailureException(e.getMessage());
    }
  }
}
