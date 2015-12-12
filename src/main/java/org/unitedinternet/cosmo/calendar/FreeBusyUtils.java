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
package org.unitedinternet.cosmo.calendar;

import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.property.FreeBusy;
import net.fortuna.ical4j.model.property.Uid;
import org.unitedinternet.cosmo.util.VersionFourGenerator;

import java.util.Iterator;
import java.util.List;

/**
 * 
 * FreeBusyUtils class.
 */
public class FreeBusyUtils {
    
    private static final VersionFourGenerator UUID_GENERATOR = new VersionFourGenerator();

    /**
     * Merge multiple VFREEBUSY components into a single VFREEBUSY
     * component.
     * @param components components to merge
     * @param range time range of new component (DTSTART/DTEND)
     * @return merged component
     */
    public static VFreeBusy mergeComponents(List<VFreeBusy> components, Period range) {
        // no merging required if there's only one component
        if (components.size() == 1) {
            return components.get(0);
        }

        // merge results into single VFREEBUSY
        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();
        
        for(VFreeBusy vfb: components) {
            PropertyList props = vfb.getProperties(Property.FREEBUSY);
            for(Iterator it = props.iterator();it.hasNext();) {
                FreeBusy fb = (FreeBusy) it.next();
                FbType fbt = (FbType)
                    fb.getParameters().getParameter(Parameter.FBTYPE);
                if (fbt == null || FbType.BUSY.equals(fbt)) {
                    busyPeriods.addAll(fb.getPeriods());
                } else if (FbType.BUSY_TENTATIVE.equals(fbt)) {
                    busyTentativePeriods.addAll(fb.getPeriods());
                } else if (FbType.BUSY_UNAVAILABLE.equals(fbt)) {
                    busyUnavailablePeriods.addAll(fb.getPeriods());
                }
            }
        }
        
        // Merge periods
        busyPeriods = busyPeriods.normalise();
        busyTentativePeriods = busyTentativePeriods.normalise();
        busyUnavailablePeriods = busyUnavailablePeriods.normalise();
        
        // Construct new VFREEBUSY
        VFreeBusy vfb =
            new VFreeBusy(range.getStart(), range.getEnd());
        String uid = UUID_GENERATOR.nextStringIdentifier();
        vfb.getProperties().add(new Uid(uid));
       
        // Add all periods to the VFREEBUSY
        if (busyPeriods.size() != 0) {
            FreeBusy fb = new FreeBusy(busyPeriods);
            vfb.getProperties().add(fb);
        }
        if (busyTentativePeriods.size() != 0) {
            FreeBusy fb = new FreeBusy(busyTentativePeriods);
            fb.getParameters().add(FbType.BUSY_TENTATIVE);
            vfb.getProperties().add(fb);
        }
        if (busyUnavailablePeriods.size() != 0) {
            FreeBusy fb = new FreeBusy(busyUnavailablePeriods);
            fb.getParameters().add(FbType.BUSY_UNAVAILABLE);
            vfb.getProperties().add(fb);
        }
        
        return vfb;
    }
    
}
