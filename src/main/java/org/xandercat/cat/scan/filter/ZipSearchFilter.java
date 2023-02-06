package org.xandercat.cat.scan.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xandercat.cat.scan.result.MatchResultNode;
import org.xandercat.swing.util.FileUtil;
import org.xandercat.swing.zenput.annotation.InputField;

/**
 * Abstract search filter capable of searching through ZIP archives.
 * 
 * @author Scott Arnold
 */
public abstract class ZipSearchFilter extends FileSearchFilter {
	
	@InputField(title="ZIP File Name(s)")
	private String zipNamePatterns;
	
	private final List<Pattern> zipNamePatternsRegEx = new ArrayList<Pattern>();
	
	public ZipSearchFilter() {
		super();
		setZipNamePatterns("*.zip");
	}
	
	/**
	 * Searches a ZIP entry / ZIP input stream for the given search string with the given case sensitivity.
	 *
	 * @param zipEntry			ZIP entry
	 * @param zipInputStream	ZIP input stream
	 * @param zipFile			ZIP file ZIP entry and ZIP input stream are from
	 * 
	 * @return					list of matches
	 * 
	 * @throws IOException
	 */
	protected abstract List<MatchResultNode> searchZipEntry(ZipEntry zipEntry, ZipInputStream zipInputStream, File zipFile) throws IOException;
	
	/**
	 * Searches a file for the given search string with the given case sensitivity.
	 * 
	 * @param file				file to search
	 * 
	 * @return					list of matches
	 * 
	 * @throws IOException
	 */
	protected abstract List<MatchResultNode> searchFile(File file) throws IOException;
	
	/**
	 * Extracts the file for the given ZIP entry of the given ZIP input stream into a temporary file.
	 * This method is provided as a convenience for subclasses.  By using this method, ZIP entries
	 * can be searched in the same manner as regular files.
	 *  
	 * @param zipEntry			ZIP entry for file to search
	 * @param zipInputStream	ZIP input stream containing file
	 * 
	 * @return					temporary file storing contents of file from ZIP entry
	 * 
	 * @throws IOException
	 */
	protected File extractFileFromZip(ZipEntry zipEntry, ZipInputStream zipInputStream) throws IOException {
		String zipEntryFileName = getZipEntryFileName(zipEntry);
		String tempName = FileUtil.getFileNameLessExtension(zipEntryFileName);
		String tempExtension = FileUtil.getExtension(zipEntryFileName);
		File extractedFile = File.createTempFile(tempName, tempExtension);
		extractedFile.deleteOnExit();
		FileOutputStream fileOutputStream = new FileOutputStream(extractedFile);
		for (int c = zipInputStream.read(); c!= -1; c = zipInputStream.read()) {
			fileOutputStream.write(c);
		}
		zipInputStream.closeEntry();
		fileOutputStream.close();
		return extractedFile;
	}
	
	public String getZipNamePatterns() {
		return this.zipNamePatterns;
	}

	public void setZipNamePatterns(String zipNamePatterns) {
		this.zipNamePatterns = zipNamePatterns;
		this.zipNamePatternsRegEx.clear();
		if (zipNamePatterns != null && zipNamePatterns.trim().length() > 0) {
			String[] individualPatterns = zipNamePatterns.split(",");
			for (String zipNamePattern : individualPatterns) {
				String regEx = FileUtil.generateRegularExpression(zipNamePattern.toLowerCase());
				this.zipNamePatternsRegEx.add(Pattern.compile(regEx));
			}
		}
	}
	
	private boolean matches(String fileName) {
		fileName = fileName.toLowerCase();
		for (Pattern zipNamePattern : this.zipNamePatternsRegEx) {
			if (zipNamePattern.matcher(fileName).matches()) {
				return true;
			}
		}		
		return false;
	}
	
	public List<MatchResultNode> search(File file) throws IOException {
		if (matches(file.getName())) {
			return searchArchive(file);
		} else {
			return searchFile(file);
		}
	}
	
	private List<MatchResultNode> searchArchive(File file) throws IOException {
		List<MatchResultNode> matchResults = new ArrayList<MatchResultNode>();
		MatchResultNode tempResult = new MatchResultNode(file.getName());	// temporary holder
		FileInputStream fileInputStream = new FileInputStream(file);
		searchArchive(tempResult, fileInputStream, file);
		@SuppressWarnings("unchecked")
		Enumeration<MatchResultNode> childrenEnum = (Enumeration<MatchResultNode>) tempResult.children();
		while (childrenEnum.hasMoreElements()) {
			matchResults.add(childrenEnum.nextElement());
		}
		try {
			fileInputStream.close();
		} catch (Exception e) { 
			// nothing really needs to be done here
		}
		return (matchResults.size() > 0)? matchResults : null;
	}
	
	private void searchArchive(MatchResultNode parentMatchResult, InputStream inputStream, File zipFile) throws IOException {
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);
		ZipEntry zipEntry = null;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			if (matches(getZipEntryFileName(zipEntry))) {
				MatchResultNode nestedMatchResult = new MatchResultNode(zipEntry.getName());
				searchArchive(nestedMatchResult, zipInputStream, zipFile);
				if (nestedMatchResult.getChildCount() > 0) {
					parentMatchResult.add(nestedMatchResult);
				}
			} else {
				List<MatchResultNode> zipMatchResults = searchZipEntry(zipEntry, zipInputStream, zipFile);
				if (zipMatchResults != null) {
					MatchResultNode matchResult = new MatchResultNode(zipEntry.getName());
					for (MatchResultNode zipMatchResult : zipMatchResults) {
						matchResult.add(zipMatchResult);
					}
					parentMatchResult.add(matchResult);
				}
			}
		}
	}
	
	protected String getZipEntryFileName(ZipEntry zipEntry) {
		String fileName = zipEntry.getName();
		int i = fileName.lastIndexOf("/");
		if (i >= 0) {
			return fileName.substring(i+1);
		} else {
			return fileName;
		}
	}

	protected String getCSVString(List<String> strings) {
		if (strings == null || strings.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(strings.get(0));
		for (int i=1; i<strings.size(); i++) {
			sb.append(',').append(strings.get(i));
		}
		return sb.toString();
	}
	
	@Override
	public Map<String, String> getSearchCriteria() {
		Map<String, String> searchCriteria = new HashMap<String, String>();
		searchCriteria.put("ZIP Name Pattern(s)", zipNamePatterns);
		return searchCriteria;
	}
}
