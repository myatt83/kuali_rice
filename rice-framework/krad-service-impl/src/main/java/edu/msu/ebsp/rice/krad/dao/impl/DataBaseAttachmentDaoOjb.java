/*
 * Copyright 2007-2009 The Kuali Foundation
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
package edu.msu.ebsp.rice.krad.dao.impl;

import org.apache.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.rice.krad.bo.Attachment;
import org.kuali.rice.krad.dao.AttachmentDao;


import edu.msu.ebsp.rice.krad.bo.DataBaseAttachment;

/**
 *  This class is the OJB implementation of the NoteDao interface.
 *
 */
public class DataBaseAttachmentDaoOjb extends PlatformAwareDaoBaseOjb implements AttachmentDao {
    private static Logger LOG = Logger.getLogger(DataBaseAttachmentDaoOjb.class);

    /**
     * Default constructor.
     */
    public DataBaseAttachmentDaoOjb() {
        super();
    }
    
    public DataBaseAttachment getAttachmentByNoteId(final Long noteId) {
        final Criteria crit = new Criteria();
        crit.addEqualTo("noteIdentifier", noteId);
        return (DataBaseAttachment) this.getPersistenceBrokerTemplate().getObjectByQuery(new QueryByCriteria(DataBaseAttachment.class, crit));          
    }

}
