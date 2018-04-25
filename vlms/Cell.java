package vlms;
import ij.*;
import ij.io.*;
import ij.gui.*;
import ij.measure.*;
import ij.process.*;
import ij.plugin.*;
import ij.plugin.filter.*;

import fiji.threshold.*;


import java.util.*;
import java.io.*;
import java.time.LocalDate;
import java.awt.*;

import vlms.utilities.*;

public class Cell {
	public Hemisegment hemiseg;
	public int vlNum = -1;
	
	public File roiPath;
	public Roi roi;
	
	public ArrayList<Double> roiX = new ArrayList<Double>(0);
	public ArrayList<Double> roiY = new ArrayList<Double>(0);
 
	public Hashtable<String,MutableDouble> data = new Hashtable<String,MutableDouble>();
	public Hashtable<String,double[]> data2 = new Hashtable<String,double[]>();

	public ArrayList<Nucleus> nucs = new ArrayList<Nucleus>();
	public int nucCount = 0;
	
	public ImagePlus cellHyp = null;
	public ImagePlus cellOrthStack = null;
	public ImagePlus cellOrth = null;
	int xLength;
	int yLength;
		
	int channelCount;
	
	int zLength;		// slices
	
	public static ArrayList<methodCalcDoc> methodDocs = new ArrayList<methodCalcDoc>();
	
	
	public Cell(Hemisegment hemiseg, File roiPath, int vlNum, Calibration cal) {
		this.hemiseg = hemiseg;
		this.vlNum = vlNum;
		
		this.roiPath = roiPath;
		//IJ.log("before openRoiCsv");
		roi = openRoiCsv(roiPath, cal);
		//IJ.log("roi = " + roi);
		makeCellHyp();
			
		makeGeoData();
		// Hemisegment loads nuc when it finishes loading both cells	
	}
	
	
	public double[] makeXCol() {
		int width = cellOrth.getWidth();
		double[] xCol = new double[width];
		for (int i = 0; i < width; i++) {
			xCol[i] = i;
		}
		return xCol;
	}
	
	public double[] longitudinalIntensity(int channel) {
		int width = cellOrth.getWidth();
		int height = cellOrth.getHeight();
		
		//cellOrth.setC(hemiseg.exper.channels.get("Cell"));
		cellOrth.setC(channel);
		ImageProcessor ip = cellOrth.getProcessor();			
		float[] pixels = (float[])ip.getPixels();
		
		//int pix = pixels[y*xLength + x]&0xff;
		
		
		
		double[] sums = new double[width];
		
		for (int x = 0; x < width; x++) {
			double sum = 0;
			for (int y = 0; y < height; y++) {
				//sum += (pixels[y*width + x]&0xff);
				sum += (pixels[y*width + x]);
				
			}
			//IJ.log("" + x + ", " + sum);
			sums[x] = sum;
			
		}
		
		
		
		double[] sums2 = new double[sums.length];
		
		double mean = 0;
		
		for (double val : sums) {
			mean += val;
		}
		mean = mean/sums.length;
		
		for (int i = 0; i < sums.length; i++) {
			sums2[i] = sums[i]/mean;
		}
		
		return sums2;
		
	}
	
	public void makeGeoData() {
		cellHyp.setRoi(roi);
		cellHyp.setC(hemiseg.exper.channels.get("Cell"));
		ResultsTable rt = new ResultsTable();
		Analyzer a = new Analyzer(cellHyp, Hemisegment.GEO, rt);
		a.measure();
		
		data = Functions.getRtRow(rt,0);
	}
		
