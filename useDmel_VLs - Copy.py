from ij.plugin.frame import RoiManager
from ij.measure import ResultsTable
from ij.plugin import ZProjector

from ij import IJ, ImageStack, ImagePlus

from vlms import Experiment, Cell
from vlms.utilities import Functions

from java.io import File

import time

dataPath = "D:/People Files/Lab/Data/Steffi NMJ Sets/"
dataDirs = ["150729_w1118","150910_Dm2-EGFP","151021_Dm2-GFPRNAi","151216-Dm2-GFP"]

nucFileSuf = "y-area-thickness";
nucHeadings = ["Y","Y Scaled to Cell","Area",
 				"Thickness(minFeret)","Thickness(Height)", 
 				"vol pix count", "vol pix sum","Cross-sectional Area",
 				"orth vol sum","stack vol sum","cropped stack vol sum",
 				"cropped stack vol sum2"]

cellFileSuf = "cell-vol-stuff"
cellHeadings = ["Area","Volume 0", "Volume 1", "Volume 2","thickness mean 0",
				"thickness mean 1","Nuc Total Area","Nuc Total Volume"];
for i in range(4) :
	for e in Experiment.insts :
		e.close()
	e = Experiment.experConstructEverything(File(dataPath + dataDirs[i]),
			nucFileSuf,nucHeadings,cellFileSuf,cellHeadings)
	


#e = Experiment(dataPath + dataDirs[0])

#if True : #len(Experiment.insts) == 0 :
#	e = Experiment.experConstructEverything(File(dataPath + dataDirs[0]),
#			nucFileSuf,nucHeadings,cellFileSuf,cellHeadings)
#else :
#	e = Experiment.insts[0]
	
	

if False :
	for doc in Cell.methodDocs :
		print(doc)

	print(e)
	
	#e.hemisegs[0].hyp.show()
	
	
	
	
	start = time.clock()
	temp = Functions.verticalCrossSection(e.hemisegs[1].hyp,[1,2])
	temp.show()
	howLong = time.clock()  - start
	print("my vetCrossSect: " + str(howLong) + " secs")
	
	
	start = time.clock()
	temp2 = ZProjector().run(e.hemisegs[1].hyp,"max")
	temp2.show()
	howLong = time.clock()  - start
	print("Zproject: " + str(howLong) + " secs")
	
	
