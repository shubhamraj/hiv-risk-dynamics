

							
'''
Created on May 2, 2009

random methods for networkx graphs 

@author: eoromero
'''
import pickle
import cPickle
import random
import os
import sys
sys.path.append('/home/ethan/workspace/modpy/src/')
from models import si
import networkx as nx
import pylab as P
from scipy import cluster, array, zeros, stats
import itertools as it
import numpy as np
import gc

ABM = si.ABM
Agent = si.Agent

def y30_sample(lineage, history, pr):
	exits = ['c_to_death', 'remove_s', 'remove_p', 'remove_c']
	inf_modes = ['incidence_p', 'incidence_c']
	
	infected = []
	sampled = []

	for k, v in history.items():
				
		tripple = []
		right = 5000
		to_add = True
		
		for event in v:		
			if event[0] in inf_modes and to_add is True:
				tripple.append(k)
				to_add = False
			if event[0] in exits and to_add is False:
				right = event[1]
				tripple.append(right)
			
		if len(tripple) is 1: 
			tripple.append(5000)		
		
		if len(tripple) is 2:
			infected.append(tripple)
		
		to_add = True
	
	s_time = 12 * 30		
	
	for a in infected:
		if a[0].i_time < s_time and a[1] > s_time and random.random() < pr:
			sampled.append(a[0])

	return sampled, s_time

def sample(abm, pr):
	""" samples out extant agents at the point of 
		maximum incidence with probability pr 
		from a pickled model """
		
	print "begin sampling"
	
	exits = ['c_to_death', 'remove_s', 'remove_p', 'remove_c']
	inf_modes = ['incidence_p', 'incidence_c']
			
	tree = abm.tree
	agents = abm.agent_history
	sampled = []
	
	bins, x1, x2 = P.hist(map(lambda x: x.i_time, tree.nodes()), bins=5000)
	s_time = bins.tolist().index(bins.max())
	s_time += 1
	
	for agent, history in agents.items():
		entry = 5002
		exit = 5001
		for event in history:
			if event[0] in inf_modes:
				entry = event[1]
			if event[0] in exits:
				exit = event[1]
		if entry <= s_time and exit >= s_time and random.random() < pr:
			sampled.append(agent)
	print "sampled", len(sampled), "agents"
	return sampled, s_time

def lin_sample(lineage, history, pr):
	exits = ['c_to_death', 'remove_s', 'remove_p', 'remove_c']
	inf_modes = ['incidence_p', 'incidence_c']
	
	infected = []
	sampled = []

	
	for k, v in history.items():
		
		tripple = []
		right = 5000
		to_add = True
		
		for event in v:		
			if event[0] in inf_modes and to_add is True:
				tripple.append(k)
				to_add = False
			if event[0] in exits and to_add is False:
				right = event[1]
				tripple.append(right)
			
		if len(tripple) is 1: 
			tripple.append(5000)		
		
		if len(tripple) is 2:
			infected.append(tripple)
		
		to_add = True
	
	bins, x1, x2 = P.hist(map(lambda x: x[0].i_time, infected), bins=5000)
	s_time = bins.tolist().index(bins.max())
	s_time += 1		
	
	for a in infected:
		if a[0].i_time < s_time and a[1] > s_time and random.random() < pr:
			sampled.append(a[0])

	return sampled, s_time
	
def clusters_at(tree, sampled, cutpoint):
	""" returns a list of lists of clusters occuring after cutpoint """
	branches = branches_sampled(tree, sampled)
	branches = list(branches)
	branches.sort(key=lambda x: x.i_time)
	clusters = []
	
	for branch in branches:
		if branch.i_time > cutpoint:
			children = all_children_of(tree, branch)
			if branch in sampled:
				children.append(branch)
			cluster = []
			for child in children:
				if child in sampled:
					cluster.append(child)
					sampled.remove(child)
			clusters.append(cluster)
	
	return clusters
	

