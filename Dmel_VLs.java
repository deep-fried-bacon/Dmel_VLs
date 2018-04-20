import ij.plugin.PlugIn;
import ij.*;
import ij.io.*;
import ij.IJ.*;

import java.util.*;
import java.io.File;

import vlms.*;
import vlms.utilities.*;


public class Dmel_VLs implements PlugIn{
	public void run(String arg) {
		String py_path = "D:\\People Files\\Lab\\Fiji\\Jython\\Dmel_VLs.py";
		
		//if(inst_count == 1) {
		Opener myOpener = new Opener();
		myOpener.open(py_path);	
		
		
		
		
		String py_path2 = IJ.getDirectory("startup") + "\\jars\\Lib\\Dmel_VLs.py";
		
		//if(inst_count == 1) {
		Opener myOpener2 = new Opener();
		myOpener.open(py_path2);	
		
		
		
		//}
		
		
	}
	
	
}