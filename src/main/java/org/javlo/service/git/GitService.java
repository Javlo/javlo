package org.javlo.service.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class GitService {

    public void checkOut(GitAccessBean gitAccesss, File file) throws GitAPIException {
        if (!file.exists()) {
            file.mkdirs();
        }
        Git git = Git.cloneRepository()
                .setURI(gitAccesss.getUrl())
                .setDirectory(file)
                .call();
    }

    public static void main(String[] args) throws GitAPIException {
        GitService service = new GitService();
        GitAccessBean access = new GitAccessBean("https://github.com/Javlo/BallImpact.git", null, null, null);
        service.checkOut(access, new File("c:/trans/git-test"));
    }

}
