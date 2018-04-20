package vlms.utilities;

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


public class Functions {
	
	/** if channels == null, uses all channels */
	public static ImagePlus verticalCrossSection(ImagePlus imp, int[] channels) {
		if (imp == null) return null;
		if (channels == null) {
			channels = new int[imp.getNChannels()];
			for (int i = 0; i < channels.length; i++) {
				channels[i] = i+1;
			}
		}
		ImageStack impStack = imp.getStack();
		
		int xLength = imp.getDimensions()[0];
		int yLength = imp.getDimensions()[1];
		
		int zLength = imp.getDimensions()[3];
		
		ImageStack outStack = new ImageStack(yLength, zLength, channels.length*xLength);
		ImagePlus outImp = new ImagePlus();
		
		for (int i = 0; i < channels.length; i++) {
			int channel = channels[i];
			for (int x = 0; x < xLength; x++) {
				byte[] xSlice = new byte[yLength*zLength];
				
				for (int z = 0; z < zLength; z++) {
					ImageProcessor ip = impStack.getProcessor(imp.getStackIndex(channel,z+1,1));			
					byte[] pixels = (byte[])ip.getPixels();
					int y = 0;
					while ((y*xLength + x) < pixels.length && (z*yLength + y) < xSlice.length) {
						xSlice[z*yLength + y] = pixels[y*xLength + x];
						y++;
					}
				}
				outStack.setPixels(xSlice,x*channels.length+i+1);			
			}
		}			
		outImp.setStack(outStack);
		outImp.setOpenAsHyperStack(true);
		outImp.setDimensions(channels.length,xLength,1);

		return outImp;
	}	
	
	/** changes were made to vertical that haven't been made to horizontal yet
		if channels == null, uses all channels */
	public static ImagePlus horizontalCrossSection(ImagePlus imp, int[] channels) {
		if (imp == null) return null;
		if (channels == null) {
			channels = new int[imp.getNSlices()];
			for (int i = 0; i < channels.length; i++) {
				channels[i] = i+1;
			}
		}		
		ImageStack impStack = imp.getStack();
		
		int xLength = imp.getDimensions()[0];
		int yLength = imp.getDimensions()[1];
		
		int staticChannelCount = imp.getDimensions()[2];
		int zLength = imp.getDimensions()[3];
		
		//ImageStack[] outStacks = new ImageStack[channels.length];
		ImageStack outStack = new ImageStack(xLength, zLength, channels.length*yLength);
		ImagePlus outImp = new ImagePlus();
		outImp.setStack(outStack);
		outImp.setDimensions(channels.length,yLength,1);
		//for (int channel = 1; channel <= staticChannelCount; channel++) {
			
		for (int i = 0; i < channels.length; i++) {
			int channel = channels[i];
			//ImageStack sliceHolder = new ImageStack(yLength,zLength);
			for (int y = 0; y < yLength; y++) {
				byte[] ySlice = new byte[xLength*zLength];
				
				for (int z = 0; z < zLength; z++) {
					ImageProcessor ip = impStack.getProcessor(imp.getStackIndex(channel,z+1,1));			
					byte[] pixels = (byte[])ip.getPixels();
					int x = 0;
					//while ((x*yLength + x) < pixels.length && (z*xLength + x) < ySlice.length) {
					while ((y*xLength + x) < pixels.length && (z*xLength + x) < ySlice.length) {
						ySlice[z*xLength + x] = pixels[y*xLength + x];
						y++;
					}
				}
				//ByteProcessor bp = new ByteProcessor(yLength,zLength,ySlice);
				//sliceHolder.addSlice(bp);
				outStack.setPixels(ySlice,outImp.getStackIndex(channel+1,y+1,1));
			}
			//outStacks[i] = (sliceHolder.duplicate());
		}			
		//ImagePlus[] outImps = new ImagePlus[outStacks.length];
		// for (int i = 0; i < outImps.length; i++) {
			// ImagePlus temp = new ImagePlus((""+(i)), outStacks[i]);
			// outImps[i] = temp.duplicate();
		// }
		return outImp;
	}	
		
