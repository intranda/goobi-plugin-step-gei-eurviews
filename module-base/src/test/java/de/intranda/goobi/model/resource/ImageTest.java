package de.intranda.goobi.model.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ImageTest {
    
    private File downloadFolder = new File("/tmp");

    @Before
    public void setUp() throws Exception {
        FileUtils.cleanDirectory(downloadFolder);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    @Ignore("This test seem to freeze")
    public void testDownloadImage() throws IOException, URISyntaxException {
        String path = "http://www.eurviews.eu/fileadmin/_eurviews/sources/H_F_7_II_1997_Berstein_et_Milza/H_F_7_II_1997_Berstein_et_Milza_Q03.jpg";
        Image image = new Image(1);
        image.setFileName(path);
        
        URL url = new URL(image.getFileName());
        String filename = Paths.get(url.getFile()).getFileName().toString();
        System.out.println("Filename = " + filename);
        
        File file = new File(downloadFolder, filename);
        
        Assert.assertFalse(file.isFile());
        
        try(InputStream in = url.openStream()){
            Files.copy(in, file.toPath());
        }
        Assert.assertTrue(file.isFile());
    }

}
