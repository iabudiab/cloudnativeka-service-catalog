package de.iabudiab.cloudnativeka.showcase.reko;

import software.amazon.awssdk.services.rekognition.model.Label;

public class DetectedLabel {

	private String name;
	private float confidence;

	public DetectedLabel(Label label) {
		this.name = label.name();
		this.confidence = label.confidence().floatValue();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getConfidence() {
		return confidence;
	}

	public void setConfidence(float confidence) {
		this.confidence = confidence;
	}

}