def brown_clusters(tree, sampled, s_time):
	""" returns a list of lists of clusters in tree
		according to the standard in Brown et al. """
	cbound = s_time - 12
	clusterlist = []
	clustered = []
	
	bpoints = branches_sampled(tree, sampled)
	
	for bp in sorted(bpoints, key=lambda x: x.i_time):

		if bp.i_time >= cbound:
			cluster = []
			childs = all_children_of(tree, bp)
			print childs
			for cbp in childs:
				if cbp in sampled and cbp not in clustered:
					cluster.append(cbp)
					clustered.append(cbp)
			clusterlist.append(cluster)
				
	print len(clustered), clustered
	return clusterlist						
		
def all_children_of(tree, A):
	""" returns all the children of A """
	front = []
	children = []
	nbrs = nx.neighbors(tree, A)
	
	for n in nbrs:		
		if n.i_time > A.i_time:
			front.append(n)
			children.append(n) 

	if len(front) < 1:
		return children
	
	while 1:
		new_front = []
		if len(front) < 1:
			return children
		for a in front:
			nbrs = nx.neighbors(tree, a)
			for n in nbrs:
				if n.i_time > a.i_time:
					new_front.append(n) 
					children.append(n)
		front = new_front

def backtrace(tree, node):
	""" returns the set of agents begining with node 
		back to the root of the tree """
	trace = []
	searching = True
	trace.append(node)
	while searching:
		pre_node = sorted(nx.neighbors(tree, node), key=lambda x: x.i_time)[0]
		trace.append(pre_node)
		if node.i_time <= 0:
			searching = False
		node = pre_node
	return set(trace)

def branch_pair(tree, A, B):
	"""returns the branch point of a pair of agents """
	
	path1 = backtrace(tree, A)
	path2 = backtrace(tree, B)
	shared = path1.union(path2) - path1.intersection(path2)
	bt = min(shared, key=lambda x: x.i_time)
	return bt

def branches_sampled(tree, sampled):	
	""" returns the branch points for the sampled subset """
	
	print "calculating branch times for", len(sampled), "agents ..."
	branch_time = set([])
	for i1, a1 in enumerate(sampled):
		print len(sampled) - i1
		for i2, a2 in enumerate(sampled):
			if i1 < i2:
				path1 = backtrace(tree, a1)
				path2 = backtrace(tree, a2)

				shared = path1.union(path2) - path1.intersection(path2)
				bt = min(shared, key=lambda x: x.i_time)
				branch_time.add(bt)
	print "done"
	return branch_time
	
def convert_to_lineage():
	inf_modes = ['incidence_p', 'incidence_c']
	exits = ['c_to_death', 'remove_s', 'remove_p', 'remove_c']
	
	parent = "/home/ethan/Dropbox/pkl/"
	index = 0
	
	for file in os.listdir(parent):
		print file
		infile = open(parent + file, 'r')
		
		lineage = nx.DiGraph(weighted=True)		
		abm = cPickle.load(infile)
		tree = abm.tree
		history = abm.agent_history
		infected = sorted(tree.nodes(), key=lambda x: x.i_time)
		terminal_map = {}
		
		for i in infected:
			try: 
				a = history[i]
			except KeyError:
				infected.remove(i)
	
		for i in infected:
			out = []
			out.append(i)
			nei = sorted(tree.neighbors(i), key=lambda x: x.i_time)
			for n in nei:
				if n.i_time > i.i_time:
					out.append(n)
			
			end_time = 5000
			
			terminus = Agent()
			terminus.i_time = end_time 
			terminus.ID = i.ID
			
			for event in history[i]:
					if event[0] in exits:
						terminus.i_time = event[1]
		
			out.append(terminus)
			terminal_map[i] = terminus 
			
			for x in range(len(out) - 1):
				lineage.add_edge(out[x], out[x + 1], data=abs(out[x].i_time - out[x + 1].i_time))
		
		dic = {'lineage' : lineage, 'history' : history, 'terminal map' : terminal_map}
		out = open(parent + 'lin' + str(index) + '.pkl', 'w')
		cPickle.dump(dic, out)
		print nx.number_connected_components(lineage.to_undirected()), nx.number_connected_components(tree)
		infile.close()
		out.close()		
		index += 1
		

