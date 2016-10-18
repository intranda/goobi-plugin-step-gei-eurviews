package de.intranda.goobi.plugins;

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
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;

import de.intranda.digiverso.normdataimporter.NormDataImporter;
import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.goobi.model.ComplexMetadataObject;
import de.intranda.goobi.model.Location;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.Publisher;
import de.intranda.goobi.model.SimpleMetadataObject;
import de.intranda.goobi.model.resource.BibliographicData;
import de.intranda.goobi.persistence.DatabaseManager;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.MetadataManager;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Data
@Log4j
public class BibliographicDataPlugin implements IStepPlugin, IPlugin {

    private Step step;
    private Process process;
    private String returnPath = "/task_edit.xhtml";
    private static final String PLUGIN_NAME = "BibliographicData";
    private static final String GUI_PATH = "/BiliographicDataPlugin.xhtml";

    
    private BibliographicData data;

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
            data = DatabaseManager.getBibliographicData(process.getId());
        } catch (SQLException e) {
            log.error(e);
        }
        if (data == null) {
            if (log.isDebugEnabled()) {
                log.debug("create new bibliographic record");
            }
            data = new BibliographicData(process.getId());
            // TODO check if document type is MMO or monograph
            data.setDocumentType("book");

            // TODO get from meta.xml
            List<StringPair> metadataList = MetadataManager.getMetadata(process.getId());
            for (StringPair sp : metadataList) {
                if (sp.getOne().equals("TitleDocMain")) {
                    data.setMaintitleOriginal(sp.getTwo());
                } else if (sp.getOne().equals("TitleDocSub1")) {
                    data.setSubtitleOriginal(sp.getTwo());
                } else if (sp.getOne().equals("Author")) {
                    Person aut = new Person();
                    aut.setRole("Author");

                    String value = sp.getTwo();
                    if (value.contains(" ")) {
                        aut.setFirstName(value.substring(value.indexOf(" ") + 1));
                        aut.setLastName(value.substring(0, value.indexOf(" ")));
                    } else {
                        aut.setLastName(value);
                    }
                    data.addBookAuthor(aut);

                    data.addVolumeAuthor(aut);
                } else if (sp.getOne().equals("DocLanguage")) {
                    data.addLanguage(new SimpleMetadataObject(sp.getTwo()));
                } else if (sp.getOne().equals("PublisherName")) {
                    Publisher pub = new Publisher();
                    pub.setRole("Verlag");
                    pub.setName(sp.getTwo());

                    data.addPublisher(pub);
                } else if (sp.getOne().equals("PlaceOfPublication")) {
                    Location loc = new Location();
                    loc.setRole("PlaceOfPublication");
                    loc.setName(sp.getTwo());
                    data.setPlaceOfPublication(loc);
                } else if (sp.getOne().equals("PublicationYear")) {
                    data.setPublicationYear(sp.getTwo());
                } else if (sp.getOne().equals("shelfmarksource")) {
                    data.setShelfmark(sp.getTwo());
                }

            }

            if (!data.getPersonList().isEmpty()) {
                for (Person author : data.getPersonList()) {
                    Person per = new Person();
                    per.setFirstName(author.getFirstName());
                    per.setLastName(author.getLastName());
                    per.setNormdataAuthority(author.getNormdataAuthority());
                    per.setNormdataValue(author.getNormdataValue());
                    per.setRole(author.getRole());

                    data.addToResourceAuthorList(per);
                }
            }
        }
        
        possiblePersons = ConfigPlugins.getPluginConfig(this).getList("elements.person");
        possiblePublisher = ConfigPlugins.getPluginConfig(this).getList("elements.publisher");
    }

    public void save() {
        try {
            DatabaseManager.saveBibliographicData(data);
        } catch (SQLException e) {
            log.error(e);
        }
    }
    
    
    @Override
    public boolean execute() {
        return false;
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
    public PluginGuiType getPluginGuiType() {
        return PluginGuiType.FULL;
    }

    @Override
    public String getPagePath() {
        return "/" + Helper.getTheme() + GUI_PATH;
    }

    
    public String search() {
        String val = "";
        if (searchOption.isEmpty()) {
            val = searchValue;
        } else {
            val = searchValue + " and BBG=" + searchOption;
        }
        URL url = convertToURLEscapingIllegalCharacters("http://normdata.intranda.com/normdata/gnd/woe/" + val);
        String string = url.toString().replace("Ä", "%C3%84").replace("Ö", "%C3%96").replace("Ü", "%C3%9C").replace("ä", "%C3%A4").replace("ö",
                "%C3%B6").replace("ü", "%C3%BC").replace("ß", "%C3%9F");
        dataList = NormDataImporter.importNormDataList(string);
        return "";
    }

    private URL convertToURLEscapingIllegalCharacters(String string) {
        try {
            String decodedURL = URLDecoder.decode(string, "UTF-8");
            URL url = new URL(decodedURL);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
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
        } else if (rowType.equals("resourceAuthor")) {
            metadata = data.getResourceAuthorList().get(Integer.parseInt(index));
        } 

        if (metadata instanceof Person) {
            Person person = (Person) metadata;
            for (NormData normdata : currentData) {
                if (normdata.getKey().equals("NORM_IDENTIFIER")) {
                    person.setNormdataAuthority("gnd");
                    person.setNormdataValue(normdata.getValues().get(0).getText());
                } else if (normdata.getKey().equals("NORM_NAME")) {
                    String value = normdata.getValues().get(0).getText().replaceAll("\\x152", "").replaceAll("\\x156", "");
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
                person.setName(filter(normdata.getValues().get(0).getText().replaceAll("\\x152", "").replaceAll("\\x156", "")));
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
        Location loc = data.getCountryList().get(Integer.parseInt(index));        
        loc.setName(currentToponym.getName());
        loc.setNormdataAuthority("geonames");
        loc.setNormdataValue("" + currentToponym.getGeoNameId());
        return "";
    }

    public String getGeonamesUrl(Location loc) {
        if (StringUtils.isBlank(loc.getNormdataValue())) {
            return null;
        } else {
            return "http://www.geonames.org/" + loc.getNormdataValue();
        }
    }
    
}
