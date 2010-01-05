'''
Created on Jul 5, 2009

@author: ethan
'''
import os
import cPickle

def extractData(parent):
	datadic1 = {}
	datadic2 = {}
	datadic3 = {}
	paramdic = {}
	for i, name in enumerate(os.listdir(parent)):
		
		print name
		
		data_level = name.split(".")[0].split("_")[1]
		instance = name.split('_')[0]
		
		if data_level != "level0":
			f = open(parent+name, 'r')
			
			if data_level == "Prevalence":
				params = []
				for i in range(3):
					line = f.readline()
					val = float(line.split(':')[1].strip())
					params.append(val)
				paramdic[instance] = params
				
			if data_level == "level1":
				data = []
				incidence_rate = 0
				num_oral = 0
				num_anal = 0
				for i, line in enumerate(f.readlines()):
					if i > 0:
						tokens = line.strip().split(",")
						incidence_rate += int(tokens[1])
						num_anal += (int(tokens[2]) + int(tokens[3]))
						num_oral += (int(tokens[4]) + int(tokens[5]))
						
				data.append(incidence_rate / float(i))
				data.append(num_anal)
				data.append(num_oral)
				datadic1[instance] = data
						
			if data_level == "level2":
				data = []
				incidence_rate = 0
				num_oral = 0
				num_anal = 0
				for i, line in enumerate(f.readlines()):
					if i > 0:
						tokens = line.strip().split(",")
						incidence_rate += int(tokens[1])
						num_anal += int(tokens[2])
						num_oral += int(tokens[3])
						
				data.append(incidence_rate / float(i))
				data.append(num_anal)
				data.append(num_oral)
				datadic2[instance] = data
			
			if data_level == "level3":
				data = []
				incidence_rate = 0
				num_oral = 0
				num_anal = 0
				for i, line in enumerate(f.readlines()):
					if i > 0:
						tokens = line.strip().split(",")
						incidence_rate += int(tokens[1])
						num_anal += int(tokens[2])
						num_oral += int(tokens[3])
						
				data.append(incidence_rate / float(i))
				data.append(num_anal)
				data.append(num_oral)
				datadic3[instance] = data
				
	cPickle.dump(paramdic, open("/home/ethan/hiv/papers/oral_risk/summary_pkl/params.pkl", 'w'))
	cPickle.dump(datadic1, open("/home/ethan/hiv/papers/oral_risk/summary_pkl/data1.pkl", 'w'))
	cPickle.dump(datadic2, open("/home/ethan/hiv/papers/oral_risk/summary_pkl/data2.pkl", 'w'))
	cPickle.dump(datadic3, open("/home/ethan/hiv/papers/oral_risk/summary_pkl/data3.pkl", 'w'))


#extractData("/home/ethan/hiv/papers/oral_risk/Oral_Anal_Data/")
			
		




class Params(object):
	''' Defines the parameters for the OA models. '''
	
	def __init__(self):		
		self.mu = 1 / (12 * 30.)
		self.n = 10000.
		
		self.chi = 9.078526
		
		self.dp = 1.0 / 2
		self.dc = 1 / (12.0 * 12.0)
		
		self.bp = (0.001 * 38)
		self.bc = (0.001) 
		
		self.pie = 0.541692
		self.zeta = 0.500000
		
		self.boost = 1

