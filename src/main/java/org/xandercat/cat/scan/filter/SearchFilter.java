package org.xandercat.cat.scan.filter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.xandercat.cat.scan.result.MatchResultNode;

/**
 * Basic interface for CatScan search filters.
 * 
 * @author Scott Arnold
 */
public interface SearchFilter {
	
	/**
	 * Search filters can return this from the search method whenever they wish to 
	 * indicate a match without creating an extra level of match detail nodes.
	 */
	public static List<MatchResultNode> EMPTY_MATCH = 
		Collections.unmodifiableList(new ArrayList<MatchResultNode>());
	
	/**
	 * Gets the name for the filter.
	 * 
	 * @return		filter name
	 */
	public String getName();
	
	/**
	 * Notifies the filter as to which directory is currently being searched.
	 * This is provided to the filter for informational purposes only.
	 * 
	 * @param directory		directory currently being processed
	 */
	public void processingDirectory(File directory);
	
	/**
	 * Searches the given file, returning a list of any matches.  If there
	 * are no matches, null should be returned.  Returning an empty list
	 * signifies a match without any extra details; this interface provides
	 * a static empty list for this purpose.
	 * 
	 * @param file		file to search
	 * 
	 * @return			list of match results nodes
	 * 
	 * @throws IOException
	 */
	public List<MatchResultNode> search(File file) throws IOException;
	
	/**
	 * Gets the current search criteria for the filter as strings.
	 * 
	 * @return			search criteria loaded into a map
	 */
	public Map<String, String> getSearchCriteria();
}
