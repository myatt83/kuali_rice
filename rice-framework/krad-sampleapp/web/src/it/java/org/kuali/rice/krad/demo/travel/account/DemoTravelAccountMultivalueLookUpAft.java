/**
 * Copyright 2005-2014 The Kuali Foundation
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
package org.kuali.rice.krad.demo.travel.account;

import org.junit.Test;
import org.kuali.rice.testtools.selenium.WebDriverLegacyITBase;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class DemoTravelAccountMultivalueLookUpAft extends WebDriverLegacyITBase {

    /**
     * /kr-krad/lookup?methodToCall=start&dataObjectClassName=org.kuali.rice.krad.demo.travel.dataobject.TravelAccount&hideReturnLink=true&multipleValuesSelect=true&lookupCollectionName=travelAccounts&suppressActions=true&conversionFields=number:foo,name:foo
     */
    public static final String BOOKMARK_URL = "/kr-krad/lookup?methodToCall=start&dataObjectClassName=org.kuali.rice.krad.demo.travel.dataobject.TravelAccount&hideReturnLink=true&multipleValuesSelect=true&lookupCollectionName=travelAccounts&suppressActions=true&conversionFields=number:foo,name:foo";
   
    /**
     * selectedCollectionLines['lookupResults']
     */
    public static final String LOOKUP_RESULTS = "selectedCollectionLines['lookupResults']";

    boolean foundA9 = false;
    boolean foundA6 = false;
    boolean foundA14 = false;

    @Override
    public String getBookmarkUrl() {
        return BOOKMARK_URL;
    }

    @Override
    protected void navigate() throws Exception {
        waitAndClickById("Demo-DemoLink", "");
        waitAndClickByLinkText("Account Multi-Value Lookup");
    }

    private boolean foundAll() {
        if (foundA6 || isTextPresent("a6")) {
            foundA6 = true;
        }

        if (foundA9 || isTextPresent("a9")) {
            foundA9 = true;
        }

        if (foundA14 || isTextPresent("a14")) {
            foundA14 = true;
        }

        return foundA6 && foundA9 && foundA14;
    }

    private void testSearchSelect() throws InterruptedException {
        waitAndClickByValue("CAT");
        waitAndClickButtonByText(WebDriverLegacyITBase.SEARCH);

        while (!foundAll()) {
            waitAndClickByLinkText("Next", "Didn't find all expected results");
        }

        waitAndClickByName(LOOKUP_RESULTS);
        assertButtonEnabledByText(WebDriverLegacyITBase.RETURN_SELECTED_BUTTON_TEXT);
        waitAndClickByName(LOOKUP_RESULTS);
        assertButtonDisabledByText(WebDriverLegacyITBase.RETURN_SELECTED_BUTTON_TEXT);

        assertMultiValueSelectAllThisPage();
        assertMultiValueDeselectAllThisPage();

        waitAndClickByName(LOOKUP_RESULTS);
        waitAndClickButtonByText(WebDriverLegacyITBase.SEARCH);
        checkForIncidentReport();
    }

    @Test
    public void testTravelAccountMultivalueLookUpSearchSelectBookmark() throws Exception {
        testSearchSelect();
        passed();
    }

    @Test
    public void testTravelAccountMultivalueLookUpSearchSelectNav() throws Exception {
        testSearchSelect();
        passed();
    }

    @Test
    public void testTravelAccountMultivalueLookUpSelectThisPageBookmark() throws Exception {
        testMultiValueSelectAllThisPage();
        passed();
    }

    @Test
    public void testTravelAccountMultivalueLookUpSelectThisPageNav() throws Exception {
        testMultiValueSelectAllThisPage();
        passed();
    }

    @Test
    public void testTravelAccountMultivalueLookUpSelectAllPagesBookmark() throws Exception {
        testMultiValueSelectAllPages();
        passed();
    }

    @Test
    public void testTravelAccountMultivalueLookUpSelectAllPagesNav() throws Exception {
        testMultiValueSelectAllPages();
        passed();
    }
}
