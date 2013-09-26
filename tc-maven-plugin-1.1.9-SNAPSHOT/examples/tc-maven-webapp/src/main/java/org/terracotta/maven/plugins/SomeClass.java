/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package org.terracotta.maven.plugins;

public class SomeClass {

  private int count;
  
  public String saySomething() {
    synchronized(this) {
      return "Hello World! " + count++;
    }
  }
  
}
