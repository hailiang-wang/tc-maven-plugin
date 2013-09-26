/*
 * All content copyright (c) Terracotta, Inc., except as may otherwise be noted in a separate copyright notice. All
 * rights reserved.
 */
package org.terracotta.maven.plugins.tc;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.tc.util.ProductInfo;

/**
 * Show Terracotta version this plugin running with
 * 
 * @author hhuynh
 * 
 * @goal version
 */
public class TerracottaVersionMojo extends AbstractMojo {

  /**
   * Type of version string: short, long, raw. Default is long
   * 
   * @parameter expression="${type}" default-value="long"
   */
  private String type;

  public void execute() throws MojoExecutionException, MojoFailureException {
    if ("long".equals(type)) {
      System.out.println(ProductInfo.getInstance().toLongString());
    } else if ("short".equals(type)) {
      System.out.println(ProductInfo.getInstance().toShortString());
    } else if ("raw".equals(type)) {
      ProductInfo.printRawData();
    }
  }
}
