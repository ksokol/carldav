/*
 * Copyright 2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.model;

import org.junit.Assert;
import org.junit.Test;
import org.unitedinternet.cosmo.model.hibernate.HibEntityFactory;
import org.unitedinternet.cosmo.util.NoteOccurrenceUtil;
import org.unitedinternet.cosmo.util.VersionFourGenerator;

/**
 * Test NoteOccurrenceItem
 */
public class NoteOccurrenceTest {
   
    private EntityFactory factory = new HibEntityFactory(new VersionFourGenerator());
    
    /**
     * Tests generate note occurrence.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGenerateNoteOccurrence() throws Exception {
        
        NoteItem note = factory.createNote();
        note.setUid("1");
        note.setDisplayName("dn");
        note.setBody("body");
        note.addStamp(factory.createEventStamp(note));
        
        NoteOccurrence no = NoteOccurrenceUtil.createNoteOccurrence(new net.fortuna.ical4j.model.Date("20070101"), note);
        NoteOccurrence no2 = NoteOccurrenceUtil.createNoteOccurrence(new net.fortuna.ical4j.model.Date("20070102"), note);
        
        
        Assert.assertEquals("1:20070101", no.getUid());
        Assert.assertEquals(note, no.getMasterNote());
        Assert.assertNotNull(no.getOccurrenceDate());
        
        Assert.assertEquals(note.getCreationDate(), no.getCreationDate());
        Assert.assertEquals("dn", no.getDisplayName());
        Assert.assertEquals("body", no.getBody());
        
        Assert.assertEquals(1, no.getStamps().size());
        
        Assert.assertFalse(no.equals(no2));
        Assert.assertTrue(no.hashCode() != no2.hashCode());
        
        try {
            no.setUid("blah");
            Assert.fail("able to perform unsupported op");
        } catch (UnsupportedOperationException e) {
            
        }
    }    
    
}
