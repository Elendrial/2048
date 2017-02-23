package me.hii488.other;

public class Data {
	
	public float[] input;

	public float[] expectedOutputs;
	public float[] trainingOutputs;
	
	public Data(float[] input, float[] expectedOutputs, float[] trainingOutputs) {
		this.input = input;
		this.expectedOutputs = expectedOutputs;
		this.trainingOutputs = trainingOutputs;
	}
	
}
