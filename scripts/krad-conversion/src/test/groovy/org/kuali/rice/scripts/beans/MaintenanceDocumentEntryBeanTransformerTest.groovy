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
package org.kuali.rice.scripts.beans

import groovy.util.logging.Log
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Tests for the {@link org.kuali.rice.scripts.beans.MaintenanceDocumentEntryBeanTransformer} class.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@Log
class MaintenanceDocumentEntryBeanTransformerTest extends BeanTransformerTestBase {

    MaintenanceDocumentEntryBeanTransformer maintenanceDocumentEntryBeanTransformer;
    String defaultTestFilePath;
    String customTestFilePath;
    @Before
    void setUp() {
        super.setUp();
        defaultTestFilePath = getDictionaryTestDir() + "MaintenanceDefinitionSample.xml";
        customTestFilePath = getDictionaryTestDir() + "MaintenanceDefinitionCustomDPCSample.xml";
        maintenanceDocumentEntryBeanTransformer = new MaintenanceDocumentEntryBeanTransformer();
        maintenanceDocumentEntryBeanTransformer.init(config);
    }

    /**
     * Verifies maintenance document entry has been converted into a valid maintenance doc entry and view
     */
    @Test
    void testTransformMaintenanceDocumentEntryBeanPlaceholder() {
        def ddRootNode = getFileRootNode(defaultTestFilePath);
        ddRootNode.bean.each { bean -> maintenanceDocumentEntryBeanTransformer.fixNamespaceProperties(bean) }

        def beanNode = ddRootNode.bean.find { "AttachmentSampleMaintenanceDocument-parentBean".equals(it.@parent) }
        try {
            maintenanceDocumentEntryBeanTransformer.transformMaintenanceDocumentEntryBean(beanNode);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("exception occurred in testing");
        }

        checkBeanParentExists(ddRootNode, "AttachmentSampleMaintenanceDocument-uifMaintenanceDocumentEntry-parentBean");
        checkBeanParentExists(ddRootNode, "AttachmentSampleMaintenanceDocument-MaintenanceView-parentBean");
    }

    /**
     * Verifies maintenance document entry has been converted into a valid maintenance doc entry and view
     */
    @Test
    void testTransformMaintenanceDocumentEntryBean() {
        def ddRootNode = getFileRootNode(defaultTestFilePath);
        ddRootNode.bean.each { bean -> maintenanceDocumentEntryBeanTransformer.fixNamespaceProperties(bean) }

        def beanNode = ddRootNode.bean.find { "MaintenanceDocumentEntry".equals(it.@parent) }
        try {
            maintenanceDocumentEntryBeanTransformer.transformMaintenanceDocumentEntryBean(beanNode);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("exception occurred in testing");
        }

        checkBeanParentExists(ddRootNode, "uifMaintenanceDocumentEntry");
        def resultMDENode = ddRootNode.bean.find { "uifMaintenanceDocumentEntry".equals(it.@parent) }
        def mdeCheckedProperties = [];
        checkBeanStructure(resultMDENode, mdeCheckedProperties, ["businessObjectEntry"]);
        //checks if the default documentPresentationControllerClass has been added
        checkBeanPropertyValueExists(resultMDENode,"documentPresentationControllerClass","org.kuali.rice.krad.maintenance.MaintenanceViewPresentationControllerBase");
        //checks if the default documentAuthorizerClass has been added
        checkBeanPropertyValueExists(resultMDENode,"documentAuthorizerClass","org.kuali.rice.krad.maintenance.MaintenanceDocumentAuthorizerBase");

        def umvCheckedProperties = ["dataObjectClassName"];
        checkBeanParentExists(ddRootNode, "Uif-MaintenanceView");
        def resultMVNode = ddRootNode.bean.find { "Uif-MaintenanceView".equals(it.@parent) }
        checkBeanStructure(resultMVNode, umvCheckedProperties, ["maintainableSections"]);

    }

