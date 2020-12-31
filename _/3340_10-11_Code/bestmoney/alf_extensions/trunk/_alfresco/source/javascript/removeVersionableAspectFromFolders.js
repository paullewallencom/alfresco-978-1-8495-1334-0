var store = "workspace://SpacesStore";
var query = "PATH:\"/app:company_home//*\" AND TYPE:\"cm:folder\" AND ASPECT:\"cm:versionable\"";
var versionableFolders = search.luceneSearch(store, query);

for each (versionableFolder in versionableFolders) {
    versionableFolder.removeAspect("cm:versionable");
    logger.log("Removed versionable aspect from folder: " + versionableFolder.name);
}

logger.log("Removed versionable aspect from " + versionableFolders.length + " folders");
