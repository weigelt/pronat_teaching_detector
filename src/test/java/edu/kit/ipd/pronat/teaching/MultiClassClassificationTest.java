package edu.kit.ipd.pronat.teaching;

import edu.kit.ipd.parse.luna.graph.*;
import edu.kit.ipd.pronat.teaching.classifier.MulticlassLabel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MultiClassClassificationTest {
	private TeachingDetector td;
	private IGraph graph;

	@Before
	public void setUp() {
		td = new TeachingDetector();
		graph = new ParseGraph();
		td.init();
	}

	@Test
	public void testTeachingTrueAllLabels() {
		String input = "hi armar starting the dishwasher means you have to go to the dishwasher if it is open close it then press the blue button two times";
		String[] expected = new String[] { "ELSE", "ELSE", "DECL", "DECL", "DECL", "DECL", "DECL", "DECL", "DECL", "DESC", "DESC", "DESC",
				"DESC", "DESC", "DESC", "DESC", "DESC", "DESC", "DESC", "DESC", "DESC", "DESC", "DESC", "DESC", "DESC", "DESC" };
		td.setUtteranceNodes(createUtteranceNodesMock(input));
		List<MulticlassLabel> mclassClfTeachingSequencePartsResult = td.getMclassClfTeachingSequencePartsResult(input);
		Assert.assertEquals(expected.length, mclassClfTeachingSequencePartsResult.size());
		for (int i = 0; i < mclassClfTeachingSequencePartsResult.size(); i++) {
			Assert.assertEquals(expected[i], mclassClfTeachingSequencePartsResult.get(i).name());
		}
	}

	@Test
	public void testTeachingFalseAllDESC() {
		String input = "go to the dishwasher if it is open close it then press the blue button two times";
		String[] expected = new String[] { "DESC", "DESC", "DESC", "DESC", "DESC", "DESC", "DESC", "DESC", "DESC", "DESC", "DESC", "DESC",
				"DESC", "DESC", "DESC", "DESC", "DESC" };
		td.setUtteranceNodes(createUtteranceNodesMock(input));
		List<MulticlassLabel> mclassClfTeachingSequencePartsResult = td.getMclassClfTeachingSequencePartsResult(input);
		Assert.assertEquals(expected.length, mclassClfTeachingSequencePartsResult.size());
		for (int i = 0; i < mclassClfTeachingSequencePartsResult.size(); i++) {
			Assert.assertEquals(expected[i], mclassClfTeachingSequencePartsResult.get(i).name());
		}
	}

	private List<INode> createUtteranceNodesMock(String utteranceAsString) {
		INodeType nodeType = graph.createNodeType("mock");
		return Arrays.stream(utteranceAsString.split(" ")).map(e -> graph.createNode(nodeType)).collect(Collectors.toList());
	}

}
