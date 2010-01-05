'''
Created on Apr 21, 2009

@author: eoromero
'''

class Params(object):
	''' Defines the parameters for the B2 models. Asymptomatic and final 
	Stage transition rate and risk ratios default to Hollingsworth 
	et al. (2008); primary stage values from Pinkerton et al. (?)  '''
	
	def __init__(self):
		import math as m
		from random import random as rnd

		
		self.math = m
		self.rnd = rnd
		
		self.time_active = 12 * 30.
		self.n = 10000.
		
		self.c0 = 0.526315
		self.c1 = 50.26315
		self.to_0 = 1 / (12.0 * 0.5)
		self.to_1 = 1 / (9.0 * 12 * 0.5)
		self.d0 = 4.0
		self.d1 = 1 / 2.0
		self.d2 = 1 / (10.2 * 12 - 11)
		self.d3 = 1 / 9.0
		
		self.b2 = 0.001
		self.p = 38.0
		self.f = 7.0
		self.b1 = self.b2 * self.p
		self.b3 = self.b2 * self.f
		
		self.mix = 1.0
		
		self.pie = 0.
		self.aie = 0.
		self.fie = 0.
		
		self.into_0 = (self.n / self.time_active) * (self.to_0 / (self.to_0 + self.to_1))
		self.into_1 = (self.n / self.time_active) * (1 - (self.to_0 / (self.to_0 + self.to_1)))


class DCM(Params):
	"""deterministic compartmental model of the b2 class"""

	def __init__(self):
		from scipy import integrate as si
		from scipy import array
		from scipy import linspace
		from scipy.optimize import fmin
		from scipy.optimize import fminbound
		
		Params.__init__(self)
		self.ode = si.odeint
		self.fminbound = fminbound 
		self.array = array 
		self.linspace = linspace
		self.fmin = fmin
	
	def dY_dt(self, y, t=0):
		"""function to pass to scipy integrator"""
		
		num_a = (y[0] + y[1] + y[2] + y[3] + y[4]) 
		num_b = (y[5] + y[6] + y[7] + y[8] + y[9])
		
		totalContCom = (num_a * self.mix * self.c0) + (num_b * self.mix * self.c1)
		
		ll3 = y[0] * (1 - self.mix) * y[4] * self.c0 * self.b3 * (1 - self.fie) / num_a
		ll2 = y[0] * (1 - self.mix) * y[3] * self.c0 * self.b2 * (1 - self.aie) / num_a
		ll1 = y[0] * (1 - self.mix) * y[2] * self.c0 * self.b1 * (1 - self.pie) / num_a
	
		incidence_aa = ll1 + ll2 + ll3
	
		if totalContCom > 0:
			lsl1 = y[0] * self.mix * self.c0 * y[2] * self.mix * self.b1 * (1 - self.pie) * self.c0 / totalContCom
			lsl2 = y[0] * self.mix * self.c0 * y[3] * self.mix * self.b2 * (1 - self.aie) * self.c0 / totalContCom
			lsl3 = y[0] * self.mix * self.c0 * y[4] * self.mix * self.b3 * (1 - self.fie) * self.c0 / totalContCom	  
			lsh1 = y[0] * self.mix * self.c0 * y[7] * self.mix * self.b1 * (1 - self.pie) * self.c1 / totalContCom
			lsh2 = y[0] * self.mix * self.c0 * y[8] * self.mix * self.b2 * (1 - self.aie) * self.c1 / totalContCom
			lsh3 = y[0] * self.mix * self.c0 * y[9] * self.mix * self.b3 * (1 - self.fie) * self.c1 / totalContCom
			
			hsl1 = y[5] * self.mix * self.c1 * y[2] * self.mix * self.b1 * (1 - self.pie) * self.c0 / totalContCom
			hsl2 = y[5] * self.mix * self.c1 * y[3] * self.mix * self.b2 * (1 - self.aie) * self.c0 / totalContCom
			hsl3 = y[5] * self.mix * self.c1 * y[4] * self.mix * self.b3 * (1 - self.fie) * self.c0 / totalContCom
			hsh1 = y[5] * self.mix * self.c1 * y[7] * self.mix * self.b1 * (1 - self.pie) * self.c1 / totalContCom
			hsh2 = y[5] * self.mix * self.c1 * y[8] * self.mix * self.b2 * (1 - self.aie) * self.c1 / totalContCom
			hsh3 = y[5] * self.mix * self.c1 * y[9] * self.mix * self.b3 * (1 - self.fie) * self.c1 / totalContCom
		
		else:
			lsl1 = 0.
			lsl2 = 0.
			lsl3 = 0.
			lsh1 = 0.
			lsh2 = 0.
			lsh3 = 0. 
			hsl1 = 0.
			hsl2 = 0.
			hsl3 = 0.
			hsh1 = 0.
			hsh2 = 0.
			hsh3 = 0.
			
		incidence_ac = lsl1 + lsl2 + lsl3 + lsh1 + lsh2 + lsh3;
		incidence_bc = hsl1 + hsl2 + hsl3 + hsh1 + hsh2 + hsh3;
	
		hh3 = y[5] * (1 - self.mix) * y[9] * self.c1 * self.b3 * (1 - self.fie) / num_b;
		hh2 = y[5] * (1 - self.mix) * y[8] * self.c1 * self.b2 * (1 - self.aie) / num_b;
		hh1 = y[5] * (1 - self.mix) * y[7] * self.c1 * self.b1 * (1 - self.pie) / num_b;
	
		incidence_bb = hh1 + hh2 + hh3;
		
		state = [	
			y[0] * - ((1.0 / self.time_active) + self.to_1) - incidence_aa - incidence_ac + (y[5] * self.to_0) + self.into_0,
			y[1] * - ((1.0 / self.time_active) + self.to_1 + self.d0) + (y[6] * self.to_0) + incidence_aa + incidence_ac,
			y[2] * - ((1.0 / self.time_active) + self.to_1 + self.d1) + (y[7] * self.to_0) + (y[1] * self.d0),
			y[3] * - ((1.0 / self.time_active) + self.to_1 + self.d2) + (y[8] * self.to_0) + (y[2] * self.d1),
			y[4] * - ((1.0 / self.time_active) + self.to_1 + self.d3) + (y[9] * self.to_0) + (y[3] * self.d2),
			y[5] * - ((1.0 / self.time_active) + self.to_0) - incidence_bb - incidence_bc + (y[0] * self.to_1) + self.into_1,
			y[6] * - ((1.0 / self.time_active) + self.to_0 + self.d0) + (y[1] * self.to_1) + incidence_bb + incidence_bc,
			y[7] * - ((1.0 / self.time_active) + self.to_0 + self.d1) + (y[2] * self.to_1) + (y[6] * self.d0),
			y[8] * - ((1.0 / self.time_active) + self.to_0 + self.d2) + (y[3] * self.to_1) + (y[7] * self.d1),
			y[9] * - ((1.0 / self.time_active) + self.to_0 + self.d3) + (y[4] * self.to_1) + (y[8] * self.d2)]
		
		return state

	def p_funct(self, yi, p, t):
		"""funtion to minimize when looking for parameters that 
		give a specific equlibrium risk, p, at time, t""" 		
		
