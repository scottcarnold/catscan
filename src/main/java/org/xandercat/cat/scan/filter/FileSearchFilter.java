package org.xandercat.cat.scan.filter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.cat.scan.result.MatchResultNode;

/**
 * Root abstract class for all file search filters.  This class implements java.io.FileFilter;
 * search filters can therefore be used in a variety of applications, including as filters
 * for JFileChoosers and as filters when using the "list" method of a java.io.File.
 * 
 * To remain compatible with the SearchFilterFactory and any applications that would use it,
 * be sure to include a default constructor.
 * 
 * @author Scott Arnold
 */
public abstract class FileSearchFilter implements FileFilter, SearchFilter {

	private static final Logger log = LogManager.getLogger(FileSearchFilter.class);
	
//	protected abstract FileSearchFilter makeClone();
	
	public boolean accept(File file) {
		try {
			List<MatchResultNode> results = search(file);
			return results != null;
		} catch (IOException ioe) {
			String fileName = (file == null)? "null" : file.getAbsolutePath();
			log.error("Unable to test file: " + fileName, ioe);
			return false;
		}
	}

//	@Override
//	protected Object clone() throws CloneNotSupportedException {
//		return makeClone();
//	}
}
