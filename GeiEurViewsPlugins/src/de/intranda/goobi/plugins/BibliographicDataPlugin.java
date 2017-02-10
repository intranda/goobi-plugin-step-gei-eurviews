package de.intranda.goobi.plugins;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;

import de.intranda.digiverso.normdataimporter.NormDataImporter;
import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.goobi.model.ComplexMetadataObject;
import de.intranda.goobi.model.Language;
import de.intranda.goobi.model.Location;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.Publisher;
import de.intranda.goobi.model.SimpleMetadataObject;
import de.intranda.goobi.model.resource.BibliographicMetadata;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

@PluginImplementation
@Data
@Log4j
public class BibliographicDataPlugin implements IStepPlugin, IPlugin {

	private Step step;
	private Process process;
	private String returnPath;
	private static final String PLUGIN_NAME = "Gei_WorldViews_BibliographicData";
	private static final String GUI_PATH = "/Gei_WorldViews_BibliographicDataPlugin.xhtml";

	private BibliographicMetadata data;

	private List<String> possibleLanguages;
	private List<String> possiblePersons;
	private List<String> possiblePublisher;
	private String displayMode = "";

	// normdata
	private String database;
	protected List<List<NormData>> dataList;

	private String searchOption;
	private String searchValue;
	private String index;
	private String rowType;

	private List<Toponym> resultList;
	private int totalResults;
	private String gndSearchValue;
	private List<Language> searchedLanguages;

	@Override
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
		this.process = step.getProzess();
		this.returnPath = returnPath;
		try {
			data = WorldViewsDatabaseManager.getBibliographicData(process.getId());
		} catch (SQLException e) {
			log.error(e);
		}
		if (data == null) {
			if (log.isDebugEnabled()) {
				log.debug("create new bibliographic record");
			}
			data = new BibliographicMetadata(process.getId());

			try {
				Fileformat ff = process.readMetadataFile();
				DigitalDocument dd = ff.getDigitalDocument();

				DocStruct anchor = null;
				DocStruct logical = dd.getLogicalDocStruct();
				if (logical.getType().isAnchor()) {
					data.setDocumentType("multivolume");
					anchor = logical;
					logical = logical.getAllChildren().get(0);
				} else {
					data.setDocumentType("book");
				}

				for (Metadata metadata : logical.getAllMetadata()) {

					if (metadata.getType().getName().equals("TitleDocMain")) {
						data.setMaintitleOriginal(metadata.getValue());
					} else if (metadata.getType().getName().equals("TitleDocSub1")) {
						data.setSubtitleOriginal(metadata.getValue());
					} else if (metadata.getType().getName().equals("DocLanguage")) {
						data.addLanguage(new SimpleMetadataObject(metadata.getValue()));
					} else if (metadata.getType().getName().equals("PublisherName")) {
						Publisher pub = new Publisher();
						pub.setRole("Verlag");
						pub.setName(metadata.getValue());
						data.addPublisher(pub);
					} else if (metadata.getType().getName().equals("PlaceOfPublication")) {
						Location loc = new Location();
						loc.setRole("PlaceOfPublication");
						loc.setName(metadata.getValue());
						data.addPlaceOfPublication(loc);
					} else if (metadata.getType().getName().equals("PublicationYear")) {
						data.setPublicationYear(metadata.getValue());
					} else if (metadata.getType().getName().equals("shelfmarksource")) {
						data.setShelfmark(metadata.getValue());
					}
				}
				if (anchor != null) {
					for (ugh.dl.Person per : anchor.getAllPersons()) {
						// TODO get list of possible roles from configuration
						// file
						if (per.getType().getName().equals("Author")) {
							Person aut = new Person();
							aut.setRole("Author");
							aut.setFirstName(per.getFirstname());
							aut.setLastName(per.getLastname());
							if (per.getAuthorityID() != null && !per.getAuthorityID().isEmpty()) {
								aut.setNormdataAuthority("gnd");
								aut.setNormdataValue(per.getAuthorityID());
							}
							data.addBookAuthor(aut);
						}
					}
					if (logical.getAllPersons() != null) {
						for (ugh.dl.Person per : logical.getAllPersons()) {

							if (per.getType().getName().equals("Author")) {
								Person aut = new Person();
								aut.setRole("Author");
								aut.setFirstName(per.getFirstname());
								aut.setLastName(per.getLastname());
								if (per.getAuthorityID() != null && !per.getAuthorityID().isEmpty()) {
									aut.setNormdataAuthority("gnd");
									aut.setNormdataValue(per.getAuthorityID());
								}
								data.addVolumeAuthor(aut);
							}
						}
					}
				} else {
					if (logical.getAllPersons() != null) {
						for (ugh.dl.Person per : logical.getAllPersons()) {

							if (per.getType().getName().equals("Author")) {
								Person aut = new Person();
								aut.setRole("Author");
								aut.setFirstName(per.getFirstname());
								aut.setLastName(per.getLastname());
								if (per.getAuthorityID() != null && !per.getAuthorityID().isEmpty()) {
									aut.setNormdataAuthority("gnd");
									aut.setNormdataValue(per.getAuthorityID());
								}
								data.addBookAuthor(aut);
							}
						}
					}
				}
			} catch (ReadException | PreferencesException | WriteException | IOException | InterruptedException
					| SwapException | DAOException e) {
				log.error(e);
			}

		}