	public static Hashtable<String,MutableDouble> getRtRow(ResultsTable rt, int rowNum) {
		String[] headings = rt.getHeadings();
		int colCount = headings.length;
		Hashtable<String,MutableDouble> row = new Hashtable<String,MutableDouble>(colCount);
		
		for (int i = 0; i < colCount; i++) {
			String heading = headings[i];
			
			int colIndex = rt.getColumnIndex(heading);
			if (colIndex == ResultsTable.COLUMN_NOT_FOUND) {
				if (heading != "Label") {
					/*** exception ***/
					IJ.log("in getRtRow rt heading missing, heading = " + heading);
				}
			}
			else {
				double val = rt.getValueAsDouble(colIndex,rowNum);
				row.put(heading,new MutableDouble(val));
			}
		}
		return row;
	}
	
	public static Hashtable<String,MutableDouble> getRtRow(ResultsTable rt, int rowNum, String prefix) {
		String[] headings = rt.getHeadings();
		int colCount = headings.length;
		Hashtable<String,MutableDouble> row = new Hashtable<String,MutableDouble>(colCount);
		
		for (int i = 0; i < colCount; i++) {
			String heading = headings[i];
			
			int colIndex = rt.getColumnIndex(heading);
			if (colIndex == ResultsTable.COLUMN_NOT_FOUND) {
				if (heading != "Label") {
					/*** exception ***/
					IJ.log("in getRtRow rt heading missing, heading = " + heading);
				}
			}
			else {
				double val = rt.getValueAsDouble(colIndex,rowNum);
				row.put(prefix + " - " + heading, new MutableDouble(val));
			}
		}
		return row;
	}

	/** returns a duplicate imp, doesn't change original image */
	public static ImagePlus cropStack(ImagePlus imp, Roi r) {
		imp.setRoi(r);
		ImagePlus outImp = imp.duplicate();
		imp.deleteRoi();
		return outImp;
	}

	/** z-Projection 
		imp2 = (new ZProjector()).run(imp,"max"); */
	
	/** works on both a single image or the current slice of a stack 
		makes a duplicate image, doesn't change original */
	public static ImagePlus autoThresholdSlice(ImagePlus imp, String method) {
		Auto_Threshold at = new Auto_Threshold();
		Object[] temp = at.exec(imp, method, false, false, true, false, false, false);
		ImagePlus outImp = (ImagePlus)temp[1];
		return outImp;
	}
	
	/** sets the current slice of original image, 
		otherwise leaves original  image untouched
		returns a duplicate image */
	public static ImagePlus autoThresholdSlice(ImagePlus imp, String method, int slice) {
		/* check if slice <= stackSize() */
		imp.setSlice(slice);
		Auto_Threshold at = new Auto_Threshold();
		Object[] temp = at.exec(imp, method, false, false, true, false, false, false);
		ImagePlus outImp = (ImagePlus)temp[1];
		return outImp;
	}
	
	/** untested!!! */
	public static ImagePlus autoThresholdStack(ImagePlus imp, String method) {
		ImagePlus outImp = imp.duplicate();
		Auto_Threshold at = new Auto_Threshold();
		for (int k = 1; k <= outImp.getStackSize(); k++){
			outImp.setSlice(k);
			at.exec(outImp, method, false, false, true, false, false, false);
		}
		return outImp;
	}
	
	/** Analyze Particle command */
	public static Overlay particleAnalyze(ImagePlus imp, int msrments, ResultsTable rt) {
		imp.setOverlay(null);
		rt.reset();
		ParticleAnalyzer pa = new ParticleAnalyzer(ParticleAnalyzer.SHOW_OVERLAY_OUTLINES, msrments, rt, 0, Double.POSITIVE_INFINITY);
		
		pa.analyze(imp);
		Overlay outOverlay = imp.getOverlay();
		imp.setOverlay(null);
		
		return outOverlay;
	}
	
