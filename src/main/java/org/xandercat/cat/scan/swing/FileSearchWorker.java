package org.xandercat.cat.scan.swing;

import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.tree.TreePath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.cat.scan.filter.ComparativeSearchFilter;
import org.xandercat.cat.scan.filter.SearchFilter;
import org.xandercat.cat.scan.result.MatchResultModel;
import org.xandercat.cat.scan.result.MatchResultNode;
import org.xandercat.cat.scan.result.MatchResultTreeCellRenderer;
import org.xandercat.cat.scan.result.MetadataNode;
import org.xandercat.swing.label.RotatingIconLabel;
import org.xandercat.swing.util.FileUtil;

/**
 * Background worker for performing a file search for a set of search parameters.
 * Search results are populated in a provided scroll pane.  Search progress will be
 * set through a label, if a label is provided.
 * 
 * @author Scott Arnold
 */
public class FileSearchWorker extends SwingWorker<MatchResultModel, File> {

	public static final String NO_MATCHES = "No matches found.";
	
	private static final Logger log = LogManager.getLogger(FileSearchWorker.class);
	private static final Set<String> scriptExtensions = new HashSet<String>();
	static {
		scriptExtensions.addAll(Arrays.asList("bat", "cmd", "sh"));
	}
	
	private JTree resultTree;
	private JScrollPane resultScrollPane;
	private File directory;
	private SearchFilter filter;
	private JLabel statusLabel;
	private MatchResultNode rootNode;
	private final Lock rootLock = new ReentrantLock();
	private int resultCountLastPublish;
	private boolean matchesFound;
	private volatile int errors;
	
	/**
	 * Constructs a new file search worker with the given parameters.
	 *  
	 * @param resultScrollPane	scroll pane to show results in
	 * @param directory			directory to search
	 * @param filter			filter to search with
	 * @param statusLabel		label where status can be updated
	 */
	public FileSearchWorker(JScrollPane resultScrollPane, File directory, SearchFilter filter, JLabel statusLabel) {
		this.resultScrollPane = resultScrollPane;
		this.directory = directory;
		this.filter = filter;
		this.statusLabel = statusLabel;
		this.rootNode = new MatchResultNode(this.directory.getAbsolutePath());
		MetadataNode criteriaNode = new MetadataNode("Search Criteria");
		Map<String, String> criteria = filter.getSearchCriteria();
		for (Map.Entry<String, String> entry : criteria.entrySet()) {
			criteriaNode.add(new MetadataNode(entry.getKey() + ": " + entry.getValue()));
		}
		this.rootNode.add(criteriaNode);
	}
	
