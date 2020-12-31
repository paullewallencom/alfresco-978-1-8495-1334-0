var folder = companyhome.childByNamePath("/User Homes/mbergljung");
var childNodes = folder.children;
for each (childNode in childNodes) {
	if (childNode.isDocument) {
		if (childNode.mimetype == "text/plain") {
			var text = childNode.content;
			logger.log("Text content = " + text);
		} else if (childNode.mimetype == "text/html") {
			var html = childNode.content;
			logger.log("Html content = " + html);
		}
	}
}