class DCM(Params):
	"""deterministic compartmental model of the OA class"""

	def __init__(self):
		from scipy import integrate as si
		from scipy import array
		from scipy import linspace
		from scipy.optimize import fmin
		from scipy.optimize import fmin_l_bfgs_b
		from scipy.optimize import fminbound
		from random import random 
		
		Params.__init__(self)
		self.ode = si.odeint
		self.boundfmin = fmin_l_bfgs_b
		self.array = array 
		self.linspace = linspace
		self.fmin = fmin
		self.rnd = random
		self.fminbound = fminbound
	
		self.incidence = 0

	def estimate_zeta_fromfile(self, file):
		#calculate incidence*, chi*, pi*; estimate zeta* 
		lines = file.readlines()
		
		n_inf, n_anal, n_oral = 0, 0, 0
		params = lines[0]
		del lines[0]
		
		for line in lines:
			 tokens = line.split(',')
			 n_inf += int(tokens[1])
			 n_anal += sum([float(x) for x in tokens[2:5]])
			 n_oral += sum([float(x) for x in tokens[5:8]])
		
		incidence = n_inf / float(len(lines) * 6)
		chi = (n_anal + n_oral) / float(len(lines) * 6 * 2)
		pie = float(n_oral) / (n_oral + n_anal)
		
		print chi, pie, incidence 
		
		self.chi = chi
		self.pie = pie
		
		x = self.estimate_Z(incidence, 10000)
		
		print x
		
		
	def dY_dt(self, y, t=0):
		"""function to pass to scipy integrator"""
		N = y[0] + y[1] + y[2]
		entry = self.n * self.mu
		oral_component = y[0] * self.chi * self.pie * self.zeta * (self.bp * (y[1] / float(N)) + 
																   self.bc * (y[2] / float(N))) 
		anal_component = y[0] * self.chi * (1 - self.pie) * (self.bp * (y[1] / float(N)) +
															 self.bc * (y[2] / float(N))) 
		
		self.incidence =  oral_component + anal_component
		
		#growth rate 
		state = self.array([- (y[0] * self.mu) - self.incidence + entry,
				 	  	    - (y[1] * self.mu) - (y[1] * self.dp) + self.incidence,
				 	  	    - (y[2] * self.mu) - (y[2] * self.dc) + (y[1] * self.dp)])
				
		return state

	def state_at(self, t=0.01, tfact=100, full_out=False):
		""" returns the state history upto time t, 
			Y[-1] gives the stat at time t
			
			tfact is the number of subdivisions for the integrator"""
		Y0 = self.array([self.n, 1.0, 1.0])
		ticks = self.linspace(0, t, t * tfact)
		Y = self.ode(self.dY_dt, Y0, ticks, full_output=full_out)
		return Y
	
	def incidence_funct(self, yi, target, time):
		""" function to minimize when fitting to the incidence
			at a certian time """
			
		self.zeta = yi[0]
		y = self.state_at(time, 100, full_out=False)
		
		print target, self.incidence
		
		return abs(target - self.incidence)
		
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
	
	def estimate_Z(self, target, time):
		""" returns the estimated values of zeta 
			if no value is within the tolerance 
			-1 is returned """
		x = self.fminbound(func=self.incidence_funct,
							  x1=0.0,
							  x2=1.0,
							  args=(target, time),
							  xtol=0.000000001,
							  full_output=True)
		
		return x
		
	
	def rnd_pset(self):
		""" returns a list of reasonable random params in the order 
			chi, pi, zeta """
		return [self.rnd() * 30, self.rnd(), self.rnd()] 
		
	
	def rnd_riskset(self, p, t, factr=1e7, pgtol=1e-10, set_to=True):
		""" returns the first parameter set that is within tol 
			of the equlibrium risk, p, at time, t, using the 
			downhill simplex method"""	
	#	args = [p, t]
		x, f, d = self.boundfmin(func=self.p_funct,
							  args=[p, t],
						 	  x0=self.rnd_pset(),
							  bounds=[(6,50),(0.0001,0.999999),(0.0001,0.20)],
							  factr=factr,
							  approx_grad=True)
		if set_to == True:
			self.chi = x[0]
			self.pie = x[1]
			self.zeta = x[2]
			
		return x, f, d


def est_Z_from_pkl():
	params = cPickle.load(open("/home/ethan/hiv/papers/oral_risk/summary_pkl/params.pkl", "r"))
	data1 = cPickle.load(open("/home/ethan/hiv/papers/oral_risk/summary_pkl/data1.pkl", "r"))
	data2 = cPickle.load(open("/home/ethan/hiv/papers/oral_risk/summary_pkl/data2.pkl", "r"))
	data3 = cPickle.load(open("/home/ethan/hiv/papers/oral_risk/summary_pkl/data3.pkl", "r"))
	
	out1 = open("/home/ethan/hiv/papers/oral_risk/dcm_estimates/dcm_estimates.csv", "w")
	
	for l in data1.items():
		print l
	
	dcm = DCM()
	
	for k, v in params.items():
		z_actual = v[2]
		
		emp_params1 = data1[k]
		target_incidence1 = emp_params1[0]
		chi1 = (emp_params1[1] + emp_params1[2]) / float(1200*6)
		pie1 = emp_params1[1] / float(emp_params1[1] + emp_params1[2])
		
		emp_params2 = data2[k]
		target_incidence2 = emp_params2[0]
		chi2 = (emp_params2[1] + emp_params2[2]) / float(1200*6)
		pie2 = emp_params2[1] / float(emp_params2[1] + emp_params2[2])
		
		emp_params3 = data3[k]
		target_incidence3 = emp_params3[0]
		chi3 = (emp_params3[1] + emp_params3[2]) / float(1200*6)
		pie3 = emp_params3[1] / float(emp_params3[1] + emp_params3[2])
		
		
		dcm.chi, dcm.pie = chi2, pie2
		
		print chi2, pie2, target_incidence2
		
		opt_out = dcm.estimate_Z(target_incidence1, 5.5 * 12.) 

		print opt_out, z_actual



dcm = DCM()
dcm.estimate_zeta_fromfile(open("/home/ethan/Dropbox/oral_risk/Oral_Anal_Data/oa_data.0","r"))
dcm.chi = 21.597
dcm.pie = 0.9723
dcm.zeta = 0.286778
y=dcm.state_at(10000, tfact=100, full_out=False)
print y[-1], dcm.incidence


#	
#import psyco
#psyco.full()
#		
#dcm = DCM()
#out = open("/home/ethan/Dropbox/oral_risk/paramsets.csv", "a")
#for i in range(1000000):
#	a,b,c = dcm.rnd_riskset(0.15, 2000, factr=1e7, set_to=True)
#	print a, b, c
#	if b < 0.01:
#		out.write("%f,%f,%f,%f\n" % (b, a[0], a[1], a[2]))
#		out.flush()
#out.close()
	