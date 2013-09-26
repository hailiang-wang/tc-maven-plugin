/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/*
 * 
 */
package org.terracotta.maven.plugins.tc;

/**
 * Run DSO processes for the "integration-test" phase.
 * 
 * Similar to <a href="terminate-mojo.html">tc:terminate</a>, but doesn't start DSO 
 * server and doesn't wait for started processes to complete (convenient for 
 * <a ref="cargo.html">integration testing</a>).
 * 
 * @author Eugene Kuleshov
 * 
 * @see DsoRunMojo
 * 
 * @goal run-integration
 */
public class DsoRunIntegrationMojo extends DsoRunMojo {
  
  protected boolean waitForCompletion() {
    return false;
  }

  protected boolean stopDsoServer() {
    return false;
  }
  
}