    /**
     * Verifies that a documentPresentationControllerClass property is added with empty value and a comment is added to
     * specify the value if a custom class is specified.
     */
    @Test
    void testTransformMaintenanceDocumentEntryNonDefaultDPCBean() {
        def ddRootNode = getFileRootNode(customTestFilePath);
        ddRootNode.bean.each { bean -> maintenanceDocumentEntryBeanTransformer.fixNamespaceProperties(bean) }

        def beanNode = ddRootNode.bean.find { "MaintenanceDocumentEntry".equals(it.@parent) }
        try {
            maintenanceDocumentEntryBeanTransformer.transformMaintenanceDocumentEntryBean(beanNode);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("exception occurred in testing");
        }

        checkBeanParentExists(ddRootNode, "uifMaintenanceDocumentEntry");
        def resultMDENode = ddRootNode.bean.find { "uifMaintenanceDocumentEntry".equals(it.@parent) }
        checkBeanPropertyValueExists(resultMDENode,"documentPresentationControllerClass","");

        def foundComment = false;
        beanNode.parent().meta.findAll { it.@key == "comment" }.each{ if (it?.@value?.equals("TODO - Add documentPresentationControllerClass for bean Id: " + beanNode.@id)) {foundComment = true;}  };
        Assert.assertTrue("No comment found for custom documentPresentationControllerClass", foundComment);

        }

    /**
     * Verifies that a documentAuthorizerClass property is added with empty value and a comment is added to
     * specify the value if a custom class is specified.
     */
    @Test
    void testTransformMaintenanceDocumentEntryNonDefaultDACBean() {
        def ddRootNode = getFileRootNode(customTestFilePath);
        ddRootNode.bean.each { bean -> maintenanceDocumentEntryBeanTransformer.fixNamespaceProperties(bean) }

        def beanNode = ddRootNode.bean.find { "MaintenanceDocumentEntry".equals(it.@parent) }
        try {
            maintenanceDocumentEntryBeanTransformer.transformMaintenanceDocumentEntryBean(beanNode);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("exception occurred in testing");
        }

        checkBeanParentExists(ddRootNode, "uifMaintenanceDocumentEntry");
        def resultMDENode = ddRootNode.bean.find { "uifMaintenanceDocumentEntry".equals(it.@parent) }
        checkBeanPropertyValueExists(resultMDENode,"documentAuthorizerClass","");

        def foundComment = false;
        beanNode.parent().meta.findAll { it.@key == "comment" }.each{ if (it?.@value?.equals("TODO - Add documentAuthorizerClass for bean Id: " + beanNode.@id)) {foundComment = true;}  };
        Assert.assertTrue("No comment found for custom documentAuthorizerClass", foundComment);
    }


    @Test
    public void testTransformMaintainableSectionsProperty() {
        def ddRootNode = getFileRootNode(defaultTestFilePath);
        def beanNode = ddRootNode.bean.find { "MaintenanceDocumentEntry".equals(it.@parent) };
        Node resultNode = beanNode.replaceNode {
            bean(parent: "Uif-MaintenanceView") {
                maintenanceDocumentEntryBeanTransformer.transformMaintainableSectionsProperty(delegate, beanNode)
            }
        }

        checkBeanPropertyExists(resultNode, "items");
        def refSize = resultNode.property.list.ref.size();
        Assert.assertEquals("number of copied references", 1, refSize)
        def sectionSize = resultNode.property.list.bean.findAll { ["Uif-VerticalBoxSection", "Uif-MaintenanceGridSection", "Uif-MaintenanceStackedCollectionSection"].contains(it.@parent) }.size();
        Assert.assertEquals("number of converted section definitions", 1, sectionSize);
    }

