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
package org.kuali.rice.core.api.criteria;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kuali.rice.core.api.mo.ModelBuilder;
import org.kuali.rice.core.api.mo.ModelObjectComplete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GenericQueryResults<T> implements CountAwareQueryResults<T>, ModelObjectComplete{

	private final List<T> results;
	private final Integer totalRowCount;
	private final boolean moreResultsAvailable;
	
	private GenericQueryResults(Builder<T> builder) {
		this.results = builder.getResults() != null ? Collections.unmodifiableList(new ArrayList<T>(builder.getResults())) : Collections.<T>emptyList();
		this.totalRowCount = builder.getTotalRowCount();
		this.moreResultsAvailable = builder.isMoreResultsAvailable();
	}

	@Override
	public List<T> getResults() {
		return results;
	}
	
	@Override
	public Integer getTotalRowCount() {
		return totalRowCount;
	}

	@Override
	public boolean isMoreResultsAvailable() {
		return moreResultsAvailable;
	}

    @Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(obj, this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static final class Builder<T> implements ModelBuilder {

		private List<T> results;
		private Integer totalRowCount;
		private boolean moreResultsAvailable;

        public static <T> Builder create() {
            return new Builder<T>();
        }

		private Builder() {
			this.results = new ArrayList<T>();
			this.moreResultsAvailable = false;
		}

        @Override
		public GenericQueryResults<T> build() {
			return new GenericQueryResults<T>(this);
		}

		public List<T> getResults() {
			return this.results;
		}

		public void setResults(List<T> results) {
			this.results = results;
		}

		public Integer getTotalRowCount() {
			return this.totalRowCount;
		}

		public void setTotalRowCount(Integer totalRowCount) {
			this.totalRowCount = totalRowCount;
		}

		public boolean isMoreResultsAvailable() {
			return this.moreResultsAvailable;
		}

		public void setMoreResultsAvailable(boolean moreResultsAvailable) {
			this.moreResultsAvailable = moreResultsAvailable;
		}
	}
}
