package org.xandercat.cat.scan.filter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xandercat.cat.scan.result.MatchResultNode;
import org.xandercat.swing.file.BinaryPrefix;
import org.xandercat.swing.file.DirectorySizeCache;
import org.xandercat.swing.file.FileSizeComparator;
import org.xandercat.swing.util.FileUtil;
import org.xandercat.swing.zenput.annotation.InputField;
import org.xandercat.swing.zenput.annotation.ValidateInteger;
import org.xandercat.swing.zenput.annotation.ValidateRequired;

public class StatSearchFilter extends FileNameSearchFilter implements ComparativeSearchFilter {

	public static enum Stat {
		LARGEST_FILES("Largest Files", false), 
		SMALLEST_FILES("Smallest Files", false), 
		LARGEST_DIRECTORIES("Largest Directories", true), 
		SMALLEST_DIRECTORIES("Smallest Directories", true),
		LONGEST_PATH_NAMES("Longest Path Names", false);
		private String label;
		private boolean directorySearch;
		private Stat(String label, boolean directorySearch) {
			this.label = label;
			this.directorySearch = directorySearch;
		}
		public boolean isDirectorySearch() {
			return directorySearch;
		}
		public String toString() {
			return label;
		}
	}
	
	@InputField(title="Stat Type")
	private Stat stat = Stat.LARGEST_FILES;
	
	@InputField(title="Max Results")
	@ValidateRequired
	@ValidateInteger(min=1, max=500)
	private Integer maxResults;
	
	private long criticalSize;
	private final List<File> files = new ArrayList<File>();
	private FileSizeComparator fileSizeComparator;
	private DirectorySizeCache directorySizeCache = DirectorySizeCache.getInstance();
	
	public StatSearchFilter() {
		super();
		this.maxResults = Integer.valueOf(20);
		this.fileSizeComparator = new FileSizeComparator(false, true);
		setZipNamePatterns(null);
		setNamePatterns("*.*");
	}
	
	public Stat getStat() {
		return stat;
	}

	public void setStat(Stat stat) {
		this.stat = stat;
	}

	public Integer getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

	@Override
	public String getName() {
		return "File Statistics";
	}

	@Override
	public FileSearchFilter makeClone() {
		StatSearchFilter filter = new StatSearchFilter();
		filter.setNamePatterns(getNamePatterns());
		filter.setZipNamePatterns(getZipNamePatterns());
		filter.setStat(getStat());
		filter.setMaxResults(getMaxResults());
		return filter;
	}

	public void beginSearch() {
		this.files.clear();
		switch (this.stat) {
		case LARGEST_FILES:
		case LARGEST_DIRECTORIES:
		case LONGEST_PATH_NAMES:
			this.criticalSize = Long.MAX_VALUE;
			break;
		case SMALLEST_FILES:
		case SMALLEST_DIRECTORIES:
			this.criticalSize = Long.MIN_VALUE;
			break;
		}
	}

