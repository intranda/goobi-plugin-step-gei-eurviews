package de.intranda.goobi.model.annotation;

public class Creator {

    private int processId;
    private int authorId;

    private String name = "";
    private String organization = "";
    private String mail = "";
    private String mail2 = "";
    private String mail3 = "";
    private String url = "";

    public Creator(String name, String organization, String mail, String mail2, String mail3, String url) {
        this.name = name;
        this.organization = organization;
        this.mail = mail;
        this.mail2 = mail2;
        this.mail3 = mail3;
        this.url = url;
    }

    public Creator(int processId) {
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

    public String getMail2() {
        return mail2;
    }

    public void setMail2(String mail2) {
        this.mail2 = mail2;
    }

    public String getMail3() {
        return mail3;
    }

    public void setMail3(String mail3) {
        this.mail3 = mail3;
    }
}
