/*
 * Copyright 2006-2012 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.samplu.krad.compview;

import edu.samplu.common.UpgradedSeleniumITBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * tests that the parent line variable is available in a sub collection
 *
 * <p>configuration done in /edu/sampleu/demo/kitchensink/UifComponentsViewP7.xml on bean id="subCollection1"</p>
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ParentLineIT extends UpgradedSeleniumITBase {
    @Override
    public String getTestUrl() {
        return "/kr-krad/uicomponents?methodToCall=start&readOnlyFields=field91&viewId=UifCompView_KNS#UifCompView-Page7";
    }

    @Test
    /**
     * tests that the size of a sub collection is correctly displayed using the parentLine el variable
     */
    public void testSubCollectionSize() throws Exception {
//        // click on collections page link
        waitAndClick("link=Collections");
//        // wait for collections page to load by checking the presence of a sub collection line item
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            try { if (selenium.isElementPresent("link=SubCollection - (3 lines)")) break; } catch (Exception e) {}
            Thread.sleep(1000);
        }
        // verify that sub collection sizes are displayed as expected
        assertEquals("SubCollection - (3 lines)", selenium.getText("link=SubCollection - (3 lines)"));
        assertEquals("SubCollection - (2 lines)", selenium.getText("link=SubCollection - (2 lines)"));
    }
}
