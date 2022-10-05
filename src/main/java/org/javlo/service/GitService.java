package org.javlo.service;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

public class GitService {

	public static void main(String[] args) throws InvalidRemoteException, TransportException, GitAPIException {
		Git git = Git.cloneRepository().setURI("https://github.com/pvandermaesen/template-bootstrap5.0.git").setDirectory(new File("c:/trans/git")).call();
	}

}
