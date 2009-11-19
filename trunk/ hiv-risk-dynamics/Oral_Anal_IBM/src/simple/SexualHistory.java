package simple;

public class SexualHistory {
	int timeStep = -1;
	int myID = -1;
	int myInfectionStatus = 0;
	int myPartnerID = -1;
	int myPartnerInfectionStatus = 0;
	int contactType = -1;
	public SexualHistory(int _timeStep, int _myID, int _myInfectionStatus,
			int _myPartnerID, int _myPartnerInfectionStatus, int _contactType) {
		this.timeStep = _timeStep;
		this.myID = _myID;
		this.myInfectionStatus = _myInfectionStatus;
		this.myPartnerInfectionStatus = _myPartnerInfectionStatus;		
		this.contactType = _contactType;
	}
}
