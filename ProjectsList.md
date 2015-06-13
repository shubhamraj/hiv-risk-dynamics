  1. URL: http://code.google.com/p/hiv-risk-dynamics/
  1. Need to add Jim's Google account on the hosting site
  1. Using subversion system (SVN) for committing changes
  1. Mailing list used KoopmanHIVgroup for notifications on the URL
  1. Checking out instructions: http://code.google.com/p/support/source/checkout
  1. Eclipse plugin: http://www.eclipse.org/subversive/downloads.php OR Tigris:  http://subclipse.tigris.org/servlets/ProjectProcess?pageID=p4wYuA OR Subclipse
See: http://www.ibm.com/developerworks/opensource/library/os-ecl-subversion/
  1. svn checkout https://hiv-risk-dynamics.googlecode.com/svn/trunk/ hiv-risk-dynamics --username 

&lt;username&gt;



Erik: (code for several tasks)
  * simulating SIR epidemics
  * simulating HIV epidemics (with PHI transmission)
  * estimation of SIR parameters
  * calculation of cluster sizes

  * UTIL.PY: methods to calculate rmse of Ne vs Prevalence trajectories alculate [R0](https://code.google.com/p/hiv-risk-dynamics/source/detail?r=0) for smooth skyline plot median residuals for sir and skyline histogram of estimated [R0](https://code.google.com/p/hiv-risk-dynamics/source/detail?r=0) values and actual [R0](https://code.google.com/p/hiv-risk-dynamics/source/detail?r=0)
    * Tree generation
    * ODE\_SIR Model (simulates all moments)
    * Calculation of cluster sizes

Ethan:
  * basic episodic risk model (IBM, java)
  * basic episodic risk model (IBM, python)
  * basic episodic risk model (DCM, python)
  * 3-site episdoic risk model (IBM, python)
  * 3-site episdoic risk model (DCM, python)
  * SI model (DCM, python)
  * SI model (IBM, python)
  * Oral/Anal model (DCM, python)

Jong-Hoon:
  * IBM for variable and concurrent partnerships
  * DCM version of concurrent partnerships (also derives for [R0](https://code.google.com/p/hiv-risk-dynamics/source/detail?r=0))
  * DCM for insertive/receptive sex acts have under different transmission probabilities and contact durations.
  * IBM for Risk behavior change and partnership durations

Jamal:
  * IBM for directional sex model (Java) [Model](Model.md)
  * IBM version of simple oral and anal sex model (Java) [Model](Model.md)
  * Python/R version for binomial regression to simulated data from IBM [Utility](Utility.md)
  * Java versio0n of contact networks based on Riolo et al. [Utility](Utility.md)

Project Hierarchy:
- Models
> - Episodic Risk
> > - basic episodic risk model (IBM, Java) [ERS](ERS.md)
> > - basic episodic risk model (IBM, Python) [ERS](ERS.md)
> > - basic episodic risk model (DCM, Python) [ERS](ERS.md)
> > - 3-site episdoic risk model (IBM, Python) [ERS](ERS.md)
> > - 3-site episdoic risk model (DCM, Python) [ERS](ERS.md)
> > - Risk behavior change and partnership durations (IBM, Java) [JHK](JHK.md)

> - Directional Sex
> > - Directional sex model (IBM, Java) [SJA](SJA.md)
> > - Insertive/receptive acts and variable contact duration (DCM) [JHK](JHK.md)

> - Contact Duration and Concurrency of Partnerships
> > - Variable and concurrent partnerships (IBM, Java) [JHK](JHK.md)

> - Oral and Anal Sex Model
> > - Simple oral and anal sex model (IBM, Java) [SJA](SJA.md)
> > - O/A model (DCM, Python) [ERS](ERS.md)

> - SIR Dynamics and Cluster Sizes for the following: (Python) [EV](EV.md)
> > - simulating SIR epidemics
> > - simulating HIV epidemics (with PHI transmission)
> > - estimation of SIR parameters
- Utilities and Scripts
> > - methods to calculate rmse of Ne vs Prevalence trajectories alculate [R0](https://code.google.com/p/hiv-risk-dynamics/source/detail?r=0) for smooth                  skyline plot median residuals for sir and skyline histogram of estimated [R0](https://code.google.com/p/hiv-risk-dynamics/source/detail?r=0) values and                  actual [R0](https://code.google.com/p/hiv-risk-dynamics/source/detail?r=0)
> > - calculation of cluster sizes
> > - Python/R version for binomial regression to simulated data from IBM [Utility](Utility.md)
> > - Java versio0n of contact networks based on Riolo et al. [Utility](Utility.md)