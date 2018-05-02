package vlms;



public class methodCalcDoc {
	public String methodName = "";
	public String methodDescrip = "";
	
	public String[] headings = {""};
	//public String heading = "";
	
	public methodCalcDoc(String methodName, String methodDescrip, String[] headings) {
	//public methodCalcDoc(String methodName, String methodDescrip, String heading) {
		this.methodName = methodName;
		this.methodDescrip = methodDescrip;
		this.headings = headings;
		//this.heading = heading;

	}
	
	public String tears() {
		return(":(");
	}
	
	public String butts(String tabs) {
		// String tabs = "";
		// for (int i = 0; i < tabCount; i++) {
			// tabs += "\t";
		// }
		String outStr = tabs + methodName + "\n";
		
		String[] lines = methodDescrip.split("\n");
		outStr += "\t" + tabs + "Description: \n"; 
		for (String line : lines) {
			outStr += "\t\t" + tabs + line + "\n";
		}
		
		String temp = "Headings: ";
		outStr += "\t" + tabs + "Headings: " + String.join(", ", headings) + "\n";
		return outStr;
		
	}
	public String toString() {
		String outStr = "methodName = " + methodName + "\n";
		outStr += "methodDescrip = " + methodDescrip + "\n";
		outStr += "headings = " + headings + "\n";
		return outStr;
	}
	


}



