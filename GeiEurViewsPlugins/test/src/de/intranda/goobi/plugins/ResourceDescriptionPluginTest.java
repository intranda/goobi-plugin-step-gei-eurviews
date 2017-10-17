package de.intranda.goobi.plugins;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ResourceDescriptionPluginTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testImageFilter() {
        ResourceDescriptionPlugin.ImageFilter filter = new ResourceDescriptionPlugin.ImageFilter();
        Assert.assertTrue(filter.accept(null, "bla blub.tif"));
        Assert.assertTrue(filter.accept(null, "bla blub.jpg"));
        Assert.assertTrue(filter.accept(null, "bla blub.JPEG"));
        Assert.assertTrue(filter.accept(null, "bla blub.tiff"));
        Assert.assertTrue(filter.accept(null, "bla blub.TIF"));
        Assert.assertTrue(filter.accept(null, "bla blub.PNG"));
        Assert.assertTrue(filter.accept(null, "bla blub.jp2"));
        Assert.assertFalse(filter.accept(null, "bla blub.xml"));
        Assert.assertFalse(filter.accept(null, "bla blubtif"));
    }

}
