package vlms;
import ij.*;
import ij.io.*;

import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.charset.Charset;

import vlms.utilities.*;


public class Experiment {
	public static ArrayList<Experiment> insts = new ArrayList<Experiment>();
	public static boolean makeLogs = true;
	
	public File path;
	
	public String name;
	public LocalDate date;
	public String genotype;
	
	public ExperimentView experView;
	
	public Hashtable<String, Integer> channels;
	
	public ArrayList<File> hemisegFileList;
	public ArrayList<Hemisegment> hemisegs;
	
	
	
	public ArrayList<Cell> cells;
	public ArrayList<Nucleus> nucs;
	
	//public static Hashtable<String, String[]> cellheading2method  = new Hashtable<String, String[]>();
	//public static Hashtable<String, String[]> nucheading2method  = new Hashtable<String, String[]>();
	
	public static Hashtable<String, Hashtable<String, String>> heading2method  = initHeading2method();
	
	public static Hashtable<String, Hashtable<String, String>> initHeading2method() {
		Hashtable<String, Hashtable<String, String>> temp = new Hashtable<String, Hashtable<String, String>>();
		temp.put("nuc", new Hashtable<String, String>());
		temp.put("cell", new Hashtable<String, String>());
		return temp;
	}
	
	
	public static Experiment experConstructEverything(File path, String outFileSuf, String[] headings) {
		Experiment exper = new Experiment(path);
		exper.runEverything();
		exper.exportNucData(outFileSuf, headings);
		return exper;
	}
	
	
	public static Experiment experConstructEverything(File path, String nucFileSuf, String[] nucHeadings, String cellFileSuf, String[] cellHeadings) {
		Experiment exper = new Experiment(path);
		exper.runEverything();
		exper.exportNucData(nucFileSuf, nucHeadings);
		exper.forEachCell();
		exper.exportCellData(cellFileSuf, cellHeadings);
		return exper;
	}
	

	
	public Experiment(File path) {
		insts.add(this);
		this.path = path;
		//experView = new ExperimentView(this);
		try {
			parseName(); // sets name, data, genotype
		}
		catch (Exception e) {}
		loadChannels(); // sets channels 

		createHemisegs(); // sets hemisegs
		
		createIterables();
	}
	
	public Experiment(String stringPath) {
		this(new File(stringPath));
	}
	
	//public static String myDumbTestVar = "butts";
	
	public void createIterables() {
		cells = new ArrayList<Cell>();
		nucs = new ArrayList<Nucleus>();
		for (Hemisegment hemiseg : hemisegs) {
			for (Cell c : hemiseg.cells) {
				cells.add(c);
				for (Nucleus nuc : c.nucs) {
					nucs.add(nuc);
				}
			}
			
		}
	}	
	
	
	public void close() {
		insts.remove(insts.indexOf(this));
		for (Nucleus nuc : nucs) {
			nuc.close();
			nuc = null;
		}
		for (Cell c : cells) {;
			c.close();
			c = null;
		}
		for (Hemisegment hemiseg : hemisegs) {
			hemiseg.close();
			hemiseg = null;
		}
	}
	
	public void parseName() {
		name = path.getName();
		String[] nameList = name.split("_");
		genotype = nameList[1];

		try {
			String tempDate = nameList[0];
			if (tempDate.length() != 6){
				/*** exception ***/
				IJ.log("date of exper folder name is not 6 chars");
			}
			else {
				int year = Integer.parseInt(tempDate.substring(0,2));
				int month = Integer.parseInt(tempDate.substring(2,4));
				int day = Integer.parseInt(tempDate.substring(4,6));
				date = LocalDate.of(2000+year, month, day);
			}
		}
		catch (Exception e) {	
		}
	}
	
	

	
	public void loadChannels() {
		File metadata = new File(path, name+"_metadata.py");
		List<String> chanStrL = new ArrayList<String>();
		try {
			chanStrL = Files.readAllLines(metadata.toPath(), Charset.defaultCharset());
		}
		catch (Exception e) {
			IJ.log("couldn't open metadata.py");
		}
		channels = new Hashtable<String, Integer>();
		boolean go = false; 
		for(int i = 0; i < chanStrL.size(); i++) {
			if (go) {
				if (chanStrL.get(i).contains("}")) {
					go = false;
				}
				else {
					String[] tempL = chanStrL.get(i).split(":");
					int v = Integer.parseInt(tempL[0]);
					String k = tempL[1];
					k = k.replaceAll("\"","\t");
					k = k.replaceAll(",","\t");
					k = k.trim();
					channels.put(k,v);
				}
			}			
			else if (chanStrL.get(i).contains("channels {")) {
				go = true;
			}
		}
	}
	
	public void createHemisegs() {
		hemisegFileList = new ArrayList<File>();
		hemisegs = new ArrayList<Hemisegment>();
		File[] subDirs = path.listFiles();
		
		for(int i = 0; i < subDirs.length; i++) {
			if (subDirs[i].getName().startsWith(name) && subDirs[i].isDirectory()) {
				hemisegFileList.add(subDirs[i]);
				hemisegs.add(new Hemisegment(this, subDirs[i]));
			}	
		}
	}
	
	
	
