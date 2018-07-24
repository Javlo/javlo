package org.javlo.data.taxonomy;

import java.util.HashSet;
import java.util.Set;

public class TaxonomyContainerBean implements ITaxonomyContainer {
	
	public static final TaxonomyContainerBean EMPTY = new TaxonomyContainerBean((String)null);
	
	private Set<String> taxonomy = new HashSet<String>();
	
	public TaxonomyContainerBean(String taxonomy) {
		if (taxonomy != null) {
			this.taxonomy.add(taxonomy);
		}
	}

	public TaxonomyContainerBean(Set<String> taxonomy) {		
		this.taxonomy.addAll(taxonomy);
	}

	@Override
	public Set<String> getTaxonomy() {
		return taxonomy;
	}

}
