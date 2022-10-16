package org.javlo.data.taxonomy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TaxonomyContainerBean implements ITaxonomyContainer {
	
	public static final TaxonomyContainerBean EMPTY = new TaxonomyContainerBean((String)null);
	
	private Set<String> taxonomy = new HashSet<String>();
	
	public TaxonomyContainerBean(String... taxonomy) {
		for (String taxo : taxonomy) {
			if (taxo != null) {
				this.taxonomy.add(taxo);
			}
		}
	}

	public TaxonomyContainerBean(Collection<String> taxonomy) {		
		this.taxonomy.addAll(taxonomy);
	}

	@Override
	public Set<String> getTaxonomy() {
		return taxonomy;
	}

}