    /**
     * Tests conversion of lookup definition's result fields into appropriate property
     *
     */
    @Test
    public void testTransformMaintainableFieldsProperty() {
        String parentBeanName = "AttachmentSampleMaintenanceDocument-parentBean";
        String sectionDefName = "MultiAttachmentSampleMaintenanceDocument-AttachmentList";
        def ddRootNode = getFileRootNode(defaultTestFilePath);
        def collectionDefBean = getCollectionDefinitionBean(ddRootNode, parentBeanName, sectionDefName);
        collectionDefBean = collectionDefBean.replaceNode {
            bean(parent: "Uif-MaintenanceStackedCollectionSection") {
                maintenanceDocumentEntryBeanTransformer.transformMaintainableFieldsProperty(delegate, collectionDefBean);
            }
        }

        def resultsFieldProperty = collectionDefBean.property.find { "items".equals(it.@name) };
        def attrFieldSize = resultsFieldProperty.list.bean.findAll { "Uif-InputField".equals(it.@parent) }.size();
        Assert.assertEquals("number of converted input fields", 2, attrFieldSize);
    }

    /**
     * Tests transformation of the includeAddLine property to addLineActions
     *
     */
    @Test
    public void transformMaintainableCollectionSectionDefinitionBeanWithIncludeAddLine() {
        def ddRootNode = getFileRootNode(defaultTestFilePath);
        def parentBean = ddRootNode.bean.find { "AttachmentSampleMaintenanceDocument-parentBean".equals(it.@id) };
        def attachmentListBean = parentBean.property.list.bean.find { "MultiAttachmentSampleMaintenanceDocument-AttachmentList".equals(it.@id) };

        attachmentListBean = attachmentListBean.replaceNode {
            bean(id: "MultiAttachmentSampleMaintenanceDocument-AttachmentList") {
                maintenanceDocumentEntryBeanTransformer.transformMaintainableSectionDefinitionBean(delegate, attachmentListBean);
            }
        }
         def collectionDefBean = attachmentListBean.bean.find { "Uif-VerticalBoxSection".equals(it.@parent) }.property.find{"items".equals(it.@name) }.
                    list.bean.find { "Uif-MaintenanceStackedCollectionSection".equals(it.@parent) };
         def addLineActionsSize = collectionDefBean?.property.findAll { "addLineActions".equals(it.@name) }.size();
         Assert.assertEquals("number of addLineActions", 1, addLineActionsSize);

        def methodToCall = collectionDefBean.property.find { "addLineActions".equals(it.@name) }.attributes().find{"p:methodToCall"};
        def actionLabel = collectionDefBean.property.find { "addLineActions".equals(it.@name) }.attributes().find{"p:actionLabel"};
        def hiddenAttr = collectionDefBean.property.find { "addLineActions".equals(it.@name) }.attributes().find{"p:hidden"};

        Assert.assertNotNull(methodToCall);
        Assert.assertNotNull(actionLabel);
        Assert.assertNotNull(hiddenAttr);

    }

    /**
     * Tests transformation of the includeMultiValueLookupLine property to collectionLookup with a Uif-QuickFinder
     *
     */
    @Test
    public void transformMaintainableCollectionSectionDefinitionBeanWithIncludeMultiValueLookupLine() {
        def ddRootNode = getFileRootNode(defaultTestFilePath);
        def parentBean = ddRootNode.bean.find { "AttachmentSampleMaintenanceDocument-parentBean".equals(it.@id) };
        def attachmentListBean = parentBean.property.list.bean.find { "MultiAttachmentSampleMaintenanceDocument-AttachmentList".equals(it.@id) };
        def sourceClassName = attachmentListBean.property.find{"maintainableItems".equals(it.@name)}.list.bean.find
        {"MaintainableCollectionDefinition".equals(it.@parent)}.property.find{"sourceClassName".equals(it.@name)}.@value

        attachmentListBean = attachmentListBean.replaceNode {
            bean(id: "MultiAttachmentSampleMaintenanceDocument-AttachmentList") {
                maintenanceDocumentEntryBeanTransformer.transformMaintainableSectionDefinitionBean(delegate, attachmentListBean);
            }
        }
        def collectionDefBean = attachmentListBean.bean.find { "Uif-VerticalBoxSection".equals(it.@parent) }.property.find{"items".equals(it.@name) }.
                list.bean.find { "Uif-MaintenanceStackedCollectionSection".equals(it.@parent) };
        def collectionLookupSize = collectionDefBean?.property.findAll { "collectionLookup".equals(it.@name) }.size();
        Assert.assertEquals("number of collectionLookups", 1, collectionLookupSize);

        def quickFinderBean = collectionDefBean.property.find { "collectionLookup".equals(it.@name) }.bean.find{"Uif-CollectionQuickFinder".equals(it.@parent)};
        Assert.assertNotNull(quickFinderBean);

        def dataObjectClassName = quickFinderBean.property.find { "dataObjectClassName".equals(it.@name) }.@value;
        Assert.assertNotNull(dataObjectClassName);
        Assert.assertEquals(sourceClassName,dataObjectClassName);

        def fieldConversion = quickFinderBean.property.find { "fieldConversions".equals(it.@name) }.@value;
        Assert.assertNotNull(fieldConversion);
        Assert.assertEquals("description:description,attachmentFile:newAttachmentFile",fieldConversion);


    }

