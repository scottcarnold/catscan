package org.xandercat.cat.scan.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.xandercat.cat.scan.filter.FileSearchFilter;
import org.xandercat.swing.util.SpringUtilities;
import org.xandercat.swing.zenput.annotation.InputField;
import org.xandercat.swing.zenput.annotation.ValidateFile;
import org.xandercat.swing.zenput.annotation.ValidateFile.Mode;
import org.xandercat.swing.zenput.annotation.ValidateRequired;
import org.xandercat.swing.zenput.error.ZenputException;
import org.xandercat.swing.zenput.processor.CommitMode;
import org.xandercat.swing.zenput.processor.InputProcessor;
import org.xandercat.swing.zenput.processor.SourceProcessor;

/**
 * Panel for setting search parameters for a search filter.  An input for the 
 * search directory is automatically added.  Other search parameter inputs
 * must be added using the addInput(...) method.  Once all inputs are added,
 * a call to the finish() method is required to finish the panel layout.
 * 
 * @author Scott Arnold
 */
public class SearchFilterPanel extends JPanel {

	private static final long serialVersionUID = 2010062201L;
	
	private int inputCount;
	private JPanel inputPanel;
	
	@InputField(title="Directory")
	@ValidateFile(mode=Mode.DIRECTORIES_ONLY, exists=true)
	@ValidateRequired
	private File directory;
	
	private JTextField directoryField;
	private JFileChooser directoryChooser;
	private FileSearchFilter filter;
	private InputProcessor inputProcessor;
	
	public SearchFilterPanel(FileSearchFilter filter) throws ZenputException {
		super(new BorderLayout());
		this.inputPanel = new JPanel(new SpringLayout());
		this.filter = filter;
		SourceProcessor sourceProcessor = new SourceProcessor(filter, this);
		this.inputProcessor = new InputProcessor(sourceProcessor, CommitMode.COMMIT_ALL, true);
		JPanel directoryPanel = new JPanel(new BorderLayout());
		this.directoryField = new JTextField();
		directoryPanel.add(this.directoryField, BorderLayout.CENTER);
		this.directoryChooser = new JFileChooser();
		this.directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		JButton selectButton = new JButton("Select...");
		selectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int returnValue = directoryChooser.showDialog(SearchFilterPanel.this, "Select");
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					directoryField.setText(directoryChooser.getSelectedFile().getAbsolutePath());
				}				
			}
		});
		directoryPanel.add(selectButton, BorderLayout.EAST);
		this.inputProcessor.registerInput("directory", directoryField);
		addInput("Directory", directoryPanel);
	}
	
	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public FileSearchFilter getFilter() {
		return filter;
	}

	public InputProcessor getInputProcessor() {
		return inputProcessor;
	}
	
	/**
	 * Add a search parameter input to the panel.
	 * 
	 * The source should be the source object for the input handler for this input.
	 * A source need only be passed to this method once, though passing it multiple
	 * times will have no ill effects.
	 * 
	 * @param label			text to use for the input label
	 * @param component		input component
	 */
	public void addInput(String label, JComponent component) {
		this.inputCount++;
		if (label != null && label.trim().length() > 0) {
			label = label + ": ";
		}
		this.inputPanel.add(new JLabel(label));
		this.inputPanel.add(component);
	}
	
	/**
	 * Finish layout of the panel.  Call this method after all inputs have been added.
	 */
	public void finish() {
		SpringUtilities.makeCompactGrid(this.inputPanel, this.inputCount, 2, 5, 5, 5, 5);
		add(this.inputPanel, BorderLayout.NORTH);
		add(new JPanel(), BorderLayout.CENTER);
	}
}
