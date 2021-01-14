package edu.kit.ipd.parse.teaching;

import edu.kit.ipd.parse.graphBuilder.GraphBuilder;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.parse.luna.tools.StringToHypothesis;
import edu.kit.ipd.parse.ner.NERTagger;
import edu.kit.ipd.parse.shallownlp.ShallowNLP;
import edu.kit.ipd.parse.srlabeler.SRLabeler;
import org.junit.Test;

public class IntegrationTest extends AbstractTeachingDetectorTest {

	ShallowNLP snlp;
	NERTagger ner;
	SRLabeler srl;
	GraphBuilder gb;
	TeachingDetector td;

	@Test
	public void basicTest() {
		String testString = "go to the table";
		PrePipelineData ppd = new PrePipelineData();
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(testString, true));

		snlp = new ShallowNLP();
		snlp.init();
		try {
			snlp.exec(ppd);
		} catch (PipelineStageException e) {
			e.printStackTrace();
		}

		ner = new NERTagger();
		ner.init();
		try {
			ner.exec(ppd);
		} catch (PipelineStageException e) {
			e.printStackTrace();
		}

		srl = new SRLabeler();
		srl.init();
		try {
			srl.exec(ppd);
		} catch (PipelineStageException e) {
			e.printStackTrace();
		}

		gb = new GraphBuilder();
		gb.init();
		try {
			gb.exec(ppd);
		} catch (PipelineStageException e) {
			e.printStackTrace();
		}

		td = new TeachingDetector();
		td.init();
		try {
			td.setGraph(ppd.getGraph());
		} catch (MissingDataException e) {
			e.printStackTrace();
		}
		td.exec();
		checkForAttributes(td.getGraph());
	}
}