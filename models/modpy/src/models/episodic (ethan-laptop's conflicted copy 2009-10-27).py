'''
Created on Oct 7, 2009

Defines the parameters for the episodic risk model. Asymptomatic and final 
Stage transition rate and risk ratios default to Hollingsworth 
et al. (2008); primary stage values from Pinkerton et al. (?)  

@author: ethan
'''

import math 
import networkx as nx
from random import random as rnd
from scipy import integrate as si
from scipy import array
from numpy import linspace
from scipy.optimize import fmin, fminbound

time_active = 12 * 30.
N = 10000.

Cg = 0.443686
Ch = 3.674185

tin_g = 62.713989
tin_h = 136.183073

D1 = 1 / 2.0
D2 = 1 / (12 * 12.)

B2 = 0.001
p = 38.0
B1 = B2 * p

new_g = (N / time_active) * (tin_g / (tin_g + tin_h))
new_h = (N / time_active) * (tin_h / (tin_g + tin_h))

def static_ID():
	id = 0
	while True:
		id += 1
		yield id


class DCM():
	def dY_dt(self, y, t=0):
		""" function to pass to scipy integrator, should return the change
		    in state, y,  over the interval t """
		    
		#variables
		pSgg = y[0] / float(sum(y))
		pSgh = y[3] / float(sum(y))
		pSh = y[3] / float(y[3] + y[4] + y[5])
		
		#exit flows
		exit_Sg = y[0] * (1 / time_active) * t 
		exit_Pg = y[1] * (1 / time_active) * t
		exit_PPg = y[2] * (1 / time_active) * t
		exit_Sh = y[3] * (1 / time_active) * t
		exit_Ph = y[4] * (1 / time_active) * t
		exit_PPh = y[5] * (1 / time_active) * t
		#episodic flows
		Sg_to_h = y[0] * (1 / tin_g) * t
		Pg_to_h = y[1] * (1 / tin_g) * t
		PPg_to_h = y[2] * (1 / tin_g) * t
		Sh_to_g = y[3] * (1 / tin_h) * t
		Ph_to_g = y[4] * (1 / tin_h) * t
		PPh_to_g = y[5] * (1 / tin_h) * t
		#entry flows
		into_g = new_g * t
		into_h = new_h * t
		#infection flows
		newinf_gg = ((y[1] + y[4]) * B1 + (y[2] + y[5]) * B2) * Cg * pSgg * t
		newinf_gh = ((y[1] + y[4]) * B1 + (y[2] + y[5]) * B2) * Cg * pSgh * t
		newinf_h = (y[4] * B1 + y[5] * B2) * Ch * pSh * t
		#stage progression flows
		Pg_to_PPg = y[1] * D1 * t
		PPg_to_d = y[2] * D2 * t
		Ph_to_PPh = y[4] * D1 * t
		PPh_to_d = y[5] * D2 * t
			
		state = [- exit_Sg - newinf_gg - Sg_to_h + into_g + Sh_to_g,
				 - exit_Pg - Pg_to_PPg - Pg_to_h + newinf_gg + Ph_to_g,
				 - exit_PPg - PPg_to_d - PPg_to_h + Pg_to_PPg + PPh_to_g,
				 - exit_Sh - newinf_gh - newinf_h - Sh_to_g + into_h + Sg_to_h,
				 - exit_Ph - Ph_to_PPh - Ph_to_g + newinf_gh + newinf_h + Pg_to_h,
				 - exit_PPh - PPh_to_d - PPh_to_g + Ph_to_PPh + PPg_to_h]
	
		return state
	
	def state_at(self, t=0.01, tfact=100):
		""" returns the state history upto time t, 
			Y[-1] gives the state at time t
			
			tfact is the number of subdivisions for the integrator"""
		initg = N * new_g / (new_g + new_h)
		inith = N - initg
		Y0 = array([initg, 1.0, 0.0, inith, 1.0, 0.0])
		ticks = linspace(0, t, t * tfact)
		Y = si.odeint(self.dY_dt, Y0, ticks)#, full_output=False)
		return Y
	
	
	def p_funct(self, yi, p, t):
		"""funtion to minimize when looking for parameters that 
		give a specific equlibrium risk, p, at time, t.
		The ordering is Cg, Ch, tin_g, tin_h""" 		
		
		global Cg 
		Cg = abs(yi[0])
		global Ch 
		Ch = abs(yi[1])
		global tin_g 
		tin_g = abs(yi[2])
		global tin_h 
		tin_h = abs(yi[3])
		
		y = state_at(t)
		risk = 1 - (y[-1][0] + y[-1][3]) / sum(y[-1])
		
		return abs(risk - p)
		
	def rnd_pset(self):
		""" returns a list of reasonable random params in the order 
			Cg, Ch, tin_g, tin_h"""
		return [rnd() * 10, rnd() * 10, rnd() * 12 * 15, rnd() * 12 * 15] 
		
	
	def rnd_riskset(self, p, t, tol):
		""" returns the first parameter set that is within tol 
			of the equlibrium risk, p, at time, t, using the 
			downhill simplex method"""	
		yopt = fmin(func=p_funct,
						 x0=rnd_pset(),
						 args=[p, t],
						 xtol=tol)
		return yopt
		
	def sample_from_cube(self, p, t, tol):
		""" returns a random sample from within the sampling hypercube
			that is within +- tol of p at t """	
		minXg, maxXg = 0.001683, 6.612523
		minXh, maxXh = 0.001954, 11.227660
		minTinG, maxTinG = 0.002085, 301.3
		minTinH, maxTinH = 0.06827, 283.60263
		
		global Cg, Ch, tin_g, tin_h 
		
		attempts = 0
		while(True):
			attempts += 1
			Cg = rnd() * (maxXg - minXg) + minXg
			Ch = rnd() * (maxXh - minXh) + minXh
			tin_g = rnd() * (maxTinG - minTinG) + minTinG
			tin_h = rnd() * (maxTinH - minTinH) + minTinH
			
			y=state_at(10000)
			prev = 1 - (y[-1][0] + y[-1][3]) / float(sum(y[-1]))
			
			if abs(prev - p) < tol:
				return [Cg, Ch, tin_g, tin_h], prev, attempts
			
class ABM():
	
	def __init__(self):
		self.state = {"sus_gen" : [],
			 		  "pei_gen" : [],
			 		  "chr_gen" : [],
			 		  "sus_hig" : [],
			 		  "pri_hig" : [],
			 		  "chr_hig" : []}
	
		self.rates = {"new_gen" : {'constant' : new_g, 'state' : None, 'callback' : None},
					  "new_hig" : {'constant' : new_h, 'state' : None, 'callback' : None},
					  "exit_sus_gen" : {'constant' : 1/time_active, 'state' : 'sus_gen', 'callback' : None},
					  "exit_pri_gen" : {'constant' : 1/time_active, 'state' : 'pri_gen', 'callback' : None},
				      "exit_chr_gen" : {'constant' : 1/time_active, 'state' : 'chr_gen', 'callback' : None},
					  "exit_sus_hig" : {'constant' : 1/time_active, 'state' : 'sus_hig', 'callback' : None},
					  "exit_pri_hig" : {'constant' : 1/time_active, 'state' : 'pri_hig', 'callback' : None},
					  "exit_chr_hig" : {'constant' : 1/time_active, 'state' : 'chr_hig', 'callback' : None},
					  "pri->chr_gen" : {'constant' : D1, 'state' : 'pri_gen', 'callback' : None},
					  "chr->dth_gen" : {'constant' : D2, 'state' : 'chr_gen', 'callback' : None},
					  "pri->chr_hig" : {'constant' : D1, 'state' : 'pri_hig', 'callback' : None},
					  "chr->dth_hih" : {'constant' : D2, 'state' : 'chr_hig', 'callback' : None},
					  "sus_hig->gen" : {'constant' : 1/tin_h, 'state' : 'sus_hig', 'callback' : None},
					  "pri_hig->gen" : {'constant' : 1/tin_h, 'state' : 'pri_hig', 'callback' : None},
					  "chr_hig->gen" : {'constant' : 1/tin_h, 'state' : 'chr_hig', 'callback' : None},
					  "sus_gen->hig" : {'constant' : 1/tin_g, 'state' : 'sus_gen', 'callback' : None},
					  "pri_gen->hig" : {'constant' : 1/tin_g, 'state' : 'pri_gen', 'callback' : None},
					  "chr_gen->hig" : {'constant' : 1/tin_g, 'state' : 'chr_gen', 'callback' : None},
					  "inf_pri_gen" : {'constant' : B1 * Cg, 'state' : 'pri_gen', 'callback' : pr_sus_gen},
					  "inf_chr_gen" : {'constant' : B2 * Cg, 'state' : 'chr_gen', 'callback' : pr_sus_gen},
					  "inf_pri_hig" : {'constant' : B1 * Cg, 'state' : 'pri_hig', 'callback' : pr_sus_hig},
					  "inf_chr_hig" : {'constant' : B2 * Cg, 'state' : 'chr_hig', 'callback' : pr_sus_hig}}
		
		self.tree = nx.DiGraph()
	
	def pr_sus_gen(self):
		return float(len(self.state['sus_gen'])) / (len(self.state['sus_gen']) + len(self.state['pri_gen']) + len(self.state['chr_gen']))
	
	def pr_sus_hig(self):
		return float(len(self.state['sus_hig'])) / (len(self.state['sus_hig']) + len(self.state['pri_hig']) + len(self.state['chr_hig']))
	
	def init_agents(self, sednum=10):
		init_gen = N * tin_g / (tin_g + tin_h)
		init_hig = N * tin_h / (tin_g + tin_h)
		#assign high risk agents to the general and high risk pools
		for i in range(init_hig):
			a = Agent()
			self.state['sus_gen'].append(a)
			self.state['sus_hig'].append(a)
		#assign general risk agents to the general risk pool
		for i in range(init_gen):
			a = Agent()
			self.append['sus_gen'].append(a)
			
		#set a node as the root and link each seed to the root
		root = Agent()
		root.i_time = 0.0
		self.tree.add_node(root)
		for i in range(sednum):
			a = Agent()
			a.i_time = 0.0
			self.tree.add_edge(root, a, length=0.0)
	
	def exact_step(self, add_to_tree=True): 
		total_rate = 0.0
		i = 0
		#go through the probability structure and calculate the cumulative
		#rate for each event and add a 'cumrate' variable to the pstruct 
		cumrate = 0.0
		for k, v in self.rates.items():
			if v['callback'] == None:
				rate = v['constant'] * len(self.state[v['state']])
			else:
				rate = v['constant'] * len(self.state[v['state']]) * v['callback']()
			cumrate += rate
			self.rates[k]['cumrate'] = cumrate 
			#if we are at the end, set totalrate to the current cumrate
			if len(v) - 1 == i:
				total_rate = cumrate
			
			i += 1

		#update the current time
		self.time = self.time + expow.rvs(1./total_rate)
		#if wp~U(0,1) is bound by (left, right) then that's it
		wp = udist.rvs() * total_rate
		left = 0.0
		name = 'name'
		for k, v in self.rates.items():
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


class Agent():
	
	def __init__(self, itime):
		self.i_time = itime


m=DCM()
print m.state_at(10, 100)

#out = open('/home/ethan/Dropbox/validcube3.params', 'a')
#attempts = 0		
#for i in range(10000):
#	p, prev, trys = sample_from_cube(0.15, 10000, 0.005)
#	attempts += trys
#	print i, attempts 
#	
#	print p, prev
#
#	out.write('%f,%f,%f,%f\n' % (abs(p[0]), abs(p[1]), abs(p[2]), abs(p[3])))
#	out.flush()
#
#out.close()


#	
#	def intervention_effect(self, effects, t, type="logit", target=0.01):
#		""" if the infection is eliminable returns the approximate intervention
#			efficacy required for elimination, otherwise returns the percent
#			reduction in population risk at time t.
#			
#			effects: a 3 element list of booleans specificing which stages are 
#					intervened upon
#			type: logit returns a continious logit transformed result """
#		self.pie = 0.0
#		self.aie = 0.0
#		self.fie = 0.0
#		y = self.state_at(t)
#		risk = 1 - (y[-1][0] + y[-1][5]) / sum(y[-1])
#		
#		self.pie = 1.0 if effects[0] else 0.0
#		self.aie = 1.0 if effects[1] else 0.0
#		self.fie = 1.0 if effects[2] else 0.0
#				
#		y_i = self.state_at(t)
#		risk_i = 1 - (y_i[-1][0] + y_i[-1][5]) / sum(y_i[-1])
#		
#		print "baseline risk: ", risk, "intervention risk: ", risk_i
#		
#		return_val = 0.0
#		
#		if risk_i < 0.001:
#			#the intevention is below threshold, estimate the 
#			#intervention effect at or near threshold 
#			target = 0.002
#			xret = self.fminbound(self.intervention_funct,
#								args=(effects, t, target),
#								x1=0.0, x2=1.0, 
#								full_output=0,
#								disp = 3,
#								xtol=0.001)
#			return_val = xret
#			return_val = return_val / 2
#		
#		else:
#			#otherwise the return value is one plus the 
#			#percentage decrease in risk normalized to [0,1]
#			return_val = ((risk_i / risk) + 1) / 2
#			
#		if type == "normal":
#			return return_val
#		elif type == "logit":
#			return math.log(return_val / (1 - return_val)) 
#		else:
#			return return_val
#		
#	def intervention_funct(self, yi, effects, t, target):
#		self.pie = yi[0] if effects[0] else 0.0
#		self.aie = yi[0] if effects[1] else 0.0
#		self.fie = yi[0] if effects[2] else 0.0
#		
#		y = self.state_at(t)
#		risk = 1 - (y[-1][0] + y[-1][5]) / sum(y[-1])
#		return abs(target - risk)