package vlms.utilities;

public class MutableDouble {
	private double value;

	public MutableDouble() {
		
	}
	
	public MutableDouble(double value) {
		this.value = value;
	}
	
	public double get() {
		return value;
	}
	
	public void set(double i) {
		value = i;
	}

	public String toString() {
		return (String.valueOf(value));
	}
}