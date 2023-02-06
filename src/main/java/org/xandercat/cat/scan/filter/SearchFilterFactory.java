package org.xandercat.cat.scan.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Factory class for building search filters.
 * 
 * @author Scott Arnold
 */
public class SearchFilterFactory {

	private static final Logger log = LogManager.getLogger(SearchFilterFactory.class);
	
	/**
	 * Creates and returns a list of default search filters.  Each search filter
	 * is constructed using it's default settings.
	 * 
	 * @return			list of default search filters
	 */
	public static final List<FileSearchFilter> getDefaultFilters() {
		List<FileSearchFilter> filters = new ArrayList<FileSearchFilter>();
		List<Class<? extends FileSearchFilter>> filterClasses = getDefaultFilterClasses();
		for (Class<? extends FileSearchFilter> filterClass : filterClasses) {
			filters.add(newFilter(filterClass));
		}
		return filters;
	}
	
	/**
	 * Creates and returns a list of default search filter classes.
	 * 
	 * @return			list of default search filter classes
	 */
	public static final List<Class<? extends FileSearchFilter>> getDefaultFilterClasses() {
		List<Class<? extends FileSearchFilter>> filterClasses = new ArrayList<Class<? extends FileSearchFilter>>();
		filterClasses.add(FileNameSearchFilter.class);
		filterClasses.add(TextSearchFilter.class);
		filterClasses.add(ClassSearchFilter.class);
		filterClasses.add(StatSearchFilter.class);
		return filterClasses;
	}
	
	/**
	 * Creates and returns a new instance of the file search filter of given class.
	 * 
	 * @param filterClass	search filter class
	 * 
	 * @return				new instance of search filter
	 */
	public static final FileSearchFilter newFilter(Class<? extends FileSearchFilter> filterClass) {
		try {
			return filterClass.newInstance();
		} catch (Exception e) {
			log.error("Unable to create new filter", e);
			return null;
		} 
	}
	
	/**
	 * Creates and returns a new search filter that is a copy of an existing search filter.
	 * 
	 * @param filter		filter to copy
	 * 
	 * @return				copy of filter
	 */
	public static final FileSearchFilter newFilter(FileSearchFilter filter) {
		try {
			return (FileSearchFilter) filter.clone();
		} catch (CloneNotSupportedException cnse) {
			log.error("Unable to create new filter from existing filter.", cnse);
			return null;
		}
	}
}
