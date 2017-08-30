package org.javlo.data.taxonomy;

import java.util.HashSet;
import java.util.Set;

public class TaxonmyContainerBean implements ITaxonomyContainer {
	
	private Set<String> taxonomy = new HashSet<String>();
	
	public TaxonmyContainerBean(String taxonomy) {		
		this.taxonomy.add(taxonomy);
	}

	public TaxonmyContainerBean(Set<String> taxonomy) {		
		this.taxonomy.addAll(taxonomy);
	}

	@Override
	public Set<String> getTaxonomy() {
		return taxonomy;
	}

}
