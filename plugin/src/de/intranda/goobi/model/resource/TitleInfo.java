package de.intranda.goobi.model.resource;

import org.apache.commons.lang.StringUtils;

import lombok.Data;
import lombok.ToString;

@Data
public class TitleInfo {
	
	private String title;
	private String subTitle;
	private String language;
	private String translationGER;
	private String translationENG;
	private String numbering;
	
	public boolean isEmpty() {
		return StringUtils.isBlank(title);
	}
	
	public boolean hasSubTitle() {
	    return StringUtils.isNotBlank(subTitle);
	}
	
	public boolean isGerman() {
		return language != null && (language.equalsIgnoreCase("ger") || language.equalsIgnoreCase("de"));
	}
	
	public boolean isEnglish() {
		return language != null && (language.equalsIgnoreCase("eng") || language.equalsIgnoreCase("en"));
	}
	
	public boolean hasLanguage() {
	    return StringUtils.isNotBlank(language);
	}
	
	public boolean hasNumbering() {
	    return StringUtils.isNotBlank(numbering);
	}
	
	public boolean hasGermanTranslation() {
        return StringUtils.isNotBlank(translationGER);
    }
	
	public boolean hasEnglishTranslation() {
        return StringUtils.isNotBlank(translationENG);
    }
	
	public String getLabel() {
	    if(StringUtils.isNotBlank(getTitle())) {
	        return getTitle();
	    } else if(StringUtils.isNotBlank(getTranslationENG())) {
	        return getTranslationENG();
	    } else {
	        return getTranslationENG();
	    }
	}
	
	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    if(StringUtils.isNotBlank(numbering)) {
	        sb.append(numbering).append(" : ");
	    }
	    sb.append(title);
	    if(StringUtils.isNotBlank(subTitle)) {
	        sb.append(" : ").append(subTitle);
	    }
	    return sb.toString();
	}

}