	public Roi openRoiCsv(File roiCsvPath, Calibration cal) {
		try (Scanner sc = new Scanner(roiCsvPath)) {
			sc.useDelimiter(",|\\s");
			
			//String tempStr = sc.next();
			if (sc.hasNextDouble()) {
				roiX.add(cal.getRawX(sc.nextDouble()));
				if (sc.hasNextDouble()) {
					roiY.add(cal.getRawY(sc.nextDouble()));
				}
			}
			else {
				sc.next();
				sc.next();
			}
			
			while (sc.hasNextDouble()) {
				double temp = sc.nextDouble();
				roiX.add(cal.getRawX(temp));
				
				if (sc.hasNextDouble()) {
					temp = sc.nextDouble();
					roiY.add(cal.getRawY(temp));
				}
				else {
					/** exception */
					IJ.log("shit, in Cell.openRoiCsv, got x value with no matching y value");
				}
			}
			float[] xF = ArrLisDouToArrFlo(roiX);
			float[] yF = ArrLisDouToArrFlo(roiY);
			
			return new PolygonRoi(xF,yF,2);
		}
		catch (FileNotFoundException e) {
			/*** exception ***/
			IJ.log("in Cell.openRoiCsv exception: " + e);
			return null;
		}
	}	
		
	public void makeCellHyp() {
		cellHyp = Functions.cropStack(hemiseg.hyp, roi);
		
		PolygonRoi tempRoi = new PolygonRoi(roi.getFloatPolygon(),2);
		cellHyp.setRoi(tempRoi);
		tempRoi.setLocation(0,0);
		roi = tempRoi;
		
		ImagePlus temp = WindowManager.getTempCurrentImage();
		WindowManager.setTempCurrentImage(cellHyp);
		IJ.run("Clear Outside", "stack");
		WindowManager.setTempCurrentImage(temp);
		
		
		xLength = cellHyp.getDimensions()[0];
		yLength = cellHyp.getDimensions()[1];
		
		channelCount = cellHyp.getDimensions()[2];
		
		zLength = cellHyp.getDimensions()[3];
	}
		
	public void makeCellOrthView() {
		if (cellHyp == null) {
			makeCellHyp();
		}
		
		this.cellOrthStack = Functions.verticalCrossSection(cellHyp, null);
	}
	
	public void makeJustCellOrthView() {
		if (cellHyp == null) {
			makeCellHyp();
		}
		int[] poop = {hemiseg.exper.channels.get("Cell")};
		this.cellOrthStack = Functions.verticalCrossSection(cellHyp, poop);
	}
	
	
	//String methodName = myFunc();
	boolean thicknessDocTemp = thicknessDoc(methodDocs);
	public static boolean thicknessDoc(ArrayList<methodCalcDoc> methodDocs) {
		String methodName = "Cell.thickness()";
		
		String methodDescrip = ("Takes cellOrthStack\n"
			+ "for each slice\n"
			+ "\tfor each column\n"
			+ "\t\tcounts pixels greater than 15 (15 - random choice) - the  \"thickness\" at that point, records if some \"on\" pixels are separated by a gap of \"off\" pixels (basically never) only counts the first set (from top of imp), gets the average and mutliplies by area\n"
			+ "thickness 0 - excludes cols that are 0 - assumed to be artificat of rectangle image of polygon roi, has the possibility of not measuring cols of 0 within cell roi - not sure if that matters\n"
			+ "thickness 1 includes those columns\n"
			+ "(volume 0) = (thickness 0) * (area of polygon roi)\n"
			+ "(volume 1) = (thickness 1) * (area of rectangle imp/roi)\n");
			//String heading = "volume 1";
			String[] headings = {"thickness min 1", "thickness max 1", "thickness mean 1", "thickness min 0", "thickness max 0", "thickness mean 0", "thickness gap count", "Volume 1", "Volume 0"};
		//public static 
		//hemiseg.exper.cellMethodLogLookup.put("Volume 1", new methodCalcDoc(methodName, methodDescrip, headings));	
		methodDocs.add(new methodCalcDoc(methodName, methodDescrip, headings));
		//methodDocs.get(1).tears();
		for (String h : headings) {
			Experiment.heading2method.get("cell").put(h, methodName);
		}
		return true;
	}
	