		possiblePersons = ConfigPlugins.getPluginConfig(this).getList("elements.person");
		possiblePublisher = ConfigPlugins.getPluginConfig(this).getList("elements.publisher");
	}

	public void save() {
		try {
			WorldViewsDatabaseManager.saveBibliographicData(data);
			Helper.setMeldung("dataSavedSuccessfully");
		} catch (SQLException e) {
			log.error(e);
			Helper.setFehlerMeldung("dataCouldNotBeSaved", e);
		}
	}

	@Override
	public boolean execute() {
		return false;
	}

	@Override
	public String cancel() {
		return "/" + Helper.getTheme() + returnPath;
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
	public PluginGuiType getPluginGuiType() {
		return PluginGuiType.FULL;
	}

	@Override
	public String getPagePath() {
		return "/" + Helper.getTheme() + GUI_PATH;
	}

	public String search() {
		String val = "";
		if (StringUtils.isBlank(searchOption)) {
			val = searchValue;
		} else {
			val = searchValue + " and BBG=" + searchOption;
		}
		URL url = convertToURLEscapingIllegalCharacters("http://normdata.intranda.com/normdata/gnd/woe/" + val);
		String string = url.toString().replace("Ä", "%C3%84").replace("Ö", "%C3%96").replace("Ü", "%C3%9C")
				.replace("ä", "%C3%A4").replace("ö", "%C3%B6").replace("ü", "%C3%BC").replace("ß", "%C3%9F");
		dataList = NormDataImporter.importNormDataList(string);
		return "";
	}

	private URL convertToURLEscapingIllegalCharacters(String string) {
		try {
			String decodedURL = URLDecoder.decode(string, "UTF-8");
			URL url = new URL(decodedURL);
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
					url.getQuery(), url.getRef());
			return uri.toURL();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	protected String filter(String str) {
		StringBuilder filtered = new StringBuilder(str.length());
		for (int i = 0; i < str.length(); i++) {
			char current = str.charAt(i);
			// current != 0x152 && current != 0x156
			if (current != 0x98 && current != 0x9C) {
				filtered.append(current);
			}
		}
		return filtered.toString();
	}

	public String getData(List<NormData> currentData) {

		ComplexMetadataObject metadata = null;
		if (rowType.equals("bookPerson")) {
			metadata = data.getPersonList().get(Integer.parseInt(index));
		} else if (rowType.equals("volumePerson")) {
			metadata = data.getVolumePersonList().get(Integer.parseInt(index));
		} else if (rowType.equals("publisher")) {
			metadata = data.getPublisherList().get(Integer.parseInt(index));
		}
		if (metadata instanceof Person) {
			Person person = (Person) metadata;
			for (NormData normdata : currentData) {
				if (normdata.getKey().equals("NORM_IDENTIFIER")) {
					person.setNormdataAuthority("gnd");
					person.setNormdataValue(normdata.getValues().get(0).getText());
				} else if (normdata.getKey().equals("NORM_NAME")) {
					String value = normdata.getValues().get(0).getText().replaceAll("\\x152", "").replaceAll("\\x156",
							"");
					value = filter(value);
					if (value.contains(",")) {
						person.setLastName(value.substring(0, value.indexOf(",")).trim());
						person.setFirstName(value.substring(value.indexOf(",") + 1).trim());
					} else if (value.contains(" ")) {
						String[] nameParts = value.split(" ");
						String first = "";
						String last = "";
						if (nameParts.length == 1) {
							last = nameParts[0];
						} else if (nameParts.length == 2) {
							first = nameParts[0];
							last = nameParts[1];
						} else {
							int counter = nameParts.length;
							for (int i = 0; i < counter; i++) {
								if (i == counter - 1) {
									last = nameParts[i];
								} else {
									first += " " + nameParts[i];
								}
							}
						}
						person.setLastName(last);
						person.setFirstName(first);
					} else {
						person.setLastName(value);
					}
				}
			}
		} else if (metadata instanceof Publisher) {
			Publisher person = (Publisher) metadata;
			getPublisherData(person, currentData);
		}
		return "";
	}

	public String getPublisherData(Publisher person, List<NormData> currentData) {
		for (NormData normdata : currentData) {
			if (normdata.getKey().equals("NORM_IDENTIFIER")) {
				person.setNormdataAuthority("gnd");
				person.setNormdataValue(normdata.getValues().get(0).getText());
			} else if (normdata.getKey().equals("NORM_NAME")) {
				person.setName(filter(
						normdata.getValues().get(0).getText().replaceAll("\\x152", "").replaceAll("\\x156", "")));
			}
		}

		return "";
	}

	public String searchGeonames() {
		String credentials = ConfigurationHelper.getInstance().getGeonamesCredentials();
		if (credentials != null) {
			WebService.setUserName(credentials);
			ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
			searchCriteria.setNameEquals(gndSearchValue);
			searchCriteria.setStyle(Style.FULL);
			try {
				ToponymSearchResult searchResult = WebService.search(searchCriteria);
				resultList = searchResult.getToponyms();
				totalResults = searchResult.getTotalResultsCount();
			} catch (Exception e) {

			}

		} else {
			// deaktiviert
			Helper.setFehlerMeldung("geonamesDeactivated");
		}
		return "";
	}

	public String getGeonamesData(Toponym currentToponym) {
		Location loc = null;
		if (rowType.equals("country")) {
			loc = data.getCountryList().get(Integer.parseInt(index));
		} else if (rowType.equals("place")) {
			loc = data.getPlaceOfPublicationList().get(Integer.parseInt(index));
		}
		if (loc != null) {
			loc.setName(currentToponym.getName());
			loc.setNormdataAuthority("geonames");
			loc.setNormdataValue("" + currentToponym.getGeoNameId());
		}
		return "";
	}

	public String getGeonamesUrl(Location loc) {
		if (StringUtils.isBlank(loc.getNormdataValue())) {
			return null;
		} else {
			return "http://www.geonames.org/" + loc.getNormdataValue();
		}
	}

	public String searchLanguage() {

		try {
			searchedLanguages = WorldViewsDatabaseManager.getLanguageList(searchValue);
		} catch (SQLException e) {
			log.error(e);
		}
		return "";
	}

	public String getLanguageData(Language currentLanguage) {

		SimpleMetadataObject lang = data.getLanguageList().get(Integer.parseInt(index));
		lang.setValue(currentLanguage.getIsoCode());
		return "";
	}
}
