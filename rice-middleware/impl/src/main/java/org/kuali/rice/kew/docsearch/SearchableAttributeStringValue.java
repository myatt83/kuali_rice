/**
 * Copyright 2005-2013 The Kuali Foundation
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
package org.kuali.rice.kew.docsearch;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.framework.persistence.jpa.OrmUtils;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeFactory;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeString;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.service.KEWServiceLocator;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@Entity
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
@Table(name="KREW_DOC_HDR_EXT_T")
//@Sequence(name="KREW_SRCH_ATTR_S",property="searchableAttributeValueId")
@NamedQueries({
	@NamedQuery(name="SearchableAttributeStringValue.FindByDocumentId", query="select s from "
            + "SearchableAttributeStringValue as s where s.documentId = :documentId"),
	@NamedQuery(name="SearchableAttributeStringValue.FindByKey", query="select s from SearchableAttributeStringValue as "
            + "s where s.documentId = :documentId and s.searchableAttributeKey = :searchableAttributeKey")
})
public class SearchableAttributeStringValue extends SearchableAttributeBase implements CaseAwareSearchableAttributeValue, Serializable {

    private static final long serialVersionUID = 8696089933682052078L;

    private static final String ATTRIBUTE_DATABASE_TABLE_NAME = "KREW_DOC_HDR_EXT_T";
    private static final boolean DEFAULT_WILDCARD_ALLOWANCE_POLICY = true;
    private static final boolean ALLOWS_RANGE_SEARCH = true;
    private static final boolean ALLOWS_CASE_INSENSITIVE_SEARCH = true;
    private static final String ATTRIBUTE_XML_REPRESENTATION = KewApiConstants.SearchableAttributeConstants.DATA_TYPE_STRING;
    private static final int STRING_MAX_LENGTH = 2000; // should match table creation

    @Id
    @GeneratedValue(generator="KREW_SRCH_ATTR_S")
	@Column(name="DOC_HDR_EXT_ID")
	private String searchableAttributeValueId;
    @Column(name="VAL")
	private String searchableAttributeValue;

    /**
     * Default constructor.
     */
    public SearchableAttributeStringValue() {
    	super();
        this.ojbConcreteClass = this.getClass().getName();
    }

    /* (non-Javadoc)
     * @see org.kuali.rice.kew.docsearch.SearchableAttributeValue#setupAttributeValue(java.lang.String)
     */
    public void setupAttributeValue(String value) {
    	this.setSearchableAttributeValue(value);
    }

	/* (non-Javadoc)
	 * @see org.kuali.rice.kew.docsearch.SearchableAttributeValue#setupAttributeValue(java.sql.ResultSet, java.lang.String)
	 */
	public void setupAttributeValue(ResultSet resultSet, String columnName) throws SQLException {
		this.setSearchableAttributeValue(resultSet.getString(columnName));
	}

	/* (non-Javadoc)
	 * @see org.kuali.rice.kew.docsearch.SearchableAttributeValue#getSearchableAttributeDisplayValue()
	 */
    public String getSearchableAttributeDisplayValue() {
        return getSearchableAttributeValue();
    }

	/* (non-Javadoc)
	 * @see org.kuali.rice.kew.docsearch.SearchableAttributeValue#getAttributeDataType()
	 */
	public String getAttributeDataType() {
		return ATTRIBUTE_XML_REPRESENTATION;
	}

	/* (non-Javadoc)
	 * @see org.kuali.rice.kew.docsearch.SearchableAttributeValue#getAttributeTableName()
	 */
	public String getAttributeTableName() {
		return ATTRIBUTE_DATABASE_TABLE_NAME;
	}

    /* (non-Javadoc)
	 * @see org.kuali.rice.kew.docsearch.SearchableAttributeValue#allowsWildcardsByDefault()
	 */
	public boolean allowsWildcards() {
		return DEFAULT_WILDCARD_ALLOWANCE_POLICY;
	}

    /* (non-Javadoc)
	 * @see org.kuali.rice.kew.docsearch.SearchableAttributeValue#allowsCaseInsensitivity()
	 */
	public boolean allowsCaseInsensitivity() {
		return ALLOWS_CASE_INSENSITIVE_SEARCH;
	}

    /* (non-Javadoc)
	 * @see org.kuali.rice.kew.docsearch.SearchableAttributeValue#allowsRangeSearches()
	 */
	public boolean allowsRangeSearches() {
		return ALLOWS_RANGE_SEARCH;
	}

	/**
	 * @return true if the {@code valueEntered} parameter is not null and is equal to or
	 * less than the specified max length defined by {@link #STRING_MAX_LENGTH}
	 *
	 * @see org.kuali.rice.kew.docsearch.SearchableAttributeValue#isPassesDefaultValidation()
	 */
	public boolean isPassesDefaultValidation(String valueEntered) {
	    if (valueEntered != null && (valueEntered.length() > STRING_MAX_LENGTH)) {
	        return false;
	    }
		return true;
	}

    /* (non-Javadoc)
     * @see org.kuali.rice.kew.docsearch.SearchableAttributeValue#isRangeValid(java.lang.String, java.lang.String)
     */
    public Boolean isRangeValid(String lowerValue, String upperValue) {
        return isRangeValid(lowerValue, upperValue, true);
    }

    /* (non-Javadoc)
     * @see org.kuali.rice.kew.docsearch.CaseAwareSearchableAttributeValue#isRangeValid(java.lang.String, java.lang.String, boolean)
     */
    public Boolean isRangeValid(String lowerValue, String upperValue, boolean caseSensitive) {
        if (allowsRangeSearches()) {
            return StringUtils.isBlank(lowerValue) ||
                   StringUtils.isBlank(upperValue) ||
                   (caseSensitive ?
                     ObjectUtils.compare(lowerValue, upperValue) <= 0 :
                     String.CASE_INSENSITIVE_ORDER.compare(lowerValue, upperValue) <= 0);
        }
        return null;
    }

	public String getSearchableAttributeValue() {
		return searchableAttributeValue;
	}

	public void setSearchableAttributeValue(String searchableAttributeValue) {
		this.searchableAttributeValue = searchableAttributeValue;
	}

	public String getSearchableAttributeValueId() {
		return searchableAttributeValueId;
	}

	public void setSearchableAttributeValueId(String searchableAttributeValueId) {
		this.searchableAttributeValueId = searchableAttributeValueId;
	}

	//@PrePersist
	public void beforeInsert(){
		OrmUtils.populateAutoIncValue(this, KEWServiceLocator.getEntityManagerFactory().createEntityManager());
	}

    @Override
    public DocumentAttributeString toDocumentAttribute() {
        return DocumentAttributeFactory.createStringAttribute(getSearchableAttributeKey(), getSearchableAttributeValue());
    }
}

