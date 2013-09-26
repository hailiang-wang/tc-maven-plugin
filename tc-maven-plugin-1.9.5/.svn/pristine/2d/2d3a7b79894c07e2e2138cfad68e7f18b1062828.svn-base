/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package org.terracotta.maven.plugins.tc;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;

public interface DsoArtifactResolver {
  
  String ROLE = DsoArtifactResolver.class.getName();

  public File resolveArtifact(String groupId, String artifactId, String version, ArtifactRepository localRepository,
      List<ArtifactRepository> remoteRepositories);

  public File resolveArtifactInRange(String groupId, String artifactId, String version,
      ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories);

  public ArtifactResolutionResult resolveArtifact(Artifact filteredArtifact, Artifact providerArtifact,
      ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories) throws ArtifactResolutionException,
      ArtifactNotFoundException;


}
