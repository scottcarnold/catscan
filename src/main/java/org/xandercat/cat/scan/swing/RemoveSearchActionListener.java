package org.xandercat.cat.scan.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Action listener for cancelling any search in progress when a search results tab 
 * is closed.
 * 
 * @author Scott Arnold
 */
public class RemoveSearchActionListener implements ActionListener {

	private FileSearchWorker searchWorker;
	
	/**
	 * Construct a new remove search action for the given search worker.
	 * 
	 * @param searchWorker				search worker associated with search results panel
	 */
	public RemoveSearchActionListener(FileSearchWorker searchWorker) {
		super();
		this.searchWorker = searchWorker;
	}
	
	public void actionPerformed(ActionEvent event) {
		if (this.searchWorker != null && !this.searchWorker.isDone()) {
			this.searchWorker.cancel(true);
		}
	}
}
