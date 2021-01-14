package edu.kit.ipd.parse.teaching;

import edu.kit.ipd.parse.luna.graph.*;
import org.junit.Assert;
import org.junit.Test;

public class TeachingDetectorTest extends AbstractTeachingDetectorTest {

	@Test
	public void basicTest() {
		IGraph graph = new ParseGraph();
		INodeType nodeType = graph.createNodeType(TeachingDetector.TOKEN_NODE_TYPE);
		IArcType arcType = graph.createArcType(TeachingDetector.NEXT_TOKEN_RELATION);
		nodeType.addAttributeToType("int", "position");
		INode tok1 = graph.createNode(nodeType);
		INode tok2 = graph.createNode(nodeType);
		INode tok3 = graph.createNode(nodeType);
		tok1.setAttributeValue("position", 0);
		tok2.setAttributeValue("position", 1);
		tok3.setAttributeValue("position", 2);
		graph.createArc(tok1, tok2, arcType);
		graph.createArc(tok2, tok3, arcType);

		TeachingDetector teachingDetector = new TeachingDetector();
		teachingDetector.init();
		teachingDetector.setGraph(graph);
		teachingDetector.exec();

		checkForAttributes(graph);
	}

}