	public boolean exportCellData(String fileSuf, String[] headings) {
		if (makeLogs) exportDataLog(fileSuf, "cell", headings);

		File outCsv = new File(path, name + "_" + fileSuf + ".csv");
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(outCsv));
			String labels = "Hemisegment,Cell,";
			
			for (int i = 0; i < headings.length; i++) {
				labels += (headings[i] + ",");
			}
			
			writer.write(labels+"\n");
			
			for (Cell c : cells) {
				String temp = c.hemiseg.name + ",vl"+c.vlNum + ",";
				for (int i = 0; i < headings.length; i++) {
					String heading2 = headingRename(headings[i]);
					if (c.data.containsKey(heading2)) {
						MutableDouble val = c.data.get(heading2);
						if (val != null) {
							temp += (c.data.get(heading2));
						}
					}
					temp += ",";
				}
				writer.write(temp + "\n");
					
			}
			writer.close();
			return true;
		}
		catch (FileNotFoundException e) {
			/** deal with exception appropriately **/
			IJ.log("FileNotFoundException in Experiment.exportCellData");
			return false;
		}
		catch (IOException e) {
			/** deal with exception appropriately **/
			IJ.log("IOException in Experiment.exportCellData");
			return false;
		}

	}
		
	public boolean exportNucData(String fileSuf, String[] headings) {
		if (makeLogs) exportDataLog(fileSuf, "nuc", headings);
		
		File outCsv = new File(path, name + "_" + fileSuf + ".csv");
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(outCsv));
			String labels = "Hemisegment,Cell,NucID,";
			
			for (int i = 0; i < headings.length; i++) {
				labels += (headings[i] + ",");
			}
			writer.write(labels+"\n");
			
			for (Cell c : cells) {
				for (Nucleus nuc : c.nucs) {
					String temp = c.hemiseg.name + ",vl"+c.vlNum + "," + nuc.id + ",";
					for (int i = 0; i < headings.length; i++) {
						String heading2 = headingRename(headings[i]);
						if (nuc.data.containsKey(heading2)) {
							MutableDouble val = nuc.data.get(heading2);
							if (val != null) {
								temp += (nuc.data.get(heading2));
							}
						}
						temp += ",";
					}
					writer.write(temp + "\n");
				}	
				writer.newLine();
			}
			
			writer.close();
			return true;
		}
		catch (FileNotFoundException e) {
			/** deal with exception appropriately **/
			IJ.log("FileNotFoundException in Experiment.exportNucData");
			return false;
		}
		catch (IOException e) {
			/** deal with exception appropriately **/
			IJ.log("IOException in Experiment.exportNucData");
			return false;
		}
	}
	
	// nucOrCell = "nuc" or "cell" - add exception
	public boolean exportDataLog(String fileSuf, String nucOrCell, String[] headings) {
		exportMethodDoc();
		
		File logPath = new File(path, name + "_" + fileSuf + ".log");
		BufferedWriter writer = null;
		
		Hashtable<String, ArrayList<String>> methodHeadingDict = new Hashtable<String, ArrayList<String>>();
		
		try {
			writer = new BufferedWriter(new FileWriter(logPath, true));
			
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			writer.write(dtf.format(now) + System.lineSeparator());
			writer.write("Git Commit Revision Num: " + GitV.gitRevNum + System.lineSeparator());
			writer.write("Git Commit Msg: " + GitV.gitMsg + System.lineSeparator());
			
			
			Hashtable<String, String> h2m = Experiment.heading2method.get(nucOrCell);
			for (String h : headings) {
				if (h2m.containsKey(h)) {
					if (methodHeadingDict.containsKey(h2m.get(h))) {
						methodHeadingDict.get(h2m.get(h)).add(h);
					}
					else {
						//String[] temp = {h};
						methodHeadingDict.put(h2m.get(h), new ArrayList<String>());
						methodHeadingDict.get(h2m.get(h)).add(h);
					}
				}
			}
			
			//IJ.log(String.valueOf(methodHeadingDict.size()));
			if (methodHeadingDict.size() > 0) {
				writer.write("Col Headings - Java Methods:" + System.lineSeparator());
				for (String m : methodHeadingDict.keySet()) {
					writer.write("\t" + String.join(", ", methodHeadingDict.get(m)));
					writer.write(" - " + m + System.lineSeparator());
				}
			}
				
			writer.newLine();	
			
			
			
			writer.close();
			return true;
		}
		catch (FileNotFoundException e) {
			/** deal with exception appropriately **/
			IJ.log("FileNotFoundException in Experiment.exportDataLog");
			return false;
		}
		catch (IOException e) {
			/** deal with exception appropriately **/
			IJ.log("IOException in Experiment.exportDataLog");
			return false;
		}
	}
	
	public boolean exportMethodDoc() {
		
		File methodDocPath = new File(path, name + "_" + "method-doc" + ".log");
		BufferedWriter writer = null;
		
		//Hashtable<String, ArrayList<String>> methodHeadingDict = new Hashtable<String, ArrayList<String>>();
		
		try {
			writer = new BufferedWriter(new FileWriter(methodDocPath, true));
			
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			writer.write(dtf.format(now) + System.lineSeparator());
			writer.write("Git Commit Revision Num: " + GitV.gitRevNum + System.lineSeparator());
			writer.write("Git Commit Msg: " + GitV.gitMsg + System.lineSeparator());
			
			writer.write("Cell:\n");
			
			
			// for (int i = 0; i < Cell.methodDocs.size(); i++) {
				// methodCalcDoc poo = Cell.methodDocs.get(i);
				// poo.tears();
			// }
			
			for (methodCalcDoc mcd : Cell.methodDocs) {
				
				writer.write(mcd.butts("\t"));
				writer.newLine();	

			}
			for (methodCalcDoc mcd : Nucleus.methodDocs) {
				//String temp = "\t";

				writer.write(mcd.butts("\t"));
				writer.newLine();	

			}
		
			
			
			
			writer.close();
			return true;
		}
		catch (FileNotFoundException e) {
			/** deal with exception appropriately **/
			IJ.log("FileNotFoundException in Experiment.exportDataLog");
			return false;
		}
		catch (IOException e) {
			/** deal with exception appropriately **/
			IJ.log("IOException in Experiment.exportDataLog");
			return false;
		}
	}
	
	
	public static String headingRename(String heading) {
		Hashtable<String,String> headingDict = new Hashtable<String,String>();
		headingDict.put("Thickness(minFeret)", "orthRoi - MinFeret");
		headingDict.put("Thickness(Height)", "orthRoi - Height");
		headingDict.put("Cross-sectional Area", "orthRoi - Area");
		// headingDict.put("orthRoi - minFeret", "Thickness(minFeret)");
		// headingDict.put("orthRoi - minFeret", "Thickness(minFeret)");
		// headingDict.put("orthRoi - minFeret", "Thickness(minFeret)");
		// headingDict.put("orthRoi - minFeret", "Thickness(minFeret)");
		
		if (headingDict.containsKey(heading)) return headingDict.get(heading);
		else return heading;
	}
	
	
	public void testOneNuc() {
		nucs.get(0).makeNucImps();
		nucs.get(0).countOrthPixels();
		nucs.get(0).yScaled();
		//nucs.get(0).sumSlicesOrthStack();
		//nucs.get(0).sumSlicesStack();
		nucs.get(0).allSliceSums();
	}
	
	
	public void testOneCell() {
		//cells.get(0).thickness();
		cells.get(0).volume2();
		
	}
	
	public void forEachCell() {
		makeNucImps();
		allSliceSums();
		
		for (Cell c : cells) {
			c.thickness();
			c.volume2();
			c.makeTotalAV();	
		}	
	}
		
	public void runEverything() {
		makeNucImps();
		countNucOrthPixels();
		nucYScaled();
		allSliceSums();
	}
	
	public void makeNucImps() {
		IJ.log("making nuc cross-sections...");
		for (Nucleus nuc : nucs) {
			nuc.makeNucImps();
		}
	}
	
	public void countNucOrthPixels() {
		IJ.log("counting pixels in nuc volume...");
		for (Nucleus nuc : nucs) {
			nuc.countOrthPixels();
		}
	}
	
	public void nucYScaled() {
		IJ.log("scaling nuc y coordinates to cell");
		for (Nucleus nuc : nucs) {
			nuc.yScaled();
		}
	}
	
	public void allSliceSums() {
		IJ.log("scaling nuc y coordinates to cell");
		for (Nucleus nuc : nucs) {
			nuc.allSliceSums();
		}
	}
	
	
	public String toString() {
		return ("vlms.Experiment: " + name);
	}
	
	public String toStringLong() {
		String has = "\nHas: ";
		String doesntHave = "\nDoesn't Have: ";
		
		if (path == null) doesntHave += "path, ";
		else has += "path, ";
		
		if (name == null) doesntHave += "name, ";
		else has += "name, ";
		if (date == null) doesntHave += "date, ";
		else has += "date, ";
		if (genotype == null) doesntHave += "genotype, ";
		else has += "genotype, ";
		
		if (experView == null) doesntHave += "experView, ";
		else has += "experView, ";
		
		if (channels == null) doesntHave += "channels, ";
		else has += "channels, ";
		
		if (hemisegFileList == null) doesntHave += "hemisegFileList, ";
		else has += "hemisegFileList, ";
		if (hemisegs == null) doesntHave += "hemisegs, ";
		else has += "hemisegs, ";
		
		has = has.substring(0, has.length() - 2);
		doesntHave = doesntHave.substring(0, doesntHave.length() - 2);
		
		return (this.toString()
				+ has
				+ doesntHave);			
	}
	
	public String fullSummary() {
		String temp = this.toString();
		
		temp += ("\npath: " + path);
		
		temp += ("\nname: " + name);
		temp += ("\ndate: " + date);
		temp += ("\ngenotype: " + genotype);
		
		temp += ("\nexperView: " + experView);
		
		temp += ("\nchannels: " + channels);
		
		temp += ("\nhemisegFileList: " + hemisegFileList);
		temp += ("\nhemisegs: " + hemisegs);
		
		return temp;
	}
}