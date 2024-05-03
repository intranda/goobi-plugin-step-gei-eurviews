package de.intranda.goobi.model.conversion;

public class SimpleFootnote implements Footnote {

    private String referenceRegex;
    private String noteRegex;
    
    
    
    /**
     * @param referenceRegex
     * @param noteRegex
     */
    public SimpleFootnote(String referenceRegex, String noteRegex) {
        super();
        this.referenceRegex = referenceRegex;
        this.noteRegex = noteRegex;
    }

    @Override
    public String getReferenceRegex() {
        return referenceRegex;
    }

    @Override
    public String getNoteRegex(String number) {
        String regex = this.noteRegex.replace("§", number);
        return regex;
    }

}
