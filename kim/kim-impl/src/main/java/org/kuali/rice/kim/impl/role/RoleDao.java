package org.kuali.rice.kim.impl.role;


import org.kuali.rice.core.api.mo.common.Attributes;
import org.kuali.rice.core.util.AttributeSet;
import org.kuali.rice.kim.api.group.GroupMember;
import org.kuali.rice.kim.api.role.RoleMember;
import org.kuali.rice.kim.api.role.RoleMembership;
import org.kuali.rice.kim.bo.role.dto.RoleMemberCompleteInfo;
import org.kuali.rice.kim.impl.common.delegate.DelegateBo;
import org.kuali.rice.kim.impl.common.delegate.DelegateMemberBo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RoleDao {

    List<RoleMemberBo> getRolePrincipalsForPrincipalIdAndRoleIds(Collection<String> roleIds, String principalId, Attributes qualification);

    List<GroupMember> getGroupPrincipalsForPrincipalIdAndGroupIds(Collection<String> groupIds, String principalId);

    List<RoleMemberBo> getRoleGroupsForGroupIdsAndRoleIds(Collection<String> roleIds, Collection<String> groupIds, Attributes qualification);

    Map<String, DelegateBo> getDelegationImplMapFromRoleIds(Collection<String> roleIds);

    List<DelegateBo> getDelegationBosForRoleIds(Collection<String> roleIds);

    List<DelegateMemberBo> getDelegationPrincipalsForPrincipalIdAndDelegationIds(Collection<String> delegationIds, String principalId);

    List<DelegateMemberBo> getDelegationGroupsForGroupIdsAndDelegationIds(Collection<String> delegationIds, List<String> groupIds);

    List<RoleMemberBo> getRoleMembersForRoleIds(Collection<String> roleIds, String memberTypeCode, Attributes qualification);

    List<RoleMemberBo> getRoleMembershipsForRoleIdsAsMembers(Collection<String> roleIds, Attributes qualification);

    List<RoleMemberBo> getRoleMembershipsForMemberId(String memberType, String memberId, Attributes qualification);

    List<RoleMemberBo> getRoleMembersForRoleIdsWithFilters(Collection<String> roleIds, String principalId, List<String> groupIds, Attributes qualification);

    Map<String, List<DelegateMemberBo>> getDelegationMembersForDelegationIds(List<String> delegationIds);

    List<RoleBo> getRoles(Map<String, String> fieldValues);

    List<GroupMember> getGroupMembers(Collection<String> groupIds);

    List<RoleMembership> getRoleMembers(Map<String, String> fieldValues);

    List<RoleMember> getRoleMembersCompleteInfo(Map<String, String> fieldValues);
}
