/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package org.terracotta.maven.plugins.tc;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
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

  public File resolveArtifact(String groupId, String artifactId, String version, ArtifactRepository localRepository,
      List remoteRepositories) {
    // TODO why do we need to do this?
    if (version.startsWith("\"")) {
      version = version.substring(1, version.length() - 1);
    }
    // hack to align OSGi version to Maven versions
    if (version.endsWith(".SNAPSHOT")) {
      version = version.substring(0, version.indexOf(".SNAPSHOT")) + "-SNAPSHOT";
    }

    VersionRange versionRange = null;
    try {
      if ("(any-version)".equals(version) || version == null) {
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

  public ArtifactResolutionResult resolveArtifact(Artifact filteredArtifact, Artifact providerArtifact,
      ArtifactRepository localRepository, List remoteRepositories) throws ArtifactResolutionException,
      ArtifactNotFoundException {
    ArtifactFilter filter = null;
    if (filteredArtifact != null) {
      filter = new ExcludesArtifactFilter(Collections.singletonList(filteredArtifact.getGroupId() + ":"
          + filteredArtifact.getArtifactId()));
    }

    Artifact originatingArtifact = artifactFactory.createBuildArtifact("dummy", "dummy", "1.0", "jar");

    return artifactResolver.resolveTransitively(Collections.singleton(providerArtifact), originatingArtifact,
        localRepository, remoteRepositories, metadataSource, filter);
  }

}
