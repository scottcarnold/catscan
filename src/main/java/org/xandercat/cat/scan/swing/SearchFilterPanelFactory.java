package org.xandercat.cat.scan.swing;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.xandercat.cat.scan.filter.ClassSearchFilter;
import org.xandercat.cat.scan.filter.FileNameSearchFilter;
import org.xandercat.cat.scan.filter.FileSearchFilter;
import org.xandercat.cat.scan.filter.SearchFilterFactory;
import org.xandercat.cat.scan.filter.StatSearchFilter;
import org.xandercat.cat.scan.filter.TextSearchFilter;
import org.xandercat.cat.scan.filter.ZipSearchFilter;
import org.xandercat.swing.zenput.error.ZenputException;
import org.xandercat.swing.zenput.processor.InputProcessor;

public class SearchFilterPanelFactory {

	public static SearchFilterPanel newSearchFilterPanel(Class<? extends FileSearchFilter> searchFilterClass) throws ZenputException {
		final FileSearchFilter filter = SearchFilterFactory.newFilter(searchFilterClass);
		SearchFilterPanel panel = new SearchFilterPanel(filter);
		InputProcessor inputProcessor = panel.getInputProcessor();
		if (filter instanceof FileNameSearchFilter) {
			final JTextField fileNamesField = new JTextField();
			inputProcessor.registerInput("namePatterns", fileNamesField);
			panel.addInput("File Name(s)", fileNamesField);
		}
		if (filter instanceof ZipSearchFilter) {
			final JTextField fileNamesField = new JTextField();
			inputProcessor.registerInput("zipNamePatterns", fileNamesField);
			panel.addInput("ZIP File Name(s)", fileNamesField);			
		}
		if (filter instanceof ClassSearchFilter) {
			final JTextField classNameField = new JTextField();
			inputProcessor.registerInput("className", classNameField);
			panel.addInput("Class or Package Name", classNameField);
		}
		if (filter instanceof TextSearchFilter) {
			final JTextField searchStringField = new JTextField();
			inputProcessor.registerInput("searchString", searchStringField);
			panel.addInput("Search String", searchStringField);
			final JCheckBox caseSensitiveField = new JCheckBox("Case Sensitive");
			inputProcessor.registerInput("caseSensitive", caseSensitiveField);
			panel.addInput(null, caseSensitiveField);
		}
		if (filter instanceof StatSearchFilter) {
			final JComboBox<StatSearchFilter.Stat> statTypeField = new JComboBox<StatSearchFilter.Stat>(StatSearchFilter.Stat.values());
			inputProcessor.registerInput("stat", statTypeField, StatSearchFilter.Stat.class);
			panel.addInput("Stat Type", statTypeField);
			final JTextField maxResultsField = new JTextField();
			inputProcessor.registerInput("maxResults", maxResultsField);
			panel.addInput("Max Results", maxResultsField);
		}
		panel.finish();
		return panel;
	}
}
