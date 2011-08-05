package org.kuali.rice.kew.docsearch;

import org.kuali.rice.core.api.uif.RemotableAttributeError;
import org.kuali.rice.kew.api.document.lookup.DocumentLookupConfiguration;
import org.kuali.rice.kew.doctype.bo.DocumentType;

import java.util.List;
import java.util.Map;

/**
 * Handles communication between {@link org.kuali.rice.kew.framework.document.lookup.DocumentSearchCustomizationService}
 * endpoints in order to invoke document search customizations from various client applications.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface DocumentSearchCustomizationMediator {

    DocumentLookupConfiguration getDocumentLookupConfiguration(DocumentType documentType);

    boolean isResultProcessingNeeded(DocumentType documentType);

    List<RemotableAttributeError> validateSearchFieldParameters(DocumentType documentType, Map<String, List<String>> parameters);

}
