package com.bloatit.model;

import java.util.Locale;

import junit.framework.TestCase;

import com.bloatit.model.data.Demand;
import com.bloatit.model.data.Member;
import com.bloatit.model.data.util.SessionManger;

public class HibernateDraftCommentTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
        SessionManger.beginWorkUnit();
        {
            Member theMember = Member.createAndPersist("Thomas", "password", "tom@gmail.com");
            theMember.setFirstname("Thomas");
            theMember.setLastname("Guyard");
            SessionManger.flush();
        }
        {
            Member theMember = Member.createAndPersist("Fred", "other", "fred@gmail.com");
            theMember.setFirstname("Frédéric");
            theMember.setLastname("Bertolus");
            SessionManger.flush();
        }
        {
            Member theMember = Member.createAndPersist("Yo", "plop", "yo@gmail.com");
            theMember.setFirstname("Yoann");
            theMember.setLastname("Plénet");
        }
        SessionManger.EndWorkUnitAndFlush();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateDemand() {}
}
