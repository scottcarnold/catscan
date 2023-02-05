package org.xandercat.cat.scan.filter;

import java.util.List;

import org.xandercat.cat.scan.result.MatchResultNode;


public interface ComparativeSearchFilter extends SearchFilter {

	public void beginSearch();
	
	public List<MatchResultNode> endSearch();
}
