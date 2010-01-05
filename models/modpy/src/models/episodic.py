'''
Created on Oct 7, 2009

Defines the parameters for the episodic risk model. Asymptomatic and final 
Stage transition rate and risk ratios default to Hollingsworth 
et al. (2008); primary stage values from Pinkerton et al. (?)  

@author: ethan
'''

import math 
import random
import networkx as nx
from random import random as rnd
from scipy import integrate as si
from scipy import array
from scipy import stats
from numpy import linspace
from scipy.optimize import fmin, fminbound

time_active = 12 * 30.
N = 10000.

#5.09345879    5.77393447  117.72334607   11.92218641
#4.743158,3.874583,242.314916,114.532402
#2.704732,8.994703,269.761780,96.073371
#4.523153,6.743514,284.719105,68.824537



Cg = 4.523153
Ch = 6.743514

tin_g = 284.719105
tin_h = 68.824537

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
		
		y = self.state_at(t)
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
		yopt = fmin(func=self.p_funct,
						 x0=self.rnd_pset(),
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
			
			y=self.state_at(10000)
			prev = 1 - (y[-1][0] + y[-1][3]) / float(sum(y[-1]))
			
			if abs(prev - p) < tol:
				return [Cg, Ch, tin_g, tin_h], prev, attempts
	
	def tempreadrun(self):
		global Cg, Ch, tin_g, tin_h
		f=open('/home/ethan/fromcube2.params')
		lines = f.readlines()
		for line in lines:
			sp = line.split(',')
			Cg = float(sp[0])
			Ch = float(sp[1])
			tin_g = float(sp[2])
			tin_h = float(sp[3])
			y = self.state_at(10000,100)
			print y[-1]
			print 1-((y[-1][0] + y[-1][3])/sum(y[-1]))
	
	def sample_from_grid(self):
		minXg, maxXg = 0.00, 6.612523
		minXh, maxXh = 0.00, 11.227660
		minTinG, maxTinG = 0.002085, 301.3
		minTinH, maxTinH = 0.06827, 283.60263
		
		global Cg, Ch, tin_g, tin_h 
		
		grid_set = [(a,b,c,d) for a in linspace(minXg,maxXg,2)
							  for b in linspace(maxXh,minXh,2)
							  for c in linspace(maxTinG,minTinG,2)
							  for d in linspace(maxTinH,minTinH,2)]
		f=open('/home/ethan/fromgrid.params', 'w')
		for i, point in enumerate(grid_set):
			Cg = point[0]
			Ch = point[1]
			tin_g = point[2]
			tin_h = point[3]	
			y = self.state_at(1000,10)
			prev = 1-((y[-1][0] + y[-1][3])/sum(y[-1]))
			if i % 100 == 0:
				print point, prev
			if prev < 1 and prev < 150.5:
				f.write('%f,%f,%f,%f\n' % (point[0],point[1],point[2],point[3]))
				f.flush()
		f.close()
				
#	def intervention_type(self):
#		""" returns one of three types: chronic, primary, or neither """
#		global B1, B2
#		
#		y = self.state_at(t)[-1]
#		risk = 1 - (y[0] + y[3]) / float(sum(y))
				
	def intervention_effect(self, stage, t, type="logit", target=0.001):
		""" if the infection is eliminable returns the approximate intervention
			efficacy required for elimination, otherwise returns the percent
			reduction in population risk at time t.
			
			effects: a 3 element list of booleans specificing which stages are 
					intervened upon
			type: logit returns a continious logit transformed result """
		
		global B1, B2
		
		y = self.state_at(t)[-1]
		risk = 1 - (y[0] + y[3]) / float(sum(y))
	
		if stage == "primary":
			B1 = B1 * 0.0
		if stage == "chronic":
			B2 = B2 * 0.0
	
		y_i = self.state_at(t)[-1]
		risk_i = 1 - (y_i[0] + y_i[3]) / float(sum(y_i))
		
		print "baseline risk: ", risk, "intervention risk: ", risk_i
		
		B1 = 0.038
		B2 = 0.001
		
		return_val = 0.0
		
		if risk_i < 0.001:
			#the intevention is below threshold, estimate the 
			#intervention effect at or near threshold 
			target = 0.000
			xret = fminbound(self.intervention_funct,
								args=(stage, t, target),
								x1=0.0, x2=1.0, 
								full_output=0,
								disp=3)
			return_val = xret
		
		else:
			#otherwise the return value is one plus the 
			#percentage decrease in risk normalized to [0,1]
			return_val = ((risk_i / risk) + 1) 
			
		if type == "normal":
			return return_val
		elif type == "logit":
			return self.math.log(return_val / (1 - return_val)) 
		else:
			return return_val
		
	def intervention_funct(self, yi, stage, t, target):
		global B1, B2
		
		if stage == "primary":
			B1 = B1 * yi[0]
		if stage == "chronic":
			B2 = B2 * yi[0]
		
		y = self.state_at(t)
		risk = 1 - (y[-1][0] + y[-1][3]) / sum(y[-1])
		
		B1 = 0.038
		B2 = 0.001
		
		return abs(target - risk) 		
				
	def write_intervention_effects(self):
		global Cg, Ch, tin_g, tin_h
		
		f=open("/home/ethan/Dropbox/NSD paper/grid.params", "r")
		out=open("/home/ethan/Dropbox/NSD paper/grid.params.full", 'w')
		
		lines = f.readlines()
		
		for line in lines:
			sp = line.split(',')
			Cg = float(sp[0])
			Ch = float(sp[1])
			tin_g = float(sp[2])
			tin_h = float(sp[3])
			
			y = self.state_at(3000,100)
			
			risk = 1-((y[-1][0] + y[-1][3])/sum(y[-1]))
			
			p_effect = self.intervention_effect("primary", 5000, "normal", target=0.001)
			c_effect = self.intervention_effect("chronic", 5000, "normal", target=0.001)
			
			out.write('%f,%f,%f,%f,%f,%f,%f\n' % (Cg, Ch, tin_g, tin_h, risk, p_effect, c_effect))
			out.flush()
		
		out.close()
		f.close()
				
				
m=DCM()
#print m.state_at(1000, 1000)[-1]
m.write_intervention_effects()
#m.intervention_effect("primary", 1000, "normal", target=0.001)
			
class ABM():
	
	def __init__(self):
		self.state = [[],[],[],[],[],[]]
	
		self.rates = [self.new_gen, self.new_hig, self.exit_sus_gen, self.exit_pri_gen, self.exit_chr_gen,
					  self.exit_sus_hig, self.exit_pri_hig, self.exit_chr_hig, self.pri2chr_gen, self.chr2dth_gen,
					  self.pri2chr_hig, self.chr2dth_hig, self.sus_hig2gen, self.pri_hig2gen, self.chr_hig2gen,
					  self.sus_gen2hig, self.pri_gen2hig, self.chr_gen2hig, self.inf_pri_gengen, self.inf_pri_genhig,
					  self.inf_pri_higgen, self.inf_pri_highig, self.inf_pri_excess, self.inf_chr_gengen, 
					  self.inf_chr_genhig, self.inf_chr_higgen, self.inf_chr_highig, self.inf_chr_excess]
						
		self.tree = nx.DiGraph()
		self.time = 0.0
	
	def new_gen(self, execute=False):
		if not execute:
			return new_g
		else:
			self.state[0].append(Agent(self.time))
	
	def new_hig(self, execute=False):
		if not execute:
			return new_h
		else:
			self.state[3].append(Agent(''))
	
	def exit_sus_gen(self, execute=False):
		if not execute:
			return len(self.state[0]) / time_active
		else:
			self.state[0].remove(self.random_agent(0))
	
	def exit_pri_gen(self, execute=False):
		if not execute:
			return len(self.state[1]) / time_active
		else:
			self.state[1].remove(self.random_agent(1))
	
	def exit_chr_gen(self, execute=False):
		if not execute:
			return len(self.state[2]) / time_active
		else:
			self.state[2].remove(self.random_agent(2))
	
	def exit_sus_hig(self, execute=False):
		if not execute:
			return len(self.state[3]) / time_active
		else:
			self.state[3].remove(self.random_agent(3))
	
	def exit_pri_hig(self, execute=False):
		if not execute:
			return len(self.state[4]) / time_active
		else:
			self.state[4].remove(self.random_agent(4))
	
	def exit_chr_hig(self, execute=False):
		if not execute:
			return len(self.state[5]) / time_active
		else:
			self.state[5].remove(self.random_agent(5))
	
	def pri2chr_gen(self, execute=False):
		if not execute:
			return len(self.state[1]) * D1
		else:
			a = self.random_agent(1)
			self.state[1].remove(a)
			self.state[2].append(a)
	
	def chr2dth_gen(self, execute=False):
		if not execute:
			return len(self.state[2]) * D2
		else:
			a = self.random_agent(2)
			self.state[2].remove(a)
	
	def pri2chr_hig(self, execute=False):
		if not execute:
			return len(self.state[4]) * D1
		else:
			a = self.random_agent(4)
			self.state[4].remove(a)
			self.state[5].append(a)
	
	def chr2dth_hig(self, execute=False):
		if not execute:
			return len(self.state[5]) * D2
		else:
			a = self.random_agent(5)
			self.state[5].remove(a)
			
	def sus_hig2gen(self, execute=False):
		if not execute:
			return len(self.state[3]) * 1/tin_h
		else:
			a = self.random_agent(3)
			self.state[3].remove(a)
			self.state[0].append(a)
	
	def pri_hig2gen(self, execute=False):
		if not execute:
			return len(self.state[4]) * 1/tin_h
		else:
			a = self.random_agent(4)
			self.state[4].remove(a)
			self.state[1].append(a)

	def chr_hig2gen(self, execute=False):
		if not execute:
			return len(self.state[5]) * 1/tin_h
		else:
			a = self.random_agent(5)
			self.state[5].remove(a)
			self.state[2].append(a)
	
	def sus_gen2hig(self, execute=False):
		if not execute:
			return len(self.state[0]) * 1/tin_g
		else:
			a = self.random_agent(0)
			self.state[0].remove(a)
			self.state[3].append(a)

	def pri_gen2hig(self, execute=False):
		if not execute:
			return len(self.state[1]) * 1/tin_g
		else:
			a = self.random_agent(1)
			self.state[1].remove(a)
			self.state[4].append(a)

	def chr_gen2hig(self, execute=False):
		if not execute:
			return len(self.state[2]) * 1/tin_g
		else:
			a = self.random_agent(2)	
			self.state[2].remove(a)
			self.state[5].append(a)				
	
	def inf_pri_gengen(self, execute=False):
		if not execute:
			pr_sus = float(len(self.state[0])) / sum([len(x) for x in self.state])
			return len(self.state[1]) * B1 * Cg * pr_sus
		else:
			s = self.random_agent(0)
			s.i_time = self.time
			i = self.random_agent(1)
			self.state[0].remove(s)
			self.state[1].append(s)
			self.tree.add_edge(i, s, length=abs(s.i_time - i.i_time))
	
	def inf_pri_genhig(self, execute=False):
		if not execute:
			pr_sus = float(len(self.state[0])) / sum([len(x) for x in self.state])
			return len(self.state[4]) * B1 * Cg * pr_sus
		else:
			s = self.random_agent(0)
			s.i_time = self.time
			i = self.random_agent(4)
			self.state[0].remove(s)
			self.state[1].append(s)
			self.tree.add_edge(i, s, length=abs(s.i_time - i.i_time))

	def inf_chr_gengen(self, execute=False):
		if not execute:
			pr_sus = float(len(self.state[0])) / sum([len(x) for x in self.state])
			return len(self.state[2]) * B2 * Cg * pr_sus
		else:
			s = self.random_agent(0)
			s.i_time = self.time
			i = self.random_agent(2)
			self.state[0].remove(s)
			self.state[1].append(s)
			self.tree.add_edge(i, s, length=abs(s.i_time - i.i_time))

	def inf_chr_genhig(self, execute=False):
		if not execute:
			pr_sus = float(len(self.state[0])) / sum([len(x) for x in self.state])
			return len(self.state[5]) * B2 * Cg * pr_sus
		else:
			s = self.random_agent(0)
			s.i_time = self.time
			i = self.random_agent(5)
			self.state[0].remove(s)
			self.state[1].append(s)
			self.tree.add_edge(i, s, length=abs(s.i_time - i.i_time))

	def inf_pri_higgen(self, execute=False):
		if not execute:
			pr_sus = float(len(self.state[3])) / sum([len(x) for x in self.state])
			return len(self.state[1]) * B1 * Cg * pr_sus
		else:
			s = self.random_agent(3)
			s.i_time = self.time
			i = self.random_agent(1)
			self.state[3].remove(s)
			self.state[4].append(s)
			self.tree.add_edge(i, s, length=abs(s.i_time - i.i_time))
	
	def inf_chr_higgen(self, execute=False):
		if not execute:
			pr_sus = float(len(self.state[3])) / sum([len(x) for x in self.state])
			return len(self.state[2]) * B2 * Cg * pr_sus
		else:
			s = self.random_agent(3)
			s.i_time = self.time
			i = self.random_agent(2)
			self.state[3].remove(s)
			self.state[4].append(s)
			self.tree.add_edge(i, s, length=abs(s.i_time - i.i_time))

	def inf_pri_highig(self, execute=False):
		if not execute:
			pr_sus = float(len(self.state[3])) / sum([len(x) for x in self.state])
			return len(self.state[4]) * B1 * Cg * pr_sus
		else:
			s = self.random_agent(3)
			s.i_time = self.time
			i = self.random_agent(4)
			self.state[3].remove(s)
			self.state[4].append(s)
			self.tree.add_edge(i, s, length=abs(s.i_time - i.i_time))

	def inf_chr_highig(self, execute=False):
		if not execute:
			pr_sus = float(len(self.state[3])) / sum([len(x) for x in self.state])
			return len(self.state[5]) * B2 * Cg * pr_sus
		else:
			s = self.random_agent(3)
			s.i_time = self.time
			i = self.random_agent(5)
			self.state[3].remove(s)
			self.state[4].append(s)
			self.tree.add_edge(i, s, length=abs(s.i_time - i.i_time))

	def inf_pri_excess(self, execute=False):
			if not execute:
				pr_sus = float(len(self.state[3])) / sum([len(x) for x in self.state[3:9]])
				return len(self.state[4]) * B1 * Ch * pr_sus
			else:
				s = self.random_agent(3)
				s.i_time = self.time
				i = self.random_agent(4)
				self.state[3].remove(s)
				self.state[4].append(s)
				self.tree.add_edge(i, s, length=abs(s.i_time - i.i_time))
	
	def inf_chr_excess(self, execute=False):
			if not execute:
				pr_sus = float(len(self.state[3])) / sum([len(x) for x in self.state[3:9]])
				return len(self.state[5]) * B2 * Ch * pr_sus
			else:
				s = self.random_agent(3)
				s.i_time = self.time
				i = self.random_agent(5)
				self.state[3].remove(s)
				self.state[4].append(s)
				self.tree.add_edge(i, s, length=abs(s.i_time - i.i_time))


	def init_agents(self, sednum=10):
		init_gen = N * tin_g / (tin_g + tin_h)
		init_hig = N * tin_h / (tin_g + tin_h)
		#assign high risk agents to the general and high risk pools
		for i in range(init_hig):
			a = Agent('')
			self.state[3].append(a)
			
		#assign general risk agents to the general risk pool
		for i in range(init_gen):
			a = Agent('')
			self.state[0].append(a)	
		
		#set a node as the root and link each seed to the root
		root = Agent(0.0)
		self.tree.add_node(root)
		for i in range(sednum):
			a = Agent(0.0)
			b = Agent(0.0)
			self.state[1].append(a)
			self.state[4].append(b)			
			self.tree.add_edge(root, a, length=0.0)
			self.tree.add_edge(root, b, length=0.0)
	
	def exact_step(self, add_to_tree=True): 
		total_rate = 0.0
		i = 0
		#go through the probability structure and calculate the cumulative
		#rate for each event and add a 'cumrate' variable to the pstruct 
		step_rates = [[x, x()] for x in self.rates]
		cum_rates = []
		cum_val = 0.0
		for i in step_rates:
			cum_val += i[1]
			cum_rates.append([i[0], cum_val])
		
		total_rate = cum_rates[-1][1]
		
		#for i in cum_rates:
		#	print i[0].__name__, i[1]
		
		#update the current time
		self.time = self.time + stats.expon.rvs(scale=1./total_rate)
		
		#if wp~U(0,1) is bound by (left, right) then that's it
		wp = stats.uniform.rvs() * total_rate
		for i, v in enumerate(cum_rates):
			if v[1] > wp:
				
				#print "******** ", wp, cum_rates[i][0].__name__  
				
				cum_rates[i][0](True)
				break
			
	def random_agent(self, state):
		""" get a uniform random agent from state state """
		rndint = random.randint(0, len(self.state[state])-1)
		return self.state[state][rndint]
		
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
		s = len(self.state[0])
		p = len(self.state[1])
		c = len(self.state[2])
		x = len(self.state[3])
		y = len(self.state[4])
		z = len(self.state[5])
		print s, p, c, self.time, float(p + c + y + z) / (s + p + c + x + y + z)
		print x, y, z
		print
	
	def run_to(self, stop, stdout=False):
		self.init_agents(100)
		i = 0
		while self.time < stop:
			self.exact_step()
			if stdout == True and i % 1000 == 0:
				self.print_prevalence()
			i += 1


class Agent():
	
	def __init__(self, itime):
		self.i_time = itime


#import psyco
#import cPickle
#psyco.full()
#
#m=ABM()
#m.run_to(3000, stdout=True)
#f = open('/home/ethan/Dropbox/tree0.pkl', 'w')
#cPickle.dump(m.tree, f)
#f.close()	