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
package org.kuali.rice.kim.impl.responsibility;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.kuali.rice.kim.api.common.template.Template;
import org.kuali.rice.kim.api.common.template.TemplateContract;
import org.kuali.rice.kim.impl.common.template.TemplateBo;
import org.kuali.rice.krad.data.jpa.PortableSequenceGenerator;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@Entity
@Table(name = "KRIM_RSP_TMPL_T")
public class ResponsibilityTemplateBo extends TemplateBo implements TemplateContract {

    private static final long serialVersionUID = 1L;

    @PortableSequenceGenerator(name = "KRIM_RSP_TMPL_ID_S")
    @GeneratedValue(generator = "KRIM_RSP_TMPL_ID_S")
    @Id
    @Column(name = "RSP_TMPL_ID")
    private String id;

    /**
     * Converts a mutable bo to its immutable counterpart
     *
     * @param bo the mutable business object
     * @return the immutable object
     */
    public static Template to(TemplateContract bo) {
        if (bo == null) {
            return null;
        }
        return Template.Builder.create(bo).build();
    }

    /**
     * Converts a immutable object to its mutable counterpart
     *
     * @param im immutable object
     * @return the mutable bo
     */
    public static ResponsibilityTemplateBo from(TemplateContract im) {
        if (im == null) {
            return null;
        }
        ResponsibilityTemplateBo bo = new ResponsibilityTemplateBo();
        bo.id = im.getId();
        bo.setNamespaceCode(im.getNamespaceCode());
        bo.setName(im.getName());
        bo.setDescription(im.getDescription());
        bo.setActive(im.isActive());
        bo.setKimTypeId(im.getKimTypeId());
        bo.setVersionNumber(im.getVersionNumber());
        bo.setObjectId(im.getObjectId());
        return bo;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
