/*
 * Copyright 2011 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
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
package org.kuali.rice.kim.impl.jaxb;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.util.jaxb.NameAndNamespacePair;
import org.kuali.rice.kim.api.group.GroupContract;
import org.kuali.rice.kim.api.identity.principal.PrincipalContract;
import org.kuali.rice.kim.api.permission.PermissionContract;
import org.kuali.rice.kim.api.role.RoleContract;
import org.kuali.rice.kim.api.role.RoleMemberContract;
import org.kuali.rice.kim.api.role.RoleUpdateService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.util.KimConstants.KimUIConstants;

import javax.xml.bind.UnmarshalException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Helper class containing static methods for aiding in parsing role XML.
 * 
 * <p>All non-private methods are package-private so that only the KIM-parsing-related code can make use of them. (TODO: Is that necessary?)
 * 
 * <p>TODO: Should this be converted into a service instead?
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public final class RoleXmlUtil {
    // Do not allow outside code to instantiate this class.
    private RoleXmlUtil() {}

    /**
     * Performs the necessary validation on the new role, then saves it.
     * 
     * @param newRole The role to persist.
     * @return The ID of the persisted role.
     * @throws IllegalArgumentException if newRole is null.
     * @throws UnmarshalException if newRole contains invalid data.
     */
    static String validateAndPersistNewRole(RoleXmlDTO newRole) throws UnmarshalException {
        if (newRole == null) {
            throw new IllegalArgumentException("Cannot persist a null role");
        }
        
        // Validate the role and (if applicable) retrieve the ID from an existing matching role.
        validateAndPrepareRole(newRole);
        
        // If necessary, assign a new role ID.
        if (StringUtils.isBlank(newRole.getRoleId())) {
            newRole.setRoleId(KimApiServiceLocator.getRoleUpdateService().getNextAvailableRoleId());
        }
        
        // Save the role.
        KimApiServiceLocator.getRoleUpdateService().saveRole(newRole.getRoleId(), newRole.getRoleName(), newRole.getRoleDescription(), newRole.getActive().booleanValue(),
                newRole.getKimTypeId(), newRole.getNamespaceCode());
        
        // Set a flag on the role to indicate that it has now been persisted so that the unmarshalling process will not save this role more than once.
        newRole.setAlreadyPersisted(true);
        
        return newRole.getRoleId();
    }
    
    /**
     * Performs the necessary validation on the new role member, then saves it.
     * 
     * @param newRoleMember The role member to save.
     * @return The ID of the persisted role member.
     * @throws IllegalArgumentException if newRoleMember is null.
     * @throws UnmarshalException if newRoleMember contains invalid data.
     */
    static String validateAndPersistNewRoleMember(RoleMemberXmlDTO newRoleMember) throws UnmarshalException {
        
        if (newRoleMember == null) {
            throw new IllegalArgumentException("Cannot persist a null role member");
        }
        
        // Validate role ID and role name/namespace.
        validateRoleIdAndRoleNameForMember(newRoleMember);
        
        // Validate member type, member ID, and member name/namespace.
        validateMemberIdentity(newRoleMember);
        
        // Validate the from/to dates, if defined.
        if (newRoleMember.getActiveFromDate() != null && newRoleMember.getActiveToDate() != null &&
                newRoleMember.getActiveFromDate().compareTo(newRoleMember.getActiveToDate()) > 0) {
            throw new UnmarshalException("Cannot create a role member whose activeFromDate occurs after its activeToDate");
        }
        
        // Define defaults as needed.
        if (newRoleMember.getQualifications() == null) {
            newRoleMember.setQualifications(new HashMap<String, String>());
        }
        
        // Save the role member.
        RoleMemberContract newMember = KimApiServiceLocator.getRoleUpdateService().saveRoleMemberForRole(
                null, newRoleMember.getMemberId(), newRoleMember.getMemberTypeCode(),
                        newRoleMember.getRoleId(), newRoleMember.getQualifications(),
                                (newRoleMember.getActiveFromDate() != null) ? new java.sql.Date(newRoleMember.getActiveFromDate().getTime()) : null,
                                (newRoleMember.getActiveToDate() != null) ? new java.sql.Date(newRoleMember.getActiveToDate().getTime()) : null);
        
        return newMember.getRoleMemberId();
    }
    
    /**
     * Performs the necessary validation on the role permission, then saves it.
     * 
     * @param newRolePermission The role permission to save.
     * @throws IllegalArgumentException if newRolePermission is null
     * @throws UnmarshalException if newRolePermission contains invalid data.
     */
    static void validateAndPersistNewRolePermission(RolePermissionXmlDTO newRolePermission) throws UnmarshalException {
        if (newRolePermission == null) {
            throw new IllegalArgumentException("newRolePermission cannot be null");
        }
        
        // Validate the role permission, and prepare its role ID if necessary.
        validateAndPrepareRolePermission(newRolePermission);
        
        // Save the role permission.
        KimApiServiceLocator.getRoleUpdateService().assignPermissionToRole(newRolePermission.getPermissionId(), newRolePermission.getRoleId());
    }
    
    /**
     * Removes any role members for a given role whose IDs are not listed in a given role member ID set.
     * 
     * @param roleId The ID of the role containing the role members.
     * @param existingMemberIds The IDs of the role members that should not be removed.
     * @throws IllegalArgumentException if roleId is blank or refers to a non-existent role, or if existingRoleMemberIds is null.
     */
    static void removeRoleMembers(String roleId, Set<String> existingRoleMemberIds) {
        if (StringUtils.isBlank(roleId)) {
            throw new IllegalArgumentException("roleId cannot be blank");
        } else if (existingRoleMemberIds == null) {
            throw new IllegalArgumentException("existingRoleMemberIds cannot be null");
        }
        RoleUpdateService roleUpdateService = KimApiServiceLocator.getRoleUpdateService();
        RoleContract role = KimApiServiceLocator.getRoleService().getRole(roleId);
        if (role == null) {
            throw new IllegalArgumentException("Cannot remove role members for role with ID \"" + roleId + "\" because that role does not exist");
        }
        
        // Remove any role members whose IDs are not in the set.
        List<? extends RoleMemberContract> roleMembers = KimApiServiceLocator.getRoleService().findRoleMembers(Collections.singletonMap("roleId", roleId));
        if (roleMembers != null && !roleMembers.isEmpty()) {
            for (RoleMemberContract roleMember : roleMembers) {
                if (!existingRoleMemberIds.contains(roleMember.getRoleMemberId())) {
                    // If the role member needs to be removed, use the member type code to determine which removal method to call.
                    String memberTypeCode = roleMember.getMemberTypeCode();
                    if (KimUIConstants.MEMBER_TYPE_PRINCIPAL_CODE.equals(memberTypeCode)) {
                        roleUpdateService.removePrincipalFromRole(roleMember.getMemberId(), role.getNamespaceCode(), role.getName(),
                                (roleMember.getAttributes() != null) ? roleMember.getAttributes() : new HashMap<String, String>());
                    } else if (KimUIConstants.MEMBER_TYPE_GROUP_CODE.equals(memberTypeCode)) {
                        roleUpdateService.removeGroupFromRole(roleMember.getMemberId(), role.getNamespaceCode(), role.getName(),
                                (roleMember.getAttributes() != null) ? roleMember.getAttributes() :new HashMap<String, String>());
                    } else if (KimUIConstants.MEMBER_TYPE_ROLE_CODE.equals(memberTypeCode)) {
                        roleUpdateService.removeRoleFromRole(roleMember.getMemberId(), role.getNamespaceCode(), role.getName(),
                                (roleMember.getAttributes() != null) ? roleMember.getAttributes() : new HashMap<String, String>());
                    }
                }
            }
        }
    }
    
    /**
     * Validates a new role's name, namespace, KIM type, and description, and sets the role's ID if the name and namespace match an existing role.
     */
    private static void validateAndPrepareRole(RoleXmlDTO newRole) throws UnmarshalException {
        // Ensure that the role name, role namespace, KIM type, and description have all been specified.
        if (StringUtils.isBlank(newRole.getRoleName()) || StringUtils.isBlank(newRole.getNamespaceCode())) {
            throw new UnmarshalException("Cannot create or override a role with a blank name or a blank namespace");
        } else if (StringUtils.isBlank(newRole.getKimTypeId())) {
            throw new UnmarshalException("Cannot create or override a role without specikfying a KIM type");
        } else if (StringUtils.isBlank(newRole.getRoleDescription())) {
            throw new UnmarshalException("Cannot create or override a role with a blank description");
        }
        
        // Attempt to find an existing matching role, and assign its ID to the validated role if it exists.
        String matchingId = KimApiServiceLocator.getRoleService().getRoleIdByName(newRole.getNamespaceCode(), newRole.getRoleName());
        if (StringUtils.isNotBlank(matchingId)) {
            newRole.setRoleId(matchingId);
        }
    }
    
    /**
     * Validates a new role member's role ID, role name, and role namespace.
     */
    private static void validateRoleIdAndRoleNameForMember(RoleMemberXmlDTO newRoleMember) throws UnmarshalException {
        // If the "roleMember" tag was not a descendant of a "role" tag, derive and validate its role information accordingly.
        if (newRoleMember instanceof RoleMemberXmlDTO.OutsideOfRole) {
            RoleMemberXmlDTO.OutsideOfRole standaloneMember = (RoleMemberXmlDTO.OutsideOfRole) newRoleMember;
            if (standaloneMember.getRoleNameAndNamespace() != null) {
                // If a name + namespace combo is given, verify that the combo maps to an existing role.
                String existingId = KimApiServiceLocator.getRoleService().getRoleIdByName(standaloneMember.getRoleNamespaceCode(), standaloneMember.getRoleName());
                if (StringUtils.isBlank(existingId)) {
                    throw new UnmarshalException("Cannot create role member for role with name \"" + standaloneMember.getRoleName() + "\" and namespace \"" +
                            standaloneMember.getRoleNamespaceCode() + "\" because such a role does not exist");
                }
                
                // If the role member defines its own role ID, verify that it's the same as the one from the existing role; otherwise, assign the member's role ID.
                if (StringUtils.isBlank(standaloneMember.getRoleId())) {
                    standaloneMember.setRoleId(existingId);
                } else if (!standaloneMember.getRoleId().equals(existingId)) {
                    throw new UnmarshalException("Cannot create role member for role with ID \"" + standaloneMember.getRoleId() + "\", name \"" +
                            standaloneMember.getRoleName() + "\", and namespace \"" + standaloneMember.getRoleNamespaceCode() +
                                    "\" because the existing role with the same name and namespace has an ID of \"" + existingId + "\" instead");
                }
            } else if (StringUtils.isBlank(standaloneMember.getRoleId())) {
                throw new UnmarshalException("Cannot create role member without providing the role ID or role name + namespace that the member belongs to");
            } else if (KimApiServiceLocator.getRoleService().getRole(standaloneMember.getRoleId()) == null) {
                throw new UnmarshalException("Cannot create role member for the role with ID \"" + standaloneMember.getRoleId() + "\" because that role does not exist");
            }
        }
        
        // Ensure that a role ID was explicitly defined or was derived from a name + namespace combo.
        if (StringUtils.isBlank(newRoleMember.getRoleId())) {
            throw new UnmarshalException("Cannot create role member without providing the role ID or role name + namespace that the member belongs to");
        }
    }
    
    /**
     * Validates a new role member's member type, member ID, member name, and (if applicable) member namespace code.
     */
    private static void validateMemberIdentity(RoleMemberXmlDTO newRoleMember) throws UnmarshalException {
        // Ensure that sufficient and non-conflicting membership info has been set. (The getMemberTypeCode() method performs such validation.)
        String memberTypeCode = newRoleMember.getMemberTypeCode();
        if (StringUtils.isBlank(memberTypeCode)) {
            throw new UnmarshalException("Cannot create a role member with no member principal/group/role identification information specified");
        }
        
        // Ensure that a valid member ID was specified, if present.
        if (StringUtils.isNotBlank(newRoleMember.getMemberId())) {
            if (KimUIConstants.MEMBER_TYPE_PRINCIPAL_CODE.equals(memberTypeCode)) {
                // If the member is a principal, ensure that the principal exists.
                if (KimApiServiceLocator.getIdentityService().getPrincipal(newRoleMember.getPrincipalId()) == null) {
                    throw new UnmarshalException("Cannot create principal role member with principal ID \"" +
                            newRoleMember.getPrincipalId() + "\" because such a person does not exist");
                }
            } else if (KimUIConstants.MEMBER_TYPE_GROUP_CODE.equals(memberTypeCode)) {
                // If the member is a group, ensure that the group exists.
                if (KimApiServiceLocator.getGroupService().getGroup(newRoleMember.getGroupId()) == null) {
                    throw new UnmarshalException("Cannot create group role member with group ID \"" +
                            newRoleMember.getGroupId() + "\" because such a group does not exist");
                }
            } else if (KimUIConstants.MEMBER_TYPE_ROLE_CODE.equals(memberTypeCode)) {
                // If the member is another role, ensure that the role exists and that the role is not trying to become a member of itself.
                if (newRoleMember.getRoleId().equals(newRoleMember.getRoleIdAsMember())) {
                    throw new UnmarshalException("The role with ID \"" + newRoleMember.getRoleIdAsMember() + "\" cannot be made a member of itself");
                } else if (KimApiServiceLocator.getRoleService().getRole(newRoleMember.getRoleIdAsMember()) == null) {
                    throw new UnmarshalException("Cannot use role with ID \"" + newRoleMember.getRoleIdAsMember() +
                            "\" as a role member because such a role does not exist");
                }
            }
        }
        
        // Ensure that a valid member name (and namespace, if applicable) was specified, if present.
        if (StringUtils.isNotBlank(newRoleMember.getMemberName())) {
            if (KimUIConstants.MEMBER_TYPE_PRINCIPAL_CODE.equals(memberTypeCode)) {
                //If the member is a principal, ensure that the principal exists and does not conflict with any existing principal ID information.
                PrincipalContract tempPrincipal = KimApiServiceLocator.getIdentityService().getPrincipalByPrincipalName(newRoleMember.getPrincipalName());
                if (tempPrincipal == null) {
                    throw new UnmarshalException("Cannot create principal role member with principal name \"" +
                            newRoleMember.getPrincipalName() + "\" because such a person does not exist");
                } else if (StringUtils.isBlank(newRoleMember.getPrincipalId())) {
                    // If no principal ID was given, assign one from the retrieved principal.
                    newRoleMember.setPrincipalId(tempPrincipal.getPrincipalId());
                } else if (!newRoleMember.getPrincipalId().equals(tempPrincipal.getPrincipalId())) {
                    throw new UnmarshalException("Cannot create principal role member with principal ID \"" + newRoleMember.getPrincipalId() +
                            "\" and principal name \"" + newRoleMember.getPrincipalName() + "\" because the principal with that name has an ID of \"" +
                                    tempPrincipal.getPrincipalId() + "\" instead");
                }
            } else if (KimUIConstants.MEMBER_TYPE_GROUP_CODE.equals(memberTypeCode)) {
                // If the member is a group, ensure that the group exists and does not conflict with any existing group ID information.
                NameAndNamespacePair groupNameAndNamespace = newRoleMember.getGroupName();
                GroupContract tempGroup = KimApiServiceLocator.getGroupService().getGroupByName(
                        groupNameAndNamespace.getNamespaceCode(), groupNameAndNamespace.getName());
                if (tempGroup == null) {
                    throw new UnmarshalException("Cannot create group role member with namespace \"" + groupNameAndNamespace.getNamespaceCode() +
                            "\" and name \"" + groupNameAndNamespace.getName() + "\" because such a group does not exist");
                } else if (StringUtils.isBlank(newRoleMember.getGroupId())) {
                    // If no group ID was given, assign one from the retrieved group.
                    newRoleMember.setGroupId(tempGroup.getId());
                } else if (!newRoleMember.getGroupId().equals(tempGroup.getId())) {
                    throw new UnmarshalException("Cannot create group role member with ID \"" + newRoleMember.getGroupId() + "\", namespace \"" +
                            groupNameAndNamespace.getNamespaceCode() + "\", and name \"" + groupNameAndNamespace.getName() +
                                    "\" because the group with that namespace and name has an ID of \"" + tempGroup.getId() + "\" instead");
                }
            } else if (KimUIConstants.MEMBER_TYPE_ROLE_CODE.equals(memberTypeCode)) {
                // If the member is another role, ensure that the role exists, does not conflict with any existing role ID information, and is not the member's role.
                NameAndNamespacePair roleNameAndNamespace = newRoleMember.getRoleNameAsMember();
                RoleContract tempRole = KimApiServiceLocator.getRoleService().getRoleByName(
                        roleNameAndNamespace.getNamespaceCode(), roleNameAndNamespace.getName());
                if (tempRole == null) {
                    throw new UnmarshalException("Cannot use role with namespace \"" + roleNameAndNamespace.getNamespaceCode() +
                            "\" and name \"" + roleNameAndNamespace.getName() + "\" as a role member because such a role does not exist");
                } else if (newRoleMember.getRoleId().equals(tempRole.getId())) {
                    throw new UnmarshalException("The role with namespace \"" + roleNameAndNamespace.getNamespaceCode() +
                            "\" and name \"" + roleNameAndNamespace.getName() + "\" cannot be made a member of itself");
                } else if (StringUtils.isBlank(newRoleMember.getRoleId())) {
                    // If no role ID was given, assign one from the retrieved role.
                    newRoleMember.setRoleIdAsMember(tempRole.getId());
                } else if (!newRoleMember.getRoleId().equals(tempRole.getId())) {
                    throw new RuntimeException("Cannot use role with ID \"" + newRoleMember.getRoleId() + "\", namespace \"" +
                            roleNameAndNamespace.getNamespaceCode() + "\", and name \"" + roleNameAndNamespace.getName() +
                                    "\" as a role member because the role with that namespace and name has an ID of \"" +
                                            tempRole.getId() + "\" instead");
                }
            }
        }
        
        // Ensure that a member ID was either explicitly defined or was derived from the member name (and namespace, if applicable).
        if (StringUtils.isBlank(newRoleMember.getMemberId())) {
            throw new RuntimeException("Cannot create a role member with no member principal/group/role identification information specified");
        }
        
    }
    
    /**
     * Validates a role permission's role and permission identification information, and assigns its role ID if needed.
     */
    private static void validateAndPrepareRolePermission(RolePermissionXmlDTO newRolePermission) throws UnmarshalException {
        
        // If this is a standalone role permission, derive and validate its role information accordingly.
        if (newRolePermission instanceof RolePermissionXmlDTO.OutsideOfRole) {
            RolePermissionXmlDTO.OutsideOfRole standaloneRolePerm = (RolePermissionXmlDTO.OutsideOfRole) newRolePermission;
            if (standaloneRolePerm.getRoleNameAndNamespace() != null) {
                // If a role name + namespace is given, assign or validate the role ID accordingly.
                String tempRoleId = KimApiServiceLocator.getRoleService().getRoleIdByName(
                        standaloneRolePerm.getRoleNamespaceCode(), standaloneRolePerm.getRoleName());
                if (StringUtils.isBlank(tempRoleId)) {
                    throw new UnmarshalException("Cannot assign permission to role with namespace \"" + standaloneRolePerm.getRoleNamespaceCode() +
                            "\" and name \"" + standaloneRolePerm.getRoleName() + "\" because that role does not exist");
                } else if (StringUtils.isBlank(standaloneRolePerm.getRoleId())) {
                    // If no role ID was given, assign one from the retrieved role.
                    standaloneRolePerm.setRoleId(standaloneRolePerm.getRoleId());
                } else if (!standaloneRolePerm.getRoleId().equals(tempRoleId)) {
                    throw new UnmarshalException("Cannot assign permission to role with ID \"" + standaloneRolePerm.getRoleId() + "\", namespace \"" +
                            standaloneRolePerm.getRoleNamespaceCode() + "\", and name \"" + standaloneRolePerm.getRoleName() +
                                    "\" because the existing role with that name and namespace has an ID of \"" + tempRoleId + "\" instead");
                }
            } else if (StringUtils.isBlank(standaloneRolePerm.getRoleId())) {
                throw new UnmarshalException(
                        "Cannot assign permission to role without providing the role ID or role name + namespace that the permission is assigned to");
            } else if (KimApiServiceLocator.getRoleService().getRole(standaloneRolePerm.getRoleId()) == null) {
                throw new UnmarshalException("Cannot assign permission to role with ID \"" + standaloneRolePerm.getRoleId() +
                        "\" because that role does not exist");
            }
        }
        
        // Ensure that a role ID was explicitly defined or was derived from a name + namespace combo.
        if (StringUtils.isBlank(newRolePermission.getRoleId())) {
            throw new UnmarshalException("Cannot assign permission to role without providing the role ID or role name + namespace that the permission is assigned to");
        }
        
        // If the permission is being identified by name and namespace, derive or validate its permission ID accordingly.
        if (newRolePermission.getPermissionNameAndNamespace() != null) {
            PermissionContract permission = KimApiServiceLocator.getPermissionService().getPermissionsByName(
                    newRolePermission.getPermissionNamespaceCode(), newRolePermission.getPermissionName());
            if (permission == null) {
                throw new UnmarshalException("Cannot get role assigned to permission with namespace \"" + newRolePermission.getPermissionNamespaceCode() +
                        "\" and name \"" + newRolePermission.getPermissionName() + "\" because that permission does not exist");
            } else if (StringUtils.isBlank(newRolePermission.getPermissionId())) {
                // If no permission ID was given, assign one from the retrieved permission.
                newRolePermission.setPermissionId(permission.getId());
            } else if (!newRolePermission.getPermissionId().equals(permission.getId())) {
                throw new UnmarshalException("Cannot get role assigned to permission with ID \"" + newRolePermission.getPermissionId() + "\", namespace \"" +
                        newRolePermission.getPermissionNamespaceCode() + "\", and name \"" + newRolePermission.getPermissionName() +
                                "\" because the existing permission with that name and namespace has an ID of \"" + permission.getId() + "\" instead");
            }
        } else if (StringUtils.isBlank(newRolePermission.getPermissionId())) {
            throw new UnmarshalException("Cannot assign permission to role without specifying the ID or name and namespace of the permission to assign");
        } else if (KimApiServiceLocator.getPermissionService().getPermission(newRolePermission.getPermissionId()) == null) {
            throw new UnmarshalException("Cannot get role assigned to permission with ID \"" + newRolePermission.getPermissionId() +
                    "\" because that permission does not exist");
        }
    }
    
}