package org.kuali.rice.krad.data.provider.jpa.testbo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.eclipse.persistence.annotations.JoinFetch;
import org.eclipse.persistence.annotations.JoinFetchType;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.data.converters.EncryptionConverter;
import org.kuali.rice.krad.data.provider.annotation.AttributeRelationship;
import org.kuali.rice.krad.data.provider.annotation.CollectionRelationship;
import org.kuali.rice.krad.data.provider.annotation.InheritProperties;
import org.kuali.rice.krad.data.provider.annotation.InheritProperty;
import org.kuali.rice.krad.data.provider.annotation.Label;
import org.kuali.rice.krad.data.provider.annotation.NonPersistentProperty;
import org.kuali.rice.krad.data.provider.annotation.Security;

@Entity
@Table(name = "KRTST_TEST_TABLE_T")
@Label("Label From Annotation")
public class TestDataObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "PK_PROP")
	String primaryKeyProperty;

	@Column(name = "STR_PROP", length = 40)
	@Label("Attribute Label From Annotation")
	@NotNull
	String stringProperty;

	@Column(name = "LONG_STR_PROP", length = 200)
	String longStringProperty;

	@Temporal(TemporalType.DATE)
	@Column(name = "DATE_PROP")
	Date dateProperty;
	@Column(name = "CURR_PROP")
	@Digits(
			integer = 10,
			fraction = 2)
	@DecimalMin(
			value = "0.00",
			message = "The currency amount may not be less than zero.")
	KualiDecimal currencyProperty;
	@Column(name = "NON_STANDARD")
	// @Convert(
	// converter = NonStandardDataTypeConverter.class)
	NonStandardDataType nonStandardDataType;

	@Column(name="BOOL_PROP")
	// @Convert("YN_BooleanConverter")
	Boolean booleanProperty;
	
	@Transient
	String nonPersistedProperty;

	@Convert(
			converter = EncryptionConverter.class)
	@Column(
			name = "ENCR_PROP")
	String encryptedProperty;

	@Column(
			name = "RO_PROP")
	@Security(
			readOnly = true)
	String readOnlyProperty;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OrderBy("collectionKeyProperty ASC")
	List<CollectionDataObject> collectionProperty;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	@OrderBy("collectionKeyProperty ASC")
	List<CollectionDataObjectTwo> collectionPropertyTwo;

	@CollectionRelationship(
			attributeRelationships = @AttributeRelationship(
					parentAttributeName = "dateProperty",
					childAttributeName = "collectionDateProperty"))
	List<SomeOtherCollection> someOtherCollection;

	@ManyToMany
	@JoinTable(
			name = "KRDATA_TEST_INDIR_LINK_T",
			joinColumns = @JoinColumn(
					name = "PK_PROP",
					referencedColumnName = "COLL_PK_PROP"),
			inverseJoinColumns = @JoinColumn(
					name = "COLL_PK_PROP",
					referencedColumnName = "PK_PROP"))
	List<IndirectlyLinkedCollectionDataObject> indirectCollection;

	@ManyToOne(
			fetch = FetchType.LAZY,
			cascade = CascadeType.REFRESH)
	@JoinColumn(
			name = "STR_PROP",
			insertable = false,
			updatable = false)
	@InheritProperty(
			name = "someOtherStringProperty")
	ReferencedDataObject referencedObject;

	@ManyToOne(
			fetch = FetchType.EAGER,
			cascade = CascadeType.REFRESH)
	@JoinColumns({ @JoinColumn(
			name = "STR_PROP",
			referencedColumnName = "STR_PROP",
			insertable = false,
			updatable = false), @JoinColumn(
			name = "DATE_PROP",
			referencedColumnName = "DATE_PROP",
			insertable = false,
			updatable = false) })
	@InheritProperties({ @InheritProperty(
			name = "someOtherStringProperty",
			label = @Label("Overridden Inherited Property Label")) })
	AnotherReferencedDataObject anotherReferencedObject;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinFetch(JoinFetchType.OUTER)
	@PrimaryKeyJoinColumn
	YetAnotherReferencedDataObject yetAnotherReferencedObject;

	@Transient
	Object extension;

	@NonPersistentProperty
	@Label("Test Data Object")
	String getKeyAndString() {
		return primaryKeyProperty + "-" + stringProperty;
	}

	public List<CollectionDataObject> getCollectionProperty() {
		return collectionProperty;
	}

	public void setCollectionProperty(List<CollectionDataObject> collectionProperty) {
		this.collectionProperty = collectionProperty;
	}

	public String getStringProperty() {
		return stringProperty;
	}

	public void setStringProperty(String stringProperty) {
		this.stringProperty = stringProperty;
	}

	public String getPrimaryKeyProperty() {
		return primaryKeyProperty;
	}

	public void setPrimaryKeyProperty(String primaryKeyProperty) {
		this.primaryKeyProperty = primaryKeyProperty;
	}

	public Date getDateProperty() {
		return dateProperty;
	}

	public void setDateProperty(Date dateProperty) {
		this.dateProperty = dateProperty;
	}

	public KualiDecimal getCurrencyProperty() {
		return currencyProperty;
	}

	public void setCurrencyProperty(KualiDecimal currencyProperty) {
		this.currencyProperty = currencyProperty;
	}

	public List<CollectionDataObjectTwo> getCollectionPropertyTwo() {
		return collectionPropertyTwo;
	}

	public void setCollectionPropertyTwo(List<CollectionDataObjectTwo> collectionPropertyTwo) {
		this.collectionPropertyTwo = collectionPropertyTwo;
	}

	public ReferencedDataObject getReferencedObject() {
		return referencedObject;
	}

	public void setReferencedObject(ReferencedDataObject referencedObject) {
		this.referencedObject = referencedObject;
	}

	public NonStandardDataType getNonStandardDataType() {
		return nonStandardDataType;
	}

	public void setNonStandardDataType(NonStandardDataType nonStandardDataType) {
		this.nonStandardDataType = nonStandardDataType;
	}

	public List<IndirectlyLinkedCollectionDataObject> getIndirectCollection() {
		return indirectCollection;
	}

	public void setIndirectCollection(List<IndirectlyLinkedCollectionDataObject> indirectCollection) {
		this.indirectCollection = indirectCollection;
	}

	public String getNonPersistedProperty() {
		return nonPersistedProperty;
	}

	public void setNonPersistedProperty(String nonPersistedProperty) {
		this.nonPersistedProperty = nonPersistedProperty;
	}

	public AnotherReferencedDataObject getAnotherReferencedObject() {
		return anotherReferencedObject;
	}

	public void setAnotherReferencedObject(AnotherReferencedDataObject anotherReferencedObject) {
		this.anotherReferencedObject = anotherReferencedObject;
	}

	public YetAnotherReferencedDataObject getYetAnotherReferencedObject() {
		return yetAnotherReferencedObject;
	}

	public void setYetAnotherReferencedObject(YetAnotherReferencedDataObject yetAnotherReferencedObject) {
		this.yetAnotherReferencedObject = yetAnotherReferencedObject;
	}

	public String getLongStringProperty() {
		return longStringProperty;
	}

	public void setLongStringProperty(String longStringProperty) {
		this.longStringProperty = longStringProperty;
	}

	public boolean isBooleanProperty() {
		return booleanProperty;
	}

	public void setBooleanProperty(boolean booleanProperty) {
		this.booleanProperty = booleanProperty;
	}

	public Object getExtension() {
		return extension;
	}

	public void setExtension(Object extension) {
		this.extension = extension;
	}

}