#		print "yi:", yi
#		print "p:", p
#		print "t:", t
		
		self.c0 = abs(yi[0])
		self.c1 = abs(yi[1])
		self.mix = abs(yi[2])
		self.to_0 = abs(yi[3])
		self.to_1 = abs(yi[4])
		
		y = self.state_at(t)
		risk = 1 - (y[ - 1][0] + y[ - 1][5]) / sum(y[ - 1])
		return abs(risk - p)
	
	def rnd_pset(self):
		""" returns a list of reasonable random params in the order 
			ca, cb, mix, to_a, to_b"""
		return [self.rnd() * 10, self.rnd() * 10, self.rnd(),
				self.rnd() * 12 * 15, self.rnd() * 12 * 15] 
		
	
	def rnd_riskset(self, p, t, tol, set_to=True):
		""" returns the first parameter set that is within tol 
			of the equlibrium risk, p, at time, t, using the 
			downhill simplex method"""	
		yopt = self.fmin(func=self.p_funct,
						 x0=self.rnd_pset(),
						 args=[p, t],
						 xtol=tol)
		if set_to == True:
			self.c0 = abs(yopt[0])
			self.c1 = abs(yopt[1])
			self.mix = abs(yopt[2])
			self.to_0 = abs(yopt[3])
			self.to_1 = abs(yopt[4])
			
		return yopt
		
		
	def state_at(self, t=0.01, tfact=100):
		""" returns the state history upto time t, 
			Y[-1] gives the stat at time t
			
			tfact is the number of subdivisions for the integrator"""
		inita = self.to_0 * self.n / (self.to_0 + self.to_1)
		initb = self.n - inita
		Y0 = self.array([initb, 1.0, 0.0, 0.0, 0.0, inita, 1.0, 0.0, 0.0, 0.0])
		ticks = self.linspace(0, t, t * tfact)
		Y = self.ode(self.dY_dt, Y0, ticks, full_output=False)
		return Y
	
	def intervention_effect(self, effects, t, type="logit", target=0.01):
		""" if the infection is eliminable returns the approximate intervention
			efficacy required for elimination, otherwise returns the percent
			reduction in population risk at time t.
			
			effects: a 3 element list of booleans specificing which stages are 
					intervened upon
			type: logit returns a continious logit transformed result """
		self.pie = 0.0
		self.aie = 0.0
		self.fie = 0.0
		y = self.state_at(t)
		risk = 1 - (y[-1][0] + y[-1][5]) / sum(y[-1])
		
		self.pie = 1.0 if effects[0] else 0.0
		self.aie = 1.0 if effects[1] else 0.0
		self.fie = 1.0 if effects[2] else 0.0
				
		y_i = self.state_at(t)
		risk_i = 1 - (y_i[-1][0] + y_i[-1][5]) / sum(y_i[-1])
		
		print "baseline risk: ", risk, "intervention risk: ", risk_i
		
		return_val = 0.0
		
		if risk_i < 0.001:
			#the intevention is below threshold, estimate the 
			#intervention effect at or near threshold 
			target = 0.002
			xret = self.fminbound(self.intervention_funct,
								args=(effects, t, target),
								x1=0.0, x2=1.0, 
								full_output=0,
								disp = 3,
								xtol=0.001)
			return_val = xret
			return_val = return_val / 2
		
		else:
			#otherwise the return value is one plus the 
			#percentage decrease in risk normalized to [0,1]
			return_val = ((risk_i / risk) + 1) / 2
			
		if type == "normal":
			return return_val
		elif type == "logit":
			return self.math.log(return_val / (1 - return_val)) 
		else:
			return return_val
		
	def intervention_funct(self, yi, effects, t, target):
		self.pie = yi[0] if effects[0] else 0.0
		self.aie = yi[0] if effects[1] else 0.0
		self.fie = yi[0] if effects[2] else 0.0
		
		y = self.state_at(t)
		risk = 1 - (y[-1][0] + y[-1][5]) / sum(y[-1])
		return abs(target - risk) 
				
