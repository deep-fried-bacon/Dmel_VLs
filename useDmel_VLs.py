from ij.plugin.frame import RoiManager
from ij.measure import ResultsTable
from ij.plugin import ZProjector

from ij import IJ, ImageStack, ImagePlus


import Dmel_VLs
from vlms import Experiment, Cell
from vlms.utilities import Functions

from java.io import File

import time

#e = Dmel_VLs.getInst(0)
#h = e.hemisegs[0]
#vl3 = h.vl3
#vl4 = h.vl4


#h.hyp.show()
#h.hyp.setRoi(vl3.roi)

#for c in [c3,c4] :
#	print("")
#	
#	for x,y in zip(c.roi.getPolygon().xpoints,c.roi.getPolygon().ypoints) :
#		print("{0}, {1}      \t{2}, {3}".format(x*0.6252,y*0.6252,x,y))

#for i in range(len(Dmel_VLs.dataDirs)) :
	#print(Dmel_VLs.dataDirs[i])	

#Dmel_VLs.runAll([0,1,2,3])

if len(Experiment.insts) > 0 :
	e = Experiment.insts[0]
else :
	e = Dmel_VLs.make(1)

	
#e = Dmel_VLs.getInst(1)
#Dmel_VLs.closeAllExper()

#Dmel_VLs.runAll([0,1,2,3])

if False :
	if len(Experiment.insts) > 0 :
		#e = Dmel_VLs.getInst(0)
		e = Experiment.insts[0]
	else :
		e = Experiment.experConstructEverything(File(Dmel_VLs.dataDirPath + Dmel_VLs.dataDirs[3]), Dmel_VLs.nucFileSuf, Dmel_VLs.nucHeadings, Dmel_VLs.cellFileSuf, Dmel_VLs.cellHeadings) 
	
	rm = RoiManager.getRoiManager()
	
	
	for h in e.hemisegs[0:2] :
		pass#h.hyp.show()
	for c in e.cells[0:4] :
		#rm.addRoi(c.roiForH)
		#print(c.data)
		temp = []
		for item in c.data.items() :
			temp.append("{0}:{1}, ".format(item[0],str(item[1])))
		temp = sorted(temp)
		for line in temp : print (line),
		print("\n")
	
		
	for c in e.cells :
		if c.data["Volume 1"] == 0 :
			pass#print(c.data)
	



