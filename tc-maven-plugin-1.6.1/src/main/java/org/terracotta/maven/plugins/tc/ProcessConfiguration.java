/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/*
 *
 */
package org.terracotta.maven.plugins.tc;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.maven2.configuration.Configuration;
import org.codehaus.cargo.maven2.configuration.Container;

/**
 * @author Eugene Kuleshov
 */
public class ProcessConfiguration {
  private String nodeName;
  private String className;
  private String args;
  private String jvmArgs;
  private int count;
  private Map<String, String> properties = new HashMap<String, String>();
  private Container container;
  private int startNode;
  private Configuration configuration;

  public String getNodeName() {
    return nodeName;
  }

  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getArgs() {
    return args;
  }

  public void setArgs(String args) {
    this.args = args;
  }

  public String getJvmArgs() {
    return jvmArgs;
  }

  public void setJvmArgs(String jvmArgs) {
    this.jvmArgs = jvmArgs;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public void addProperty(String key, String value) {
    properties.put(key, value);
  }

  public Container getContainer() {
    return container;
  }

  public void setContainer(Container container) {
    this.container = container;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  public int getStartNode() {
    return startNode;
  }

  public void setStartNode(int startNode) {
    this.startNode = startNode;
  }

  public String toString() {
    return getNodeName() + " : " + getCount() + " : " + getClassName();
  }

}
