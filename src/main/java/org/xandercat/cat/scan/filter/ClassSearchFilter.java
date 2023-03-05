package org.xandercat.cat.scan.filter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xandercat.cat.scan.result.MatchResultNode;
import org.xandercat.swing.zenput.annotation.InputField;
import org.xandercat.swing.zenput.annotation.ValidateRequired;


/**
 * Search filter for finding Java classes or packages.  This search can find classes or
 * packages within the regular file system or within typical Java archives including 
 * jar, war, and ear archives.  You can change what types of archives to search using the
 * setZipExtensions method.  It should be noted that a package will not be found unless
 * it contains at least one class file within it.
 * 
 * @author Scott Arnold
 */
public class ClassSearchFilter extends FileNameSearchFilter {

	@InputField(title="Class or Package Name")
	@ValidateRequired
	private String className;
	
	private String classSearchString;
	private boolean caseSensitive;
	
	public ClassSearchFilter() {
		super();
		setNamePatterns("*.class,*.java");
		setZipNamePatterns("*.jar,*.war,*.ear");
	}
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
		updateClassSearchString();
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		updateClassSearchString();
	}

	protected List<MatchResultNode> searchFile(File file) throws IOException {
		if (super.searchFile(file) != null) {
			String filePath = file.getAbsolutePath().replaceAll("\\\\", "/");
			if (matches(filePath)) {
				// no actual entries required; just need to return non-null result to indicate a match
				return SearchFilter.EMPTY_MATCH;
			}
		}
		return null;
	}
	
	protected List<MatchResultNode> searchZipEntry(ZipEntry zipEntry, ZipInputStream zipInputStream, File zipFile) throws IOException {
		if (super.searchZipEntry(zipEntry, zipInputStream, zipFile) != null) {
			if (matches(zipEntry.getName())) {
				// no actual entries required; just need to return non-null result to indicate a match
				return SearchFilter.EMPTY_MATCH;
			}
		}
		return null;
	}
	
	private void updateClassSearchString() {
		this.classSearchString = this.className;
		if (this.className != null) {
			if (this.className.endsWith(".class")) {
				this.classSearchString = this.classSearchString.substring(0, this.classSearchString.length() - 6);
			}
			this.classSearchString = this.classSearchString.replaceAll("\\.", "/");
			if (this.className.endsWith(".class")) {
				this.classSearchString = this.classSearchString + ".class";
			}
			if (!this.caseSensitive) {
				this.classSearchString = this.classSearchString.toLowerCase();
			}
		}
	}
	
	private boolean matches(String name) {
		if (name == null) {
			return false;
		}
		if (this.caseSensitive) {
			return name.indexOf(this.classSearchString) >= 0;
		} else {
			return name.toLowerCase().indexOf(this.classSearchString) >= 0;
		}
	}
	
	public String getName() {
		return "Java Class/Package Search";
	}

	@Override
	public FileSearchFilter makeClone() {
		ClassSearchFilter clone = new ClassSearchFilter();
		clone.setCaseSensitive(isCaseSensitive());
		clone.setClassName(getClassName());
		clone.setNamePatterns(getNamePatterns());
		clone.setZipNamePatterns(getZipNamePatterns());
		return clone;
	}

	@Override
	public Map<String, String> getSearchCriteria() {
		Map<String, String> searchCriteria = super.getSearchCriteria();
		searchCriteria.put("Class or Package", className);
		searchCriteria.put("Case Sensitive", String.valueOf(caseSensitive));
		return searchCriteria;
	}
}
