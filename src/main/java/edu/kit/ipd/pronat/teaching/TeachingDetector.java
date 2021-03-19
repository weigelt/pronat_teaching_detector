package edu.kit.ipd.pronat.teaching;

import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.ParseGraph;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import edu.kit.ipd.pronat.teaching.classifier.BinaryNeuralClassifier;
import edu.kit.ipd.pronat.teaching.classifier.MulticlassLabel;
import edu.kit.ipd.pronat.teaching.classifier.MulticlassNeuralClassifier;
import edu.kit.ipd.pronat.teaching.util.GraphUtils;
import org.kohsuke.MetaInfServices;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Sebastian Weigelt
 * @author Vanessa Steurer
 */
@MetaInfServices(AbstractAgent.class)
public class TeachingDetector extends AbstractAgent {

	private static final String ID = "TeachingDetector";
	private static final Logger logger = LoggerFactory.getLogger(TeachingDetector.class);
	public static final String NEXT_TOKEN_RELATION = "relation";
	public static final String TOKEN_NODE_TYPE = "token";
	public static final String SRL_NODE_TYPE = "srl";
	public static final String STRING = "String";
	public static final String FLOAT_PRIMITIVE = "float";
	static final String IS_TEACHING_SEQUENCE = "isTeachingSequence";
	static final String IS_TEACHING_SEQUENCE_PROB = "isTeachingSequenceProbability";
	static final String TEACHING_SEQUENCE_PART = "teachingSequencePart";

	public static final String FIRST_LEVEL_BASIC_OPTIMIZATION_PROP = "FIRST_LEVEL_BASIC_OPTIMIZATION";
	public static final String FIRST_LEVEL_BASIC_OPTIMIZATION_THRESHOLD_PROP = "FIRST_LEVEL_BASIC_OPTIMIZATION_THRESHOLD";
	public static final String FIRST_LEVEL_ADVANCED_OPTIMIZATION_PROP = "FIRST_LEVEL_ADVANCED_OPTIMIZATION";
	public static final String FIRST_LEVEL_ADVANCED_OPTIMIZATION_CLASSIFICATION_THRESHOLD_PROP = "FIRST_LEVEL_ADVANCED_OPTIMIZATION_CLASSIFICATION_THRESHOLD";
	public static final String FIRST_LEVEL_ADVANCED_OPTIMIZATION_DECL_LABEL_THRESHOLD_PROP = "FIRST_LEVEL_ADVANCED_OPTIMIZATION_DECL_LABEL_THRESHOLD";
	private static final Properties props = ConfigManager.getConfiguration(TeachingDetector.class);

	private List<INode> utteranceNodes;
	private BinaryNeuralClassifier binClf;
	private MulticlassNeuralClassifier mclassClf;

	public TeachingDetector() {
		setId(ID);
	}

	@Override
	public void init() {
		binClf = new BinaryNeuralClassifier(props);
		mclassClf = new MulticlassNeuralClassifier(props);
	}

	@Override
	protected void exec() {

		if (!checkMandatory()) {
			logger.info("Mandatory pre-condition not met, exiting!");
		} else if (checkExecBefore()) {
			logger.info("Was executed before, exiting!");
		} else {

			new GraphUtils(graph);

			try {
				utteranceNodes = GraphUtils.getNodesOfUtterance();
			} catch (MissingDataException e) {
				logger.error("No valid ParseGraph. Abort Agent execution.", e);
				return;
			}
			// read utterance
			String utterance = GraphUtils.getUtteranceString(utteranceNodes);
			// TODO: pre-processing? Lemmatization? Exclude stop-words?
			logger.debug("input sentence: {}", utterance);
			// do binary prediction (first level)
			float[] binaryPrediction = getBinaryClfIsTeachingSequenceResult(utterance);
			boolean isTeachingSequence = binClf.isTeachingSequence(binaryPrediction, props);

			// do multiclass prediction (second level), iff we the utterance is a teaching sequence 
			// or we are supposed to use the advanced classification optimization
			// otherwise set all mclassLabels to DESC
			List<MulticlassLabel> mclassLabels;
			boolean doAdvancedFirstLevelOptimization = Boolean
					.parseBoolean(props.getProperty(FIRST_LEVEL_ADVANCED_OPTIMIZATION_PROP, "true"));
			if (isTeachingSequence || doAdvancedFirstLevelOptimization) {
				mclassLabels = getMclassClfTeachingSequencePartsResult(utterance);
			} else {
				mclassLabels = setMclassLabelsToDESC(utterance);
			}

			// advanced first level classification optimization
			if (!isTeachingSequence && doAdvancedFirstLevelOptimization) {
				float advancedClsThreshold = Float
						.parseFloat(props.getProperty(FIRST_LEVEL_ADVANCED_OPTIMIZATION_CLASSIFICATION_THRESHOLD_PROP, "0.008"));
				long advancedDECLLabelThreshold = Long
						.parseLong(props.getProperty(FIRST_LEVEL_ADVANCED_OPTIMIZATION_DECL_LABEL_THRESHOLD_PROP, "2"));
				long DECLcount = mclassLabels.stream().filter(l -> l.equals(MulticlassLabel.DECL)).count();
				if (binaryPrediction[0] > advancedClsThreshold && DECLcount >= advancedDECLLabelThreshold) {
					isTeachingSequence = true;
				}
			}
			// Save results in the graph
			saveToGraph(isTeachingSequence, binaryPrediction, mclassLabels);
		}

	}

