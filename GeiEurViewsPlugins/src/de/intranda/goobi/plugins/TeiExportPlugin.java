package de.intranda.goobi.plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import javax.xml.stream.XMLInputFactory;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.intranda.goobi.model.HtmlToTEIConvert;
import de.intranda.goobi.model.HtmlToTEIConvert.ConverterMode;
import de.intranda.goobi.model.KeywordHelper;
import de.intranda.goobi.model.Location;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.Corporation;
import de.intranda.goobi.model.SimpleMetadataObject;
import de.intranda.goobi.model.resource.BibliographicMetadata;
import de.intranda.goobi.model.resource.Context;
import de.intranda.goobi.model.resource.Image;
import de.intranda.goobi.model.resource.Keyword;
import de.intranda.goobi.model.resource.ResouceMetadata;
import de.intranda.goobi.model.resource.Topic;
import de.intranda.goobi.model.resource.Transcription;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Data
@Log4j
public class TeiExportPlugin implements IStepPlugin, IPlugin {

	public static final String DEFAULT_TEXT_CONTEXT = "Ziel ist es, Selbstverortungen und Alteritätskonzept zu erheben sowie Auszüge aus Schulbücher aus aller Welt im Hinblick auf Vorstellungen von übernationalen Zugehörigkeiten und Teilhabe an historisch prägenden Ereignissen und Prozessen abzubilden. Mit dem Quellenmaterial wird es NutzerInnen ermöglicht, transnationale, regionale und interkulturelle Verflechtungen zu erschließen. Wir fokussieren in der Projektphase 2016-22 vor allem auf Vorstellungen von Europäizität sowie alternativen Sinnstiftungsangeboten, auf Gesellschaftskonzepte und Modernitätsverständnisse.";
	public static final String DEFAULT_TEXT_AVAILABILITY = "Available with prior consent of depositor (GEI) for purposes of academic research and teaching only.";
	public static final String DEFAULT_TEXT_SAMPLING = "Quellenauszüge sind im Hinblick auf Repräsentation, Deutungsmuster und/ oder Perspektive der Darstellung möglichst markant. Es sind Darstellungen, die in besonders weit verbreiteten und genutzten Schulbüchern vermittelt werden oder aber als Sonderpositionierungen (inhaltlich oder z.B. auch didaktisch motiviert) gekennzeichnet werden können. Damit den NutzerInnen der Edition die Einordnung der jeweiligen Auszüge erleichtert wird, werden die Textanteile durch Kooperationspartner und/ oder Redaktion (mit wissenschaftlicher und Regionalexpertise) kontextualisiert und kommentiert sowie nah am Ausgangstext ins Deutsche und Englische übersetzt.";

	public enum LanguageEnum {

		GERMAN("ger", Locale.GERMAN), ENGLISH("eng", Locale.ENGLISH), ORIGINAL("original", Locale.ENGLISH);

		@Getter
		private String language;
		@Getter
		private Locale locale;

		private LanguageEnum(String language, Locale locale) {
			this.language = language;
			this.locale = locale;
		}
	}

	private static final String PLUGIN_NAME = "Gei_WorldViews_RtfToTeiExport";

	public static final Namespace TEI = Namespace.getNamespace("http://www.tei-c.org/ns/1.0");
	protected static final Namespace XML = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");

	private Step step;
	private Process process;
	private String returnPath;

	private BibliographicMetadata bibliographicData;
	private ResouceMetadata resouceMetadata;
	private List<Context> descriptionList;
	private List<Transcription> transcriptionList;
	private List<Image> currentImages;
	private List<Topic> topicList;

