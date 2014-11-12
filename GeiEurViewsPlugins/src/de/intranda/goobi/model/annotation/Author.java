package de.intranda.goobi.model.annotation;

public class Author {

    private String name = "";
    private String organization = "";
    private String mail = "";
    private String url = "";

    public Author(String name, String organization, String mail, String url) {
        this.name = name;
        this.organization = organization;
        this.mail = mail;
        this.url = url;
    }

    public Author() {

    }

    public String getName() {
        return name;
    }

    public String getOrganization() {
        return organization;
    }

    public String getMail() {
        return mail;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
