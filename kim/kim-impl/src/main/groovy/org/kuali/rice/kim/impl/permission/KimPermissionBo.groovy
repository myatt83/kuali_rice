/*
 * Copyright 2006-2011 The Kuali Foundation
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

package org.kuali.rice.kim.impl.permission

import java.util.List;

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type
import org.kuali.rice.kim.api.permission.KimPermission
import org.kuali.rice.kim.api.permission.KimPermissionContract
import org.kuali.rice.kim.impl.group.GroupMemberBo;
import org.kuali.rice.kim.impl.role.RolePermissionBo
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase
import org.springframework.util.AutoPopulatingList;

@Entity
@Table(name="KRIM_PERM_T")
public class KimPermissionBo extends PersistableBusinessObjectBase implements KimPermissionContract {
    private static final long serialVersionUID = 1L;

    @Id
	@Column(name="PERM_ID")
	String id

	@Column(name="NMSPC_CD")
	String namespaceCode
	
    @Column(name="NM")
	String name

	@Column(name="DESC_TXT", length=400)
	String description;

	@Column(name="PERM_TMPL_ID")
	String templateId
	
	@Column(name="ACTV_IND")
	@Type(type="yes_no")
	boolean active

	@OneToOne(targetEntity=KimPermissionTemplateBo.class,cascade=[],fetch=FetchType.EAGER)
	@JoinColumn(name="PERM_TMPL_ID", insertable=false, updatable=false)
	KimPermissionTemplateBo template;
	
	@OneToMany(targetEntity=PermissionAttributeBo.class,cascade=[CascadeType.ALL],fetch=FetchType.EAGER,mappedBy="id")
	@Fetch(value = FetchMode.SELECT)
	List<PermissionAttributeBo> permissionAttributes
	
	@OneToMany(targetEntity=RolePermissionBo.class,cascade=[CascadeType.ALL],fetch=FetchType.EAGER,mappedBy="id")
    @Fetch(value = FetchMode.SELECT)
	List<RolePermissionBo> rolePermissions
		
    /**
     * Converts a mutable bo to its immutable counterpart
     * @param bo the mutable business object
     * @return the immutable object
     */
    static KimPermission to(KimPermissionBo bo) {
        if (bo == null) {
            return null
        }

        return KimPermission.Builder.create(bo).build();
    }

    /**
     * Converts a immutable object to its mutable counterpart
     * @param im immutable object
     * @return the mutable bo
     */
    static KimPermissionBo from(KimPermission im) {
        if (im == null) {
            return null
        }

        KimPermissionBo bo = new KimPermissionBo()
        bo.id = im.id
        bo.namespaceCode = im.namespaceCode
        bo.name = im.name
        bo.description = im.description
        bo.active = im.active
        bo.templateId = im.templateId
        bo.versionNumber = im.versionNumber
		bo.objectId = im.objectId;

        return bo
    }

}
