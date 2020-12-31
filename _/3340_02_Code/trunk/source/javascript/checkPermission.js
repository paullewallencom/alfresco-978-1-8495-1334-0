if (space.hasPermission("CreateChildren")) {
	// Ok, we can go ahead and create a folder or file
} else {
	logger.log("User ("+ person.properties.userName + ") does not have permission to create a file in the (" + space.name + ") folder");
}
