'''
Created on May 22, 2009

@author: eoromero
'''

from scipy import stats
exdist = stats.expon
udist = stats.uniform
import networkx as nx
from random import choice
#from scipy import integrate as si
from scipy import array
from scipy import linspace
from scipy.optimize import fmin
from scipy.optimize import fminbound
from scipy import random



def static_ID():
	id = 0
	while True:
		id += 1
		yield id

ID = static_ID().next

class Agent():
	#generator function for agent IDs
	def __init__(self):
		self.i_time = 'na'
		self.infector = 'na'
		self.ID = ID()

class ABM():
	
	def __init__(self):
		self.chi = 6.5
		self.bp = 0.038
		self.bc = 0.001
		self.dp = 1 / 2.
		self.dc = 1 / (12 * 12.)
		self.mu = 1 / (30 * 12.)
		self.n = 10000

		self.time = 0.0
		self.state = {'s' : [], 'p' : [], 'c' : []}
		self.tree = nx.LabeledGraph()
		
		self.agent_history = {}
		
		
	def pstruct(self):
		return {'new_s' : {'origin' : None, 
						   'terminus' : 's', 
						   'rate' : self.n * self.mu,
						   'callback' : None,
						   'infector' : None}, 
				'remove_s' : {'origin' : 's',
							  'terminus' : None,
							  'rate' : self.mu,
							  'callback' : None,
							  'infector' : None},
				'remove_p' : {'origin' : 'p',
							  'terminus' : None,
							  'rate' : self.mu,
							  'callback' : None,
							  'infector' : None},
				'remove_c' : {'origin' : 'c',
							  'terminus' : None,
							  'rate' : self.mu,
							  'callback' : None,
							  'infector' : None},
				'incidence_p' : {'origin' : 's',
							     'terminus' : 'p',
							     'rate' : self.chi * self.bp,
							     'callback' : self.primary_callback,
							     'infector' : 'p'},
				'incidence_c' : {'origin' : 's',
							     'terminus' : 'p',
							     'rate' : self.chi * self.bc,
							     'callback' : self.chronic_callback,
							     'infector' : 'c'},
				'p_to_c' : {'origin' : 'p',
							'terminus' : 'c',
							'rate' : self.dp,
							'callback' : None,
							'infector' : None},
				'c_to_death' : {'origin' : 'c',
							    'terminus' : None,
							    'rate' : self.dc,
							    'callback' : None,
							    'infector' : None}}

	def primary_callback(self):
		return float(len(self.state['p'])) / (len(self.state['s']) + len(self.state['p']) + len(self.state['c']))
		
	def chronic_callback(self):
		return float(len(self.state['c'])) / (len(self.state['s']) + len(self.state['p']) + len(self.state['c']))
	
	def init_agents(self, sednum=10):
		for i in range(self.n):
			a = Agent()
			self.state['s'].append(a)
			self.agent_history[a] = [['new_s', 0.0]]
		#set a node as the root and link each seed to the root
		root = Agent()
		root.i_time = 0.0
		self.tree.add_node(root)
		for i in range(sednum):
			a = Agent()
			a.i_time = 0.0
			self.agent_history[a] = [['new_s', 0.0], ['incident_p', 0.0]]
			self.state['p'].append(a)
			self.tree.add_node(a)
			self.tree.add_edge(root, a, data=0.0)
	
	def exact_step(self, add_to_tree=True):
		p = self.pstruct() 
		total_rate = 0.0
		i = 0
		#go through the probability structure and calculate the cumulative
		#rate for each event and add a 'cumrate' variable to the pstruct 
		cumrate = 0.0
		for k, v in p.items():
			if v['origin'] == None: state = 1 
			else: state = len(self.state[v['origin']])
			if v['callback'] == None: callback = 1
			else: callback = v['callback']()
			rate = state * v['rate'] * callback
			cumrate += rate
			v['cumrate'] = cumrate
			if i == len(p) - 1 : total_rate = cumrate
			i += 1
		#update the current time
		self.time = self.time + exdist.rvs(loc=0, scale=(1/total_rate))
		#if wp~U(0,1) is bound by (left, right) then that's it
		wp = udist.rvs() * total_rate
		left = 0.0
		name = 'name'
		for k, v in p.items():
			right = v['cumrate'] 
			if left < wp and wp <= right:
				name = k
				break
			left = right
		#the event is named name, now record execute it
		#mass is conserved, so no origin means zeroth order reaction
		if p[name]['origin'] == None:
			agent = Agent()
			self.state[p[name]['terminus']].append(agent)
			self.agent_history[agent] = [['new_s', self.time]]
		#mass is conserved, so no terminus means removal event
		elif p[name]['terminus'] == None:
			agent = self.random_agent(p[name]['origin'])			
			self.state[(p[name]['origin'])].remove(agent)
			self.agent_history[agent].append([name, self.time])
		#otherwise just moves a dude from one state to another 
		else:
			#if the event is infection add guys to the infection tree
			agent = self.random_agent(p[name]['origin'])
			self.agent_history[agent].append([name, self.time])
			if p[name]['infector'] != None and add_to_tree == True:
				agent.i_time = self.time
				infector = self.random_agent(p[name]['infector'])
				self.agent_history[infector].append([name, self.time])
				#self.tree.add_node(agent)
				#self.tree.add_node(infector) #nx does not complain if a node is added twice
				self.tree.add_edge(infector, agent, data=(agent.i_time - infector.i_time))
			self.state[(p[name]['origin'])].remove(agent)
			self.state[(p[name]['terminus'])].append(agent)
			
	def random_agent(self, state):
		""" get a uniform random agent from state state """
		rndint = random.randint
		return self.state[state][rndint(0, len(self.state[state]))]
		
	def state_count(self):
		""" returns the count of the state vector """
		cstate = []
		for i in self.state:
			cstate.append(len(i))
		return cstate
	
	def print_state(self):
		""" print the state of the system to the std.out """
		print self.time, len(self.state['s']), len(self.state['p']), len(self.state['c']) 
	
	def print_prevalence(self):
		""" prints the prevalence of the current state to the std. out """
		s = len(self.state['s'])
		p = len(self.state['p'])
		c = len(self.state['c'])
		print self.time, float(p + c) / (s + p + c)
	
	def run_to(self, stop, stdout=False):
		self.init_agents(10)
		i = 0
		while self.time < stop:
			self.exact_step()
			if stdout == True and i % 1000 == 0:
				self.print_prevalence()
			i += 1

#import cPickle 
#for i in range(15, 16):
#	f = open('/home/ethan/Dropbox/abm%u.pkl' % i, 'w')
#	abm=ABM()
#	abm.run_to(5000, True)
#	cPickle.dump(abm, f)
#	f.close()
	

	

	


	