package edu.kit.ipd.parse.teaching;

import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import org.junit.Assert;

public abstract class AbstractTeachingDetectorTest {

	protected void checkForAttributes(IGraph graph) {
		for (INode node : graph.getNodesOfType(graph.getNodeType(TeachingDetector.TOKEN_NODE_TYPE))) {
			Assert.assertEquals("false", (String) node.getAttributeValue(TeachingDetector.IS_TEACHING_SEQUENCE_ATTRIBUTE_NAME));
			Assert.assertEquals(1.0f, (float) node.getAttributeValue(TeachingDetector.IS_TEACHING_SEQUENCE_PROBABILITY_ATTRIBUTE_NAME),
					0.01f);
			Assert.assertEquals(MulticlassLabel.DESC.toString(),
					(String) node.getAttributeValue(TeachingDetector.TEACHING_SEQUENCE_PART_ATTRIBUTE_VALUE));
		}
	}
}
