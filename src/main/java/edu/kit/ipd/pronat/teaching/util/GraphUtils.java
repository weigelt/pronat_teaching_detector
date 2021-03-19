package edu.kit.ipd.pronat.teaching.util;

import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sebastian Weigelt
 * @author Vanessa Steurer
 */
import java.util.*;

public final class GraphUtils {
	private static IGraph graph;

	private static final Logger logger = LoggerFactory.getLogger(GraphUtils.class);

	public GraphUtils(IGraph graph) {
		GraphUtils.graph = graph;
	}

	public static String getUtteranceString(List<INode> utteranceNodes) {
		StringJoiner utteranceString = new StringJoiner(" ");

		for (INode node : utteranceNodes) {
			String token = (String) node.getAttributeValue("value");
			utteranceString.add(token);
		}

		return utteranceString.toString();
	}

	public static List<INode> getNodesOfUtterance() throws MissingDataException {
		ArrayList<INode> result = new ArrayList<>();
		IArcType nextArcType;
		if ((nextArcType = graph.getArcType("relation")) != null) {
			if (graph instanceof ParseGraph) {
				ParseGraph pGraph = (ParseGraph) graph;
				INode current = pGraph.getFirstUtteranceNode();
				List<? extends IArc> outgoingNextArcs = current.getOutgoingArcsOfType(nextArcType);
				boolean hasNext = !outgoingNextArcs.isEmpty();
				result.add(current);
				while (hasNext) {
					//assume that only one NEXT arc exists
					if (outgoingNextArcs.size() == 1) {
						current = outgoingNextArcs.toArray(new IArc[outgoingNextArcs.size()])[0].getTargetNode();
						result.add(current);
						outgoingNextArcs = current.getOutgoingArcsOfType(nextArcType);
						hasNext = !outgoingNextArcs.isEmpty();
					} else {
						logger.error("Nodes have more than one NEXT Arc");
						throw new IllegalArgumentException("Nodes have more than one NEXT Arc");
					}
				}
			} else {
				logger.error("Graph is no ParseGraph!");
				throw new MissingDataException("Graph is no ParseGraph!");
			}
		} else {
			logger.error("Next Arctype does not exist!");
			throw new MissingDataException("Next Arctype does not exist!");
		}
		return result;
	}
}