	protected java.text.SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
	protected DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);

	public PluginType getType() {
		return PluginType.Step;
	}

	@Override
	public String getTitle() {
		return PLUGIN_NAME;
	}

	@Override
	public void initialize(Step step, String returnPath) {
		this.step = step;
		this.returnPath = returnPath;
		this.process = step.getProzess();

		try {
			resouceMetadata = WorldViewsDatabaseManager.getResourceMetadata(process.getId());
			if (resouceMetadata != null) {
				bibliographicData = WorldViewsDatabaseManager
						.getBibliographicData(resouceMetadata.getBibliographicDataId());
			}
			descriptionList = WorldViewsDatabaseManager.getDescriptionList(process.getId());
			transcriptionList = WorldViewsDatabaseManager.getTransciptionList(process.getId());
			currentImages = WorldViewsDatabaseManager.getImages(process.getId());

			topicList = KeywordHelper.getInstance().initializeKeywords();

			List<StringPair> keywordList = WorldViewsDatabaseManager.getKeywordList(process.getId());
			for (StringPair sp : keywordList) {
				for (Topic topic : topicList) {
					if (topic.getNameDE().equals(sp.getOne())) {
						for (Keyword keyword : topic.getKeywordList()) {
							if (keyword.getKeywordNameDE().equals(sp.getTwo())) {
								keyword.setSelected(true);
								break;
							}
						}

					}
				}
			}
		} catch (SQLException e) {
			log.error(e);
		}

	}

	@Override
	public boolean execute() {
		File teiDirectory = getTeiDirectory();
		if (teiDirectory == null) {
			logError("Unable to create directory for TEI");
			return false;
		}
		boolean fileCreated = false;
		List<String> languagesWritten = new ArrayList<>();
		for (LanguageEnum language : EnumSet.allOf(LanguageEnum.class)) {
			if (teiExistsForLanguage(language)) {
				File teiFile = new File(teiDirectory,
						getStep().getProzess().getTitel() + "_" + language.getLanguage() + ".xml");
				try {
					Document oldTeiDocument = null;
					try {
						oldTeiDocument = getDocumentFromFile(teiFile);
					} catch (IOException | JDOMException e) {
						log.error(e);
						logError("Error reading existing tei file " + teiFile);
						return false;
					}
					Document teiDocument = createTEiDocForLanguage(language);
					if (oldTeiDocument != null) {
						Element text = oldTeiDocument.getRootElement().getChild("text", null);
						text.detach();
						teiDocument.getRootElement().removeChild("text", null);
						teiDocument.getRootElement().addContent(text);
					}

					XMLOutputter xmlOutput = new XMLOutputter();
					xmlOutput.setFormat(Format.getPrettyFormat());
					xmlOutput.output(teiDocument, new FileWriter(teiFile));
					fileCreated = true;
					languagesWritten.add(language.getLanguage());
				} catch (JDOMException e) {
					log.error(e);
					logError("Invalid xml in editable fields");
					return false;
				} catch (IOException e) {
					log.error(e);
					logError("Error writing TEI to file");
					return false;
				}
			}
		}

		String symLinkPath = ConfigPlugins.getPluginConfig(this).getString("linkFilesTo");
		if (StringUtils.isNotBlank(symLinkPath)) {
			try {
				File symLink = new File(symLinkPath, teiDirectory.getName());
				if (!symLink.exists()) {
					Files.createSymbolicLink(symLink.toPath(), teiDirectory.toPath());
				}
			} catch (IOException e) {
				log.error(e);
				logError("Error creating symlink at " + symLinkPath
						+ ". The folder may not exist or have limited access.");
				return false;
			}
		}

		if (!fileCreated) {
			logError("Missing content for all languages");
			return false;
		}

		String languagesWrittenMessage = StringUtils.join(languagesWritten, ", ");
		handleSuccess(languagesWrittenMessage);
		return true;
	}

	private Document getDocumentFromFile(File file) throws JDOMException, IOException {
		if (file.isFile()) {
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(file);
			return document;
		} else {
			return null;
		}
	}

	private void handleSuccess(String message) {
		LogEntry entry = new LogEntry();
		entry.setContent("TEI file(s) written");
		entry.setType(LogType.INFO);
		getProcessLog().add(entry);
		entry.setCreationDate(new Date());
		entry.setProcessId(getProcess().getId());
		ProcessManager.saveLogEntry(entry);
		Helper.setMeldung(Helper.getTranslation("success_writing_tei", message));
	}

	/**
	 * @param errorMessage
	 */
	private void logError(String errorMessage) {
		LogEntry errorEntry = new LogEntry();
		errorEntry.setContent("Failed to create TEI documents: " + errorMessage);
		errorEntry.setType(LogType.ERROR);
		getProcessLog().add(errorEntry);
		errorEntry.setCreationDate(new Date());
		errorEntry.setProcessId(getProcess().getId());
		ProcessManager.saveLogEntry(errorEntry);
		Helper.setFehlerMeldung(Helper.getTranslation("error_writing_tei", errorMessage));
	}

	/**
	 * 
	 * 
	 * @return the process directory for tei transcription, creating it if it
	 *         doesn't exist
	 */
	private File getTeiDirectory() {
		try {
			File dir = new File(getStep().getProzess().getOcrDirectory(), getStep().getProzess().getTitel() + "_tei");
			if (!dir.isDirectory() && !dir.mkdirs()) {
				log.error("Failed to create ocr-directory for process " + getStep().getProcessId());
				return null;
			}
			return dir;
		} catch (SwapException | DAOException | IOException | InterruptedException e) {
			log.error("Failed to get ocr-directory for process " + getStep().getProcessId());
			return null;
		}
	}

	protected boolean teiExistsForLanguage(LanguageEnum language) {
		for (Transcription transcription : transcriptionList) {
			if (transcription.getLanguage().equals(language.getLanguage())
					&& StringUtils.isNotBlank(transcription.getTranscription())) {
				return true;
			}
		}
		return false;
	}

	protected Document createTEiDocForLanguage(LanguageEnum language) throws JDOMException, IOException {
		Document teiDocument = new Document();
		Element teiRoot = new Element("TEI", TEI);
		teiDocument.setRootElement(teiRoot);
		teiRoot.setAttribute("id", "GEI-textbooks", XML);
		teiRoot.setAttribute("version", "5.0");

		Element teiHeader = createHeader(language);
		teiRoot.addContent(teiHeader);

		Element text = new Element("text", TEI);
		text.setAttribute("lang", getTranscription(language).getLanguageCode(), XML);
		teiRoot.addContent(text);

		Element body = createBody(language);
		text.addContent(body);

		return teiDocument;
	}

	/**
	 * @param language
	 * @throws IOException
	 * @throws JDOMException
	 */
	protected Element createBody(LanguageEnum language) throws JDOMException, IOException {

		Element body = new Element("body", TEI);

		for (Transcription transcription : transcriptionList) {
			if (transcription.getLanguage().equals(language.getLanguage())) {
				Element div = new Element("div", TEI);
				createTextElement(convertBody(transcription.getTranscription()), div);
				body.addContent(div);

			}
		}

		return body;
	}

	protected Element createTextElement(String text, Element wrapper) throws JDOMException, IOException {
		text = HtmlToTEIConvert.removeUrlEncoding(text);
		StringReader reader = new StringReader("<div>" + text + "</div>");
		Document doc = new SAXBuilder().build(reader);
		Element root = doc.getRootElement();
		List<Content> content = root.removeContent((Filter<Content>) (Filters.element().or(Filters.text())));
		while (content.size() == 1 && content.get(0) instanceof Element) {
			content = ((Element) content.get(0))
					.removeContent((Filter<Content>) (Filters.element().or(Filters.text())));
			removeEmptyText(content);
		}
		wrapper.addContent(content);
		return wrapper;
	}

	/**
	 * @param content
	 */
	private void removeEmptyText(List<Content> content) {
		ListIterator<Content> iterator = content.listIterator();
		while (iterator.hasNext()) {
			Content c = iterator.next();
			if (c instanceof Text) {
				if (StringUtils.isBlank(((Text) c).getText())) {
					iterator.remove();
				}
			}
		}
	}

	protected String convertBody(String text) {
		return new HtmlToTEIConvert(ConverterMode.resource).convert(text);
	}

	protected Element createTitleStmt(LanguageEnum language) {
		Element titleStmt = new Element("titleStmt", TEI);

		boolean titleWritten = false;
		if ((language.equals(LanguageEnum.GERMAN)) && StringUtils.isNotBlank(bibliographicData.getMaintitleGerman())) {
			Element title = new Element("title", TEI);
			title.setAttribute("lang", "ger", XML);
			title.setText(bibliographicData.getMaintitleGerman());
			titleStmt.addContent(title);
			titleWritten = true;
		} else if ((language.equals(LanguageEnum.ENGLISH))
				&& StringUtils.isNotBlank(bibliographicData.getMaintitleEnglish())) {
			Element title = new Element("title", TEI);
			title.setAttribute("lang", "eng", XML);
			title.setText(bibliographicData.getMaintitleEnglish());
			titleStmt.addContent(title);
			titleWritten = true;
		}
		if (StringUtils.isNotBlank(bibliographicData.getMaintitleOriginal()) && (!titleWritten || !isGermanOrEnglish(bibliographicData.getLanguageMainTitle()))) {
			Element title = new Element("title", TEI);
			// TODO originalsprache
			if(StringUtils.isNotBlank(bibliographicData.getLanguageMainTitle())) {
				title.setAttribute("lang", bibliographicData.getLanguageMainTitle(), XML);
			}
			title.setText(bibliographicData.getMaintitleOriginal());
			titleStmt.addContent(title);
		}

		for (Transcription transcription : transcriptionList) {
			if (transcription.getLanguage().equals(language.getLanguage())) {
				for (SimpleMetadataObject person : transcription.getTranslatorList()) {
					Element editor = new Element("editor", TEI);
					titleStmt.addContent(editor);
					Element persName = new Element("persName", TEI);
//					persName.setAttribute("ref", "edu.experts.id");
					editor.addContent(persName);
					editor.setAttribute("role", "translator");
					persName.setText(person.getValue());
				}
			}
		}
		if (!resouceMetadata.getResourceAuthorList().isEmpty()) {
			for (Person person : resouceMetadata.getResourceAuthorList()) {
				Element author = new Element("author", TEI);
				Element persName = createPersonName(person);
				if (persName != null) {
					if (StringUtils.isNotBlank(person.getNormdataValue())) {
						persName.setAttribute("ref", person.getNormdataValue());
					}
					author.addContent(persName);
					titleStmt.addContent(author);
				}
			}
		} else {
			for (Person person : bibliographicData.getPersonList()) {
				Element author = new Element("author", TEI);
				Element persName = createPersonName(person);
				if (persName != null) {
					if (StringUtils.isNotBlank(person.getNormdataValue())) {
						persName.setAttribute("ref", person.getNormdataValue());
					}
					author.addContent(persName);
					titleStmt.addContent(author);
				}
			}
		}

		return titleStmt;
	}

	public static boolean isGermanOrEnglish(String language) {
		return "de".equalsIgnoreCase(language) || "ger".equalsIgnoreCase(language) || "en".equalsIgnoreCase(language) || "eng".equalsIgnoreCase(language);
	}

	/**
	 * @param person
	 * @return
	 */
	protected Element createPersonName(Person person) {
		Element persName = new Element("persName", TEI);
		if (StringUtils.isNotBlank(person.getLastName())) {
			Element surname = new Element("surname", TEI);
			surname.setText(person.getLastName());
			persName.addContent(surname);
		}
		if (StringUtils.isNotBlank(person.getFirstName())) {
			Element forename = new Element("forename", TEI);
			forename.setText(person.getFirstName());
			persName.addContent(forename);
		}
		if (persName.getContentSize() > 0) {
			return persName;
		} else {
			return null;
		}
	}

	protected Element createBibDataEditionStmt(LanguageEnum language) {
		Element editionStmt = null;
		if (StringUtils.isNotBlank(bibliographicData.getEdition())) {
			editionStmt = new Element("editionStmt", TEI);

			Element edition = new Element("edition", TEI);
			editionStmt.addContent(edition);
			// edition.setAttribute("n", bibliographicData.getEdition());
			edition.setText(bibliographicData.getEdition());
			return editionStmt;
		}
		return null;
	}

	private Element createExtent(String number) {
		Element extent = new Element("extent", TEI);
		Element measure = new Element("measure", TEI);

		if (number == null) {
			int images = 0;
			for (Image img : currentImages) {
				if ("Quelle".equals(img.getStructType())) {
					images++;
				}
			}
			number = images + "";
		}
		measure.setAttribute("unit", "pages");
		measure.setAttribute("quantity", number);
		if (number.equals("1")) {
			measure.setText(number + " Seite");
		} else {
			measure.setText(number + " Seiten");
		}
		extent.addContent(measure);
		return extent;
	}

	private Element createSeriesStmt(LanguageEnum language) {
		Element seriesStmt = new Element("seriesStmt", TEI);
		
		boolean titleWritten = false;
		if ((language.equals(LanguageEnum.GERMAN)) && StringUtils.isNotBlank(bibliographicData.getVolumeTitleGerman())) {
			Element title = new Element("title", TEI);
			title.setText(bibliographicData.getVolumeTitleGerman());
			title.setAttribute("lang", "ger", XML);
			seriesStmt.addContent(title);
			titleWritten = true;
		}

		if ((language.equals(LanguageEnum.ENGLISH)) && StringUtils.isNotBlank(bibliographicData.getVolumeTitleEnglish())) {
			Element title = new Element("title", TEI);
			title.setText(bibliographicData.getVolumeTitleEnglish());
			title.setAttribute("lang", "eng", XML);
			seriesStmt.addContent(title);
			titleWritten = true;
		}
		
		if (StringUtils.isNotBlank(bibliographicData.getVolumeTitleOriginal()) && (!titleWritten || !isGermanOrEnglish(bibliographicData.getLanguageVolumeTitle()))) {
			Element title = new Element("title", TEI);
			title.setText(bibliographicData.getVolumeTitleEnglish());
			if(StringUtils.isNotBlank(bibliographicData.getLanguageVolumeTitle())) {				
				title.setAttribute("lang", bibliographicData.getLanguageVolumeTitle(), XML);
			}
			seriesStmt.addContent(title);
		}

		for (Person person : bibliographicData.getVolumePersonList()) {
			Element editor = new Element(person.getRole().toLowerCase(), TEI);
			seriesStmt.addContent(editor);
			Element persName = createPersonName(person);
			if (persName != null) {
				if (StringUtils.isNotBlank(person.getNormdataValue())) {
					persName.setAttribute("ref", person.getNormdataValue());
				}
				editor.addContent(persName);
			}
		}
		for (Corporation person : bibliographicData.getVolumeCorporationList()) {
			Element editor = new Element(person.getRole().toLowerCase(), TEI);
			seriesStmt.addContent(editor);
			Element persName = new Element("orgName", TEI);
			persName.setText(person.getName());
			if (StringUtils.isNotBlank(person.getNormdataValue())) {
				persName.setAttribute("ref", person.getNormdataValue());
			}
			editor.addContent(persName);
		}
		
		if (StringUtils.isNotBlank(bibliographicData.getVolumeNumber())) {
			Element biblScope = new Element("biblScope", TEI);
			biblScope.setAttribute("unit", "volume");
			biblScope.setText(bibliographicData.getVolumeNumber());
			seriesStmt.addContent(biblScope);
		}

		if (seriesStmt.getContentSize() > 0) {
			return seriesStmt;
		} else {
			return null;
		}
	}

	protected Element createPublicationStmt(LanguageEnum language) {
		Element publicationStmt = new Element("publicationStmt", TEI);
		Element authority = new Element("authority", TEI);
		publicationStmt.addContent(authority);
		Element orgName1 = new Element("orgName", TEI);
		orgName1.setAttribute("ref", "edu.experts.id");
		orgName1.setAttribute("role", "hostingInstitution");
		orgName1.setText("Georg-Eckert-Institut");
		authority.addContent(orgName1);
		Element orgName2 = new Element("orgName", TEI);
		orgName2.setAttribute("ref", "edu.experts.id");
		orgName2.setAttribute("role", "project");
		orgName2.setText("WorldViews");
		authority.addContent(orgName2);

		Date currentDate = new Date();

		Element date = new Element("date", TEI);
		String dateString = formatter.format(currentDate);
		date.setAttribute("when", dateString);
		date.setAttribute("type", "publication");

		date.setText(df.format(currentDate));

		publicationStmt.addContent(date);

		// Element idnoPid = new Element("idno", TEI);
		// idnoPid.setAttribute("type", "CHANGEME");
		// publicationStmt.addContent(idnoPid);
		// idnoPid.setText("1234567890");
		Element idnoUPIDCMDI = new Element("idno", TEI);
		idnoUPIDCMDI.setAttribute("type", "PIDCMDI");
		publicationStmt.addContent(idnoUPIDCMDI);
		idnoUPIDCMDI.setText("0987654321");
		Element availability = new Element("availability", TEI);
		publicationStmt.addContent(availability);
		Element p = new Element("p", TEI);
		String availabilityText = DEFAULT_TEXT_AVAILABILITY;
		if (getTranscription(language) != null
				&& StringUtils.isNotBlank(getTranscription(language).getAvailability())) {
			availabilityText = getTranscription(language).getAvailability();
			// TODO Weiteres p für weitere Sprachen?
			p.setText(availabilityText);
			availability.addContent(p);
		}

		Element licence = new Element("licence", TEI);
		licence.setAttribute("target", "http://creativecommons.org/licenses/by-sa/3.0/");
		licence.setText("Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0)");
		availability.addContent(licence);

		return publicationStmt;
	}

	private Element createBibliographicPublicationStmt() {
		Element publicationStmt = new Element("publicationStmt", TEI);
		Element publisherElement = new Element("publisher", TEI);

		for (Corporation publisher : bibliographicData.getPublisherList()) {
			Element orgName = new Element("orgName", TEI);
			if (!publisher.getNormdataValue().isEmpty()) {
				orgName.setAttribute("ref", publisher.getNormdataValue());
			}
			orgName.setText(publisher.getName());
			publisherElement.addContent(orgName);
		}
		if (publisherElement.getContentSize() > 0) {
			publicationStmt.addContent(publisherElement);
		}

		for (Location loc : bibliographicData.getPlaceOfPublicationList()) {
			Element pubPlace = new Element("pubPlace", TEI);
			if (!loc.getNormdataValue().isEmpty()) {
				pubPlace.setAttribute("ref", loc.getNormdataValue());
			}
			pubPlace.setText(loc.getName());
			publicationStmt.addContent(pubPlace);
		}

		if (StringUtils.isNotBlank(bibliographicData.getPublicationYear())) {
			Element date = new Element("date", TEI);
			date.setAttribute("when", bibliographicData.getPublicationYear());
			date.setText(bibliographicData.getPublicationYear());
			publicationStmt.addContent(date);
		}
		if (publicationStmt.getContentSize() > 0) {
			return publicationStmt;
		} else {
			return null;
		}
	}

	private Element createBbiliographicTitleStmt(LanguageEnum language) {
		Element titleStmt = new Element("titleStmt", TEI);

		boolean titleWritten = false;
		if (language.equals(LanguageEnum.GERMAN) && StringUtils.isNotBlank(bibliographicData.getMaintitleGerman())) {
			Element title = new Element("title", TEI);
			title.setAttribute("lang", "ger", XML);
			title.setText(bibliographicData.getMaintitleGerman());
			titleStmt.addContent(title);
			titleWritten = true;
		}
		if (language.equals(LanguageEnum.ENGLISH) && StringUtils.isNotBlank(bibliographicData.getMaintitleEnglish())) {
			Element title = new Element("title", TEI);
			title.setAttribute("lang", "eng", XML);
			title.setText(bibliographicData.getMaintitleEnglish());
			titleStmt.addContent(title);
			titleWritten = true;
		}
		
		if (StringUtils.isNotBlank(bibliographicData.getMaintitleOriginal()) && (!titleWritten || !isGermanOrEnglish(bibliographicData.getLanguageMainTitle()))) {
			Element title = new Element("title", TEI);
			// TODO originalsprache
			// title.setAttribute("lang", "ger", XML);
			StringBuilder sb = new StringBuilder(bibliographicData.getMaintitleOriginal());
			if(StringUtils.isNotBlank(bibliographicData.getSubtitleOriginal())) {
				sb.append(" ").append(bibliographicData.getSubtitleOriginal());
			}
			title.setText(sb.toString());
			if(StringUtils.isNotBlank(bibliographicData.getLanguageMainTitle())) {
				title.setAttribute("lang", bibliographicData.getLanguageMainTitle(), XML);
			}
			titleStmt.addContent(title);
		}


		for (Person person : bibliographicData.getPersonList()) {
			if (StringUtils.isNotBlank(person.getFirstName()) || StringUtils.isNotBlank(person.getLastName())) {
				Element editor = new Element(person.getRole().toLowerCase(), TEI);
				titleStmt.addContent(editor);
				Element persName = createPersonName(person);
				if (persName != null) {
					if (StringUtils.isNotBlank(person.getNormdataValue())) {
						persName.setAttribute("ref", person.getNormdataValue());
					}
					editor.addContent(persName);
				}
			}
		}
		for (Corporation publisher : bibliographicData.getCorporationList()) {
			if (StringUtils.isNotBlank(publisher.getName())) {
				Element editor = new Element(publisher.getRole().toLowerCase(), TEI);
				titleStmt.addContent(editor);
				Element corpName = new Element("orgName", TEI);
				editor.addContent(corpName);

				if (!publisher.getNormdataValue().isEmpty()) {
					corpName.setAttribute("ref", publisher.getNormdataValue());
				}
				corpName.setText(publisher.getName());
			}
		}

		if (titleStmt.getContentSize() > 0) {
			return titleStmt;
		} else {
			return null;
		}

	}

	protected Element createSourceDesc(LanguageEnum language) {
		Element sourceDesc = new Element("sourceDesc", TEI);

		Element biblFull = new Element("biblFull", TEI);

		sourceDesc.addContent(biblFull);
		Element titleStmt = createBbiliographicTitleStmt(language);
		if (titleStmt != null) {
			biblFull.addContent(titleStmt);
		}
		Element editionStmt = createBibDataEditionStmt(language);
		if (editionStmt != null) {
			biblFull.addContent(editionStmt);
		}

		Element extent = createExtent(bibliographicData.getNumberOfPages());

		biblFull.addContent(extent);

		Element publicationStmt = createBibliographicPublicationStmt();
		if (publicationStmt != null) {
			biblFull.addContent(publicationStmt);
		}

		if (bibliographicData.getDocumentType().equals("multivolume")) {
			Element seriesStmt = createSeriesStmt(language);
			if (seriesStmt != null) {
				biblFull.addContent(seriesStmt);
			}
		}

		Element msDesc = createMsDesc();
		if (msDesc != null) {
			sourceDesc.addContent(msDesc);
		}
		return sourceDesc;
	}

	private Element createMsDesc() {
		Element msDesc = new Element("msDesc", TEI);
		Element msIdentifier = new Element("msIdentifier", TEI);

		if (StringUtils.isNotBlank(bibliographicData.getPhysicalLocation())) {
			Element repository = new Element("repository", TEI);
			repository.setText(bibliographicData.getPhysicalLocation());
			msIdentifier.addContent(repository);
		}

		if (!bibliographicData.getShelfmark().isEmpty()) {
			Element idno = new Element("idno", TEI);
			msIdentifier.addContent(idno);

			Element shelfmark = new Element("idno", TEI);
			shelfmark.setText(bibliographicData.getShelfmark());
			shelfmark.setAttribute("type", "shelfmark");
			idno.addContent(shelfmark);
		}

		if (msIdentifier.getContentSize() > 0) {
			msDesc.addContent(msIdentifier);
			return msDesc;
		} else {
			return null;
		}

	}

	protected Element createEncodingDesc(LanguageEnum language) throws JDOMException, IOException {
		Element encodingDesc = new Element("encodingDesc", TEI);

		Element projectDesc = new Element("projectDesc", TEI);
		encodingDesc.addContent(projectDesc);
		projectDesc.setAttribute("lang", getTranscription(language).getLanguageCode(), XML);
		String context = DEFAULT_TEXT_CONTEXT;

		if (getDescription(language) != null && StringUtils.isNotBlank(getDescription(language).getProjectContext())) {
			context = getDescription(language).getProjectContext();
		}
		Element p = new Element("p", TEI);
		createTextElement(context, p);
		projectDesc.addContent(p);

		Element samplingDecl = new Element("samplingDecl", TEI);
		samplingDecl.setAttribute("lang", getTranscription(language).getLanguageCode(), XML);
		String select = DEFAULT_TEXT_SAMPLING;
		if (getDescription(language) != null && StringUtils.isNotBlank(getDescription(language).getSelectionMethod())) {
			select = getDescription(language).getSelectionMethod();
		}
		Element p2 = new Element("p", TEI);
		createTextElement(select, p2);
		samplingDecl.addContent(p2);
		encodingDesc.addContent(samplingDecl);

		return encodingDesc;
	}

	private Transcription getTranscription(LanguageEnum language) {
		for (Transcription transcription : getTranscriptionList()) {
			if (transcription.getLanguage().equals(language.getLanguage())) {
				return transcription;
			}
		}
		return null;
	}

	private Context getDescription(LanguageEnum language) {
		for (Context description : getDescriptionList()) {
			if (description.getLanguage().equals(language.getLanguage())) {
				return description;
			}
		}
		return null;
	}

	protected Element createHeader(LanguageEnum language) throws JDOMException, IOException {
		Element teiHeader = new Element("teiHeader", TEI);
		Element fileDesc = new Element("fileDesc", TEI);
		teiHeader.addContent(fileDesc);

		Element titleStmt = createTitleStmt(language);
		if (titleStmt != null) {
			fileDesc.addContent(titleStmt);
		}

		Element editionStmt = createEditionStmt(language, "");
		if (editionStmt != null) {
			fileDesc.addContent(editionStmt);
		}
		Element extent = createExtent(null);
		if (extent != null) {
			fileDesc.addContent(extent);
		}
		Element publicationStmt = createPublicationStmt(language);
		if (publicationStmt != null) {
			fileDesc.addContent(publicationStmt);
		}

		Element sourceDesc = createSourceDesc(language);
		if (sourceDesc != null) {
			fileDesc.addContent(sourceDesc);
		}

		Element encodingDesc = createEncodingDesc(language);
		if (encodingDesc != null) {
			teiHeader.addContent(encodingDesc);
		}

		Element profileDesc = createProfileDesc(language);
		if (profileDesc != null) {
			teiHeader.addContent(profileDesc);
		}

		Element revisionDesc = createRevisionDesc();
		if (revisionDesc != null) {
			teiHeader.addContent(revisionDesc);
		}

		return teiHeader;
	}

	protected Element createProfileDesc(LanguageEnum currentLang) throws JDOMException, IOException {
		Element profileDesc = new Element("profileDesc", TEI);
		Element langUsage = new Element("langUsage", TEI);

		for (SimpleMetadataObject currentLanguage : bibliographicData.getLanguageList()) {
			Element language = new Element("language", TEI);
			language.setAttribute("ident", currentLanguage.getValue());
			language.setText(currentLanguage.getValue());
			langUsage.addContent(language);
		}
		if (langUsage.getContentSize() > 0) {
			profileDesc.addContent(langUsage);
		}

		List<Element> abstractList = new ArrayList<>();

		getAbstracts(currentLang, abstractList);
		if (abstractList.isEmpty()) {
			getAbstracts(LanguageEnum.ENGLISH, abstractList);
		}
		if (abstractList != null && !abstractList.isEmpty()) {
			profileDesc.addContent(abstractList);
		}

		// Element textDesc = new Element("textDesc", TEI);
		// profileDesc.addContent(textDesc);
		// textDesc.setAttribute("n", "schoolbook");

		if (StringUtils.isNotBlank(bibliographicData.getEducationLevel())) {
			Element domainEducationalLevel = new Element("classCode", TEI);
			domainEducationalLevel.setAttribute("scheme", "WV.educationalLevel");
			domainEducationalLevel.setAttribute("lang", currentLang.language, XML);
			domainEducationalLevel
					.setText(Helper.getString(currentLang.getLocale(), bibliographicData.getEducationLevel()));
			profileDesc.addContent(domainEducationalLevel);
		}
		if (StringUtils.isNotBlank(bibliographicData.getSchoolSubject())) {
			Element domainEducationalSubject = new Element("classCode", TEI);
			domainEducationalSubject.setAttribute("scheme", "WV.educationalSubject");
			domainEducationalSubject.setAttribute("lang", currentLang.language, XML);
			domainEducationalSubject
					.setText(Helper.getString(currentLang.getLocale(), bibliographicData.getSchoolSubject()));
			profileDesc.addContent(domainEducationalSubject);
		}

		for (Location loc : bibliographicData.getCountryList()) {
			Element domainLocation = new Element("classCode", TEI);
			domainLocation.setAttribute("scheme", "WV.placeOfUse");
			// domainLocation.setAttribute("lang", currentLang.language, XML);
			domainLocation.setText(loc.getName());
			profileDesc.addContent(domainLocation);
		}

		for (SimpleMetadataObject loc : bibliographicData.getStateList()) {
			Element domainLocation = new Element("classCode", TEI);
			domainLocation.setAttribute("scheme", "WV.placeOfUse");
			// domainLocation.setAttribute("lang", currentLang.language, XML);
			domainLocation.setText(loc.getValue());
			profileDesc.addContent(domainLocation);
		}

		Element textClass = new Element("textClass", TEI);
		profileDesc.addContent(textClass);

		boolean keywordsWritten = false;
		if (!topicList.isEmpty()) {
			// TODO richtige Sprache
			Element keywords = new Element("keywords", TEI);
			keywords.setAttribute("scheme", "WV.topics");
			if (currentLang.getLanguage().equals("ger")) {
				keywords.setAttribute("lang", "ger", XML);
			} else {
				keywords.setAttribute("lang", "eng", XML);
			}
			for (Topic topic : topicList) {
				for (Keyword currentKeyword : topic.getKeywordList()) {
					if (currentKeyword.isSelected()) {
						Element term = new Element("term", TEI);
						if (currentLang.getLanguage().equals("ger")) {
							term.setText(topic.getNameDE() + " - " + currentKeyword.getKeywordNameDE());
						} else {
							term.setText(topic.getNameEN() + " - " + currentKeyword.getKeywordNameEN());
						}
						keywordsWritten = true;
						keywords.addContent(term);
					}
				}
			}
			if(keywordsWritten) {				
				textClass.addContent(keywords);
			}
		}

		Element classCode2 = new Element("classCode", TEI);
		classCode2.setAttribute("scheme", "WV.textType");
		if (currentLang.getLanguage().equals("ger")) {			
			classCode2.setText("Schulbuchquelle");
			classCode2.setAttribute("lang", "ger", XML);
		} else {
			classCode2.setText("textbook source");
			classCode2.setAttribute("lang", "eng", XML);
		}
		textClass.addContent(classCode2);

		Element classCode = new Element("classCode", TEI);
		classCode.setAttribute("scheme", "WV.sourceType");
		classCode.setText(resouceMetadata.getResourceType());
		classCode.setAttribute("lang", "ger", XML);
		textClass.addContent(classCode);

		return profileDesc;
	}

	private void getAbstracts(LanguageEnum currentLang, List<Element> abstractList) throws JDOMException, IOException {
		for (Context context : descriptionList) {
			if (currentLang.getLanguage().equals(context.getLanguage())) {

				if (StringUtils.isNotBlank(context.getBookInformation())) {
					Element abstractElement = new Element("abstract", TEI);
					abstractElement.setAttribute("lang", context.getLanguageCode(), XML);
					abstractElement.setAttribute("id", "ProfileDescAbstractSchoolbook", XML);
					Element p = new Element("p", TEI);
					createTextElement(context.getBookInformation(), p);
					abstractElement.addContent(p);
					abstractList.add(abstractElement);
				}

				if (StringUtils.isNotBlank(context.getShortDescription())) {
					Element abstractElement = new Element("abstract", TEI);
					abstractElement.setAttribute("lang", context.getLanguageCode(), XML);
					abstractElement.setAttribute("id", "ProfileDescAbstractShort", XML);
					Element p = new Element("p", TEI);
					createTextElement(context.getShortDescription(), p);
					abstractElement.addContent(p);
					abstractList.add(abstractElement);
				}

				if (StringUtils.isNotBlank(context.getLongDescription())) {
					Element abstractElement = new Element("abstract", TEI);
					abstractElement.setAttribute("lang", context.getLanguageCode(), XML);
					abstractElement.setAttribute("id", "ProfileDescAbstractLong", XML);
					Element p = new Element("p", TEI);
					createTextElement(context.getLongDescription(), p);
					abstractElement.addContent(p);
					abstractList.add(abstractElement);
				}
			}
		}
	}

	protected Element createRevisionDesc() {
		Element revisionDesc = new Element("revisionDesc", TEI);

		for (LogEntry logEntry : getProcessLog()) {
			if (StringUtils.isNotBlank(logEntry.getSecondContent())) {
				Element change = new Element("change", TEI);
				revisionDesc.addContent(change);
				Date date = logEntry.getCreationDate();
				change.setAttribute("when", formatter.format(date));
				change.setText(logEntry.getContent());
			}
		}

		if (revisionDesc.getContentSize() > 0) {
			return revisionDesc;
		} else {
			return null;
		}
	}

	protected Element createEditionStmt(LanguageEnum language, String editionStr) {
		Element editionStmt = new Element("editionStmt", TEI);

		Element edition = new Element("edition", TEI);
		editionStmt.addContent(edition);
		String versionNo = getLatestRevision();
		edition.setAttribute("n", versionNo);
		if (StringUtils.isNotBlank(editionStr)) {
			editionStr += ", ";
		}
		edition.setText(editionStr + "Version " + versionNo);
		return editionStmt;
	}

	protected String getLatestRevision() {
		if (!getProcessLog().isEmpty()) {
			LogEntry logEntry = getProcessLog().get(getProcessLog().size() - 1);
			if (StringUtils.isNotBlank(logEntry.getSecondContent())) {
				return logEntry.getSecondContent();
			}
		}
		return "1";
	}

	/**
	 * @return
	 */
	private List<LogEntry> getProcessLog() {
		try {
			return process.getProcessLog();
		} catch (NoSuchMethodError e) {
			log.warn("Unable to get ProcessLog; Not implemented");
			return new ArrayList<LogEntry>();
		} catch (NullPointerException e) {
			log.warn("No process log found");
			return new ArrayList<LogEntry>();
		}
	}

	@Override
	public String cancel() {
		return returnPath;
	}

	@Override
	public String finish() {
		return returnPath;
	}

	@Override
	public HashMap<String, StepReturnValue> validate() {
		return null;
	}

	@Override
	public Step getStep() {
		return step;
	}

	@Override
	public PluginGuiType getPluginGuiType() {
		return PluginGuiType.NONE;
	}

	@Override
	public String getPagePath() {
		return null;
	}

	public String getDescription() {
		return getTitle();
	}

}