def get_terminus(lineage, internal):
	""" returns the terminal node of an internal branch """
	
	pair = []
	pair.append(internal)
	
	for n in lineage.nodes():
		if n.ID == internal.ID and n != internal:
			pair.append(n)
	
	return pair
	
def lin_backtrace(lineage, terminal_map, node):
	#pair = get_terminus(lineage, node)
	#pair.sort(key=lambda x: x.i_time)
	bt = []
	index = terminal_map[node]
	
	while 1:
		pindex = lineage.predecessors(index)
		if pindex != []:
			bt.append(pindex[0])
			index = pindex[0]
		else:
			return set(bt)
		

def lin_branches(lineage, terminal_map, history, s_time, sampled):	
	""" returns the branch points for the sampled subset 
		for a lineage type tree """
	sampled.sort(key=lambda x: x.i_time)
	btimes = []

	#use as universal ancestor 
	t = Agent()
	t.i_time = 0.0

	for i1, s1 in enumerate(sampled):
		for i2, s2 in enumerate(sampled):
			if i2 > i1:
				bt1 = lin_backtrace(lineage, terminal_map, s1)
				bt2 = lin_backtrace(lineage, terminal_map, s2)
				ibt = bt1.intersection(bt2)
				if len(ibt) > 0:
					time = max(ibt, key=lambda x: x.i_time)
				else:
					time = t
				btimes.append(time)

	return list(set(btimes))
	
def stage_at(history, node, s_time):
	""" returns the stage, 'p' for primary and 'c' for chronic at s_time """
	stage = 'p' 
	
	for e in history[node]:
		if e[0] == 'p_to_c' and e[1] < s_time:
			stage = 'c'
	
	return stage

def clusters_at(lineage, sampled, branches, s_time, genetic, temporal=''):
	""" returns a list of cluster sizes """
	lineage = add_false_root(lineage)
	valid_branches = [x for x in sampled if x.i_time > (s_time - genetic)]
	out_clust = []	
	
	for vb in sorted(valid_branches, key=lambda x: x.i_time):
		clu = [x for x in all_children_of(lineage, vb) if x in sampled]
		sampled = [x for x in sampled if x not in clu]
		
		if len(clu) > 0:
			out_clust.append(len(clu))
			
	for s in sampled:
		out_clust.append(1)

	return out_clust

def add_false_root(lineage):
	sn = [x for x in lineage.nodes() if x.i_time == 0.0]
	if len(sn) > 0:
		r = Agent()
		r.i_time = 0.0
		for n in sn:
			lineage.add_edge(r, n, data=0.0)
	
	return lineage 

def pr_clustering(clusters):
	return sum([x for x in clusters if x > 1]) / float(sum(clusters))

def branch_main():	
	parent = "/home/ethan/hiv/papers/jidletter/pkl/full/"
	brch_mi = open("/home/ethan/hiv/papers/jidletter/branches_mi", 'w')
	brch_30 = open("/home/ethan/hiv/papers/jidletter/branches_30y", 'w')
		
	for file in os.listdir(parent):
		print file 
		
		infile = open(parent + file, 'r')
		d = cPickle.load(infile)
		infile.close()
		
		lineage = d['lineage']
		history = d['history']
		branch_mi = d['branches_maxinc']
		branch_30 = d['branches_30y']
		smp_mi = d['samples_maxinc']
		smp_30 = d['samples_30y']
		s_time = d['maxinc_time']
		
		for pr in smp_mi.keys():
			p_mi, p_30 = 0, 0
			for a in smp_mi[pr]:
				if stage_at(history, a, s_time) == 'p': p_mi += 1
			for a in smp_30[pr]:
				if stage_at(history, a, s_time) == 'p': p_30 += 1
			brch_mi.write('%f,%u,%u;' % (pr, p_mi, s_time))
			brch_30.write('%f,%u,%u;' % (pr, p_30, 12 * 30))
			for b in branch_mi[pr]:
				brch_mi.write('%f,' % b.i_time)
			for b in branch_30[pr]:
				brch_30.write('%f,' % b.i_time)
			brch_mi.write('\n')
			brch_30.write('\n')
			brch_mi.flush()
			brch_30.flush()

	brch_mi.close()
	brch_30.close()
	
