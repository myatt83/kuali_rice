/*
 * Copyright 2007 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.msu.ebsp.rice.krad.service.impl;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.krad.bo.Attachment;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.dao.AttachmentDao;
import org.kuali.rice.krad.service.AttachmentService;

import edu.msu.ebsp.rice.krad.bo.DataBaseAttachment;

import java.util.UUID;



/**
 * Attachment service implementation which stores the attachments in DataBase rather than FileSystem , it stores in the pending directory first 
 * upon saving or routing the document it stores as clob in the database *  
 
 */
public class DataBaseAttachmentServiceImpl implements AttachmentService {
    
	
	private static Logger LOG = Logger.getLogger(DataBaseAttachmentServiceImpl.class);

    private ConfigurationService kualiConfigurationService;
    private AttachmentDao attachmentDao;

	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.krad.service.AttachmentService#createAttachment(org.kuali.rice.krad.bo.PersistableBusinessObject, java.lang.String, java.lang.String, int, java.io.InputStream, java.lang.String)
	 */
	public Attachment createAttachment(final PersistableBusinessObject parent,
                                       final String uploadedFileName, final String mimeType, final int fileSize,
                                       final InputStream fileContents, final String attachmentType) throws IOException {
		
		if ( LOG.isDebugEnabled() ) {
            LOG.debug("starting to create attachment for document: " + parent.getObjectId());
        }
        if (parent == null) {
            throw new IllegalArgumentException("invalid (null or uninitialized) document");
        }
        if (StringUtils.isBlank(uploadedFileName)) {
            throw new IllegalArgumentException("invalid (blank) fileName");
        }
        if (StringUtils.isBlank(mimeType)) {
            throw new IllegalArgumentException("invalid (blank) mimeType");
        }
        if (fileSize <= 0) {
            throw new IllegalArgumentException("invalid (non-positive) fileSize");
        }
        if (fileContents == null) {
            throw new IllegalArgumentException("invalid (null) inputStream");
        }
        final String uniqueFileNameGuid = UUID.randomUUID().toString();
        final DataBaseAttachment attachment = new DataBaseAttachment();
        attachment.setAttachmentIdentifier(uniqueFileNameGuid);
        attachment.setAttachmentFileName(uploadedFileName);
        attachment.setAttachmentFileSize(new Long(fileSize));
        attachment.setAttachmentMimeTypeCode(mimeType);
        attachment.setAttachmentTypeCode(attachmentType);       
        final byte[] bytes = new byte[fileContents.available()];
        fileContents.read(bytes);
        attachment.setAttachmentContent(bytes);      
        LOG.debug("finished creating attachment for document: " + parent.getObjectId());      
		return attachment;
	}
	
    /**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.kns.service.AttachmentService#deleteAttachmentContents(org.kuali.rice.kns.bo.Attachment)
	 */
	public void deleteAttachmentContents(final Attachment attachment) {
        if(attachment instanceof DataBaseAttachment){
            final DataBaseAttachment dbAttachment =(DataBaseAttachment)attachment;
            dbAttachment.setAttachmentContent(null);
        }
	}

	/**
	 * This overridden method ...
	 *
	 * @see org.kuali.rice.kns.service.AttachmentService#deletePendingAttachmentsModifiedBefore(long)
	 * As there is no pending directory now this method does not require implementation
	 */
	public void deletePendingAttachmentsModifiedBefore(final long modificationTime) {
     // purge pending attachment step
    }
    
	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.kns.service.AttachmentService#getAttachmentByNoteId(java.lang.Long)
	 */
	public Attachment getAttachmentByNoteId(final Long noteId) {
		
	    return attachmentDao.getAttachmentByNoteId(noteId);
	}

	/**
	 * This overridden method ... store it n database from FS
	 * 
	 * @see org.kuali.rice.krad.service.AttachmentService#moveAttachmentsWherePending(java.util.List, java.lang.String)
	 * As there is no pending directory now this method does not require implementation
	 */
	public void moveAttachmentsWherePending(final List notes, final String objectId) {
	//as no pending is there now
	}
 
	/**
	 * This overridden method ... store it n database from FS
	 * 
	 * @see org.kuali.rice.krad.service.AttachmentService#moveAttachmentWherePending(Note)
	 * As there is no pending directory now this method does not require implementation
	 */
    public void moveAttachmentWherePending(Note note) {
        // no pending directory
    }
	
	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.kns.service.AttachmentService#retrieveAttachmentContents(org.kuali.rice.kns.bo.Attachment)
	 */
	public InputStream retrieveAttachmentContents(final Attachment attachment)
			throws IOException {
	    if(attachment.getNoteIdentifier()!=null){ //saved
	        final DataBaseAttachment dbAttachment = (DataBaseAttachment) getAttachmentByNoteId(attachment.getNoteIdentifier());            
            return new BufferedInputStream(new ByteArrayInputStream(dbAttachment.getAttachmentContent()));
        }
	    else {
            final DataBaseAttachment dbAttachment = (DataBaseAttachment) attachment;
            return new BufferedInputStream(new ByteArrayInputStream(dbAttachment.getAttachmentContent())); 
        } 
	}
	
    // needed for Spring injection
    /**
     * Sets the data access object
     * 
     * @param d
     */
    public void setAttachmentDao(final AttachmentDao d) {
        this.attachmentDao = d;
    }

    /**
     * Retrieves a data access object
     */
    public AttachmentDao getAttachmentDao() {
        return attachmentDao;
    }

    /**
     * Gets the configService attribute. 
     * @return Returns the configService.
     */
    public ConfigurationService getKualiConfigurationService() {
        return kualiConfigurationService;
    }

    /**
     * Sets the configService attribute value.
     * @param configService The configService to set.
     */
    public void setKualiConfigurationService(ConfigurationService configService) {
        this.kualiConfigurationService = configService;
    }
}



