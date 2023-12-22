package org.javlo.service.git;

public class GitAccessBean {

    private String url;
    private String login;
    private String password;
    private String branch;

    public GitAccessBean(String url, String login, String password, String branch) {
        this.url = url;
        this.login = login;
        this.password = password;
        this.branch = branch;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
