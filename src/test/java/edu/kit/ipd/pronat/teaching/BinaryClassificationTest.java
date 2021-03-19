package edu.kit.ipd.pronat.teaching;

import edu.kit.ipd.parse.luna.tools.ConfigManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

public class BinaryClassificationTest {

	private TeachingDetector td;
	private static final Properties props = ConfigManager.getConfiguration(TeachingDetector.class);

	@BeforeClass
	public static void classSetUp() {
		props.setProperty(TeachingDetector.FIRST_LEVEL_BASIC_OPTIMIZATION_PROP, "false");
		props.setProperty(TeachingDetector.FIRST_LEVEL_ADVANCED_OPTIMIZATION_PROP, "false");
	}

	@Before
	public void setUp() {
		td = new TeachingDetector();
		td.init();
	}

	@Test
	public void testTeachingTrue() {
		String input = "hi armar starting the dishwasher means you have to go to the dishwasher machine and press the blue button two times that is how you start the dishwasher";
		float[] binaryClfIsTeachingSequenceResult = td.getBinaryClfIsTeachingSequenceResult(input);
		Assert.assertEquals(1, binaryClfIsTeachingSequenceResult.length);
		boolean teachingSequence = td.getBinClf().isTeachingSequence(binaryClfIsTeachingSequenceResult, props);
		Assert.assertTrue(teachingSequence);
	}

	@Test
	public void testTeachingFalse() {
		String input = "go to the dishwasher machine and press the blue button two times";
		float[] binaryClfIsTeachingSequenceResult = td.getBinaryClfIsTeachingSequenceResult(input);
		Assert.assertEquals(1, binaryClfIsTeachingSequenceResult.length);
		boolean teachingSequence = td.getBinClf().isTeachingSequence(binaryClfIsTeachingSequenceResult, props);
		Assert.assertFalse(teachingSequence);
	}
}