	public List<MatchResultNode> endSearch() {
		if (this.files.size() == 0) {
			return null;
		}
		switch (this.stat) {
		case LARGEST_FILES:
		case LARGEST_DIRECTORIES:
			this.fileSizeComparator.setAscending(false);
			Collections.sort(this.files, this.fileSizeComparator);
			break;
		case SMALLEST_FILES:
		case SMALLEST_DIRECTORIES:
			this.fileSizeComparator.setAscending(true);
			Collections.sort(this.files, this.fileSizeComparator);
			break;
		case LONGEST_PATH_NAMES:
			Collections.sort(this.files, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o2.getAbsolutePath().length() - o1.getAbsolutePath().length();
				}
			});
			break;
		}
		DirectorySizeCache directorySizeCache = DirectorySizeCache.getInstance();
		List<MatchResultNode> matches = new ArrayList<MatchResultNode>();
		for (File file : this.files) {
			StringBuilder sb = new StringBuilder();
			long size = this.stat.isDirectorySearch()? directorySizeCache.loadDirectorySize(file).getBytes() : file.length();
			sb.append(FileUtil.formatFileSize(size, BinaryPrefix.GiB));
			sb.append(" -- ");
			sb.append(file.getAbsolutePath());
			matches.add(new MatchResultNode(sb.toString()));
		}
		return matches;
	}
		
	private void internalSearchFile(File file) throws IOException {
		long fileLength = this.stat.isDirectorySearch()? directorySizeCache.loadDirectorySize(file).getBytes() : file.length();
		if (this.files.size() < this.maxResults.intValue()) {
			this.files.add(file);
			switch (this.stat) {
			case LARGEST_FILES:
			case LARGEST_DIRECTORIES:
				if (fileLength < this.criticalSize) {
					this.criticalSize = fileLength;
				}
				break;
			case SMALLEST_FILES:
			case SMALLEST_DIRECTORIES:
				if (fileLength > this.criticalSize) {
					this.criticalSize = fileLength;
				}
				break;
			case LONGEST_PATH_NAMES:
				if (file.getAbsolutePath().length() < this.criticalSize) {
					this.criticalSize = file.getAbsolutePath().length();
				}
				break;
			}
		} else {
			switch (this.stat) {
			case LARGEST_FILES:
			case LARGEST_DIRECTORIES:
				if (fileLength > this.criticalSize) {
					this.files.add(file);
					boolean found = false;
					long newCriticalSize = Long.MAX_VALUE;
					for (Iterator<File> iter = this.files.iterator(); iter.hasNext();) {
						File rfile = iter.next();
						long rFileLength = this.stat.isDirectorySearch()? directorySizeCache.loadDirectorySize(rfile).getBytes() : rfile.length();
						if (!found && rFileLength == this.criticalSize) {
							iter.remove();
							found = true;
						} else {
							if (rFileLength < newCriticalSize) {
								newCriticalSize = rFileLength;
							}
						}
					}
					this.criticalSize = newCriticalSize;
				}
				break;
			case LONGEST_PATH_NAMES:
				int pathLength = file.getAbsolutePath().length();
				if (pathLength > this.criticalSize) {
					this.files.add(file);
					boolean found = false;
					long newCriticalSize = Long.MAX_VALUE;
					for (Iterator<File> iter = this.files.iterator(); iter.hasNext();) {
						File rFile = iter.next();
						long rPathLength = rFile.getAbsolutePath().length();
						if (!found && rPathLength == this.criticalSize) {
							iter.remove();
							found = true;
						} else {
							if (rPathLength < newCriticalSize) {
								newCriticalSize = rPathLength;
							}
						}
					}
					this.criticalSize = newCriticalSize;
				}
				break;
			case SMALLEST_FILES:
			case SMALLEST_DIRECTORIES:
				if (fileLength < this.criticalSize) {
					this.files.add(file);
					boolean found = false;
					long newCriticalSize = 0;
					for (Iterator<File> iter = this.files.iterator(); iter.hasNext();) {
						File rfile = iter.next();
						long rFileLength = this.stat.isDirectorySearch()? directorySizeCache.loadDirectorySize(rfile).getBytes() : rfile.length();
						if (!found && rFileLength == this.criticalSize) {
							iter.remove();
							found = true;
						} else {
							if (rFileLength > newCriticalSize) {
								newCriticalSize = rFileLength;
							}
						}
					}						
					this.criticalSize = newCriticalSize;
				}
				break;
			}
		}		
	}
	
	@Override
	public void processingDirectory(File directory) {
		super.processingDirectory(directory);
		if (this.stat.isDirectorySearch() && fileNameMatches(directory.getName())) {
			try {
				internalSearchFile(directory);
			} catch (IOException e) {
				//TODO: Log error
			}
		}
	}

	@Override
	protected List<MatchResultNode> searchFile(File file) throws IOException {
		if (!this.stat.isDirectorySearch() && super.searchFile(file) != null) {
			internalSearchFile(file);
		}
		return null;
	}

	@Override
	protected List<MatchResultNode> searchZipEntry(ZipEntry zipEntry, ZipInputStream zipInputStream, File zipFile) throws IOException {
		if (!this.stat.isDirectorySearch() && super.searchZipEntry(zipEntry, zipInputStream, zipFile) != null) {
			internalSearchFile(new ZipEntryFile(zipFile, zipEntry));
		}
		return null;
	}

	@Override
	public Map<String, String> getSearchCriteria() {
		Map<String, String> searchCriteria = super.getSearchCriteria();
		searchCriteria.put("Stat Type", stat.toString()); 
		searchCriteria.put("Max Results", String.valueOf(maxResults));
		return searchCriteria;
	}
}
