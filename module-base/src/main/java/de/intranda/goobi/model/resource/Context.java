package de.intranda.goobi.model.resource;

import org.apache.commons.lang.StringUtils;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import lombok.Data;

public @Data class Context {

    private static final PolicyFactory RICH_POLICY =
            new HtmlPolicyBuilder()
                    .allowElements("b", "i", "em", "strong", "br", "p", "ul", "ol", "li")
                    .toFactory();

    private Integer processID;
    private Integer descriptionID;
    private String language;
    private String languageCode;
    private String bookInformation;
    private String shortDescription;
    private String longDescription;
    private String projectContext;
    private String selectionMethod;

    public String getBookInformation() {
        return bookInformation == null ? null : RICH_POLICY.sanitize(bookInformation);
    }

    public String getShortDescription() {
        return shortDescription == null ? null : RICH_POLICY.sanitize(shortDescription);
    }

    public String getLongDescription() {
        return longDescription == null ? null : RICH_POLICY.sanitize(longDescription);
    }

    public String getProjectContext() {
        return projectContext == null ? null : RICH_POLICY.sanitize(projectContext);
    }

    public String getSelectionMethod() {
        return selectionMethod == null ? null : RICH_POLICY.sanitize(selectionMethod);
    }

    public Context(int processId) {
        this.processID = processId;
    }

    public Context(int processId, String language) {
        this.processID = processId;
        this.language = language;
    }

    public String getLanguageCode() {
        if (isOriginalLanguage()) {
            if (StringUtils.isNotBlank(languageCode)) {
                return languageCode;
            } else {
                return "";
            }
        } else {
            return language;
        }
    }

    public boolean isOriginalLanguage() {
        return "original".equalsIgnoreCase(language);
    }

    public void setOriginalLanguage(boolean original) {
        if (original) {
            if (StringUtils.isBlank(languageCode)) {
                languageCode = language;
            }
            language = "original";
        } else {
            if (!StringUtils.isBlank(languageCode)) {
                language = languageCode;
            }
            languageCode = null;
        }
    }

}
