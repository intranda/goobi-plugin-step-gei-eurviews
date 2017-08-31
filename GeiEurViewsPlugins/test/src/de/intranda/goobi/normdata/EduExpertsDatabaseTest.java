package de.intranda.goobi.normdata;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.intranda.goobi.model.Person;

public class EduExpertsDatabaseTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCreateRecord() throws IOException {
        Person person = new Person();
        person.setFirstName("JUNIT");
        person.setLastName("Test");
        EduExpertsDatabase db = new EduExpertsDatabase();
        Assert.assertTrue("Failed to create record", db.createRecord(person));
    }

}
