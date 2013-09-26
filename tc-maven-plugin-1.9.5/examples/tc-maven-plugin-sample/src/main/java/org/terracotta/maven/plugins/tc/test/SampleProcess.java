/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/*
 * 
 */
package org.terracotta.maven.plugins.tc.test;

import java.util.ArrayList;

public class SampleProcess {
  
  // root
  static ArrayList nodes = new ArrayList();
  
  
  private final String nodeName;
  
  
  public SampleProcess(String nodeName) {
    this.nodeName = nodeName;
  }

  public static void main(String[] args) throws Exception {
    SampleProcess node = new SampleProcess(System.getProperty("tc.nodeName"));
    node.process();
  }

  private void process() throws Exception {
    synchronized(nodes) {
      nodes.add(this);
      nodes.notifyAll();
    }
  }

  public String toString() {
    return nodeName;
  }
  
}
