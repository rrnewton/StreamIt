package at.dms.kjc.slir;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import at.dms.kjc.CType;
import at.dms.kjc.common.CommonUtils;
import at.dms.kjc.flatgraph.FlatNode;
import at.dms.kjc.flatgraph.FlatVisitor;
import at.dms.kjc.sir.SIRFileReader;
import at.dms.kjc.sir.SIRFileWriter;
import at.dms.kjc.sir.SIRIdentity;
import at.dms.kjc.sir.SIROperator;
import at.dms.kjc.sir.SIRWriter;
import at.dms.kjc.sir.lowering.RenameAll;

class SIRToFilterNodes implements FlatVisitor {

	public SIRToFilterNodes() {
		/* Do Nothing */
	}

	public HashMap<SIROperator, InputNode> inputNodes;

	public HashMap<SIROperator, OutputNode> outputNodes;

	public HashMap<SIROperator, WorkNode> filterNodes;

	public HashSet<WorkNode> generatedIds;

	private Map<SIROperator, int[]>[] exeCounts;

	public void createNodes(FlatNode top,
			Map<SIROperator, int[]>[] executionCounts) {
		inputNodes = new HashMap<SIROperator, InputNode>();
		outputNodes = new HashMap<SIROperator, OutputNode>();
		filterNodes = new HashMap<SIROperator, WorkNode>();
		generatedIds = new HashSet<WorkNode>();
		this.exeCounts = executionCounts;
		top.accept(this, null, true);
	}

	@Override
	public void visitNode(FlatNode node) {
		OutputNode output = new OutputNode();
		InputNode input = new InputNode();
		WorkNodeContent content;
		int mult = 1;

		if (node.isFilter()) {
			if (node.contents instanceof SIRFileWriter) {
				content = new FileOutputContent((SIRFileWriter) node.contents);
			} else if (node.contents instanceof SIRWriter) {
                content = new WriterOutputContent((SIRWriter) node.contents);
            } else if (node.contents instanceof SIRFileReader) {
				content = new FileInputContent((SIRFileReader) node.contents);
			} else if (node.contents instanceof SIRIdentity) {
				content = new IDFilterContent(((SIRIdentity) node.contents));
			} else {
				content = new WorkNodeContent(node.getFilter());
			}
		} else if (node.isSplitter()) {
			CType type = CommonUtils.getOutputType(node);
			SIRIdentity id = new SIRIdentity(type);
			RenameAll.renameAllFilters(id);
			// content = new FilterContent(id);
			content = new IDFilterContent(id);
			if (!node.isDuplicateSplitter())
				mult = node.getTotalOutgoingWeights();

		} else {
			// joiner
			CType type = CommonUtils.getOutputType(node);
			SIRIdentity id = new SIRIdentity(type);
			RenameAll.renameAllFilters(id);
			// content = new FilterContent(id);
			content = new IDFilterContent(id);
			mult = node.getTotalIncomingWeights();

		}

		if (exeCounts[0].containsKey(node.contents)) {
			System.out
					.println("** setting init mult " + node.contents + " "
							+ mult + " "
							+ exeCounts[0].get(node.contents)[0]);
			content.setInitMult(mult
					* exeCounts[0].get(node.contents)[0]);
		} else {
			content.setInitMult(0);
		}
		if (exeCounts[1].containsKey(node.contents)) {
			content.setSteadyMult(mult
					* exeCounts[1].get(node.contents)[0]);
		} else {
			content.setSteadyMult(0);
		}
		WorkNode filterNode = new WorkNode(content);
		if (node.isSplitter() || node.isJoiner())
			generatedIds.add(filterNode);

		inputNodes.put(node.contents, input);
		outputNodes.put(node.contents, output);
		filterNodes.put(node.contents, filterNode);
	}
}