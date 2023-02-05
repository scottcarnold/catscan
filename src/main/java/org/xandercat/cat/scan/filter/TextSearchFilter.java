package org.xandercat.cat.scan.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xandercat.cat.scan.result.MatchResultNode;


/**
 * Search filter for searching text files.
 * 
 * @author Scott Arnold
 */
public class TextSearchFilter extends FileNameSearchFilter implements Cloneable {
	
	private String searchString;
	private String internalSearchString;
	private boolean caseSensitive;
	
	public TextSearchFilter() {
		super();
		setNamePatterns("*.txt");
	}
	
	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
		updateInternalSearchString();
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		updateInternalSearchString();
	}

	private void updateInternalSearchString() {
		this.internalSearchString = (this.caseSensitive || this.searchString == null)? this.searchString : this.searchString.toLowerCase();
	}
	
	private List<MatchResultNode> internalSearchFile(File file) throws IOException {
		List<MatchResultNode> matchResults = null;
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		int row = 0;
		while ((line = reader.readLine()) != null) {
			row++;
			int col = caseSensitive? line.indexOf(internalSearchString) : line.toLowerCase().indexOf(internalSearchString);
			if (col >= 0) {
				if (matchResults == null) {
					matchResults = new ArrayList<MatchResultNode>();
				}
				matchResults.add(new MatchResultNode(getMatchMessage(row, col, line, searchString)));
			}
		}		
		return matchResults;
	}
	
	@Override
	protected List<MatchResultNode> searchFile(File file) throws IOException {
		if (super.searchFile(file) != null) {
			return internalSearchFile(file);
		}
		return null;
	}

	@Override
	protected List<MatchResultNode> searchZipEntry(ZipEntry zipEntry, ZipInputStream zipInputStream, File zipFile) throws IOException {
		if (super.searchZipEntry(zipEntry, zipInputStream, zipFile) != null) {
			File file = extractFileFromZip(zipEntry, zipInputStream);
			return internalSearchFile(file);
		}
		return null;
	}

	private String getMatchMessage(int row, int col, String line, String searchString) {
		final int surroundChars = 20;
		StringBuilder sb = new StringBuilder();
		sb.append("Line ").append(row).append(", Column ").append(col).append(": ");
		int start = Math.max(col-surroundChars, 0);
		int stop = Math.min(col+searchString.length()+surroundChars, line.length());
		if (start > 0) {
			sb.append("...");
		}
		sb.append(line.substring(start, stop));
		if (stop < line.length()) {
			sb.append("...");
		}
		return sb.toString();
	}
	
	@Override
	public String getName() {
		return "Text File Search";
	}

//	@Override
//	public FileSearchFilter makeClone() {
//		TextSearchFilter clone = new TextSearchFilter();
//		clone.setCaseSensitive(isCaseSensitive());
//		clone.setSearchString(getSearchString());
//		clone.setNamePatterns(getNamePatterns());
//		clone.setZipNamePatterns(getZipNamePatterns());
//		return clone;
//	}

	@Override
	public Map<String, String> getSearchCriteria() {
		Map<String, String> searchCriteria = super.getSearchCriteria();
		searchCriteria.put("Search Text", searchString);
		searchCriteria.put("Case Sensitive", String.valueOf(caseSensitive));
		return searchCriteria;
	}
}
