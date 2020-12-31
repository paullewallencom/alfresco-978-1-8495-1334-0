var childNodes = space.children; 
for each (childNode in childNodes) {
	var nodeRef = childNode.nodeRef;
	var nodeProperties = childNode.properties;
	var type = childNode.type;
	logger.log("Type is " + type);
	if (childNode.isDocument) {
		// Do something with the file metadata or content
		var filename = nodeProperties["cm:name"];
		logger.log("File name is " + filename);
	} else if (childNode.isContainer) {
		// Do something with folder metadata
		var foldername = nodeProperties.name;      
		logger.log("Folder name is " + foldername);
	}
}