	private List<MulticlassLabel> setMclassLabelsToDESC(String utterance) {
		return Arrays.stream(utterance.split(" ")).map(e -> MulticlassLabel.DESC).collect(Collectors.toList());
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
		return graph.getNodeType(TOKEN_NODE_TYPE).containsAttribute(IS_TEACHING_SEQUENCE, STRING);
	}

	/**
	 * Applies and returns the binary classification for given String (utterance)
	 * 
	 * @param utterance
	 *            the utterance to classify
	 * @return the classification result
	 */
	float[] getBinaryClfIsTeachingSequenceResult(String utterance) {
		INDArray binResult = binClf.getSinglePrediction(utterance, binClf.getModel());
		float[] predictedClasses = binClf.getPredictedClasses(binResult, 0);

		logger.info("Found teaching sequence prediction: {}.", predictedClasses);
		return predictedClasses;
	}

	/**
	 * Returns the instance of the BinaryNeuralClassifier in use.
	 * 
	 * @return the BinaryNeuralClassifier in use
	 */
	BinaryNeuralClassifier getBinClf() {
		return binClf;
	}

	List<MulticlassLabel> getMclassClfTeachingSequencePartsResult(String utterance) {
		List<MulticlassLabel> mclassLabels;

		// load model from dl4j
		INDArray mclassPrediction = mclassClf.getSinglePrediction(utterance, mclassClf.getModel());
		mclassLabels = mclassClf.getInterpretedPredictedLabels(mclassPrediction, utteranceNodes.size());

		return mclassLabels;
	}

	private void saveToGraph(boolean binaryIsTeachingSequence, float[] isTeachingSequenceProbability, List<MulticlassLabel> mclassLabels) {
		if (!graph.getNodeType(TOKEN_NODE_TYPE).containsAttribute(IS_TEACHING_SEQUENCE, STRING)) {
			graph.getNodeType(TOKEN_NODE_TYPE).addAttributeToType(STRING, IS_TEACHING_SEQUENCE);
		}

		if (!graph.getNodeType(TOKEN_NODE_TYPE).containsAttribute(IS_TEACHING_SEQUENCE_PROB, FLOAT_PRIMITIVE)) {
			graph.getNodeType(TOKEN_NODE_TYPE).addAttributeToType(FLOAT_PRIMITIVE, IS_TEACHING_SEQUENCE_PROB);
		}

		if (!graph.getNodeType(TOKEN_NODE_TYPE).containsAttribute(TEACHING_SEQUENCE_PART, STRING)) {
			graph.getNodeType(TOKEN_NODE_TYPE).addAttributeToType(STRING, TEACHING_SEQUENCE_PART);
		}

		for (int i = 0; i < utteranceNodes.size(); i++) {
			INode currentNode = utteranceNodes.get(i);
			currentNode.setAttributeValue(IS_TEACHING_SEQUENCE, String.valueOf(binaryIsTeachingSequence));
			// first index of float-Array equals probability for binary-TeachingSequence-class
			currentNode.setAttributeValue(IS_TEACHING_SEQUENCE_PROB, isTeachingSequenceProbability[0]);
			currentNode.setAttributeValue(TEACHING_SEQUENCE_PART, mclassLabels.get(i).toString());
		}
	}

	/**
	 * FOR TESTING ONLY: Sets the utterance nodes
	 * 
	 * @param utteranceNodes
	 *            The list of utterance nodes to set
	 */
	void setUtteranceNodes(List<INode> utteranceNodes) {
		this.utteranceNodes = utteranceNodes;
	}
}
