var filename = "helloworld.txt";
var file = space.createFile(filename);

var newFileNodeRef = file.nodeRef;

logger.log("New file node ref = " + newFileNodeRef);

file.mimetype = "text/plain";
file.content = "Hello World!";

var props = new Array(2);
props["cm:title"] = "Hello World!";
props["cm:description"] = "This is the traditional Hello World example.";
file.addAspect("cm:titled", props);

var parentFolder = file.parent;
logger.log("Parent folder = " + parentFolder.properties.name);

var isVersioned = file.hasAspect("cm:versionable");
logger.log("File versioned = " + isVersioned);

logger.log("Deleting file...");
file.remove();

var folderName = "MyFolder";
var folder = space.createFolder(folderName);

logger.log("New folder node ref = " + folder.nodeRef);

logger.log("Deleting folder...");
folder.remove();
