package de.intranda.goobi.plugins;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Step;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.SimpleMetadataObject;
import de.intranda.goobi.model.annotation.Contribution;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.Topic;
import de.intranda.goobi.model.resource.Transcription;
import de.intranda.goobi.persistence.DatabaseManager;
import de.intranda.goobi.plugins.TeiExportPlugin.LanguageEnum;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Data
@Log4j
public class TeiAnnotationExportPlugin extends TeiExportPlugin {

	private Contribution contribution;
	private ResourceAnnotationPlugin dataPlugin;

	@Override
	public void initialize(Step step, String returnPath) {
		super.initialize(step, returnPath);
		try {
			this.contribution = DatabaseManager.getContribution(getProcess().getId());
			this.dataPlugin = new ResourceAnnotationPlugin();
			DatabaseManager.getContributionDescription(dataPlugin);
		} catch (SQLException e) {
			log.error(e);
		}
	}

	@Override
	protected Element createHeader(LanguageEnum language) {
		Element teiHeader = new Element("teiHeader", TeiExportPlugin.TEI);
		Element fileDesc = new Element("fileDesc", TeiExportPlugin.TEI);
		teiHeader.addContent(fileDesc);

		Element titleStmt = createTitleStmt(language);
		fileDesc.addContent(titleStmt);

		Element editionStmt = createEditionStmt(language);
		if (editionStmt != null) {
			fileDesc.addContent(editionStmt);
		}
		Element publicationStmt = createPublicationStmt();
		fileDesc.addContent(publicationStmt);

		Element sourceDesc = createSourceDesc(language);
		fileDesc.addContent(sourceDesc);

		Element encodingDesc = createEncodingDesc(language);
		teiHeader.addContent(encodingDesc);

		Element profileDesc = createProfileDesc(language);
		teiHeader.addContent(profileDesc);

		Element revisionDesc = createRevisionDesc();
		teiHeader.addContent(revisionDesc);

		return teiHeader;
	}
	
	@Override
	protected Element createTitleStmt(LanguageEnum language) {
		Element titleStmt = new Element("titleStmt", TEI);

            Element title = new Element("title", TEI);
            title.setAttribute("lang", language.getLanguage(), TeiExportPlugin.XML);
            title.setText(getTitle(language));
            titleStmt.addContent(title);
            
            for(Person person : getDataPlugin().getAuthorList()) {
            	Element author = new Element("author", TEI);
                Element persName = new Element("persName", TEI);
                Element forename = new Element("forename", TEI);
                Element surname = new Element("surname", TEI);
                surname.setText(person.getLastName());
                persName.addContent(surname);
                forename.setText(person.getFirstName());
                persName.addContent(forename);
                if (StringUtils.isNotBlank(person.getNormdataValue())) {
                    persName.setAttribute("ref", person.getNormdataValue());
                }
                author.addContent(persName);
                titleStmt.addContent(author);
            }
            
            List<SimpleMetadataObject> translatorList = isTranslation(language) ? getContribution().getTranslatorListTranslation() : getContribution().getTranslatorListOriginal();
            
                for (SimpleMetadataObject person : translatorList) {
                    Element editor = new Element("editor", TEI);
                    titleStmt.addContent(editor);
                    Element persName = new Element("persName", TEI);
//                    persName.setAttribute("ref", "edu.experts.id");
                    editor.addContent(persName);
                    editor.setAttribute("role", "translator");
                    persName.setText(person.getValue());
                }

                
        return titleStmt;
	}
	
	
	
	@Override
	protected Element createEditionStmt(LanguageEnum language) {
		Element editionStmt = null;
        if (StringUtils.isNotBlank(getDataPlugin().getEdition())) {
            editionStmt = new Element("editionStmt", TEI);

            Element edition = new Element("edition", TEI);
            editionStmt.addContent(edition);
            edition.setAttribute("n", getDataPlugin().getEdition());
            edition.setText(getDataPlugin().getEdition() + ", Version " + getDataPlugin().getEdition());
        }
        return editionStmt;
	}
	
