package de.intranda.goobi.plugins;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.geonames.Toponym;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;

import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.goobi.model.ComplexMetadataObject;
import de.intranda.goobi.model.Corporation;
import de.intranda.goobi.model.Language;
import de.intranda.goobi.model.Location;
import de.intranda.goobi.model.Person;
import de.intranda.goobi.model.SimpleMetadataObject;
import de.intranda.goobi.model.resource.BibliographicMetadata;
import de.intranda.goobi.normdata.NormdataSearch;
import de.intranda.goobi.persistence.WorldViewsDatabaseManager;
import de.sub.goobi.config.ConfigPlugins;
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
    private NormdataSearch search;

    private String index;
    private String rowType;

    private List<String> possibleLanguages;
    private List<String> possiblePersons;
    private List<String> possibleCorporations;
    private List<String> possiblePublisher;
    private String displayMode = "";

    // normdata

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
        this.search = new NormdataSearch(ConfigPlugins.getPluginConfig(this));
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

                DocStruct volume = null;
                DocStruct logical = dd.getLogicalDocStruct();
                if (logical.getType().isAnchor()) {
                    data.setDocumentType(BibliographicMetadata.MULTIVOLUME);
                    // anchor = logical;
                    // logical = logical.getAllChildren().get(0);
                    volume = logical.getAllChildren().get(0);
                } else {
                    data.setDocumentType(BibliographicMetadata.MONOGRAPH);
                }

                for (Metadata metadata : logical.getAllMetadata()) {

                    if (metadata.getType().getName().equals("TitleDocMain")) {
                        data.getMainTitle().setTitle(metadata.getValue());
                    } else if (metadata.getType().getName().equals("TitleDocSub1")) {
                        data.getMainTitle().setSubTitle(metadata.getValue());
                    } else if (metadata.getType().getName().equals("DocLanguage")) {
                        data.addLanguage(new SimpleMetadataObject(metadata.getValue()));
                    } else if (metadata.getType().getName().equals("PublisherName")) {
                        Corporation pub = new Corporation();
                        pub.setRole("Editor");
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
                    } else if (metadata.getType().getName().equals("SeriesTitle")) {
                        data.getSeriesTitle().setTitle(metadata.getValue());
                    } else if (metadata.getType().getName().equals("SeriesOrder")) {
                        data.getSeriesTitle().setNumbering(metadata.getValue());
                    } else if(metadata.getType().getName().equals("CatalogIDSource")) {
                        data.setMainIdentifier(metadata.getValue());
                    }
                }
                if (logical.getAllPersons() != null) {
                    for (ugh.dl.Person per : logical.getAllPersons()) {

                        if (per.getType().getName().equals("Author") || per.getType().getName().equals("Editor")) {
                            Person aut = new Person();
                            aut.setRole("Author");
                            aut.setFirstName(per.getFirstname());
                            aut.setLastName(per.getLastname());
                            if (per.getAuthorityID() != null && !per.getAuthorityID().isEmpty()) {
                                aut.setNormdataAuthority("gnd");
                                aut.setNormdataValue(per.getAuthorityValue());
                            }
                            data.addBookAuthor(aut);
                        }
                    }
                }

                if (volume != null) {
                    for (Metadata metadata : volume.getAllMetadata()) {

                        if (metadata.getType().getName().equals("TitleDocMain")) {
                            data.getVolumeTitle().setTitle(metadata.getValue());
                        } else if (metadata.getType().getName().equals("TitleDocSub1")) {
                            data.getVolumeTitle().setSubTitle(metadata.getValue());

                        } else if (metadata.getType().getName().equals("CurrentNo")) {
                            data.getVolumeTitle().setNumbering(metadata.getValue());
                        } else if (metadata.getType().getName().equals("DocLanguage")) {
                            SimpleMetadataObject object = new SimpleMetadataObject(metadata.getValue());
                            if (!data.getLanguageList().contains(object)) {
                                data.addLanguage(object);
                            }
                        } else if (metadata.getType().getName().equals("PublisherName")) {
                            Corporation pub = new Corporation();
                            pub.setRole("Editor");
                            pub.setName(metadata.getValue());
                            if (!data.getPublisherList().contains(pub)) {
                                data.addPublisher(pub);
                            }
                        } else if (metadata.getType().getName().equals("PlaceOfPublication")) {
                            Location loc = new Location();
                            loc.setRole("PlaceOfPublication");
                            loc.setName(metadata.getValue());
                            if (!data.getPlaceOfPublicationList().contains(loc)) {
                                data.addPlaceOfPublication(loc);
                            }
                        } else if (metadata.getType().getName().equals("PublicationYear")) {
                            data.setPublicationYear(metadata.getValue());
                        } else if (metadata.getType().getName().equals("shelfmarksource")) {
                            data.setShelfmark(metadata.getValue());
                        } else if (metadata.getType().getName().equals("SeriesTitle")) {
                            data.getSeriesTitle().setTitle(metadata.getValue());
                        } else if (metadata.getType().getName().equals("SeriesOrder")) {
                            data.getSeriesTitle().setNumbering(metadata.getValue());
                        } else if(metadata.getType().getName().equals("CatalogIDSource")) {
                            data.setVolumeIdentifier(metadata.getValue());
                        }
                    }
                    if (volume.getAllPersons() != null) {
                        for (ugh.dl.Person per : volume.getAllPersons()) {
                            if (per.getType().getName().equals("Author") || per.getType().getName().equals("Editor")) {
                                Person aut = new Person();
                                aut.setRole("Author");
                                aut.setFirstName(per.getFirstname());
                                aut.setLastName(per.getLastname());
                                if (per.getAuthorityID() != null && !per.getAuthorityID().isEmpty()) {
                                    aut.setNormdataAuthority("gnd");
                                    aut.setNormdataValue(per.getAuthorityValue());
                                }
                                data.addVolumeAuthor(aut);
                            }
                        }
                    }
                }

            } catch (ReadException | PreferencesException | WriteException | IOException | InterruptedException | SwapException | DAOException e) {
                log.error(e);
            }

        }

        possiblePersons = ConfigPlugins.getPluginConfig(this).getList("elements.person");
        possibleCorporations = ConfigPlugins.getPluginConfig(this).getList("elements.corporation");
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
        String database = "gnd";
        ComplexMetadataObject object = getSelectedObject(false);
        if (object != null && StringUtils.isNotBlank(object.getNormdataAuthority())) {
            database = object.getNormdataAuthority();
        }
        return search.search(database);
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

    private ComplexMetadataObject getSelectedObject(boolean isPerson) {
        ComplexMetadataObject metadata = null;

        int currentIndex = Integer.parseInt(index);
        if (rowType.equals("bookPerson")) {
            metadata = data.getPersonList().get(currentIndex);
        } else if (rowType.equals("volumePerson")) {
            metadata = data.getVolumePersonList().get(currentIndex);
        } else if (rowType.equals("bookCorporation")) {
            metadata = data.getCorporationList().get(currentIndex);
        } else if (rowType.equals("volumeCorporation")) {
            metadata = data.getVolumeCorporationList().get(currentIndex);
        } else if (rowType.equals("publisher")) {
            metadata = data.getPublisherList().get(currentIndex);
        } else if (rowType.equals("seriesResponsibility")) {
            metadata = data.getSeriesResponsibilityList().get(currentIndex);
            if (isPerson) {
                Person person = new Person();
                data.getSeriesResponsibilityList().remove(currentIndex);
                data.getSeriesResponsibilityList().add(currentIndex, person);
                metadata = person;
            }
        }
        return metadata;
    }

    public String getData(List<NormData> currentData) {

        ComplexMetadataObject metadata = getSelectedObject(isPerson(currentData));
        metadata.resetNormdataValues();

        if (metadata instanceof Person) {
            Person person = (Person) metadata;
            for (NormData normdata : currentData) {
                if (normdata.getKey().equals("NORM_IDENTIFIER")) {
                    person.setNormdataAuthority("gnd");
                    person.setNormdataValue(normdata.getValues().get(0).getText());
                } else if (normdata.getKey().equals("NORM_IDENTIFIER_EDU_EXPERTS")) {
                    person.setNormdataAuthority("edu.experts");
                    person.setNormdataValue(normdata.getValues().get(0).getText());
                } else if (normdata.getKey().equals("URI")) {
                    person.setNormdataAuthority("gnd");
                    person.setNormdataUri(normdata.getValues().get(0).getText());
                } else if (normdata.getKey().equals("URI_EDU_EXPERTS")) {
                    person.setNormdataAuthority("edu.experts");
                    person.setNormdataUri(normdata.getValues().get(0).getText());
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
        } else if (metadata instanceof Corporation) {
            Corporation person = (Corporation) metadata;
            getCorporationData(person, currentData);
        }
        return "";
    }

    private boolean isPerson(List<NormData> currentData) {
        for (NormData normData : currentData) {
            if ("NORM_NAME".equals(normData.getKey())) {
                return true;
            }
        }
        return false;
    }

    public String getCorporationData(Corporation person, List<NormData> currentData) {
        for (NormData normdata : currentData) {
            if (normdata.getKey().equals("NORM_IDENTIFIER")) {
                person.setNormdataAuthority("gnd");
                person.setNormdataValue(normdata.getValues().get(0).getText());
            } else if (normdata.getKey().equals("NORM_NAME") || normdata.getKey().equals("NORM_ORGANIZATION")) {
                person.setName(filter(StringUtils.join(normdata.getValues(), "; ").replaceAll("\\x152", "").replaceAll("\\x156", "")));
            } else if (normdata.getKey().equals("NORM_IDENTIFIER_EDU_EXPERTS")) {
                person.setNormdataAuthority("edu.experts");
                person.setNormdataValue(normdata.getValues().get(0).getText());
            } else if (normdata.getKey().equals("URI")) {
                person.setNormdataAuthority("gnd");
                person.setNormdataUri(normdata.getValues().get(0).getText());
            } else if (normdata.getKey().equals("URI_EDU_EXPERTS")) {
                person.setNormdataAuthority("edu.experts");
                person.setNormdataUri(normdata.getValues().get(0).getText());
            }
        }

        return "";
    }

    public String searchGeonames() {
        return search.searchGeonames();
    }

    public String getGeonamesData(Toponym currentToponym) {
        Location loc = null;
        if (rowType.equals("country")) {
            loc = data.getCountryList().get(Integer.parseInt(index));
        } else if (rowType.equals("state")) {
            loc = data.getStateList().get(Integer.parseInt(index));
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
        return search.searchLanguage();
    }

    public String getLanguageData(Language currentLanguage) {

        switch (rowType) {
            case "languageMainTitle":
            case "languageVolumeTitle":
                data.getMainTitle().setLanguage(currentLanguage.getIsoCode());
                //                data.getVolumeTitle().setLanguage(currentLanguage.getIsoCode());
                break;
            case "languageSeriesTitle":
                data.getSeriesTitle().setLanguage(currentLanguage.getIsoCode());
                break;
            case "language":
            default:
                if (StringUtils.isNumeric(index) && Integer.parseInt(index) > -1 && Integer.parseInt(index) < data.getLanguageList().size()) {
                    SimpleMetadataObject lang = data.getLanguageList().get(Integer.parseInt(index));
                    lang.setValue(currentLanguage.getIsoCode());
                } else {
                    log.error("Attempting to select language, but langugage list index is '" + index + "'");
                }
        }

        return "";
    }

}
