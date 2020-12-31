var filename = "uuidMapping.properties";
var file = companyhome.childByNamePath(filename);

if (file == null) {
   file = space.createFile(filename);
}

if (file != null){
	var store = "workspace://SpacesStore";
	var query = "+PATH:\"/app:company_home//*\" +TYPE:\"cm:folder\"";
	var folders = search.luceneSearch(store, query);

	var content = "";
	for each (folder in folders) {
		var uuid = folder.properties["sys:node-uuid"];
		var pathAndName = folder.displayPath + "/" +  folder.name;
        content += pathAndName + "=" + uuid + "\r\n";
	}
    file.content = content;
}