package org.xandercat.cat.scan.result;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.xandercat.cat.scan.media.Icons;
import org.xandercat.cat.scan.swing.FileSearchWorker;

/**
 * Tree cell renderer for match results.  
 * 
 * @author Scott Arnold
 */
public class MatchResultTreeCellRenderer implements TreeCellRenderer {

	private DefaultTreeCellRenderer defaultRenderer;
	
	public MatchResultTreeCellRenderer() {
		this.defaultRenderer = new DefaultTreeCellRenderer();
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		JLabel label = (JLabel) this.defaultRenderer.getTreeCellRendererComponent(
				tree, value, selected, expanded, leaf, row, hasFocus);
		if (value instanceof MetadataNode) {
			label.setIcon(Icons.INFO_ICON);
			return label;
		}
		MatchResultNode node = (MatchResultNode) value;
		if (node.getFile() != null) {
			if (!node.getFile().isDirectory()) {
				label.setIcon(this.defaultRenderer.getDefaultLeafIcon());
			} else if (leaf) {
				label.setIcon(this.defaultRenderer.getDefaultClosedIcon());
			}
		} else {
			 if (leaf) {
				if (FileSearchWorker.NO_MATCHES.equals(node.getUserObject())) {
					label.setIcon(Icons.WARNING_ICON);
				} else {
					label.setIcon(Icons.CHECKED_ICON);
				}
			}
		}
		return label;
	}
}
