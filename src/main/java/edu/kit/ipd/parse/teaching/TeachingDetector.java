package edu.kit.ipd.parse.teaching;

import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.ParseGraph;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MetaInfServices(AbstractAgent.class)
public class TeachingDetector extends AbstractAgent {

	private static final String ID = "TeachingDetector";
	private static final Logger logger = LoggerFactory.getLogger(TeachingDetector.class);
	public static final String NEXT_TOKEN_RELATION = "relation";
	public static final String TOKEN_NODE_TYPE = "token";
	public static final String STRING = "String";
	public static final String FLOAT_PRIMITIVE = "float";
	public static final String IS_TEACHING_SEQUENCE_ATTRIBUTE_NAME = "isTeachingSequence";
	public static final String IS_TEACHING_SEQUENCE_PROBABILITY_ATTRIBUTE_NAME = "isTeachingSequenceProbability";
	public static final String TEACHING_SEQUENCE_PART_ATTRIBUTE_VALUE = "teachingSequencePart";

	@Override
	public void init() {
		setId(ID);
	}

	@Override
	protected void exec() {

		if (!checkMandatory()) {
			logger.info("Mandatory pre-condition not met, exiting!");
		} else if (checkExecBefore()) {
			logger.info("Was executed before, exiting!");
		} else {
			createMockAttributes(graph.getNodeType(TOKEN_NODE_TYPE));
			INode currNode = ((ParseGraph) graph).getFirstUtteranceNode();
			IArcType at = graph.getArcType(NEXT_TOKEN_RELATION);
			if (currNode.getType().equals(graph.getNodeType(TOKEN_NODE_TYPE))) {
				addMockedClassification(currNode);
				while (currNode.getNumberOfOutgoingArcs() > 0 && currNode.getOutgoingArcsOfType(at) != null
						&& currNode.getOutgoingArcsOfType(at).size() > 0) {
					currNode = currNode.getOutgoingArcsOfType(at).get(0).getTargetNode();
					if (currNode.getType().equals(graph.getNodeType(TOKEN_NODE_TYPE))) {
						addMockedClassification(currNode);
					} else {
						logger.error("Unexpected node typ: {}", currNode.getType().getName());
					}
				}
			}
		}

	}

	private boolean checkMandatory() {

		if (!(graph instanceof ParseGraph) || !graph.hasArcType(NEXT_TOKEN_RELATION) || !graph.hasNodeType(TOKEN_NODE_TYPE)
				|| graph.getNodesOfType(graph.getNodeType(TOKEN_NODE_TYPE)).isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	private boolean checkExecBefore() {
		return graph.getNodeType(TOKEN_NODE_TYPE).containsAttribute(IS_TEACHING_SEQUENCE_ATTRIBUTE_NAME, STRING);
	}

	private void addMockedClassification(INode node) {
		node.setAttributeValue(IS_TEACHING_SEQUENCE_ATTRIBUTE_NAME, "false");
		node.setAttributeValue(IS_TEACHING_SEQUENCE_PROBABILITY_ATTRIBUTE_NAME, 1.0f);
		node.setAttributeValue(TEACHING_SEQUENCE_PART_ATTRIBUTE_VALUE, MulticlassLabel.DESC.toString());
	}

	private void createMockAttributes(INodeType nodeType) {
		nodeType.addAttributeToType(STRING, IS_TEACHING_SEQUENCE_ATTRIBUTE_NAME);
		nodeType.addAttributeToType(FLOAT_PRIMITIVE, IS_TEACHING_SEQUENCE_PROBABILITY_ATTRIBUTE_NAME);
		nodeType.addAttributeToType(STRING, TEACHING_SEQUENCE_PART_ATTRIBUTE_VALUE);
	}
}
