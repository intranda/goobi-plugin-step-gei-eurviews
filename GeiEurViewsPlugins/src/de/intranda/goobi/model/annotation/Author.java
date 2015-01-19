package de.intranda.goobi.model.annotation;

public class Author {

    private int processId;
    private int authorId;

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

    public Author(int processId) {
        this.processId = processId;
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

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }
    
    public int getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }
}
