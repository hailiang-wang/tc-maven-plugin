/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package org.terracotta.maven.plugins.tc;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.ResolutionNode;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * Plexus component used to handle dependency resolution
 * 
 * @author Eugene Kuleshov
 * 
 * @plexus.component role-hint="resolver"
 */
public class DsoArtifactResolverImpl extends AbstractLogEnabled implements DsoArtifactResolver {

  /**
   * Factory used to creates artifacts
   * 
   * @plexus.requirement
   */
  protected ArtifactFactory artifactFactory;

  /**
   * Resolver used to resolve artifacts
   * 
   * @plexus.requirement
   */
  protected ArtifactResolver artifactResolver;

  /**
   * For retrieval of artifact's metadata.
   * 
   * @plexus.requirement
   */
  private ArtifactMetadataSource metadataSource;

  /**
   * For collecting artifacts that match
   * 
   * @plexus.requirement
   */
  private ArtifactCollector artifactCollector;

  public File resolveArtifact(String groupId, String artifactId, String version, ArtifactRepository localRepository,
      List<ArtifactRepository> remoteRepositories) {
    // strip double quotes if present
    if (version.startsWith("\"")) {
      version = version.substring(1, version.length() - 1);
    }
    // hack to align OSGi version to Maven versions
    version = version.replaceAll("\\.SNAPSHOT", "-SNAPSHOT");

    // convert an OSGi exact version (which is specified with a range like [1.0.0,1.0.0]) to just 1.0.0 to appease maven
    if (version.startsWith("[") && version.endsWith("]")) {
      String[] split = version.replace("[", "").replace("]", "").split(",");
      if (split.length == 2 && split[0].equals(split[1])) {
        version = split[0];
      }
    }

    VersionRange versionRange = null;
    try {
      if ("(any-version)".equals(version)) {
        versionRange = VersionRange.createFromVersion("1.0.0");
      } else {
        versionRange = VersionRange.createFromVersionSpec(version);
      }
    } catch (InvalidVersionSpecificationException ex) {
      throw new RuntimeException("Invalid version spec " + version + " for " + groupId + ":" + artifactId, ex);
    }

    Artifact artifact = artifactFactory.createDependencyArtifact( //
        groupId, artifactId, versionRange, "jar", null, Artifact.SCOPE_RUNTIME);

    try {
      artifactResolver.resolve(artifact, remoteRepositories, localRepository);
      return artifact.getFile();

    } catch (AbstractArtifactResolutionException ex) {
      getLogger().error("Can't resolve artifact " + artifact.toString(), ex);
    }
    return null;
  }

  public File resolveArtifactInRange(String groupId, String artifactId, String version,
      ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories) {

    Artifact selectedArtifact = null;
    try {
      Set<Artifact> artifacts = new HashSet<Artifact>();
      version = version.replaceAll(".SNAPSHOT", "-SNAPSHOT");
      VersionRange versionRange = VersionRange.createFromVersionSpec(version);

      Artifact artifact = artifactFactory.createDependencyArtifact( //
          groupId, artifactId, versionRange, "jar", null, Artifact.SCOPE_RUNTIME);
      artifacts.add(artifact);

      Artifact originatingArtifact = createDummyOriginatingArtifact();

      ArtifactResolutionResult result = artifactCollector.collect(artifacts, originatingArtifact, localRepository,
          remoteRepositories, metadataSource, null, Collections.EMPTY_LIST);
      @SuppressWarnings("unchecked")
      Set<ResolutionNode> nodes = result.getArtifactResolutionNodes();
      ResolutionNode resultNode = nodes.iterator().next();
      selectedArtifact = resultNode.getArtifact();

      artifactResolver.resolve(selectedArtifact, remoteRepositories, localRepository);
      return selectedArtifact.getFile();

    } catch (InvalidVersionSpecificationException ex) {
      throw new RuntimeException("Invalid version range spec " + version + " for " + groupId + ":" + artifactId, ex);
    } catch (AbstractArtifactResolutionException ex) {
      getLogger().error("Can't resolve artifact " + selectedArtifact, ex);
    }
    return null;
  }

  public ArtifactResolutionResult resolveArtifact(Artifact filteredArtifact, Artifact providerArtifact,
      ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories)
      throws ArtifactResolutionException, ArtifactNotFoundException {
    ArtifactFilter filter = null;
    if (filteredArtifact != null) {
      filter = new ExcludesArtifactFilter(Collections.singletonList(filteredArtifact.getGroupId() + ":"
          + filteredArtifact.getArtifactId()));
    }

    Artifact originatingArtifact = createDummyOriginatingArtifact();

    return artifactResolver.resolveTransitively(Collections.singleton(providerArtifact), originatingArtifact,
        localRepository, remoteRepositories, metadataSource, filter);
  }

  private Artifact createDummyOriginatingArtifact() {
    return artifactFactory.createBuildArtifact("dummy", "dummy", "1.0", "jar");
  }

}
