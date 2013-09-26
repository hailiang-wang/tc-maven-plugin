/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/*
 * 
 */
package org.terracotta.maven.plugins.tc.test;

public class SampleUtils {

  public static int getTotalNodes() {
    try {
      return Integer.parseInt(System.getProperty("tc.numberOfNodes", "0"));
    } catch(Exception e) {
      return 0;
    }
  }

  
}
