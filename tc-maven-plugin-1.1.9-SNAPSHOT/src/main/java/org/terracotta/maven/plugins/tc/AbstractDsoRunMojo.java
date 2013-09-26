/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/*
 * 
 */
package org.terracotta.maven.plugins.tc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @requiresDependencyResolution runtime
 * @configurator override
 */
public abstract class AbstractDsoRunMojo extends DsoLifecycleMojo {

  /**
   * Node names that should be started
   * 
   * @parameter expression="${activeNodes}"
   * @optional
   */
  protected String activeNodes;

  protected Set getActiveNodes() {
    if (activeNodes == null) {
      return Collections.EMPTY_SET;
    }
    return new HashSet(Arrays.asList(activeNodes.split(",")));
  }

  protected boolean waitForCompletion() {
    return true;
  }

}
