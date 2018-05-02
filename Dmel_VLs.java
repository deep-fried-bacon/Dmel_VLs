import java.util.*;
import java.io.File;

import ij.plugin.PlugIn;
import ij.*;
import ij.io.*;
import ij.IJ.*;

import vlms.*;
import vlms.utilities.*;


public class Dmel_VLs implements PlugIn {
	
	public static String dataDirPath = "D:/People Files/Lab/Data/Steffi NMJ Sets/";
	public static String[] dataDirs = {"150729_w1118","150910_Dm2-EGFP","151021_Dm2-GFPRNAi","151216_Dm2-GFP"};

	
	public static String nucFileSuf = "y-area-thickness";
	public static String[] nucHeadings = {"Y","Y Scaled to Cell","Area",
											"Thickness(minFeret)","Thickness(Height)", 
											"vol pix count", "vol pix sum","Cross-sectional Area",
											"orth vol sum","stack vol sum","cropped stack vol sum",
											"cropped stack vol sum2"};
	
	//public static String[] nucHeadingsAll = {hemiseg.geoHeadings}
	
	
	public static String cellFileSuf = "cell-vol-stuff";
	public static String[] cellHeadings = {"Area","Volume 0", "Volume 1", "Volume 2","thickness mean 0", "thickness max 0", "thickness min 0", "thickness mean 1", "thickness max 1", "thickness min 1","Nuc Total Area","Nuc Total Volume"};
	
	
	public static void closeExper(Experiment exper) {
		exper.close();
		exper = null;
		System.gc();

	}
	
	public static void closeAllExper() {
		for (Experiment e : Experiment.insts) {
			closeExper(e);
		}
	}
	
	public static void runAll(int[] dataDirNum) {
		for (int i = 0; i < dataDirNum.length; i++) {
			Experiment exper = Experiment.experConstructEverything(new File(dataDirPath + dataDirs[i]), nucFileSuf, nucHeadings, cellFileSuf, cellHeadings);
			closeExper(exper);
		}
		return;
	}
	public static Experiment make(int dataDirNum) {
		return new Experiment(dataDirPath + dataDirs[dataDirNum]);
	}		
	
	public static void runAndCloseAll() {
		for (int i = 0; i < 4; i++) {
			Experiment temp = Experiment.experConstructEverything(
				new File(dataDirPath + dataDirs[i]), 
				nucFileSuf, nucHeadings,
				cellFileSuf, cellHeadings);
		}
		return;
	}
	
	public static Experiment getInst(int i) {
		return Experiment.insts.get(i);
	}
	
	public void run(String arg) {
		String py_path = "D:\\People Files\\Lab\\Fiji\\Java\\Dmel_VLs\\useDmel_VLs.py";
		Opener myOpener = new Opener();
		myOpener.open(py_path);		


		
	}
}