'''
Created on Jul 5, 2009

@author: ethan
'''
class Params(object):
	''' Defines the parameters for the OA models. '''
	
	def __init__(self):		
		self.mu = 1 / (12 * 30.)
		self.n = 10000.
		
		self.chil = 1.9681
		self.chih = 2.0
		
		self.dp = 1.0 / 2
		self.dc = 1 / 12.0 * 100
		
		self.bp = (0.001 * 35) * 1
		self.bc = (0.001) * 1
		
		self.pie = 0.22214
		self.zeta = 1.

class DCM(Params):
	""" deterministic compartmental model of the OA class """

	def __init__(self):
		from scipy import integrate as si
		from scipy import array
		from scipy import linspace
		from scipy.optimize import fmin
		from scipy.optimize import fmin_l_bfgs_b
		from random import random 
		from math import fsum
		
		Params.__init__(self)
		self.ode = si.odeint
		self.boundfmin = fmin_l_bfgs_b
		self.array = array 
		self.linspace = linspace
		self.fmin = fmin
		self.rnd = random
		self.fsum = fsum
	
	def dY_dt(self, y, t=0):
		"""function to pass to scipy integrator, returns growth rate"""
		
		#out flows
		out_sa = y[0] * self.mu + incidence_a
		out_pa = y[1] * (self.mu + self.dp)
		out_ca = y[2] * (self.mu + self.dc)
		out_sl = y[3] * self.mu + incidence_l
		out_pl = y[4] * (self.mu + self.dp)
		out_cl = y[5] * (self.mu + self.dc)
		out_sh = y[6] * self.mu + incidence_h
		out_ph = y[7] * (self.mu + self.dp)
		out_ch = y[8] * (self.mu + self.dc)
		#in flows
		in_sa = into_a
		in_pa = incidence_a 
		in_ca = y[1] * self.dp
		in_sl = into_l
		in_pl = incidence_l 
		in_cl = y[4] * self.dp
		in_sh = into_h
		in_ph = incidence_h 
		in_ch = y[7] * self.dp
		
		into_a = self.n * self.pa * self.mu
		into_l = self.n * self.pl * self.mu
		into_h = self.n * self.ph * self.mu
		
		incidence_a = oral_a + anal_a
		incidence_l = oral_l + anal_l
		incidence_h = oral_h + anal_h
		
		oral_a = oral_a
		
		total_contacts = fsum(y[0:3]) * self.chi_a + fsum(y[3:6]) * self.chi_l + fsum(y[6:9]) * self.chi_h 
		
	
		N = y[0] + y[1] + y[2] + y[3] + y[4] + y[5] + y[6] + y[7] + y[8] 
		
		return [in_sa - out_sa, in_pa - out_pa, in_ca - out_ca,
				in_sl - out_sl, in_pl - out_pl, in_cl - out_cl,
				in_sh - out_sh, in_ph - out_ph, in_ch - out_ch,]
		
#		
#		entry = self.n * self.mu
#		oral_component = y[0] * self.chi * self.pie * self.zeta * (self.bp * (y[1] / float(N)) + 
#																   self.bc * (y[2] / float(N))) 
#		anal_component = y[0] * self.chi * (1 - self.pie) * (self.bp * (y[1] / float(N)) +
#															 self.bc * (y[2] / float(N))) 
#		
#		incidence =  oral_component + anal_component 
#		
#		#growth rate 
#		state = self.array([- (y[0] * self.mu) - incidence + entry,
#				 	  	    - (y[1] * self.mu) - (y[1] * self.dp) + incidence,
#				 	  	    - (y[2] * self.mu) - (y[2] * self.dc) + (y[1] * self.dp)])
#				 
#	#	print N, y, incidence
#				
#		return state

	def state_at(self, t=0.01, tfact=100, full_out=False):
		""" returns the state history upto time t, 
			Y[-1] gives the stat at time t
			
			tfact is the number of subdivisions for the integrator"""
		Y0 = self.array([self.n, 1.0, 1.0])
		ticks = self.linspace(0, t, t * tfact)
		Y = self.ode(self.dY_dt, Y0, ticks, full_output=full_out)
		return Y
		
	def p_funct(self, yi, p, t):
		"""funtion to minimize when looking for parameters that 
		give a specific equlibrium risk, p, at time, t""" 		
		
		self.chi = abs(yi[0])
		self.pie = abs(yi[1])
		self.zeta = abs(yi[2])
		
	#	print p
		
		y = self.state_at(t)
		risk = 1 - float(y[-1][0]) / sum(y[-1])
		
	#	print abs(risk - p)
		
		return abs(risk - p) * 100
	
	def rnd_pset(self):
		""" returns a list of reasonable random params in the order 
			chi, pi, zeta """
		return [self.rnd() * 30, self.rnd(), self.rnd()] 
		
	
	def rnd_riskset(self, p, t, xtol=10, set_to=True):
		""" returns the first parameter set that is within tol 
			of the equlibrium risk, p, at time, t, using the 
			downhill simplex method"""	
	#	args = [p, t]
		x, f, d = self.boundfmin(func=self.p_funct,
							  args=[p, t],
						 	  x0=self.rnd_pset(),
							  bounds=[(0,30),(0,1),(0,1)],
							  factr=xtol,
							  approx_grad=True)
		if set_to == True:
			self.chi = x[0]
			self.pie = x[1]
			self.zeta = x[2]
			
		return x, f, d
		
