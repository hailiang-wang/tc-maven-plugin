/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/*
 * 
 */
package org.terracotta.maven.plugins.tc;


/**
 * Terminate running DSO processes for the "integration-test" phase.
 * 
 * Similar to <a href="terminate-mojo.html">tc:terminate</a> but also stops DSO server 
 * (convenient for <a ref="cargo.html">integration testing</a>).
 * 
 * @goal terminate-integration
 * @execute phase="post-integration-test"
 * 
 * @author Eugene Kuleshov
 * 
 * @see DsoTerminateMojo
 */
public class DsoTerminateIntegrationMojo extends DsoTerminateMojo {

  protected boolean stopDsoServer() {
    return true;
  }
  
}