	public void thickness() {
		ArrayList<Double> allCounts = new ArrayList<Double>();
		ArrayList<Double> allCounts2 = new ArrayList<Double>();
		int gaps = 0;
		makeJustCellOrthView();
		
		for (int slice = 0; slice < cellOrthStack.getNSlices(); slice++) {
			cellOrthStack.setSlice(slice);
			ByteProcessor bp = (ByteProcessor)cellOrthStack.getProcessor();
			byte[] pixels = (byte[])bp.getPixels();
			
			int xLength = cellOrthStack.getWidth();
			int yLength = cellOrthStack.getHeight();
			
			for (int x = 0; x < xLength; x++) {
				int count = 0;
				
				for (int y = 0; y < yLength; y++) {
					boolean started = false;
					boolean stopped = false;
					
					int pix = pixels[y*xLength + x]&0xff;
					if (pix > 15) {
						if (stopped) gaps++;
						else {
							count++;
							started = true;
						}
					
					}
					else if (started) stopped = true;
				}
				if (count > 0) allCounts2.add(hemiseg.cal.getZ(count));
				allCounts.add(hemiseg.cal.getZ(count));
			}
			
		}
		
		double[] temp = arrayStats(allCounts);
		double[] temp2 = arrayStats(allCounts2);

		
		data.put("thickness min 1", new MutableDouble(temp[0]));
		data.put("thickness max 1", new MutableDouble(temp[1]));
		data.put("thickness mean 1", new MutableDouble(temp[2]));
		data.put("thickness gap count", new MutableDouble(gaps));
		
		double volume = data.get("Height").get()*data.get("Width").get() * temp[2];
		data.put("Volume 1", new MutableDouble(volume));
		
		data.put("thickness min 0", new MutableDouble(temp2[0]));
		data.put("thickness max 0", new MutableDouble(temp2[1]));
		data.put("thickness mean 0", new MutableDouble(temp2[2]));
		
		double volume2 = data.get("Area").get() * temp2[2];
		data.put("Volume 0", new MutableDouble(volume2));
		
		
		
	}
	
	/* <methodDoc> */
	boolean volume2DocTemp = volume2Doc(methodDocs);
	public static boolean volume2Doc(ArrayList<methodCalcDoc> methodDocs) {
		String methodName = "Cell.volume2()";
		String methodDescrip = ("Use cellHyp, set min to 15, sumSlices"
			+ "need to double check whether set min is doing what we want it to do - set >15 to 0 not to 15");
		String[] headings = {"Volume 2"};
		methodDocs.add(new methodCalcDoc(methodName, methodDescrip, headings));
		
		for (String h : headings) {
			Experiment.heading2method.get("cell").put(h, methodName);
		}
		return true;
	}
		/* </methodDoc>*/
	
	public void volume2() {
		int c = hemiseg.exper.channels.get("Cell");
		cellHyp.setRoi(roi);
		Duplicator d = new Duplicator();
		ImagePlus temp = d.run(cellHyp,c,c,1,cellHyp.getNSlices(),1,1);

		IJ.setMinAndMax(temp, 15, 255);
		IJ.run(temp, "Apply LUT", "stack");
		
		double sum = Functions.sumSlices(temp, hemiseg.cal.pixelDepth);
		data.put("Volume 2", new MutableDouble(sum));
		/* 
		String methodName = "Cell.volume2()";
		String methodDescrip = ("Use cellHyp, set min to 15, sumSlices"
								 + "need to double check whether set min is doing what we want it to do - set >15 to 0 not to 15");
		String[] headings = {"Volume 2"};
		hemiseg.exper.cellMethodLogLookup.put("Volume 2", new methodCalcDoc(methodName, methodDescrip, headings));		
	 */
	 }
	
