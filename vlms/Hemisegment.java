package vlms;
import ij.*;
import ij.io.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.filter.*;

import java.util.*;
import java.io.*;
import java.time.LocalDate;

import vlms.utilities.*;


public class Hemisegment { 
	public static int GEO = 159457; 		/** without display label 158433 **/
	public static int INTENS = 1934366 ; 	/** without display label 1933342 **/
	
	/** SUF = suffix **/
	public static String HYP_SUF = ".tif";
	public static String NUC_BIN_SUF =  "_Nuc-bin.tif";
	
	public static String VL3_CSV_SUF =  "_XY-VL3.csv";
	public static String VL4_CSV_SUF =  "_XY-VL4.csv";
	
	public Experiment exper;
	
	public File path;
	public String name;
	public ArrayList<File> fileList = new ArrayList<File>();
	
	public ResultsTable rt;
	public String[] geoHeadings;
	

	public ImagePlus hyp = null;
	public Calibration cal = null;
	public int sliceCount = -1;

	public ImagePlus nucBin = null;
	
	public File vl3Csv = null;
	public File vl4Csv = null;
	
	public Cell vl3 = null;
	public Cell vl4 = null;
	
	public ArrayList<Cell> cells = new ArrayList<Cell>(); // for the purpose of iterating, especially in the context of not knowing if both cells are there
	
	
	public Hemisegment(Experiment exper, File hsPath) {
		this.exper = exper;
		this.path = hsPath;
		name = path.getName();
		IJ.log("starting hemiseg:" + name);

		loadFiles();	// sets fileList, (if files are there) hyp -->(cal, sliceCount), 
						// nucBin, vl3Csv-->(vl3), vl4Csv-->(vl4)
		loadNucs();
	}
	
	public Hemisegment (Experiment exper, File hsPath, int a) {
		this.exper = exper;
		this.path = hsPath;
		name = path.getName();
		IJ.log("starting hemiseg:" + name);

		loadFiles();	// sets fileList, (if files are there) hyp -->(cal, sliceCount), 
						// nucBin, vl3Csv-->(vl3), vl4Csv-->(vl4)
		//loadNucs();
	}
	
	public void loadFiles() {
		File[] allFiles = path.listFiles();
		for(File f : allFiles) {
			//IJ.log("\t" + f.getName());
		}
			

		for (int i = 0; i < allFiles.length; i++) {			
			if (allFiles[i].getName().equals(buildFileName(HYP_SUF))) {
				hyp = IJ.openImage(allFiles[i].getPath());
				cal = hyp.getCalibration();
				sliceCount = hyp.getNSlices();
			}
			else if (allFiles[i].getName().equals(buildFileName(NUC_BIN_SUF))) {
				nucBin = IJ.openImage(allFiles[i].getPath());
				//IJ.log("got nucBin");
			}
			else if (allFiles[i].getName().equals(buildFileName(VL3_CSV_SUF))) {
				vl3Csv = allFiles[i];
				//IJ.log("\t\tvl3");

			}
			else if (allFiles[i].getName().equals(buildFileName(VL4_CSV_SUF))) {
				vl4Csv = allFiles[i];
				//IJ.log("\t\tvl4");
			}
			else {
				fileList.add(allFiles[i]);
			}
		}
		
		if (cal != null) {
			if (vl3Csv != null) {
				vl3 = new Cell(this, vl3Csv, 3, cal);
				cells.add(vl3);
			}
			if (vl4Csv != null) {
				vl4 = new Cell(this, vl4Csv, 4, cal);
				cells.add(vl4);
			}
		}
	}
	
	public void loadNucs() {
		rt = new ResultsTable();
		
		Overlay nucOverlay = Functions.particleAnalyze(nucBin,GEO,rt);
		
		geoHeadings = rt.getHeadings();
		
		for (int i = 0; i < nucOverlay.size(); i++) {
			Hashtable<String,MutableDouble> nucRow = Functions.getRtRow(rt,i);
			Roi nucRoi = nucOverlay.get(i);
			int nucRoiX = (int)nucRoi.getContourCentroid()[0];
			int nucRoiY = (int)nucRoi.getContourCentroid()[1];
			
			for (Cell c : cells) {
				if (c.roi.contains(nucRoiX,nucRoiY)) {
					c.nucs.add(new Nucleus(c, c.nucCount, nucRoi, nucRow));
					c.nucCount++;
					continue;
				}
			}	
		}	
	}

	public String buildFileName(String suffix) {
		return (name + suffix);
	}
	
	public File buildFile(String suffix) {
		return new File(path, name + suffix);
	}
	
	
	public String toString() {
		return ("vlms.Hemisegment: " + name);
	}
		
	public String toStringLong() {
		
		String has = "\nHas: ";
		String doesntHave = "\nDoesn't Have: ";
		
		if (exper == null) doesntHave += "exper, ";
		else has += "exper, ";
		
		if (path == null) doesntHave += "path, ";
		else has += "path, ";
		if (name == null) doesntHave += "name, ";
		else has += "name, ";
		if (fileList == null) doesntHave += "fileList, ";
		else has += "fileList, ";
		
		if (rt == null) doesntHave += "rt, ";
		else has += "rt, ";
		if (geoHeadings == null) doesntHave += "geoHeadings, ";
		else has += "geoHeadings, ";
		
		if (hyp == null) doesntHave += "hyp, ";
		else has += "hyp, ";
		if (cal == null) doesntHave += "cal, ";
		else has += "cal, ";
		if (sliceCount == -1) doesntHave += "sliceCount, ";
		else has += "sliceCount, ";
		
		if (nucBin == null) doesntHave += "nucBin, ";
		else has += "nucBin, ";
		
		if (vl3Csv == null) doesntHave += "vl3Csv, ";
		else has += "vl3Csv, ";
		if (vl4Csv == null) doesntHave += "vl4Csv, ";
		else has += "vl4Csv, ";
		
		if (vl3 == null) doesntHave += "vl3, ";
		else has += "vl3, ";
		if (vl4 == null) doesntHave += "vl4, ";
		else has += "vl4, ";
		
		has = has.substring(0, has.length() - 2);
		doesntHave = doesntHave.substring(0, doesntHave.length() - 2);
		
		return (this.toString() 
				+ has
				+ doesntHave);		
	}
	
	public String fullSummary() {
		String temp = this.toString();
		
		temp += ("\nexper: " + exper);

		temp += ("\npath: " + path);
		temp += ("\nname: " + name);
		temp += ("\nfileList: " + fileList);
		
		temp += ("\nrt: " + rt);
		temp += ("\ngeoHeadings: " + geoHeadings);
		
		temp += ("\nhyp: " + hyp);
		temp += ("\ncal: " + cal);
		temp += ("\nsliceCount: " + sliceCount);
		
		temp += ("\nnucBin: " + nucBin);
		
		temp += ("\nvl3Csv: " + vl3Csv);
		temp += ("\nvl4Csv: " + vl4Csv);
		
		temp += ("\nvl3: " + vl3);
		temp += ("\nvl4: " + vl4);		
		
		return temp;
	}
}



