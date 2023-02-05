package org.xandercat.cat.scan.result;

import java.io.File;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

public class MatchResultNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 2010061501L;

	private File file;	// file associated with node, if any
	
	public MatchResultNode(File file) {
		this(file.getName(), file);
	}
	
	public MatchResultNode(String message) {
		this(message, null);
	}
	
	public MatchResultNode(String message, File file) {
		super(message);
		this.file = file;
	}
	
	public MatchResultNode(MatchResultNode node) {
		super(node.getUserObject());
		this.file = node.file;
		@SuppressWarnings("unchecked")
		Enumeration<MatchResultNode> children = node.children();
		while (children.hasMoreElements()) {
			add(new MatchResultNode(children.nextElement()));
		}
	}
	
	public File getFile() {
		return file;
	}
}