    /**
     * Tests transformation of the includeMultiValueLookupLine property to collectionLookup with a Uif-QuickFinder does
     * not happen when  includeMultiValueLookupLine is false
     *
     */
    @Test
    public void transformIncludeMultiValueLookupLineFalse() {
        def ddRootNode = getFileRootNode(defaultTestFilePath);
        def parentBean = ddRootNode.bean.find { "AttachmentSampleMaintenanceDocument-parentBean".equals(it.@id) };
        def attachmentListBean = parentBean.property.list.bean.find { "MultiAttachmentSampleMaintenanceDocument-AttachmentList".equals(it.@id) };
        attachmentListBean.property.find{"maintainableItems".equals(it.@name)}.list.bean.find
        {"MaintainableCollectionDefinition".equals(it.@parent)}.property.find{"includeMultipleLookupLine".equals(it.@name)}.@value = false;

        attachmentListBean = attachmentListBean.replaceNode {
            bean(id: "MultiAttachmentSampleMaintenanceDocument-AttachmentList") {
                maintenanceDocumentEntryBeanTransformer.transformMaintainableSectionDefinitionBean(delegate, attachmentListBean);
            }
        }
        def collectionDefBean = attachmentListBean.bean.find { "Uif-VerticalBoxSection".equals(it.@parent) }.property.find{"items".equals(it.@name) }.
                list.bean.find { "Uif-MaintenanceStackedCollectionSection".equals(it.@parent) };
        def collectionLookupSize = collectionDefBean?.property.findAll { "collectionLookup".equals(it.@name) }.size();
        Assert.assertEquals("number of collectionLookups", 0, collectionLookupSize);

        def quickFinderBean = collectionDefBean.property.find { "collectionLookup".equals(it.@name) }?.bean.find{"Uif-CollectionQuickFinder".equals(it.@parent)};
        Assert.assertNull("QuickFinderBean should be null",quickFinderBean);

    }

    /**
     * Tests transformation of the includeMultiValueLookupLine property to collectionLookup with a Uif-QuickFinder when
     * no sourceClassName is specified. Defaults to the businessObjectClass
     *
     */

