package org.javlo.user;

import java.util.Set;

public class UserInfoBean {

    private String login;
    private String firstname;
    private String lastname;
    private String email;
    private Set<String> roles;

    public UserInfoBean() {
    }

    public UserInfoBean(IUserInfo userInfo) {
        this.login = userInfo.getLogin();
        this.firstname = userInfo.getFirstName();
        this.lastname = userInfo.getLastName();
        this.roles = userInfo.getRoles();
        this.email = userInfo.getEmail();
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
