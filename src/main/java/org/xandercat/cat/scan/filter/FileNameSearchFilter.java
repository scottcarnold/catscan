package org.xandercat.cat.scan.filter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

//import org.apache.log4j.Logger;
import org.xandercat.cat.scan.result.MatchResultNode;
import org.xandercat.swing.util.FileUtil;

/**
 * Search filter for finding files by file name.  Name patterns should be Operating
 * System style, using only periods and asterisks as special characters.  Name patterns
 * are not case sensitive.
 * 
 * @author Scott Arnold
 */
public class FileNameSearchFilter extends ZipSearchFilter {

	//private static final Logger log = Logger.getLogger(FileNameSearchFilter.class);
	
	private final List<String> namePatterns = new ArrayList<String>();
	private final List<Pattern> nameRegExPatterns = new ArrayList<Pattern>();
	
	public FileNameSearchFilter() {
		setNamePatterns("*.*");
	}
	
	/**
	 * Gets the name patterns matched by this filter.  
	 * 
	 * @return		name patterns
	 */
	public List<String> getNamePatterns() {
		List<String> copy = new ArrayList<String>();
		copy.addAll(namePatterns);
		return copy;
	}

	/**
	 * Sets the name patterns matched by this filter.  
	 * 
	 * @param namePatterns
	 */
	public void setNamePatterns(List<String> namePatterns) {
		this.namePatterns.clear();
		this.nameRegExPatterns.clear();
		this.namePatterns.addAll(namePatterns);
		for (String namePattern : this.namePatterns) {
			String regEx = FileUtil.generateRegularExpression(namePattern.toLowerCase());
			this.nameRegExPatterns.add(Pattern.compile(regEx));
		}
	}

	public void setNamePatterns(String... namePatterns) {
		setNamePatterns(Arrays.asList(namePatterns));
	}
	
	private boolean matches(String fileName) {
		fileName = fileName.toLowerCase();
		for (Pattern namePattern : this.nameRegExPatterns) {
			Matcher nameMatcher = namePattern.matcher(fileName);
			if (nameMatcher.matches()) {
				return true;
			}
		}		
		return false;
	}
	
	protected boolean fileNameMatches(String fileName) {
		return matches(fileName);
	}
	
	@Override
	public void processingDirectory(File directory) {
		// no action required
	}

	@Override
	protected List<MatchResultNode> searchFile(File file) throws IOException {
		if (matches(file.getName())) {
			return SearchFilter.EMPTY_MATCH;
		} else {
			return null;
		}
	}

	@Override
	protected List<MatchResultNode> searchZipEntry(ZipEntry zipEntry, ZipInputStream zipInputStream, File zipFile) throws IOException {
		if (matches(getZipEntryFileName(zipEntry))) {
			return SearchFilter.EMPTY_MATCH;
		} else {
			return null;
		}
	}
	
	public String getName() {
		return "File Name Search";
	}

//	@Override
//	public FileSearchFilter makeClone() {
//		FileNameSearchFilter clone = new FileNameSearchFilter();
//		clone.setNamePatterns(getNamePatterns());
//		clone.setZipNamePatterns(getZipNamePatterns());
//		return clone;
//	}

	@Override
	public Map<String, String> getSearchCriteria() {
		Map<String, String> searchCriteria = super.getSearchCriteria();
		searchCriteria.put("File Name Pattern(s)", getCSVString(namePatterns));
		return searchCriteria;
	}
	
	
}