	protected boolean isTranslation(LanguageEnum language) {
		return language.getLanguage().equals(getContribution().getLanguageTranslation());
	}
	
    protected Element createPublicationStmt() {
        Element publicationStmt = new Element("publicationStmt", TEI);

        Element publisher = new Element("publisher", TEI);
        publicationStmt.addContent(publisher);
        
        if(StringUtils.isNotBlank(getDataPlugin().getPublisher())) {        	
        	Element hostOrg = new Element("orgName", TEI);
        	hostOrg.setAttribute("role", "hostingInstitution");
        	hostOrg.setText(getDataPlugin().getPublisher());
        	publisher.addContent(hostOrg);
        }
        
        if(StringUtils.isNotBlank(getDataPlugin().getProject())) {        	
        	Element project = new Element("orgName", TEI);
        	project.setAttribute("role", "project");
        	project.setText(getDataPlugin().getProject());
        	publisher.addContent(project);
        }
        
        Date currentDate = new Date();
        Element date = new Element("date", TEI);
        String dateString = formatter.format(currentDate);
        date.setAttribute("when", dateString);
        date.setAttribute("type", "publication");
        date.setText(df.format(currentDate));
        publicationStmt.addContent(date);
        
        Element availability = new Element("availability", TEI);
        publicationStmt.addContent(availability);
        
        Element p = new Element("p", TEI);
        p.setText(getDataPlugin().getAvailability());
        availability.addContent(p);
        
        Element licence = new Element("licence", TEI);
        licence.setAttribute("target", "http://creativecommons.org/licenses/by-sa/3.0/");
        licence.setText(getDataPlugin().getLicence());
        availability.addContent(licence);

        //        Element idnoPid = new Element("idno", TEI);
        //        idnoPid.setAttribute("type", "URN");
        //        publicationStmt.addContent(idnoPid);
        //        idnoPid.setText("1234567890");
//        Element idnoUPIDCMDI = new Element("idno", TEI);
//        idnoUPIDCMDI.setAttribute("type", "PIDCMDI");
//        publicationStmt.addContent(idnoUPIDCMDI);
//        idnoUPIDCMDI.setText("0987654321");
        return publicationStmt;
    }
    
    protected Element createEncodingDesc(LanguageEnum language) {
        Element encodingDesc = new Element("encodingDesc", TEI);

        Element projectDesc = new Element("projectDesc", TEI);
        projectDesc.setAttribute("lang", language.getLanguage(), XML);
        encodingDesc.addContent(projectDesc);
            
        Element p = new Element("p", TEI);
        String text = "Kommentare dienen der Erläuterung und Interpretation der ausgesuchten Quelle, insbesondere wenn diese nicht selbsterklärend ist. Essays dienen der vertieften Interpretation von Quellen in ihrem Entstehungskontext (thematisch, räumlich, zeitlich, disziplinenspezifisch). Bildungsgeschichten liefern den nationalen bildungshistorischen Hintergrund für die Fächer Geschichte und Geographie (sowie möglichst auch Staatsbürgerkunde und Werteerziehung o.ä.) von um 1870 bis in die Gegenwart.";
        if(StringUtils.isNotBlank(getContext(language))) {
        	text = getContext(language);
        }
        p.setText(text);
        projectDesc.addContent(p);

        return encodingDesc;
    }
    
