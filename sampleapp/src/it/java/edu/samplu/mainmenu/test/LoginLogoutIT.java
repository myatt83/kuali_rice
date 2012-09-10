/**
 * Copyright 2005-2011 The Kuali Foundation
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
package edu.samplu.mainmenu.test;

import edu.samplu.common.UpgradedSeleniumITBase;
import org.junit.Test;

/**
 * tests that user 'quickstart' can log in and log out
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class LoginLogoutIT extends UpgradedSeleniumITBase {
    @Override
    public String getTestUrl() {
        return PORTAL;
    }
    
    @Test
    public void testBlah() throws Exception {
        waitAndClick("link=Main Menu");
        waitForPageToLoad();
        waitAndClick("//input[@name='imageField' and @value='Logout']");
    }
}