	/** untested modified version of cell.thickness() */
	public static Hashtable<String, MutableDouble> avgThickness(ImagePlus orthImp) {
		ArrayList<Integer> allCounts = new ArrayList<Integer>();
		int gaps = 0;
		
		for (int slice = 0; slice < orthImp.getNSlices(); slice++) {
			orthImp.setSlice(slice);
			ByteProcessor bp = (ByteProcessor)orthImp.getProcessor();
			byte[] pixels = (byte[])bp.getPixels();
			
			int xLength = orthImp.getWidth();
			int yLength = orthImp.getHeight();
			
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
				if (count > 0) allCounts.add(count);
			}
		}
		Hashtable<String, MutableDouble> outHashtable = arrayStats(allCounts, "Thickness");
		outHashtable.put("avgThickness gap count", new MutableDouble(gaps));
		return outHashtable;		
	}
	
	/** untested modified version of cell.arrayStats() */
	public static Hashtable<String, MutableDouble> arrayStats(ArrayList<Integer> inArr) {
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
		
		Hashtable<String, MutableDouble> outHashtable = new Hashtable<String,MutableDouble>(3);
		outHashtable.put("Mean", new MutableDouble(mean));
		outHashtable.put("Min", new MutableDouble(min));
		outHashtable.put("Max", new MutableDouble(max));
		return outHashtable;
	}
	
	/** untested modified version of cell.arrayStats(),
		with added prefix option */
	public static Hashtable<String,MutableDouble> arrayStats(ArrayList<Integer> inArr, String prefix) {
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
		
		Hashtable<String,MutableDouble> outHashtable = new Hashtable<String,MutableDouble>(3);
		outHashtable.put(prefix + " - Mean", new MutableDouble(mean));
		outHashtable.put(prefix + " - Min", new MutableDouble(min));
		outHashtable.put(prefix + " - Max", new MutableDouble(max));
		return outHashtable;
	}
	
	public static double sumSlices(ImagePlus impStack, double scale) {
		if (impStack == null) {
			/* exception */
			IJ.log("Functions.sumSlices: impStack = null");
			return -1;
		}
		
		ResultsTable rt = new ResultsTable();

		for (int slice = 1; slice <= impStack.getStackSize(); slice++) {
			impStack.setSlice(slice);
			Analyzer a = new Analyzer(impStack, (Measurements.AREA|Measurements.AREA_FRACTION), rt);
	
			a.measure();
		}
		
		double[] percCol = rt.getColumnAsDoubles(rt.getColumnIndex("%Area"));
		double totArea = rt.getValueAsDouble(rt.getColumnIndex("Area"),0);
		
		double sum = 0;
		
		for (int i = 0; i < percCol.length; i++) {
			sum += (totArea * percCol[i] * scale)/100;
		}
				rt.show("butts");

		return sum;
	}
	
	/** I think this is the same as sumSlices(),
		but there might be some small change somewhere
		probably can just delete */
	public static double _sumSlices(ImagePlus impStack, double scale) {
		if (impStack == null) {
			/* exception */
			IJ.log("Functions.sumSlices: impStack = null");
			return -1;
		}
		ResultsTable rt = new ResultsTable();

		for (int slice = 1; slice <= impStack.getStackSize(); slice++) {
			impStack.setSlice(slice);
			Analyzer a = new Analyzer(impStack, (Measurements.AREA|Measurements.AREA_FRACTION), rt);
		
			a.measure();
		}
		double[] percCol = rt.getColumnAsDoubles(rt.getColumnIndex("%Area"));
		double totArea = rt.getValueAsDouble(rt.getColumnIndex("Area"),0);
		
		double sum = 0;
		
		for (int i = 0; i < percCol.length; i++) {
			sum += (totArea * percCol[i] * scale)/100;
		}

		return sum;
	}	
}



