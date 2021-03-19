package edu.kit.ipd.pronat.teaching.classifier;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sebastian Weigelt
 * @author Vanessa Steurer
 */
public enum MulticlassLabel {
	DECL(1), DESC(2), ELSE(3);

	private final int id;
	public static final Map<Integer, MulticlassLabel> LabelLookup = new HashMap<>();

	static {
		for (MulticlassLabel l : MulticlassLabel.values()) {
			LabelLookup.put(l.getId(), l);
		}
	}

	MulticlassLabel(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static Map<Integer, MulticlassLabel> getLabelLookup() {
		return LabelLookup;
	}
}
