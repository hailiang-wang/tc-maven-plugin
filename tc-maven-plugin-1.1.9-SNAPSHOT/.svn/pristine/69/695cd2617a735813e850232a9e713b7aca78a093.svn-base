/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/**
 * 
 */
package org.terracotta.maven.plugins.tc;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.tc.config.schema.NewCommonL1Config;
import com.tc.config.schema.NewCommonL2Config;
import com.tc.config.schema.NewConfig;
import com.tc.config.schema.dynamic.ParameterSubstituter;
import com.tc.config.schema.setup.ConfigurationSetupException;

/**
 * Clean DSO data folder and logs
 * 
 * @author Eugene Kuleshov
 * 
 * @goal clean
 * @execute phase="validate"
 * @phase clean
 * @configurator override
 */
public class DsoCleanMojo extends AbstractDsoServerMojo {

  /**
   * Fail build on error
   * 
   * @parameter expression="${failOnError}" default-value="true"
   */
  private boolean failOnError;

  public void execute() throws MojoExecutionException, MojoFailureException {
    NewCommonL1Config clientConfig;
    NewCommonL2Config serverConfig;
    try {
      serverConfig = getServerConfig(serverName);
      clientConfig = getClientConfig();
    } catch (ConfigurationSetupException ex) {
      throw new MojoExecutionException("Unable to read server configuration", ex);
    }

    // make sure no server is running
    String status = null;
    try {
      status = getServerStatus(getJMXUrl(serverConfig));
      getLog().info("Server Status: " + status);
    } catch (Exception ex) {
      // expected error when server is not running
    }
    if ("OK".equals(status)) {
      stop(true);
    }

    LogManager.shutdown();  // a hack to close all the logs opened while reading configuration 

    {
      getLog().info("------------------------------------------------------------------------");

      String logsValue = getValue(clientConfig, "logs");
      getLog().info("Client logs directory template: " + logsValue);

      String statsValue = getValue(clientConfig, "statistics");
      getLog().info("Client stat directory template: " + statsValue);
      
      List startables = getStartables();
      if(startables.isEmpty()) {
        deleteClientFolders(clientConfig, logsValue, statsValue);
      } else {
        for (Iterator it = startables.iterator(); it.hasNext();) {
          Startable startable = (Startable) it.next();
          String nodeName = startable.getNodeName();
          System.setProperty("tc.nodeName", nodeName);
          deleteClientFolders(clientConfig, logsValue, statsValue);
          System.getProperties().remove("tc.nodeName");
        }
      }
    }
    
    {
      String dataValue = getValue(serverConfig, "data");
      String data = getLocation(dataValue, serverConfig.dataPath().getFile());
  
      getLog().info("------------------------------------------------------------------------");
      getLog().info("Server data directory template: " + dataValue);
      getLog().info("Deleting server data directory: " + data);
      delete("data", data);
    }
    
    {
      String logsValue = getValue(serverConfig, "logs");
      String logs = getLocation(logsValue, serverConfig.logsPath().getFile());
  
      getLog().info("------------------------------------------------------------------------");
      getLog().info("Server logs directory template: " + logsValue);
      getLog().info("Deleting server logs directory: " + logs);
      delete("logs", logs);
    }
    
    {
      String statsValue = getValue(serverConfig, "statistics");
      String stats = getLocation(statsValue, serverConfig.statisticsPath().getFile());
      
      getLog().info("------------------------------------------------------------------------");
      getLog().info("Server statistics directory template: " + statsValue);
      getLog().info("Deleting server statistics directory: " + stats);
      delete("statistics", stats);
    }
  }

  private void deleteClientFolders(NewCommonL1Config clientConfig, String logsValue, String statsValue)
      throws MojoFailureException {
    {
      String logs = getLocation(logsValue, clientConfig.logsPath().getFile());
      getLog().info("Deleting client logs directory: " + logs);
      delete("logs", logs);
    }
    {
      String stats = getLocation(statsValue, clientConfig.statisticsPath().getFile());
      getLog().info("Deleting client stat directory: " + stats);
      delete("statistics", stats);
    }
  }

  private String getLocation(String template, File file) {
    if (template == null) {
      return file.getAbsolutePath();
    } else {
      return ParameterSubstituter.substitute(template);
    }
  }

  private void delete(String name, String dir) throws MojoFailureException {
    try {
      FileUtils.deleteDirectory(new File(dir));
    } catch (IOException ex) {
      String msg = "Can't delete " + name + " directory " + dir;
      getLog().error(msg, ex);
      if (failOnError) {
        throw new MojoFailureException(msg);
      }
    }
  }

  private String getValue(NewConfig config, String elementName) {
    XmlObject bean = config.getBean();
    if(bean==null) {
      return null;
    }
    
    Node node = bean.getDomNode();
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE && elementName.equals(child.getLocalName())) {
        Node textNode = child.getFirstChild();
        if (textNode.getNodeType() == Node.TEXT_NODE) {
          return ((Text) textNode).getData();
        }
      }
    }
    return null;
  }

}
