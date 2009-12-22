"""
determines branching times from nexus tree
"""

from pylab import *
from dendropy import datasets
import copy, pdb, dendropy

def truncate_and_add_distToRoot(tree):
	""" calculates distance to root and determines whether a node 
	should be included in the analysis (branch involves male & clade b only)
	
	returns list of branching times and sampling times for male and 
	clade b nodes
	"""
	for tip in tree.leaf_iter():
		l = tip.taxon.label
		try:
			if (not "f" in l) and (not "F" in l) and (not "b" in l) and (not "C02301am" in l):
				tip.include = True
			else:
				tip.include = False
		except:
			pdb.set_trace()
	#
	for node in tree.postorder_node_iter():
		node.age = node.distance_from_root() #DONE check tree functions
		if not node.is_leaf():
			ch = node.child_nodes()
			if ch[0].include == False or ch[1].include == False:
				node.report_branching_time = False
			else: 
				node.report_branching_time = True
			if ch[0].include == True or ch[1].include == True:
				node.include = True
			else:
				node.include = False
			#
		#
	branchingTimes = list()
	sampleTimes = list()
	for node in tree.internal_nodes():
		if node.report_branching_time:
			branchingTimes.append(node.age)
	for node in tree.leaf_iter():
		if node.include:
			sampleTimes.append(node.age)
	#
	return branchingTimes, sampleTimes
	#
#

if __name__=='__main__':
	d = datasets.Dataset()
	d.read( open("May09SubsetDates.summary", "rU"), "NEXUS" )
	tree = d.trees_blocks[0][0]
	
	# branching times and sampling times: 
	bt,st = truncate_and_add_distToRoot(tree)
	
