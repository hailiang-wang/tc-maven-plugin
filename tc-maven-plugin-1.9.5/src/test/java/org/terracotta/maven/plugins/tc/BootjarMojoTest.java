/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
/*
 * 
 */

package org.terracotta.maven.plugins.tc;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.DefaultArtifactHandlerStub;
import org.apache.maven.plugin.testing.stubs.StubArtifactRepository;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author Eugene Kuleshov
 */
public class BootjarMojoTest extends AbstractMojoTestCase {

  public void testBootjarCommandLine() throws Exception {
    String config = "<project>\n" + // 
        "  <build>\n" + //
        "    <plugins>\n" + // 
        "      <plugin>\n" + //
        "        <groupId>org.terracotta.maven.plugins</groupId>\n" + // 
        "        <artifactId>tc-maven-plugin</artifactId>\n" + //
        "        <configuration>\n" + //
        "          <verbose>true</verbose>\n" + // 
        "          <overwriteBootjar>true</overwriteBootjar>\n" + // 
        "          <jvmargs>-Xmx256m</jvmargs>\n" + //
        "          <bootJar>boot.jar</bootJar>\n" + //
        "          <config>config.xml</config>\n" + //
        "        </configuration>\n" + //
        "      </plugin>\n" + //
        "    </plugins>\n" + //
        "  </build>\n" + //
        "</project>";

    BootjarMojo bootjarMojo = createBootJarMojo(config);
    matches("(.*)java(.*) -Dcom.tc.l1.modules.repositories=(.*)repo "
        + "-Dtc.classpath=file:(.*) -cp com.tc.object.tools.BootJarTool -v -w -o (.*)boot.jar",
        bootjarMojo.createBootjarCommandLine().toString());
  }

  public void testBootjarDefaultCommandLine() throws Exception {
    String config = "<project>\n" + // 
    "  <build>\n" + //
    "    <plugins>\n" + // 
    "      <plugin>\n" + //
    "        <groupId>org.terracotta.maven.plugins</groupId>\n" + // 
    "        <artifactId>tc-maven-plugin</artifactId>\n" + //
    "        <configuration>\n" + //
    "          <bootJar>boot.jar</bootJar>\n" + //
    "          <config>config.xml</config>\n" + //
    "        </configuration>\n" + //
    "      </plugin>\n" + //
    "    </plugins>\n" + //
    "  </build>\n" + //
    "</project>";
    
    File tcConfig = new File("config.xml");
    tcConfig.createNewFile();
    tcConfig.deleteOnExit();
    BootjarMojo bootjarMojo = createBootJarMojo(config);
    matches("(.*)java(.*) -Dcom.tc.l1.modules.repositories=(.*)repo "
        + "-Dtc.classpath=file:(.*) -cp com.tc.object.tools.BootJarTool -o (.*)boot.jar -f (.*)config.xml",
        bootjarMojo.createBootjarCommandLine().toString());
    
//    C:\j2sdk1.4.2_13\jre/bin/java.exe 
//    -Dcom.tc.l1.modules.repositories=file:/C:/dev/terr/tc-maven-plugin/repo 
//    -Dtc.classpath=file:/C:/DOCUME~1/eu/LOCALS~1/Temp/tc-classpath7816.tmp 
//    -cp com.tc.object.tools.BootJarTool 
//    -o C:\dev\terr\tc-maven-plugin\boot.jar 
//    -f C:\dev\terr\tc-maven-plugin\config.xml
    
  }
  
  private BootjarMojo createBootJarMojo(String config) throws XmlPullParserException, IOException, Exception,
      IllegalAccessException {
    Xpp3Dom xpp3dom = Xpp3DomBuilder.build(new StringReader(config));
    PlexusConfiguration pluginConfiguration = extractPluginConfiguration("tc-maven-plugin", xpp3dom);

    BootjarMojo bootjarMojo = new BootjarMojo();
    // BootjarMojo bootjarMojo = (BootjarMojo) lookupMojo("bootjar", "src/test/java/org/terracotta/maven/plugins/tc/bootjarMojo.xml");

    configureMojo(bootjarMojo, pluginConfiguration);
    
    setVariableValueToObject(bootjarMojo, "localRepository", new StubArtifactRepository("repo"));

    ArrayList<DefaultArtifact> pluginArtifacts = new ArrayList<DefaultArtifact>();
    pluginArtifacts.add(new DefaultArtifact("org.terracotta", "terracotta", //
        VersionRange.createFromVersion("2.8.0-SNAPSHOT"), null, "jar", null, new DefaultArtifactHandlerStub("jar")));
    setVariableValueToObject(bootjarMojo, "pluginArtifacts", pluginArtifacts);

    return bootjarMojo;
  }

  private void matches(String expectedPattern, String value) {
    assertTrue(value, Pattern.matches(expectedPattern, value));
  }

}
