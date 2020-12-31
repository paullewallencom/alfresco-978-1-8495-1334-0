var store = "workspace://SpacesStore";
var query = "+PATH:\"/app:company_home//*\" +ASPECT:\"cm:versionable\"";
var versionableContentItems = search.luceneSearch(store, query);

for each (versionableContentItem in versionableContentItems) {
	// Do something with a versionable content
	logger.log("Found versioned content = "+versionableContentItem.name);
}