	@Override
	protected MatchResultModel doInBackground() throws IOException {
		if (this.statusLabel != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setStatus("Searching...", true);
				}
			});
		}
		if (this.filter instanceof ComparativeSearchFilter) {
			((ComparativeSearchFilter) this.filter).beginSearch();
		}
		search(this.rootNode, this.directory, this.filter);
		if (this.filter instanceof ComparativeSearchFilter) {
			List<MatchResultNode> endSearchNodes = ((ComparativeSearchFilter) this.filter).endSearch();
			if (endSearchNodes != null && endSearchNodes.size() > 0) {
				this.rootLock.lock();
				try {
					for (MatchResultNode endSearchNode : endSearchNodes) {
						rootNode.add(endSearchNode);
					}
				} finally {
					this.rootLock.unlock();
				}
			}
		}		
		matchesFound = rootNode.getChildCount() > 1;
		if (!matchesFound) {
			this.rootLock.lock();
			try {
				rootNode.add(new MatchResultNode(NO_MATCHES));
			} finally {
				this.rootLock.unlock();
			}
		}
		return buildModel();
	}

	private MatchResultModel buildModel() {
		this.rootLock.lock();
		try {
			return new MatchResultModel(new MatchResultNode(this.rootNode));
		} finally {
			this.rootLock.unlock();
		}
	}
	
	private void setStatus(String message) {
		if (this.statusLabel != null) {
			this.statusLabel.setText(message);
		}
	}
	
	private void setStatus(String message, boolean animate) {
		setStatus(message);
		if (this.statusLabel != null && (this.statusLabel instanceof RotatingIconLabel)) {
			if (animate) {
				((RotatingIconLabel) this.statusLabel).startAnimate();
			} else {
				((RotatingIconLabel) this.statusLabel).stopAnimate();
			}
		}
	}
	
	@Override
	protected void done() {
		if (isCancelled()) {
			setStatus("Search cancelled.", false);
		} else {
			try {
				showTree(get());
				int resultCount = matchesFound? this.rootNode.getLeafCount()-filter.getSearchCriteria().size() : 0;
				StringBuilder sb = new StringBuilder();
				sb.append("Search complete - ").append(resultCount);
				if (resultCount == 1) {
					sb.append(" match.");
				} else {
					sb.append(" matches.");
				}
				if (errors > 0) {
					sb.append("  ").append(errors).append(" error(s) during search.");
				}
				setStatus(sb.toString(), false);
			} catch (Exception e) {
				log.error("Unable to complete file search.", e);
				setStatus("Search could not be completed due to an error.", false);
			}
		}
	}

	@Override
	protected void process(List<File> files) {
		if (files != null && files.size() > 0) {
			File latestFile = files.get(files.size()-1);
			setStatus("Searching " + latestFile.getName() + "...");
		}
		int resultCount = 0;
		this.rootLock.lock();
		try {
			resultCount = this.rootNode.getLeafCount();
		} finally {
			this.rootLock.unlock();
		}
		if (this.resultCountLastPublish < resultCount) {
			this.resultCountLastPublish = resultCount;
			showTree(buildModel());
		}
	}

	private void showTree(MatchResultModel model) {
		if (this.resultTree == null) {
			this.resultTree = new JTree(model);
			this.resultTree.setToggleClickCount(0);
			this.resultTree.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
						TreePath path = resultTree.getPathForLocation(e.getX(), e.getY());
						if (path == null) {
							return;
						}
						File file = ((MatchResultNode) path.getLastPathComponent()).getFile();
						if (file == null || !Desktop.isDesktopSupported()) {
							return;
						}
						if (scriptExtensions.contains(FileUtil.getExtension(file))) {
							if (Desktop.getDesktop().isSupported(Desktop.Action.EDIT)) {
								try {
									Desktop.getDesktop().edit(file);
								} catch (IOException ioe) {
									log.warn("File " + file.getAbsolutePath() + " cannot be edited.", ioe);
								}
							}							
						} else {
							if (Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
								try {
									Desktop.getDesktop().open(file);
								} catch (IOException ioe) {
									log.warn("File " + file.getAbsolutePath() + " cannot be opened.", ioe);
								}
							}
						}
					}
				}
			});
			this.resultTree.setCellRenderer(new MatchResultTreeCellRenderer());
			this.resultScrollPane.setViewportView(this.resultTree);
			for (int i=0; i<resultTree.getRowCount(); i++) {
				TreePath treePath = resultTree.getPathForRow(i);
				if (!(treePath.getLastPathComponent() instanceof MetadataNode)) { 
					resultTree.expandRow(i);
				}
			}
		} else {
			this.resultTree.setModel(model);
			//TODO: should not expand paths that the user has collapsed.
			for (int i=0; i<resultTree.getRowCount(); i++) {
				TreePath treePath = resultTree.getPathForRow(i);
				if (!(treePath.getLastPathComponent() instanceof MetadataNode)) { 
					resultTree.expandRow(i);
				}
			}			
		}
	}
	
	private void orderFilesBeforeDirectories(File[] files) {
		//TODO: Determine how much time this is taking to determine if it is excessive
		int i = 0;
		File tempFile = null;
		while (i < files.length) {
			if (files[i].isDirectory()) {
				int j = i+1;
				while (j < files.length && files[j].isDirectory()) {
					j++;
				}
				if (j < files.length) {
					// bubble file up
					tempFile = files[j];
					for (int k = j; k>i; k--) {
						files[k] = files[k-1];
					}
					files[i] = tempFile;
				}
				i = j+1;
			} else {
				i++;
			}
		}
	}
	
	private boolean search(MatchResultNode parent, File file, SearchFilter filter) {
		if (file.isDirectory()) {
			filter.processingDirectory(file);
			File[] subfiles = file.listFiles();
			boolean subfileMatchFound = false;
			if (subfiles != null) {
				orderFilesBeforeDirectories(subfiles);
				for (File subfile : subfiles) {
					MatchResultNode subnode = new MatchResultNode(subfile);
					if (search(subnode, subfile, filter)) {
						this.rootLock.lock();
						try {
							parent.add(subnode);
						} finally {
							this.rootLock.unlock();
						}
						subfileMatchFound = true;
					}
				}
			}
			return subfileMatchFound;
		} else {
			try {
				publish(file);
				List<MatchResultNode> resultNodes = filter.search(file);
				if (resultNodes != null) {
					this.rootLock.lock();
					try {
						for (MatchResultNode resultNode : resultNodes) {
							parent.add(resultNode);
						}
					} finally {
						this.rootLock.unlock();
					}
					return true;
				}
			} catch (IOException ioe) {
				log.error("File could not be searched: " + file.getAbsolutePath(), ioe);
				this.errors++;
			}
		}
		return false;
	}
}
