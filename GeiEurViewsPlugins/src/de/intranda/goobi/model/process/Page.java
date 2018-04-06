package de.intranda.goobi.model.process;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import lombok.Data;
import ugh.dl.ContentFile;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;

public @Data class Page implements Comparable<Page> {

    private final Logger logger = Logger.getLogger(Page.class);
    private static final AtomicInteger idCount = new AtomicInteger(0);

    private final DocStruct ds;
    private final Integer id;
    private String docType = "Quelle";
    private Path filePath = null;

    public Page(DocStruct ds) {
        this.ds = ds;
        this.id = idCount.getAndIncrement();
        initFilePath();
    }

    public Page(Page orig, Path filePath) {
        this.ds = orig.ds;
        this.id = orig.id;
        this.docType = orig.docType;
        this.filePath = filePath;
    }

    public String getPageNumber() {
        List<Metadata> mdList = ds.getAllMetadata();
        String logPage = "uncounted";
        String physPage = "uncounted";
        for (Metadata metadata : mdList) {
            if (metadata.getType()
                    .getName()
                    .equals("physPageNumber")) {
                physPage = metadata.getValue();
            } else if (metadata.getType()
                    .getName()
                    .equals("logicalPageNumber")) {
                logPage = metadata.getValue();
            }
        }
        return physPage + " : " + logPage;
    }

    public Integer getOrder() {
        List<Metadata> mdList = ds.getAllMetadata();
        for (Metadata metadata : mdList) {
            if (metadata.getType()
                    .getName()
                    .equals("physPageNumber")) {
                try {
                    Integer order = Integer.valueOf(metadata.getValue());
                    return order;
                } catch (NullPointerException | NumberFormatException e) {
                    logger.error("Unbale to retrieve page order from " + metadata.getValue(), e);
                }
            }
        }
        return 0;
    }
    
    public String getLabel() {
        List<Metadata> mdList = ds.getAllMetadata();
        for (Metadata metadata : mdList) {
            if (metadata.getType()
                    .getName()
                    .equals("logicalPageNumber")) {
                    return metadata.getValue();
            }
        }
        return Integer.toString(getOrder());
    }
    
    

    /**
     * @param page
     */
    public void initFilePath() {
        List<ContentFile> contentFiles = this.getDs()
                .getAllContentFiles();
        if (contentFiles != null && !contentFiles.isEmpty()) {
            try {
                URI uri = new URI(contentFiles.get(0).getLocation());
                this.setFilePath(Paths.get(uri));
            } catch (URISyntaxException e) {
                this.setFilePath(Paths.get(contentFiles.get(0).getLocation()));
            }
        }
    }

    public String toString() {
        return getPageNumber();
    }

    @Override
    public int compareTo(Page o) {
        return getOrder().compareTo(o.getOrder());
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof Page) {
            return this.getOrder()
                    .equals(((Page) other).getOrder());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getOrder();
    }

}
