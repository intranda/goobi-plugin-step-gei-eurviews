package de.intranda.goobi.model.annotation;

public class Annotation {

    private Integer annotationId = null;
    
    private Integer processId;

    private String title;

    private String language;

    private String content;

    private String translator;

    private String reference;

    public Annotation(int processId) {
        this.processId = processId;
    }

    public int getProcessId() {
        return processId;
    }

    public String getTitle() {
        return title;
    }

    public String getLanguage() {
        return language;
    }

    public String getContent() {
        return content;
    }

    public String getTranslator() {
        return translator;
    }

    public String getReference() {
        return reference;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTranslator(String translator) {
        this.translator = translator;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Integer getAnnotationId() {
        return annotationId;
    }
    
    public void setAnnotationId(Integer annotationId) {
        this.annotationId = annotationId;
    }
    
}
