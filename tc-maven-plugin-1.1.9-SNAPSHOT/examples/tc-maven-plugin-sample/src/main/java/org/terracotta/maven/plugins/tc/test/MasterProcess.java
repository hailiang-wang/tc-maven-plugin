/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/*
 * 
 */
package org.terracotta.maven.plugins.tc.test;


public class MasterProcess {
  
  private final String nodeName;
  private final int totalNodes;
  
  
  public MasterProcess(String nodeName, int totalNodes) {
    this.nodeName = nodeName;
    this.totalNodes = totalNodes;
  }

  public static void main(String[] args) throws Exception {
    MasterProcess node = new MasterProcess(System.getProperty("tc.nodeName"), SampleUtils.getTotalNodes());
    node.process();
  }

  private void process() throws Exception {
    System.err.println("Number of nodes: " + totalNodes);

    synchronized(SampleProcess.nodes) {
      SampleProcess.nodes.add(this);
      
      while(SampleProcess.nodes.size()<totalNodes) {
        try {
          SampleProcess.nodes.wait(100L);
        } catch (Exception e) {
          // ignore
        }
      }
      
      System.err.println("Nodes: " + SampleProcess.nodes);
    }
  }

  public String toString() {
    return nodeName;
  }
  
}