def infectors_stage(history, agent):
	inf_events = ['incidence_p', 'incidence_c']
	hist = history[agent]
	for event in hist:
		if event[0] in inf_events:
			return event[0].split('_')[1]
		
	

def clust_main():			
	parent = "/home/ethan/hiv/papers/jidletter/"
	
	outmi = open(parent + 'sumary.mi', 'w')
	outmi.write('freq,cut,p.clu,mean.clu,med.clu,std.clu,act.pri\n')
	out3 = open(parent + 'sumary.30y', 'w')
	out3.write('freq,cut,p.clu,mean.clu,med.clu,std.clu,act.pri\n')
	
	inf_mi, inf_3 = {}, {}
	clu_mi, clu_3 = {}, {}
	
	cuts = [6]
	cuts.extend([(x+1)*12 for x in range(19)])

	for freq in np.linspace(0.05, 1.0, 20):
		inf_mi[freq], inf_3[freq] = [], []
		
	infile = open(parent + "pkl/full/" + "lin0.pkl.full", 'r')
	data = cPickle.load(infile)
	infile.close()
	
	c_mi = data['clu_mi']
	c_3 = data['clu_30y']
	
	for inst in c_mi:
		freq = inst[0]
		cut = inst[1]
		
		if not clu_mi.has_key(freq):
			clu_mi[freq] = {}
		
		if not clu_mi[freq].has_key(cut):
			clu_mi[freq][cut] = []
	
	for inst in c_3:
		freq = inst[0]
		cut = inst[1]
		
		if not clu_3.has_key(freq):
			clu_3[freq] = {}
		
		if not clu_3[freq].has_key(cut):
			clu_3[freq][cut] = []
		
		
	for file in os.listdir(parent + "pkl/full/"):
		print file
		
		infile = open(parent + "pkl/full/" + file, 'r')
		data = cPickle.load(infile)
		infile.close()
		
		history = data['history']
		smp_mi = data['samples_maxinc']
		smp_3 = data['samples_30y']
		c_mi = data['clu_mi']
		c_3 = data['clu_30y']
		
		for freq in np.linspace(0.05, 1.0, 20):
			for mi in smp_mi[freq]:
				inf_mi[freq].append(infectors_stage(history, mi))
			for th in smp_3[freq]:
				inf_3[freq].append(infectors_stage(history, th))
		
		for inst in c_mi:	
			freq = inst[0]
			cut = inst[1]
			
			for i, tok in enumerate(inst):
				if i > 1:
					clu_mi[freq][cut].append(int(tok))
					
		for inst in c_3:	
			freq = inst[0]
			cut = inst[1]
			
			for i, tok in enumerate(inst):
				if i > 1:
					clu_3[freq][cut].append(int(tok))
	
	for k, v in inf_mi.items():
		pcount = 0
		for tok in v:
			if tok == 'p': pcount += 1
		sk = str(k)
		sk = sk + '00000000000000'
		inf_mi[sk[0:8]] = float(pcount)/len(v)

	for k, v in inf_3.items():
		pcount = 0
		for tok in v:
			if tok == 'p': pcount += 1
		sk = str(k)
		sk = sk + '00000000000000'
		inf_3[sk[0:8]] = float(pcount)/len(v)

	
	for k, v in clu_mi.items():
		for k2, v2 in v.items():
			prclust = pr_clustering(v2)
			mean = stats.mean(v2) 
			median = stats.median(v2)
			std = stats.tstd(v2)

			outmi.write('%s,%s,%f,%f,%f,%f,%f\n' % (k, k2, prclust, mean, median, std, inf_mi[k]))
	
	for k, v in clu_3.items():
		for k2, v2 in v.items():
			prclust = pr_clustering(v2)
			mean = stats.mean(v2) 
			median = stats.median(v2)
			std = stats.tstd(v2)

			out3.write('%s,%s,%f,%f,%f,%f,%f\n' % (k, k2, prclust, mean, median, std, inf_3[k]))
			
