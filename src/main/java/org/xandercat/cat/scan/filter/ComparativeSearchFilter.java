package org.xandercat.cat.scan.filter;

import java.io.File;
import java.util.List;

import org.xandercat.cat.scan.result.MatchResultNode;


public interface ComparativeSearchFilter extends SearchFilter {

	public void beginSearch(File rootDirectory);
	
	public List<MatchResultNode> endSearch();
}
