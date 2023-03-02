package org.xandercat.cat.scan.result;

import java.io.File;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;

import org.xandercat.swing.tree.TreeStateSaveableNode;

public class MatchResultNode extends DefaultMutableTreeNode implements TreeStateSaveableNode {

	private static final long serialVersionUID = 2010061501L;

	private File file;	// file associated with node, if any
	private String uniqueId;
	
	public MatchResultNode(File file) {
		this(file.getName(), file);
	}
	
	public MatchResultNode(String message) {
		this(message, null);
	}
	
	public MatchResultNode(String message, File file) {
		super(message);
		this.file = file;
		this.uniqueId = UUID.randomUUID().toString();
	}
	
	public MatchResultNode(MatchResultNode node) {
		super(node.getUserObject());
		this.file = node.file;
		this.uniqueId = node.uniqueId;
		@SuppressWarnings("unchecked")
		Enumeration<MatchResultNode> children = node.children();
		while (children.hasMoreElements()) {
			MatchResultNode matchResultNode = children.nextElement();
			if (matchResultNode instanceof MetadataNode) {
				add(new MetadataNode(matchResultNode));
			} else {
				add(new MatchResultNode(matchResultNode));
			}
		}
	}
	
	public File getFile() {
		return file;
	}

	@Override
	public Serializable getUniqueId() {
		return uniqueId;
	}
}