class ABM(Params):
	"""agent based implementation of the b2 model"""
	
	def __init__(self):
		import networkx as nx
		from random import shuffle 
		from random import choice 
		
		Params.__init__(self)
		self.nx = nx 
		self.shuffle = shuffle
		self.choice = choice 
		self.dt = self.set_dt()
		self.time = 0
		self.stat={}
		
		self.all = []
		self.sus_all = []
		self.sus_in_0 = []
		self.sus_in_1 = []
		self.inf_all = []
		self.inf_in_0 = []
		self.inf_in_1 = []

	def setup(self):
		print "set dt = %f" % self.dt
	
	def run(self):
		self.seed_model()
		counter = 0
		for i in range(10000000):
			counter += 1
			self.time += self.dt
			numa = 0
			for a in self.all:
				self.update(a)
			self.new_agents()
			if counter % 12 == 0: 
				a, b = self.get_states()
				print "at time, ", self.time
				print a
				print b
				print ""
			#print len(self.sus_in_0), len(self.sus_in_1) 
			
	def seed_model(self):
		p0 = self.into_0 / (self.into_0 + self.into_1)
		eq0 = int(self.n * p0)
		eq1 = int(self.n * (1 - p0))
		for i in range(eq0):
			a = Agent(0, 0)
			self.all.append(a)
			self.sus_all.append(a)
			self.sus_in_0.append(a)
		for i in range(eq1):
			a = Agent(1, 0)
			self.all.append(a)
			self.sus_all.append(a)
			self.sus_in_1.append(a)
		for i in range(3):
			a = Agent(0, 1)
			self.all.append(a)
			self.inf_all.append(a)
			self.inf_in_0.append(a)
		for i in range(3):
			a = Agent(1, 1)
			self.all.append(a)
			self.inf_all.append(a)
			self.inf_in_1.append(a)
	
	def get_states(self):
		state0 = [0,0,0,0,0]
		state1 = [0,0,0,0,0]
		for a in self.all:
			if a.phase == 0:
				state0[a.stage] += 1
			elif a.phase == 1:
				state1[a.stage] += 1
		return state0, state1
	
	def update(self, agent):
		#is the agent removed, remove it and move to next agent
		if self.is_removed(agent) == True:
			self.erase(agent)
		else:
			#does the agent switch phase
			if self.phase_adv(agent) == True:
				self.toggle_phase(agent)
			#does the agent advance in stage
			if self.stage_adv(agent) == True:
				if agent.stage == 0: print "WHAT THE FUCK"
				if agent.stage == 4:
					pass
