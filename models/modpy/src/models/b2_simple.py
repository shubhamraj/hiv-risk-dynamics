'''
Created on May 22, 2009

@author: eoromero
'''

from scipy import stats
exdist = stats.expon
udist = stats.uniform
import networkx as nx
from random import choice
from psyco.classes import *
from scipy import integrate as si
from scipy import array
from scipy import linspace
from scipy.optimize import fmin
from scipy.optimize import fminbound
		
#psyco.full()
#psyco.profile()

class Params(psyobj):
	
	def __init__(self):
		self.c_g = 5.
		self.c_h = 10.
		self.t_in_g = 10.
		self.t_in_h = 120.
		self.t_in_l = 0.25
		self.t_in_p = 1.
		self.t_in_r = 120.
		self.n = 10000.
		self.t_atrisk = 25*12.
		self.time = 0.0
		
		self.pie = 0.
		self.rie = 0.
		
		self.b_p = 0.35
		self.b_r = 0.008
		
		self.into_g = (self.n / self.t_atrisk) * (self.t_in_g / (self.t_in_g + self.t_in_h))
		self.into_h = (self.n / self.t_atrisk) * (1 - (self.t_in_g / (self.t_in_g + self.t_in_h)))
		
		self.pstruct = {}
		self.ev = { 'to g':1/self.t_in_h,
				    'to h':1/self.t_in_g,
				    'l to p':1/self.t_in_l,
					'p to r':1/self.t_in_p,
					'r to d':1/self.t_in_r,
					'entry_h':self.into_h,
					'entry_g':self.into_g,
					'exit':1/self.t_atrisk,
					'inf ph':self.b_p * self.c_h,
					'inf rh':self.b_r * self.c_h,
					'inf pg':self.b_p * self.c_g,
					'inf rg':self.b_r * self.c_g }
	
		
	def update_pstruct(self,state,t):
		""" returns a dict of dicts of all state connections
			'value': the linear change in state over time t 
			'trans': the state variables affected by that change
			'max': the number of events to exaustion for each flow """
		
		ev = self.ev
			
		ec_freq_pg = state[2] * (state[0]/sum(state[0:4]))
		ec_freq_rg = state[3] * (state[0]/sum(state[0:4]))
		ec_freq_ph = state[6] * (state[5]/sum(state[5:7]))
		ec_freq_rh = state[7] * (state[5]/sum(state[5:7]))			
		
		self.pstruct = {'mu_s_g':{'value':state[0] * ev['exit'] * t, 'trans':[-1,0,0,0,0,0,0,0], 'max':'na'},
						'mu_l_g':{'value':state[1] * ev['exit'] * t, 'trans':[0,-1,0,0,0,0,0,0], 'max':'na'},
						'mu_p_g':{'value':state[2] * ev['exit'] * t, 'trans':[0,0,-1,0,0,0,0,0], 'max':'na'},
						'mu_r_g':{'value':state[3] * ev['exit'] * t, 'trans':[0,0,0,-1,0,0,0,0], 'max':'na'},
						'mu_s_h':{'value':state[4] * ev['exit'] * t, 'trans':[0,0,0,0,-1,0,0,0], 'max':'na'},
						'mu_l_h':{'value':state[5] * ev['exit'] * t, 'trans':[0,0,0,0,0,-1,0,0], 'max':'na'},
						'mu_p_h':{'value':state[6] * ev['exit'] * t, 'trans':[0,0,0,0,0,0,-1,0], 'max':'na'},
						'mu_r_h':{'value':state[7] * ev['exit'] * t, 'trans':[0,0,0,0,0,0,0,-1], 'max':'na'},
						'ent_g':{'value':self.into_g * t, 'trans':[1,0,0,0,0,0,0,0], 'max':'na'},
						'ent_h':{'value':self.into_h * t, 'trans':[0,0,0,0,1,0,0,0], 'max':'na'},
						'l_g':{'value':state[1] * ev['l to p'] * t, 'trans':[0,-1,1,0,0,0,0,0], 'max':state[1]},
						'p_g':{'value':state[2] * ev['p to r'] * t, 'trans':[0,0,-1,1,0,0,0,0], 'max':state[2]},
						'r_g':{'value':state[3] * ev['r to d'] * t, 'trans':[0,0,0,-1,0,0,0,0], 'max':state[3]},
						'l_h':{'value':state[5] * ev['l to p'] * t, 'trans':[0,0,0,0,0,-1,1,0], 'max':state[5]},
						'p_h':{'value':state[6] * ev['p to r'] * t, 'trans':[0,0,0,0,0,0,-1,1], 'max':state[6]},
						'r_h':{'value':state[7] * ev['r to d'] * t, 'trans':[0,0,0,0,0,0,0,-1], 'max':state[7]},
						's_h->g':{'value':state[0] * ev['to h'] * t, 'trans':[-1,0,0,0,1,0,0,0], 'max':state[0]},
						'l_h->g':{'value':state[1] * ev['to h'] * t, 'trans':[0,-1,0,0,0,1,0,0], 'max':state[1]},
						'p_h->g':{'value':state[2] * ev['to h'] * t, 'trans':[0,0,-1,0,0,0,1,0], 'max':state[2]},
						'r_h->g':{'value':state[3] * ev['to h'] * t, 'trans':[0,0,0,-1,0,0,0,1], 'max':state[3]},
						's_g->h':{'value':state[4] * ev['to g'] * t, 'trans':[1,0,0,0,-1,0,0,0], 'max':state[4]},
						'l_g->h':{'value':state[5] * ev['to g'] * t, 'trans':[0,1,0,0,0,-1,0,0], 'max':state[5]},
						'p_g->h':{'value':state[6] * ev['to g'] * t, 'trans':[0,0,1,0,0,0,-1,0], 'max':state[6]},
						'r_g->h':{'value':state[7] * ev['to g'] * t, 'trans':[0,0,0,1,0,0,0,-1], 'max':state[7]},
						'inf_p_g':{'value':ec_freq_pg * ev['inf pg'] * t, 'trans':[-1,1,0,0,0,0,0,0], 'max':state[0]},
						'inf_r_g':{'value':ec_freq_rg * ev['inf rg'] * t, 'trans':[-1,1,0,0,0,0,0,0], 'max':state[0]},
						'inf_p_h':{'value':ec_freq_pg * ev['inf ph'] * t, 'trans':[0,0,0,0,-1,1,0,0], 'max':state[4]},
						'inf_r_h':{'value':ec_freq_rg * ev['inf rh'] * t, 'trans':[0,0,0,0,-1,1,0,0], 'max':state[4]}}

