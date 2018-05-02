from ij.plugin.frame import RoiManager
from ij.measure import ResultsTable

from ij import IJ, ImageStack, ImagePlus

from vlms import Experiment
from vlms.utilities import Functions

from java.io import File

import time

#print(Experiment.insts)
if len(Experiment.insts) == 0 :
	e = Experiment.experConstructEverything(File(dataPath + dataDirs[0]),"butts",["Area"])
else :
	e = Experiment.insts[0]
#print(e)



s = time.clock()
time.sleep(5)
e = time.clock() - s
print(str(e) + " = 5sec")