#					self.erase(agent)
				else:
					agent.stage += 1
			#did the agent make contact 
			if self.contact(agent) == True:
				#contact is made at the common site
				if self.rnd() <= self.mix:
					p_inf = len(self.inf_all) / float(len(self.all))
					#the agent is susceptible, contact is infectious
					if agent.stage == 0 and self.rnd() <= p_inf:
						self.all_sus.remove(agent)
						self.all_inf.append(agent)
						if agent.phase == 0:
							self.sus_in_0.remove(agent)
							self.inf_in_0.append(agent)
						elif agent.phase == 1:
							self.sus_in_1.remove(agent)
							self.inf_in_1.append(agent)
					#agent is infectious
					elif agent.stage == 1 and self.rnd() >= p_inf:
						pass
			
	def toggle_phase(self, agent):
		if agent.phase == 0 and agent.stage == 0:
			agent.phase = 1
			self.sus_in_0.remove(agent)
			self.sus_in_1.append(agent)
		elif agent.phase == 0 and agent.stage != 0:
			agent.phase = 1
			self.inf_in_0.remove(agent)
			self.inf_in_1.append(agent)
		elif agent.phase == 1 and agent.stage == 0:
			agent.phase = 0
			self.sus_in_1.remove(agent)
			self.sus_in_0.append(agent)
		elif agent.phase == 1 and agent.stage != 0:
			agent.phase = 0
			self.inf_in_1.remove(agent)
			self.inf_in_0.append(agent)

	def new_agents(self):
		if self.rnd() <= self.rate2prob(self.into_0):
			a = Agent(0, 0)
			self.all.append(a)
			self.sus_all.append(a)
			self.sus_in_0.append(a)
		if self.rnd() <= self.rate2prob(self.into_1):
			a = Agent(1, 0)
			self.all.append(a)
			self.sus_all.append(a)
			self.sus_in_1.append(a)

	def erase(self, agent):
		s = agent.stage
		p = agent.phase
		
		self.all.remove(agent)
		if p == 0 and s == 0:
			self.sus_all.remove(agent)
			self.sus_in_0.remove(agent)
		elif p == 0 and s != 0:
			self.inf_all.remove(agent)
			self.inf_in_0.remove(agent)
		elif p == 1 and s == 0:
			self.sus_all.remove(agent)
			self.sus_in_1.remove(agent)
		elif p == 1 and s != 0:
			self.inf_all.remove(agent)
			self.inf_in_1.remove(agent)

	def contact(self, agent):
		""" returns true if a contact sufficent to transmit is 
			made in the interval, dt"""
		if agent.phase == 0 and agent.stage == 2:
			if self.rnd() <= self.rate2prob(self.c0 * self.b1):
				return True
			else:
				return False
		elif agent.phase == 0 and agent.stage == 3:
			if self.rnd() <= self.rate2prob(self.c0 * self.b2):
				return True
			else:
				return False
		elif agent.phase == 0 and agent.stage == 4:
			if self.rnd() <= self.rate2prob(self.c0 * self.b3):
				return True
			else:
				return False
		elif agent.phase == 1 and agent.stage == 2:
			if self.rnd() <= self.rate2prob(self.c1 * self.b1):
				return True
			else:
				return False
		elif agent.phase == 1 and agent.stage == 3:
			if self.rnd() <= self.rate2prob(self.c1 * self.b2):
				return True
			else:
				return False
		elif agent.phase == 1 and agent.stage == 4:
			if self.rnd() <= self.rate2prob(self.c1 * self.b3):
				return True
			else:
				return False

	def is_removed(self, agent):
		if self.rnd() <= self.rate2prob(1 / self.time_active):
			return True
		else:
			return False
	
	def phase_adv(self, agent):
		""" returns true if the agent is to switch phases
			false otherwise """
		if agent.phase == 0:
			if self.rnd() <= self.rate2prob(self.to_1):
				return True
			else:
				return False
		elif agent.phase == 1:
			if self.rnd() <= self.rate2prob(self.to_0):
				return True
			else:
				return False

	def stage_adv(self, agent):
		""" returns ture if the agent is to advance
			stage durring the interval dt"""
		if agent.stage == 1:
			if self.rnd() <= 1 - self.rate2prob(self.d0):
				return True
			else:
				return False
		elif agent.stage == 2:
			if self.rnd() <= self.rate2prob(self.d1):
				return True
			else:
				return False
		elif agent.stage == 3:
			if self.rnd() <= self.rate2prob(self.d2):
				return True
			else:
				return False
		elif agent.stage == 4:
			if self.rnd() <= self.rate2prob(self.d3):
				return True
			else:
				return False
	
	def rate2prob(self, lambd):
		""" returns the probability of an event at rate lamdb
			over the interval self.dt"""
		return 1 - self.math.e ** (-lambd * self.dt)
			
	
	def set_dt(self):
		return 0.1 / max([self.c0, self.c1, self.to_0, self.to_1, self.d0,
						   self.d1, self.d2, self.d3, self.into_0, self.into_1])

class Agent(object):
	NEXT_ID = 0
	def __init__(self, phase, stage):
		self.stage = stage
		self.phase = phase 
		self.infected_time = - 1
		self.infectors_stage = - 1
		self.ID = Agent.NEXT_ID 
		Agent.NEXT_ID += 1
				
