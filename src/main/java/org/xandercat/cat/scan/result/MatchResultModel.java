package org.xandercat.cat.scan.result;

import javax.swing.tree.DefaultTreeModel;

public class MatchResultModel extends DefaultTreeModel {

	private static final long serialVersionUID = 2010061501L;
	
	public MatchResultModel(MatchResultNode root) {
		super(root);
	}
}
