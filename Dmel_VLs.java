import java.util.*;
import java.io.File;

import ij.plugin.PlugIn;
import ij.*;
import ij.io.*;
import ij.IJ.*;

import vlms.*;
import vlms.utilities.*;


public class Dmel_VLs implements PlugIn{
	public void run(String arg) {
		String py_path = "D:\\People Files\\Lab\\Fiji\\Java\\Dmel_VLs\\useDmel_VLs.py";
		Opener myOpener = new Opener();
		myOpener.open(py_path);		
	}	
}
