/*
 * Copyright 2007 The Kuali Foundation
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
package edu.msu.ebsp.rice.krad.bo;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.kuali.rice.krad.bo.Attachment;
/**
 *  This stores the edocs attachment as a clob in the database
 */
@Entity
@Table(name="KRNS_ATT_T")
public class DataBaseAttachment extends Attachment {

	@Column(name="FILE_CONTENT")
	private byte[] attachmentContent;
	
	 /**
     * Default constructor.
     */
    public DataBaseAttachment() {

    }


	public byte[] getAttachmentContent() {
        return attachmentContent;
    }

    public void setAttachmentContent(byte[] attachmentContent) {
        this.attachmentContent = attachmentContent;
    }

   
	
}

