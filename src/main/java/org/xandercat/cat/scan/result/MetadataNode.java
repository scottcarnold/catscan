package org.xandercat.cat.scan.result;

public class MetadataNode extends MatchResultNode {

	private static final long serialVersionUID = -3539037442657029717L;

	public MetadataNode(String metadata) {
		super(metadata);
	}
	
	public MetadataNode(MatchResultNode matchResultNode) {
		super(matchResultNode);
	}
}