	public double[] arrayStats(ArrayList<Double> inArr) {
		double min = inArr.get(0).intValue();
		double max = inArr.get(0).intValue();
		double sum = 0;
		for (int i = 0; i < inArr.size(); i++) {
			double num = inArr.get(i).intValue();
			if (num > max) max = num;
			else if (num < min) min = num;
			sum += num;
		}
		double mean = sum/inArr.size();
		double[] outArr = {min,max,mean};
		return outArr;
	}
	
	
	public void makeTotalAV() {
		double nucTotalArea = 0;
		double nucTotalVolume = 0;
		for (Nucleus nuc : nucs) {
			MutableDouble temp = nuc.data.get("Area");
			if (temp != null) nucTotalArea += temp.get();
			
			MutableDouble temp2 = nuc.data.get("cropped stack vol sum2");
			if (temp2 != null) nucTotalVolume += temp2.get();
		}
		
		data.put("Nuc Total Area", new MutableDouble(nucTotalArea));
		data.put("Nuc Total Volume", new MutableDouble(nucTotalVolume));
	}
	
	public MutableDouble yScaled(MutableDouble num) {
		Rectangle bounds = roi.getBounds();
		double height = bounds.height;
		double start = bounds.y;
		
		double yPoint = num.get();
		double yPointTemp = yPoint - start;
		double yPointScaled = yPointTemp/height;
		
		MutableDouble outNum = new MutableDouble(yPointScaled);
		return outNum;
	}
	
	public MutableDouble yScaled(double yPoint) {
		Rectangle bounds = roi.getBounds();
		double height = bounds.height;
		double start = bounds.y;
		
		double yPointTemp = yPoint - start;
		double yPointScaled = yPointTemp/height;
		
		MutableDouble outNum = new MutableDouble(yPointScaled);
		return outNum;
	}
	
	
	/*** ArrayList<double> to float[]
	***/
	public float[] ArrLisDouToArrFlo(ArrayList<Double> arrLis) {
		float[] f = new float[arrLis.size()];
		for (int i = 0; i < arrLis.size(); i++) {
			f[i] = arrLis.get(i).floatValue();
		}
		return f;
	}
	
	public String cellID () {
		return ("" + hemiseg.name + " vl" + vlNum);
	}

		
	public String toString() {
		return ("vlms.Cell: " + hemiseg.name + " vl" + vlNum);
	}
		
	public String toStringLong() {
		String has = "\nHas: ";
		String doesntHave = "\nDoesn't Have: ";
		
		if (hemiseg == null) doesntHave += "hemiseg, ";
		else has += "hemiseg, ";
		if (vlNum == -1) doesntHave += "vlNum, ";
		else has += "vlNum, ";
		
		if (roiPath == null) doesntHave += "roiPath, ";
		else has += "roiPath, ";
		if (roi == null) doesntHave += "roi, ";
		else has += "roi, ";
		
		if (roiX == null) doesntHave += "roiX, ";
		else has += "roiX, ";
		if (roiY == null) doesntHave += "roiY, ";
		else has += "roiY, ";
				
		if (nucs == null) doesntHave += "nucs, ";
		else has += "nucs, ";
		if (nucCount == -1) doesntHave += "nucCount, ";
		else has += "nucCount, ";
		
		has = has.substring(0, has.length() - 2);
		doesntHave = doesntHave.substring(0, doesntHave.length() - 2);
		
		return (this.toString()
				+ has
				+ doesntHave);		
	}
	
	public String fullSummary() {
		String temp = this.toString();
		
		temp += ("\nhemiseg: " + hemiseg);
		temp += ("\nvlNum: " + vlNum);
		
		temp += ("\nroiPath: " + roiPath);
		temp += ("\nroi: " + roi);
		
		temp += ("\nroiX: " + roiX);
		temp += ("\nroiY: " + roiY);
		
		
		temp += ("\nnucs: " + nucs);
		temp += ("\nnucCount: " + nucCount);
		
		return temp;
	}
}








