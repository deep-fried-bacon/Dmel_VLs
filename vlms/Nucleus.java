/*
	data can contain geoHeadings, "orthRoi - " + geoHeadings, 
		"vol pix count", "vol pix sum", "yScaled"
*/
package vlms;
import ij.*;
import ij.io.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.filter.*;
import ij.plugin.*;
import ij.process.*;

import java.util.*;
import java.awt.*;

import vlms.utilities.*;


public class Nucleus {
	public Cell cell;
	public int id;
	public Roi roi;
	public Hashtable<String, MutableDouble> data;
	//public Hashtable<String, MutableDouble[]> sliceData;
	
	private ImagePlus stack = null;
	private ImagePlus orthStack = null;
	private ImagePlus orth = null;
	private ImagePlus orthThresh = null;
	private ImagePlus croppedOrthStack = null;
		
	public Roi orthRoi = null;
	
	public double chunkX = -1;
	public double chunkY = -1;
	public double chunkZ = -1;
	
	public double xCal = -1;
	public double yCal = -1;
	public double zCal = -1;

	
	public Nucleus (Cell cell, int id, Roi roi, Hashtable<String, MutableDouble> data) {
		if (cell == null || roi == null || data == null) {
			 throw new NullPointerException();
		}
		else {
			this.cell = cell;
			this.chunkZ = cell.hemiseg.sliceCount;
			
			this.xCal = cell.hemiseg.cal.pixelWidth;
			this.yCal = cell.hemiseg.cal.pixelHeight;
			this.zCal = cell.hemiseg.cal.pixelDepth;
			
			this.id = id;
			this.roi = roi;
			this.data = data;		
		}
	}
	
	public void makeNucImps() {	
		int nucChan = cell.hemiseg.exper.channels.get("Nuclei");
		ImagePlus cellNucChan;
		
		Duplicator d = new Duplicator();
		if (nucChan < cell.hemiseg.hyp.getNChannels()) {
			cellNucChan = d.run(cell.hemiseg.hyp, nucChan, nucChan, 1, cell.hemiseg.sliceCount, 1, 1);
		}
		else {
			/* exception */
			//cellNucChan d.run(cell.hemiseg.hyp, 1, 1, 1, cell.hemiseg.sliceCount, 1, 1)
			return;
		}
		
		setStack(Functions.cropStack(cellNucChan, roi));
			
		setOrthStack(Functions.verticalCrossSection(stack,null));
		
		setOrth((new ZProjector()).run(orthStack,"max"));
		
		setOrthThresh(Functions.autoThresholdSlice(orth,"IsoData"));
	
		ResultsTable rt = new ResultsTable();
		Overlay nucOrthOverlay = Functions.particleAnalyze(orthThresh, Hemisegment.GEO, rt);
		
		int index = 0;
		if (nucOrthOverlay.size() > 1) {
			int count = 0;
			for (int j = 0; j < nucOrthOverlay.size(); j++) {
				if (nucOrthOverlay.get(j).getStatistics().area > 75) {
					count++;
					index = j;	
				}
			}
			if (count > 1) {
				/* exception */
				IJ.log(cell.hemiseg.name + " vl"+cell.vlNum + ": makeNucImps - multiple large rois in nuc cross-section");
				for (int j = 0; j < nucOrthOverlay.size(); j++) {
					IJ.log("    \t" + nucOrthOverlay.get(j).getStatistics().area);
				}
				return;
			}
			else if (count < 1) {
				/* exception */
				IJ.log(cell.hemiseg.name + " vl"+cell.vlNum + ": makeNucImps - no large rois in nuc cross-section");
				for (int j = 0; j < nucOrthOverlay.size(); j++) {
					IJ.log("    \t" + nucOrthOverlay.get(j).getStatistics().area);
				}
				return;
			}
		} 
		else if (nucOrthOverlay.size() < 1) {
			/* exception */
			IJ.log(cell.hemiseg.name + " vl"+cell.vlNum + ": makeNucImps - no rois in nuc cross-section");
			return;
		}	
		
		Hashtable<String,MutableDouble> row = Functions.getRtRow(rt,index,"orthRoi");
				
		data.putAll(row);
		orthRoi = nucOrthOverlay.get(index);
	}

	
	public void countOrthPixels () {
		if (orthStack == null) {
			/* exception */
			IJ.log(fullId());
			IJ.log("\tcountOrthPixels: orthStack = null");
			return;
		}
		if (orthRoi == null) {
			/* exception */
			IJ.log(fullId());
			IJ.log("\tcountOrthPixels: orthRoi = null");
			return;
		}
		
		setCroppedOrthStack(Functions.cropStack(orthStack, orthRoi));
		int pixelCount = 0;
		int pixelSum = 0;

		for (int slice = 0; slice < getCroppedOrthStack().getNSlices(); slice++) {	
			ImageProcessor ip = getCroppedOrthStack().getProcessor();
			
			byte[] pix = (byte[])ip.getPixels();
			
			for (int i = 0; i < pix.length; i++) {
				int temp = pix[i]&0xff;
				if (temp > 0){
					pixelCount++;
					pixelSum += temp;
				}
				else if (temp < 0) {
					/* exception */
					IJ.log("Nucleus.countOrthPixels : pixel value < 0");
				}
			}	
		}
		
		data.put("vol pix count", new MutableDouble((double)pixelCount));
		data.put("vol pix sum", new MutableDouble((double)pixelSum));
	}

