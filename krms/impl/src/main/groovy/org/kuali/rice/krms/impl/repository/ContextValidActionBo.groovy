package org.kuali.rice.krms.impl.repository

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase

public class ContextValidActionBo extends PersistableBusinessObjectBase {

	def String id
	def String contextId
	def String actionTypeId
	
	def Long versionNumber
} 