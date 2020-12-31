logger.log("Start creating users and groups"); 

var bestMoneyParentGroupName = "BEST_MONEY";
var sysAdminsGroupName = "SYSTEM_ADMINS";
var sectionCommitteeGroupName = "SECTION_COMMITTEE";
var steeringCommitteeGroupName = "STEERING_COMMITTEE";
var executiveCommitteeGroupName = "EXECUTIVE_COMMITTEE";
var executiveBoardGroupName = "EXECUTIVE_BOARD";
var pressTeamGroupName = "PRESSTEAM";

var baseUserName = "bmuser";
var firstName = "BM";
var baseLastName = "USER";
var startEmailAddress = baseUserName;
var endEmailAddress = "@bestmoney.com";
var password = "1234";
var startUserNumber = 1;  
var endUserNumber = 20; 

// Create the Best Money groups that we need
var parentGroup = groups.getGroup(bestMoneyParentGroupName);
if (parentGroup != null) {
	logger.log("Root group " + bestMoneyParentGroupName + " already exists, aborting user and group creation.");
} else {
	var parentGroup = groups.createRootGroup(bestMoneyParentGroupName, bestMoneyParentGroupName);
	var sysAdmins = parentGroup.createGroup(sysAdminsGroupName, sysAdminsGroupName);
	var sectionCommittee = parentGroup.createGroup(sectionCommitteeGroupName, sectionCommitteeGroupName);
	var steeringCommittee = parentGroup.createGroup(steeringCommitteeGroupName, steeringCommitteeGroupName);
	var executiveCommittee = parentGroup.createGroup(executiveCommitteeGroupName, executiveCommitteeGroupName);
	var executiveBoard = parentGroup.createGroup(executiveBoardGroupName, executiveBoardGroupName);
	var pressTeam = parentGroup.createGroup(pressTeamGroupName, pressTeamGroupName); 

	// Loop and create test users
	for (var userNumber = startUserNumber; userNumber <= endUserNumber; userNumber++) {
		var userName = baseUserName + userNumber;
		var lastName = baseLastName + " " + userNumber;
		var emailAddress = startEmailAddress + userNumber + endEmailAddress;
		
		// Create new user and 
		// enable account so user can login and so admin can edit user
		var enableAccount = true;
		var newUser = people.createPerson(userName,firstName, lastName,emailAddress, password ,enableAccount);

		// Make sure home folders can be seen only by the owner
		var homeFolder = newUser.properties["cm:homeFolder"];
		homeFolder.setInheritsPermissions(false);

		// Populate all groups with some users
		var username = newUser.properties["cm:userName"];
		if (userNumber == 1) { 
			sysAdmins.addAuthority(username);
			logger.log("Created new system admin user: " + userName + " and added to group: " + sysAdminsGroupName);
		} else if (userNumber > 1 && userNumber <= 5) { 
			executiveCommittee.addAuthority(username);
			executiveBoard.addAuthority(username); 
			logger.log("Created new user: " + userName + " and added to groups: " + executiveCommitteeGroupName + ", " + executiveBoardGroupName);
		} else if (userNumber > 5 && userNumber <= 10) { 
			sectionCommittee.addAuthority(username);
			logger.log("Created new user: " + userName + " and added to group: " + sectionCommitteeGroupName);
		} else if (userNumber > 10 && userNumber <= 15) { 
			steeringCommittee.addAuthority(username);
			logger.log("Created new user: " + userName + " and added to group: " + steeringCommitteeGroupName);
		} else if (userNumber > 15 && userNumber <= 20) { 
			pressTeam.addAuthority(username);
			logger.log("Created new user: " + userName + " and added to group: " + pressTeamGroupName);
		}
	}

	logger.log("Users and groups created");
}