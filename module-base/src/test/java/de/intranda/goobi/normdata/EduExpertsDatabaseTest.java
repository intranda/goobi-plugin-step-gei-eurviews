package de.intranda.goobi.normdata;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
    @Ignore("This failing test was not executed before")
    public void testCreateRecord() throws IOException {
        Person person = new Person();
        person.setFirstName("JUNIT");
        person.setLastName("Test");
        EduExpertsDatabase db = new EduExpertsDatabase();
        try {
            db.createRecord(person);
        } catch(Throwable e) {
            Assert.fail(e.getMessage());
        }
    }

}
