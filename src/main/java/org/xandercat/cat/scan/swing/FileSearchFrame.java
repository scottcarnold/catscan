package org.xandercat.cat.scan.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.cat.scan.filter.FileSearchFilter;
import org.xandercat.cat.scan.filter.SearchFilter;
import org.xandercat.cat.scan.filter.SearchFilterFactory;
import org.xandercat.cat.scan.media.Icons;
import org.xandercat.swing.app.ApplicationFrame;
import org.xandercat.swing.dialog.AboutDialog;
import org.xandercat.swing.label.SpinnerIconLabel;
import org.xandercat.swing.panel.CloseableTab;
import org.xandercat.swing.util.PlatformTool;
import org.xandercat.swing.zenput.error.ZenputException;
import org.xandercat.swing.zenput.processor.InputProcessor;
import org.xandercat.swing.zenput.util.ValidationErrorUtil;

public class FileSearchFrame extends ApplicationFrame {
	
	private static final long serialVersionUID = 2023020501L;
	private static final Logger log = LogManager.getLogger(FileSearchFrame.class);
	
	private JButton searchButton;
	
	private JTabbedPane inputPane;
	private JTabbedPane resultTabbedPane;
	private List<SearchFilterPanel> searchFilterPanels = new ArrayList<SearchFilterPanel>();
	private AboutDialog aboutDialog;
	
	private Executor executor;
	
	public FileSearchFrame(String appName, String appVersion) {
		super(appName, appVersion);
		if (PlatformTool.isWindows()) {
			setIconImage(Icons.CATSCAN_ICON.getImage());
		}
		buildComponents();
		setContentPane(prepareLayout());
		pack();
		setSize(600, 600);
		setLocationRelativeTo(null);
	}
	
	private void buildComponents() {
		this.executor = Executors.newFixedThreadPool(3);
		
		// action buttons
		this.searchButton = new JButton("Search");
		this.searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				SearchFilterPanel searchPanel = searchFilterPanels.get(inputPane.getSelectedIndex());
				InputProcessor inputProcessor = searchPanel.getInputProcessor();
				try {
					if (inputProcessor.validate()) {
						FileSearchFilter filter = SearchFilterFactory.newFilter(searchPanel.getFilter());
						executeSearch(filter, searchPanel.getDirectory());						
					} else {
						ValidationErrorUtil.showMessageDialog(FileSearchFrame.this, 
								inputProcessor, 
								inputProcessor.getErrors(), 
								"The following fields need to be corrected:");
					}
				} catch (ZenputException e) {
					log.error("Search cancelled.  Unable to validate inputs.", e);
				}
			}
		});
		
		// results
		this.resultTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		this.resultTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		// about dialog
		this.aboutDialog = new AboutDialog(this);
		File aboutMarkdownFile = new File("RELEASE_NOTES.md");
		this.aboutDialog.addMarkdownContent(aboutMarkdownFile, "background-color: #F0F0F0; padding-left: 10px; padding-right: 10px");
		this.aboutDialog.build();
	}
	
	private Container prepareLayout() {
		JPanel inputPanel = new JPanel(new BorderLayout());
		this.inputPane = new JTabbedPane(JTabbedPane.TOP);
		this.inputPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		List<Class<? extends FileSearchFilter>> filterClasses = SearchFilterFactory.getDefaultFilterClasses();
		for (Class<? extends FileSearchFilter> filterClass : filterClasses) {
			try {
				SearchFilterPanel panel = SearchFilterPanelFactory.newSearchFilterPanel(filterClass);
				this.searchFilterPanels.add(panel);
				JScrollPane scrollPane = new JScrollPane(panel);
				scrollPane.getVerticalScrollBar().setUnitIncrement(5);
				inputPane.addTab(panel.getFilter().getName(), scrollPane);
			} catch (ZenputException ie) {
				log.error("Unable to add filter of type " + filterClass.getName(), ie);
			}
		}
		inputPanel.add(inputPane, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 3));
		buttonPanel.add(new JLabel());
		JPanel searchButtonPanel = new JPanel(new FlowLayout());
		searchButtonPanel.add(this.searchButton);
		buttonPanel.add(searchButtonPanel);
		JPanel versionPanel = new JPanel(new BorderLayout());
		JPanel versionContentPanel = new JPanel(new FlowLayout());
		JLabel versionLabel = new JLabel("Version " + getApplicationVersion());
		JButton versionInfoButton = new JButton(Icons.INFO_ICON);
		versionInfoButton.addActionListener(actionEvent -> aboutDialog.setVisible(true));
		versionContentPanel.add(versionLabel);
		versionContentPanel.add(versionInfoButton);
		versionPanel.add(versionContentPanel, BorderLayout.EAST);
		buttonPanel.add(versionPanel);
		inputPanel.add(buttonPanel, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(this.searchButton);
		JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, this.resultTabbedPane);
		mainPanel.setOneTouchExpandable(true);
		return mainPanel;
	}
	
	private void executeSearch(FileSearchFilter filter, File directory) {
		JPanel searchResultsPanel = new JPanel(new BorderLayout());
		searchResultsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JScrollPane searchResultsScrollPane = new JScrollPane();
		SpinnerIconLabel searchResultsStatusLabel = new SpinnerIconLabel("Waiting to search...", 200, 16, 6, 2);
		CloseableTab tab = new CloseableTab(this.resultTabbedPane);
		tab.setToolTipText(getSearchCriteriaText(filter, directory));
		FileSearchWorker searchWorker = new FileSearchWorker(
				searchResultsScrollPane, directory, filter, searchResultsStatusLabel);
		searchResultsPanel.add(searchResultsScrollPane, BorderLayout.CENTER);
		searchResultsPanel.add(searchResultsStatusLabel, BorderLayout.SOUTH);
		String filterName = filter.getName();
		String tabTitle = null;
		if (filterName.toLowerCase().endsWith(" search")) {
			tabTitle = filterName.substring(0, filterName.length() - 7) + " Results";
		} else {
			tabTitle = filterName + " Results";
		}
		tab.addActionListener(new RemoveSearchActionListener(searchWorker));
		this.resultTabbedPane.addTab(tabTitle, searchResultsPanel);
		int tabIndex = this.resultTabbedPane.indexOfComponent(searchResultsPanel);
		this.resultTabbedPane.setTabComponentAt(tabIndex, tab);
		this.resultTabbedPane.setSelectedComponent(searchResultsPanel);
		this.executor.execute(searchWorker);
	}
	
	private String getSearchCriteriaText(SearchFilter filter, File directory) {
		Map<String, String> searchCriteria = filter.getSearchCriteria();
		StringBuilder sb = new StringBuilder("<html><b>" + filter.getName() + " Results For Criteria:</b>");
		sb.append("<p>").append(directory.getAbsolutePath()).append("</p>");
		sb.append("<table>");
		for (Map.Entry<String, String> entry : searchCriteria.entrySet()) {
			sb.append("<tr><td>").append(entry.getKey()).append(":</td><td>").append(entry.getValue()).append("</td></tr>");
		}
		sb.append("</table></html>");
		return sb.toString();
	}
}
