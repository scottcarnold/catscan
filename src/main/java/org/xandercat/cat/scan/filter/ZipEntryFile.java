package org.xandercat.cat.scan.filter;

import java.io.File;
import java.util.zip.ZipEntry;

public class ZipEntryFile extends File {

	private static final long serialVersionUID = 2010062801L;
	
	private ZipEntry zipEntry;
	private File zipFile;
	
	public ZipEntryFile(File zipFile, ZipEntry zipEntry) {
		super(zipEntry.getName());
		this.zipEntry = new ZipEntry(zipEntry);
		this.zipFile = zipFile;
	}

	@Override
	public String getAbsolutePath() {
		return zipFile.getAbsolutePath() + File.separator + super.getName();
	}

	@Override
	public String getPath() {
		return zipFile.getPath() + File.separator + super.getName();
	}

	@Override
	public boolean isDirectory() {
		return zipEntry.isDirectory();
	}

	@Override
	public boolean isFile() {
		return !zipEntry.isDirectory();
	}

	@Override
	public long lastModified() {
		return zipEntry.getTime();
	}

	@Override
	public long length() {
		return zipEntry.getSize();
	}
}