#		for k,v in clu_mi.items():
#			for k2,v2 in v.items():
#				print k, k2, v2
#		for k,v in clu_3.items():
#			for k2,v2 in v.items():
#				print k, k2, v2		
			
def pickelize(filearg):
	p_parent = "/home/ethan/hiv/papers/jidletter/pkl/full/"
	cm_parent = "/home/ethan/hiv/papers/jidletter/clu/mi/"
	c3_parent = "/home/ethan/hiv/papers/jidletter/clu/30y/"
	
	def sort_funct(name):
		number = ''
		for c in name:
			if c.isdigit():
				number += c
		return int(number)
	
	pkls = sorted(os.listdir(p_parent), key=sort_funct)
	clu_m = sorted(os.listdir(cm_parent), key=lambda x: int(x.split('.')[1]))
	clu_3 = sorted(os.listdir(c3_parent), key=lambda x: int(x.split('.')[1]))
	
	for i, file in enumerate(pkls):
		print file, clu_m[i], clu_3[i]
		
		cmdat, c3dat = [], []
		
		filep = open(p_parent + file, 'r')
		filem = open(cm_parent + clu_m[i], 'r')
		file3 = open(c3_parent + clu_3[i], 'r')
		data = cPickle.load(filep)
		
		linesm = filem.readlines()
		lines3 = file3.readlines()
		
		filep.close()
		filem.close()
		file3.close()
		
		for lm in linesm:
			cmdat.append([x.strip() for x in lm.split(',')])
		for l3 in lines3:
			c3dat.append([x.strip() for x in l3.split(',')])	
		
		data['clu_mi'] = cmdat
		data['clu_30y'] = c3dat
		
		cPickle.dump(data, open(p_parent + file, 'w'))


def clust_jk():
	f = open('/home/ethan/hiv/ober_meeting/tree3.0', 'r')
	t = cPickle.load(f)
	f.close()
	n = t.nodes()
	n.sort(key=lambda x: x.i_time)
	t = t.to_undirected()
	
	sample = []
	
	for x in n:
		if x.i_time > 2000:
			sample.append(x)
		if len(sample) == 500:
			break
	
	dist = []
	for i in range(500):
		dist.append([])
		
	for i1, a in enumerate(sample):
		print i1
		for i2, b in enumerate(sample):
			if i2 > i1:
				 d = distance(t, a, b)
				 dist[i1].append(d)
				 dist[i2].append(d)

	cPickle.dump(dist, open('/home/ethan/hiv/ober_meeting/dist3.pkl', 'w'))

def nsd():
	f = open('/home/ethan/hiv/ober_meeting/dist3.pkl', 'r')
	dist = cPickle.load(f)
	f.close()
	ddict = {}
	for cut in [0.5, 2.5, 4.5, 6.5, 8.5, 10.5, 12.5, 14.5, 16.5, 18.5]:
		ddict[cut] = []
		for row in dist:
			nei = 1
			for col in row:
				if col < cut:
					nei += 1
			ddict[cut].append(nei)
	out = open('/home/ethan/hiv/ober_meeting/plot3', 'w')
	for k,v in ddict.items():
		for datum in v:
			out.write('%f,%u\n' % (k, datum))
			
	out.close()
	
	
	
def distance(t, a, b):
	bta = backtrace(t, a)
	btb = backtrace(t, b)
	u = bta.union(btb)
	i = u.intersection(bta)
	root = sorted(i, key=lambda x: x.i_time, reverse=True)[0]

	return float((a.i_time - root.i_time) + (b.i_time - root.i_time))
	
	
nsd()
	