    @Test
    public void transformIncludeMultiValueLookupLineNoSourceClass() {
        def ddRootNode = getFileRootNode(defaultTestFilePath);
        def parentBean = ddRootNode.bean.find { "AttachmentSampleMaintenanceDocument-parentBean".equals(it.@id) };
        def attachmentListBean = parentBean.property.list.bean.find { "MultiAttachmentSampleMaintenanceDocument-AttachmentList".equals(it.@id) };
        def businessObjectClass = attachmentListBean.property.find{"maintainableItems".equals(it.@name)}.list.bean.find
        {"MaintainableCollectionDefinition".equals(it.@parent)}.property.find{"businessObjectClass".equals(it.@name)}.@value;

        attachmentListBean.property.find{"maintainableItems".equals(it.@name)}.list.bean.find
        {"MaintainableCollectionDefinition".equals(it.@parent)}.property.find{"sourceClassName".equals(it.@name)}.@value = "";

        attachmentListBean = attachmentListBean.replaceNode {
            bean(id: "MultiAttachmentSampleMaintenanceDocument-AttachmentList") {
                maintenanceDocumentEntryBeanTransformer.transformMaintainableSectionDefinitionBean(delegate, attachmentListBean);
            }
        }
        def collectionDefBean = attachmentListBean.bean.find { "Uif-VerticalBoxSection".equals(it.@parent) }.property.find{"items".equals(it.@name) }.
                list.bean.find { "Uif-MaintenanceStackedCollectionSection".equals(it.@parent) };
        def collectionLookupSize = collectionDefBean?.property.findAll { "collectionLookup".equals(it.@name) }.size();
        Assert.assertEquals("number of collectionLookups", 1, collectionLookupSize);

        def quickFinderBean = collectionDefBean.property.find { "collectionLookup".equals(it.@name) }.bean.find{"Uif-CollectionQuickFinder".equals(it.@parent)};
        Assert.assertNotNull(quickFinderBean);

        def dataObjectClassName = quickFinderBean.property.find { "dataObjectClassName".equals(it.@name) }.@value;
        Assert.assertNotNull(dataObjectClassName);
        Assert.assertEquals(businessObjectClass,dataObjectClassName);

        def fieldConversion = quickFinderBean.property.find { "fieldConversions".equals(it.@name) }.@value;
        Assert.assertNotNull(fieldConversion);
        Assert.assertEquals("description:description,attachmentFile:newAttachmentFile",fieldConversion);

    }

    /**
     * Tests transformation of the webScriptFiles property to additionalScriptFiles
     *
     */
    @Test
    public void transformWebScriptFilesTest() {
        def ddRootNode = getFileRootNode(defaultTestFilePath);
        ddRootNode.bean.each { bean -> maintenanceDocumentEntryBeanTransformer.fixNamespaceProperties(bean) }
        def beanNode = ddRootNode.bean.find { "MaintenanceDocumentEntry".equals(it.@parent) };
        try {
            maintenanceDocumentEntryBeanTransformer.transformMaintenanceDocumentEntryBean(beanNode);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("exception occurred in testing");
        }
        checkBeanParentExists(ddRootNode, "Uif-MaintenanceView");

        def resultMDENode = ddRootNode.bean.find { "Uif-MaintenanceView".equals(it.@parent) }
        checkBeanPropertyExists(resultMDENode, "additionalScriptFiles");

        def foundComment = false;
        beanNode.parent().meta.findAll { it.@key == "comment" }.each {
            if (it?.@value?.equals("TODO: Check if script files are still relevant and correct")) {
                foundComment = true;
            }
        };
        Assert.assertTrue("No comment found for custom additionalScriptFiles", foundComment);

        def noOfFiles = resultMDENode.property.find { "additionalScriptFiles".equals(it.@name) }.list.value.size();
        Assert.assertEquals("No of Script Files", noOfFiles, 4);

        def customRootNode = getFileRootNode(customTestFilePath);
        customRootNode.bean.each { bean -> maintenanceDocumentEntryBeanTransformer.fixNamespaceProperties(bean) }
        // def webScriptFileNode = ddRootNode.bean.find { "MaintenanceDocumentEntry".equals(it.@parent) }.property.find{"webScriptFiles".equals(it.@name)};

        // System.out.println("webScriptFileNode::"+ddRootNode.children().getProperties());
        //boolean removed = ddRootNode.children().getProperties().remove(webScriptFileNode) ;
        //System.out.println("removed::"+removed);

        def noScriptFiles = customRootNode.bean.find { "MaintenanceDocumentEntry".equals(it.@parent) };
        try {
            maintenanceDocumentEntryBeanTransformer.transformMaintenanceDocumentEntryBean(noScriptFiles);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("exception occurred in testing");
        }

        checkBeanParentExists(customRootNode, "Uif-MaintenanceView");

        def resultNode = customRootNode.bean.find { "Uif-MaintenanceView".equals(it.@parent) }
        checkBeanPropertyNotExists(resultNode, "additionalScriptFiles");

        foundComment = false;
        resultNode.parent().meta.findAll { it.@key == "comment" }.each {
            if (it?.@value?.equals("TODO: Check if script files are still relevant and correct")) {
                foundComment = true;
            }
        };
        Assert.assertFalse("No comment found for additionalScriptFiles", foundComment);

    }

