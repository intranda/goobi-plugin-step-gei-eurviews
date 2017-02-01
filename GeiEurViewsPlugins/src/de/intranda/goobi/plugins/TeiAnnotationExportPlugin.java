package de.intranda.goobi.plugins;


import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Step;
import org.jdom2.Element;


import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.SimpleMetadataObject;
import de.intranda.goobi.model.annotation.Contribution;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.Topic;
import de.intranda.goobi.persistence.DatabaseManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Data
@EqualsAndHashCode(callSuper=false)
@Log4j
public class TeiAnnotationExportPlugin extends TeiExportPlugin {

	public static final String DEFAULT_TEXT_CONTEXT = "Kommentare dienen der Erläuterung und Interpretation der ausgesuchten Quelle, insbesondere wenn diese nicht selbsterklärend ist. Essays dienen der vertieften Interpretation von Quellen in ihrem Entstehungskontext (thematisch, räumlich, zeitlich, disziplinenspezifisch). Bildungsgeschichten liefern den nationalen bildungshistorischen Hintergrund für die Fächer Geschichte und Geographie (sowie möglichst auch Staatsbürgerkunde und Werteerziehung o.ä.) von um 1870 bis in die Gegenwart.";
	
	private List<Contribution> contributionList;
	private ResourceAnnotationPlugin dataPlugin;

	private static final String PLUGIN_NAME = "Gei_WorldViews_Annotation_RtfToTeiExport";

	@Override
	public void initialize(Step step, String returnPath) {
		super.initialize(step, returnPath);
		try {
			this.contributionList = DatabaseManager.getContributions(getProcess().getId());
			this.dataPlugin = new ResourceAnnotationPlugin();
			this.dataPlugin.setProcessId(getProcess().getId());
			this.dataPlugin.initialize(getStep(), "");
			DatabaseManager.getContributionDescription(dataPlugin);
		} catch (SQLException e) {
			log.error(e);
		}
	}

	@Override
	public String getTitle() {
		return PLUGIN_NAME;
	}

	@Override
	protected Element createHeader(LanguageEnum language) {
		Element teiHeader = new Element("teiHeader", TeiExportPlugin.TEI);
		Element fileDesc = new Element("fileDesc", TeiExportPlugin.TEI);
		teiHeader.addContent(fileDesc);

		Element titleStmt = createTitleStmt(language);
		fileDesc.addContent(titleStmt);

		Element editionStmt = createEditionStmt(language, getDataPlugin().getEdition());
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

		for (Person person : getDataPlugin().getAuthorList()) {
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

		Contribution contribution = getContribution(language);
		if (contribution != null) {

			List<SimpleMetadataObject> translatorList = getContribution(language).getTranslatorList();

			for (SimpleMetadataObject person : translatorList) {
				Element editor = new Element("editor", TEI);
				titleStmt.addContent(editor);
				Element persName = new Element("persName", TEI);
				// persName.setAttribute("ref", "edu.experts.id");
				editor.addContent(persName);
				editor.setAttribute("role", "translator");
				persName.setText(person.getValue());
			}

		}
		return titleStmt;
	}

	public Contribution getContribution(LanguageEnum language) {
		for (Contribution contribution : contributionList) {
			if (contribution.getLanguage().equals(language.getLanguage())) {
				return contribution;
			}
		}
		return null;
	}

	protected Element createPublicationStmt() {
		Element publicationStmt = new Element("publicationStmt", TEI);

		Element publisher = new Element("publisher", TEI);
		publicationStmt.addContent(publisher);

		if (StringUtils.isNotBlank(getDataPlugin().getPublisher())) {
			Element hostOrg = new Element("orgName", TEI);
			hostOrg.setAttribute("role", "hostingInstitution");
			hostOrg.setText(getDataPlugin().getPublisher());
			publisher.addContent(hostOrg);
		}

		if (StringUtils.isNotBlank(getDataPlugin().getProject())) {
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

		// Element idnoPid = new Element("idno", TEI);
		// idnoPid.setAttribute("type", "URN");
		// publicationStmt.addContent(idnoPid);
		// idnoPid.setText("1234567890");
		// Element idnoUPIDCMDI = new Element("idno", TEI);
		// idnoUPIDCMDI.setAttribute("type", "PIDCMDI");
		// publicationStmt.addContent(idnoUPIDCMDI);
		// idnoUPIDCMDI.setText("0987654321");
		return publicationStmt;
	}

	protected Element createEncodingDesc(LanguageEnum language) {
		Element encodingDesc = new Element("encodingDesc", TEI);

		Element projectDesc = new Element("projectDesc", TEI);
		projectDesc.setAttribute("lang", language.getLanguage(), XML);
		encodingDesc.addContent(projectDesc);

		Element p = new Element("p", TEI);
		String text = DEFAULT_TEXT_CONTEXT;
		if (StringUtils.isNotBlank(getContext(language))) {
			text = getContext(language);
		}
		createTextElement(text, p);
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

		String abstractText = getAbstrakt(currentLang);
		if (StringUtils.isNotBlank(abstractText)) {
			Element p = new Element("p", TEI);
			createTextElement(abstractText, p);
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

	public String getContext(LanguageEnum language) {
		return getContribution(language).getContext();
	}

	public String getAbstrakt(LanguageEnum language) {
		return getContribution(language).getAbstrakt();
	}

	public String getContent(LanguageEnum language) {
		return getContribution(language).getContent();
	}

	/**
	 * @param language
	 * @return
	 */
	private String getTitle(LanguageEnum language) {
		return getContribution(language).getTitle();
	}

	@Override
	protected Element createSourceDesc(LanguageEnum language) {
		Element sourceDesc = new Element("sourceDesc", TeiExportPlugin.TEI);
		Element p = new Element("p", TEI);
		p.addContent("born digital");
		sourceDesc.addContent(p);
		return sourceDesc;
	}

	@Override
	protected Element createBody(LanguageEnum language) {

		Element body = new Element("body", TEI);

		Contribution contribution = getContribution(language);

		String content = contribution.getContent();

		Element div = new Element("div", TEI);
		createTextElement(convertBody(content), div);
		body.addContent(div);

		return body;
	}

	@Override
	protected boolean teiExistsForLanguage(LanguageEnum language) {
		Contribution contribution = getContribution(language);
		return contribution != null && StringUtils.isNotBlank(contribution.getContent());
	}

}
