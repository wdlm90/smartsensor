package fcl.harvestmoon;

public class SmartConfig {
	
	private float samplingRate ;

	/*Default constructor*/
	public SmartConfig() {
		super();
		this.samplingRate = 1;
	}

	/*Parameterised constructors*/
	public SmartConfig(float samplingRate) {
		super();
		this.samplingRate = samplingRate;
	}

	/*Getters and setters*/
	public float getSamplingRate() {
		return samplingRate;
	}

	public void setSamplingRate(float samplingRate) {
		this.samplingRate = samplingRate;
	}
	
}