	public void yScaled () {
		double inY = data.get("Y").get();
		double inY2 = cell.hemiseg.cal.getRawY(inY);
		MutableDouble outY = cell.yScaled(inY2);
		data.put("Y Scaled to Cell",outY);
	}
	
	public void allSliceSums() {
		sumSlicesOrthStack();
		sumSlicesSubStack();
		sumSlicesStack();
	}
	
	public void sumSlicesOrthStack() {
		if (orthStack == null) {
			/* exception */
			IJ.log(fullId());
			IJ.log("\tsumSlicesOrthStack: orthStack = null");
			return;
		}
		if (orthRoi == null) {
			/* exception */
			IJ.log(fullId());
			IJ.log("\tsumSlicesOrthStack: orthRoi = null");
			return;
		}
		
		ImagePlus orthStackCrop = Functions.cropStack(orthStack, orthRoi);
		
		double sum = Functions.sumSlices(orthStackCrop, xCal);
		if (sum == -1) return;

		data.put("orth vol sum", new MutableDouble(sum));
	}
	
	public void sumSlicesStack() {
		double sum = Functions.sumSlices(stack, zCal);
		if (sum == -1) return;
		
		data.put("stack vol sum", new MutableDouble(sum));
	}
	
	public void sumSlicesSubStack() {
		if (orthRoi == null) {
			/*exception*/
			IJ.log(fullId());
			IJ.log("orthRoi == null");
			return;
		}
		
		Rectangle bounds = orthRoi.getBounds();
		
		int top = bounds.y + 1;
		int bot = bounds.y + bounds.height;
		
		Duplicator d = new Duplicator();
		ImagePlus croppedStack = d.run(stack,top,bot);
	
		double sum = Functions.sumSlices(croppedStack, zCal);
		if (sum != -1) {
			data.put("cropped stack vol sum", new MutableDouble(sum));
		}
		
		top = top - 3;
		if (top < 1) top = 1;
		bot = bot + 3;
		if (bot > stack.getStackSize()) bot = stack.getStackSize();
		ImagePlus croppedStack2 = d.run(stack,top,bot);
		double sum2 = Functions.sumSlices(croppedStack2, zCal);
		
		if (sum2 != -1) {
			data.put("cropped stack vol sum2", new MutableDouble(sum2));
		}
	}
	

	public ImagePlus getStack() {
		return stack;
	}
	public boolean setStack(ImagePlus stack) {
		if (stack == null) {
			//IJ.log("null");
			return false;
		}
		if (!(stack.getNSlices() == chunkZ)) {
			//IJ.log("chunkZ = " +  chunkZ);
			//IJ.log("stack.getNChannels() = " +  stack.getNChannels());
			return false;
		}
		else {
			if (chunkX == -1) chunkX = stack.getWidth();
			else if (!(stack.getWidth() == chunkX)) {
				//IJ.log("ChunkX");
				return false;
			}
			
			if (chunkY == -1) chunkY = stack.getHeight();
			else if (!(stack.getWidth() == chunkY)) {
				//IJ.log("ChunkY");
				return false;
			}
			
			
			this.stack = stack;
			this.stack.setCalibration(cell.hemiseg.cal);
			
			return true;
		}
	}
	
	public ImagePlus getOrthStack() {
		return orthStack;
	}
	public boolean setOrthStack(ImagePlus orthStack) {
		if (orthStack == null) return false;
		if (!(orthStack.getHeight() == chunkZ)) return false;
		else {
			if (chunkX == -1) chunkX = orthStack.getNSlices();
			else if (!(orthStack.getNSlices() == chunkX)) return false;
			
			if (chunkY == -1) chunkY = orthStack.getWidth();
			else if (!(orthStack.getWidth() == chunkY)) return false;
			
			
			this.orthStack = orthStack;
			this.orthStack.setCalibration(cell.hemiseg.cal.copy());
			
			this.orthStack.getCalibration().pixelWidth = yCal;
			this.orthStack.getCalibration().pixelHeight = zCal;
			this.orthStack.getCalibration().pixelDepth = xCal;
			
			return true;
		}
	}
	