    /**
     * retrieves collection definition from a maintenance document entry
     *
     * @param rootNode
     * @param parentBeanName
     * @param sectionDefBeanName
     * @return
     */
    Node getCollectionDefinitionBean(def rootNode, String parentBeanName, String sectionDefBeanName) {
        def beanNode = rootNode.bean.find { parentBeanName.equals(it.@id) };
        def maintainableItemsList = beanNode.property.find { "maintainableSections".equals(it.@name) }.list;
        def sectionDefBean = maintainableItemsList.bean.find { sectionDefBeanName.equals(it.@id) };
        def collectionDefBean = sectionDefBean.property.list.bean.find { "MaintainableCollectionDefinition".equals(it.@parent) };
        return collectionDefBean;
    }

    @Test
    public void testTransformMaintainableSectionDefinitionBean() {
        String MAINT_SECT_DEF_BEAN_ID = "AttachmentSampleMaintenanceDocument-EditAttachment-parentBean";
        def ddRootNode = getFileRootNode(defaultTestFilePath);
        def beanNode = ddRootNode.bean.find { MAINT_SECT_DEF_BEAN_ID.equals(it.@id) };
        Node resultNode = beanNode.replaceNode {
            bean(parent: "Uif-MaintenanceView") {
                property(name: "items") {
                    list {
                        maintenanceDocumentEntryBeanTransformer.transformMaintainableSectionDefinitionBean(delegate, beanNode)
                    }
                }
            }
        }
        def sectionResultNode = resultNode.property.find{ "items".equals(it.@name)}.list.bean[0];

        // check that new grid or vertical box section includes help
        checkBeanPropertyExists(sectionResultNode, "items");
        checkBeanPropertyExists(sectionResultNode, "help");
        checkBeanPropertyExists(sectionResultNode, "disclosure.defaultOpen");

        def refSize = resultNode.property.find { "items".equals(it.@name) }.list.bean.size();
        Assert.assertEquals("number of beans created", 1, refSize)
        //def sectionSize = resultNode.property.list.bean.findAll { ["Uif-MaintenanceGridSection", "Uif-MaintenanceStackedCollectionSection"].contains(it.@parent) }.size();
        //Assert.assertEquals("number of converted section definitions", 2, sectionSize);

    }

    /**
     * Tests conversion of lookup definition's result fields into appropriate property
     *
     */
    @Test
    public void testTransformMaintainableItemsProperty() {
        def ddRootNode = getFileRootNode(defaultTestFilePath);
        def beanNode = ddRootNode.bean.find { "MaintainableSectionDefinition".equals(it.@parent) };

        beanNode = beanNode.replaceNode {
            bean(parent: "Uif-MaintenanceGridSection") {
                maintenanceDocumentEntryBeanTransformer.transformMaintainableItemsProperty(delegate, beanNode);
            }
        }

        // confirm lookup fields has been replaced with criteria fields
        checkBeanPropertyExists(beanNode, "items");
        def resultsFieldProperty = beanNode.property.find { "items".equals(it.@name) };
        def attrFieldSize = resultsFieldProperty.list.bean.findAll { "Uif-InputField".equals(it.@parent) }.size();
        Assert.assertEquals("number of converted data fields did not match", 3, attrFieldSize);
    }

}
