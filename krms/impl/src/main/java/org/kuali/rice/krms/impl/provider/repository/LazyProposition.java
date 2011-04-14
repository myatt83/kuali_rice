/*
 * Copyright 2011 The Kuali Foundation
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
package org.kuali.rice.krms.impl.provider.repository;

import org.kuali.rice.krms.api.engine.ExecutionEnvironment;
import org.kuali.rice.krms.api.repository.PropositionDefinition;
import org.kuali.rice.krms.api.type.KrmsTypeDefinition;
import org.kuali.rice.krms.framework.engine.Proposition;
import org.kuali.rice.krms.framework.type.PropositionTypeService;
import org.kuali.rice.krms.impl.type.KrmsTypeResolver;

/**
 * TODO... 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
final class LazyProposition implements Proposition {

	private final PropositionDefinition propositionDefinition;
	private final KrmsTypeDefinition typeDefinition;
	private final KrmsTypeResolver resolver;
	
	private final Object mutex = new Object();
	
	// volatile for double-checked locking idiom
	private volatile Proposition proposition;
	
	LazyProposition(PropositionDefinition propositionDefinition, KrmsTypeDefinition typeDefinition, KrmsTypeResolver resolver) {
		this.propositionDefinition = propositionDefinition;
		this.typeDefinition = typeDefinition;
		this.resolver = resolver;
		this.proposition = null;
	}
	
	@Override
	public boolean evaluate(ExecutionEnvironment environment) {
		return getProposition().evaluate(environment);
	}
	
	/**
	 * Gets the proposition using a lazy double-checked locking mechanism as documented in Effective Java Item 71.
	 */
	private Proposition getProposition() {
		Proposition localProposition = proposition;
		if (localProposition == null) {
			synchronized (mutex) {
				localProposition = proposition;
				if (localProposition == null) {
					proposition = localProposition = constructProposition();
				}
			}
		}
		return localProposition;
	}
	
	private Proposition constructProposition() {
		PropositionTypeService propositionTypeService = resolver.getPropositionTypeService(propositionDefinition, typeDefinition);
		Proposition proposition = propositionTypeService.loadProposition(propositionDefinition);
		if (proposition == null) {
			proposition = new Proposition() {
				@Override
				public boolean evaluate(ExecutionEnvironment environment) {
					return true;
				}
			};
		}
		return proposition;
	}

	
}