	public ImagePlus getOrth() {
		return orth;
	}
	public boolean setOrth(ImagePlus orth) {
		if (orth == null) return false;
		if (!(orth.getHeight() == chunkZ)) return false;
		else {
			if (chunkY == -1) chunkY = orth.getWidth();
			else if (!(orth.getWidth() == chunkY)) return false;
			
			
			this.orth = orth;
			this.orth.setCalibration(cell.hemiseg.cal.copy());
			
			this.orth.getCalibration().pixelWidth = yCal;
			this.orth.getCalibration().pixelHeight = zCal;
			this.orth.getCalibration().pixelDepth = xCal;
			return true;
		}
	}
	
	public ImagePlus getOrthThresh() {
		return orthThresh;
	}
	public boolean setOrthThresh(ImagePlus orthThresh) {
		if (orthThresh == null) return false;
		if (!(orthThresh.getHeight() == chunkZ)) return false;
		else {
			if (chunkY == -1) chunkY = orthThresh.getWidth();
			else if (!(orthThresh.getWidth() == chunkY)) return false;
			
			
			this.orthThresh = orthThresh;
			
			this.orth.setCalibration(cell.hemiseg.cal.copy());
			
			this.orthThresh.getCalibration().pixelWidth = yCal;
			this.orthThresh.getCalibration().pixelHeight = zCal;
			this.orthThresh.getCalibration().pixelDepth = xCal;
			
			return true;
		}
	}
	
	public ImagePlus getCroppedOrthStack() {
		return croppedOrthStack;
	}
	public boolean setCroppedOrthStack(ImagePlus croppedOrthStack) {
		if (croppedOrthStack == null) return false;
		else {
			if (chunkX == -1) chunkX = croppedOrthStack.getNSlices();
			else if (!(croppedOrthStack.getNSlices() == chunkX)) {
				return false;
			}
		
			this.croppedOrthStack = croppedOrthStack;
			
			this.croppedOrthStack.setCalibration(cell.hemiseg.cal.copy());
			
			this.croppedOrthStack.getCalibration().pixelWidth = yCal;
			this.croppedOrthStack.getCalibration().pixelHeight = zCal;
			this.croppedOrthStack.getCalibration().pixelDepth = xCal;
			
			return true;
		}
	}
	
	public String fullId() {
		return ("Hemisegment " + cell.hemiseg.name + " vl" + cell.vlNum + " Nuc " + id);
	}
	
	public String toString() {
		return ("vlms.Nucleus: " + fullId());
	}
	
	public String toStringLong() {
		String has = "Has: ";
		String doesntHave = "Doesn't Have: ";
		
		if (roi == null) doesntHave += "roi, ";
		else has += "roi, ";
		if (data == null) doesntHave += "data, ";
		else has += "data, ";
		
		if (stack == null) doesntHave += "stack, ";
		else has += "stack, ";
		if (orthStack == null) doesntHave += "orthStack, ";
		else has += "orthStack, ";
		
		if (orth == null) doesntHave += "orth, ";
		else has += "orth, ";
		if (orthThresh == null) doesntHave += "orthThresh, ";
		else has += "orthThresh, ";
		if (orthRoi == null) doesntHave += "orthRoi, ";
		else has += "orthRoi, ";
		
		if (chunkX == -1) doesntHave += "chunkX, ";
		else has += "chunkX, ";
		if (chunkY == -1) doesntHave += "chunkY, ";
		else has += "chunkY, ";
		if (chunkZ == -1) doesntHave += "chunkZ, ";
		else has += "chunkZ, ";
		
		has = has.substring(0, has.length() - 2);
		doesntHave = doesntHave.substring(0, doesntHave.length() - 2);
		
		return (this.toString()
				+ has + "\n"
				+ doesntHave);
	}
		
	public String fullSummary(boolean dataTables) {
		String temp = this.toString();
		temp += ("\nroi: " + roi);
		
		if (dataTables) {
			temp += ("\ndata: " + data);

		}
		else {
			temp += ("\ndata: " + data);
		}
		
		temp += ("\nstack: " + stack);
		temp += ("\northStack: " + orthStack);
		temp += ("\north: " + orth);
		temp += ("\northThresh: " + orthThresh);
		
		temp += ("\northRoi: " + orthRoi);
		
		
		temp += ("\nchunkX: " + chunkX + ", chunkY: " + chunkY + ", chunkZ: " + chunkZ);
		
		return temp;
	}
		
}