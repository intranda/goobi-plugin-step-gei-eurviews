package de.intranda.goobi.model;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import lombok.Data;
import lombok.ToString;

@Data
public class SourceType extends SimpleMetadataObject {
        
    private String valueGer;
    private String valueEng;
    private String labelGer;
    private String labelEng;
    
    public String getValue() {
        return getValueEng();
    }
    
    public void setValue(String value) {
       setValueEng(value);
    }
    
    public boolean hasValue() {
        return StringUtils.isNotBlank(getValue());
    }
    
    
    public String getValue(Locale locale) {
        if(locale.getLanguage().equals(Locale.GERMAN.getLanguage())) {                
            return valueGer;
        } else {
            return valueEng;
        }
    }
    
    public String getLabel(Locale locale) {
        if(locale.getLanguage().equals(Locale.GERMAN.getLanguage())) {                
            return labelGer;
        } else {
            return labelEng;
        }
    }

    /**
     * @param valueGer
     * @param labelGer
     * @param valueEng
     * @param labelEng
     */
    public SourceType(String valueGer, String labelGer, String valueEng, String labelEng) {
        super(null);
        this.valueGer = valueGer;
        this.labelGer = labelGer;
        this.valueEng = valueEng;
        this.labelEng = labelEng;
    }
    
    public String toString() {
        return getValue();
    }
}