    @Override
    protected Element createProfileDesc(LanguageEnum currentLang) {
        Element profileDesc = new Element("profileDesc", TEI);
        Element langUsage = new Element("langUsage", TEI);
        profileDesc.addContent(langUsage);

            Element language = new Element("language", TEI);
            language.setAttribute("ident", currentLang.getLanguage());
            language.setText(currentLang.getLanguage());
            langUsage.addContent(language);


        Element abstr = new Element("abstract", TEI);
        abstr.setAttribute("lang", currentLang.getLanguage(), XML);
        profileDesc.addContent(abstr);
        
        String abstractText = getAbstract(currentLang);
        if(StringUtils.isNotBlank(abstractText)) {        	
        	Element p = new Element("p", TEI);
        	p.setText(abstractText);
        	abstr.addContent(p);
        }

        Element textClass = new Element("textClass", TEI);
        profileDesc.addContent(textClass);
        
        Element keywords = new Element("keywords", TEI);
        keywords.setAttribute("scheme", "WV.topics");
        if (currentLang.getLanguage().equals("ger")) {
        	keywords.setAttribute("lang", "ger", XML);
        } else {
        	keywords.setAttribute("lang", "eng", XML);
        }
        List<Topic> topics = getDataPlugin().getTopicList();
        for (Topic topic : topics) {
            for (Keyword currentKeyword : topic.getKeywordList()) {
                if (currentKeyword.isSelected()) {
                    Element term = new Element("term", TEI);
                    if (currentLang.getLanguage().equals("ger")) {
                        term.setText(topic.getNameDE() + " - " + currentKeyword.getKeywordNameDE());
                    } else {
                        term.setText(topic.getNameEN() + " - " + currentKeyword.getKeywordNameEN());
                    }

                    keywords.addContent(term);
                }
            }
        }
        textClass.addContent(keywords);

        Element classCode = new Element("classCode", TEI);
        classCode.setAttribute("scheme", "WV.textType");
        	classCode.setAttribute("lang", currentLang.getLanguage(), XML);
        classCode.setText(getDataPlugin().getContributionType());
        textClass.addContent(classCode);

        return profileDesc;
    }


	private String getContext(LanguageEnum language) {
		return language.getLanguage().equals(getContribution().getLanguageTranslation()) ? getContribution().getContentTranslation() : getContribution().getContextOriginal();
	}
	
	private String getAbstract(LanguageEnum language) {
		return language.getLanguage().equals(getContribution().getLanguageTranslation()) ? getContribution().getAbstractTranslation() : getContribution().getAbstractOriginal();
	}


	/**
	 * @param language
	 * @return
	 */
	private String getTitle(LanguageEnum language) {
		return getContribution().getLanguageTranslation().equals(language.getLanguage()) ? getContribution().getTitleTranslation() : getContribution().getTitleOriginal();
	}
	
	@Override
	protected Element createSourceDesc(LanguageEnum language){
		Element sourceDesc = new Element("sourceDesc", TeiExportPlugin.TEI);
		sourceDesc.addContent("<p>born digital</p>");
		return sourceDesc;
	}

	@Override
	protected Element createBody(LanguageEnum language) {
		
		Element body = new Element("body", TEI);
		
		String content;
		if(language.getLanguage().equals(contribution.getLanguageTranslation())) {
			content = contribution.getLanguageTranslation();
		} else {
			content = contribution.getLanguageOriginal();
		}
		
                String fulltext = "<div>" + convertBody(content) + "</div>";
                try {
                    StringReader reader = new StringReader(fulltext);
                    Document teiBody = new SAXBuilder().build(reader);
                    Element root = teiBody.getRootElement();
                    Element div = root.getChild("div", TEI);

                    div.detach();

                    body.addContent(div);
                } catch (JDOMException | IOException e) {
                    log.error(e);
                }
		
		return body;
	}

	@Override
	protected boolean teiExistsForLanguage(LanguageEnum language) {
		String langString = language.getLanguage();
		boolean contentExists = false;

		if (langString.equals(contribution.getLanguageOriginal())) {
			contentExists = StringUtils.isNotBlank(contribution.getContentOriginal());
		} else if (langString.equals(contribution.getLanguageTranslation())) {
			contentExists = StringUtils.isNotBlank(contribution.getContentTranslation());
		}
		return contentExists;
	}

}