class ABM(Params, psyobj):
	
	def __init__(self):
		Params.__init__(self)
		self.tree = nx.DiGraph(weighted=True)
		self.sg = []
		self.lg = []
		self.pg = []
		self.rg = []
		self.sh = []
		self.lh = []
		self.ph = []
		self.rh = []
		self.state = [self.sg, self.lg, self.pg, self.rg, self.sh, self.lh, self.ph, self.rh]
		self.cg_factor = 10
	
	def init_agents(self, sednum=5):
		fract_g = int(self.n * self.into_g / (self.into_g + self.into_h))
		fract_h = int(self.n * self.into_h / (self.into_g + self.into_h))
		for i in range(fract_g):
			self.sg.append(self.Agent())
		for i in range(fract_h):
			self.sh.append(self.Agent())
		for i in range(sednum):
			a = self.Agent()
			a.i_time = 0.0
			self.lh.append(a)
	
	def exact_step(self):
		self.update_pstruct(self.state_count(), t=1)
		p = self.pstruct
		sort_key = sorted(p.keys())
		cumrates = []
		cumrates.append(0.0)
		for i, k in enumerate(sort_key):
			cumrate = 0
			for s in sort_key[0:i+1]:
				cumrate += p[s]['value']
			cumrates.append(cumrate)
		totalrate = cumrates[-1]
		self.time = self.time + exdist.rvs(loc=0,scale=(1/totalrate))
		wp = udist.rvs() * totalrate
		eind = -1
		for i, r in enumerate(cumrates):
			if r > wp:
				eind = i - 1
				break
		event_key = sort_key[eind]
		if event_key.split('_')[0] == 'inf':
			if event_key('_')[1] == 'p' and event_key('_')[2] == 'g':
				inf = self.choice(self.p_g)
				sus = self.choice
			

	
	def state_count(self):
		""" returns the count of the state vector """
		cstate = []
		for i in self.state:
			cstate.append(len(i))
		return cstate
	
	class Agent(psyobj):
		def __init__(self):
			i_time = 'na'
				
	
class DCM(Params, psyobj):
	
	def __init__(self):
		Params.__init__(self)
		self.ode = si.odeint
		self.fminbound = fminbound 
		self.array = array 
		self.linspace = linspace
		self.fmin = fmin
	
	def dX_dt(self, Y, t=0):
		""" Function that can be passes to various scipy integrators,
			returns the state, Y, at time t """
		self.update_pstruct(Y, t)
		flows = [Y]
		for k, v in self.pstruct.items():
			flow = []
			for i, f in enumerate(v['trans']):
				flow.append(f * v['value'])
			flows.append(self.array(flow))
		aY = self.array(flows)
		#print aY
		return sum(aY)
		

dcm = DCM()
		
