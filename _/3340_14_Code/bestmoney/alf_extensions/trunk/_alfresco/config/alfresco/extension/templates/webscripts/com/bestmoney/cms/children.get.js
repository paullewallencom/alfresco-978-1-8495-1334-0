function main()
{
    // Get the arguments and set default if they are not specified
    var store_type = args["store_type"];
    var store_id = args["store_id"];
    var path = args["path"];
    var types = args["types"];
    var filter = args["filter"];
    var maxItems = args["maxItems"];
    var includeAllowableActions = args["includeAllowableActions"];
    var includeRelationships = args["includeRelationships"];
    var callbackFunctionName = args["callback"];

    if (store_type == null) {
        store_type = "workspace";
    }

    if (store_id == null) {
        store_id = "SpacesStore";
    }

    if (path == null) {
        path = "/Company Home";
    }

    if (types == null) {
        types = "Any";
    }

    if (filter == null) {
        filter = "*";
    }

    if (maxItems == null) {
        maxItems = 0;
    }

    if (includeAllowableActions == null) {
        includeAllowableActions = false;
    }

    if (includeRelationships == null) {
        includeRelationships = "none";
    }

    // Get the node the path is pointing at
    var curNode = companyhome;
    var str = path.replace("/Company Home", "");
    if (str != "") {
        curNode = companyhome.childByNamePath(str.substring(1)); // loose the first /
    } else {
        // The path was just "/Company Home" and curNode is already pointing at it
    }

    // Get all the children under specified path
    var children = curNode.children;
    var results = {};
    var numDocs = 0;
    var numSpaces = 0;

    results.node = curNode;
    results.callbackFunctionName = callbackFunctionName;
    results.spaces = new Array();
    results.documents = new Array();

    // Iterate through children and populate return objects
    for (var i = 0; i < children.length; i++) {
        if (children[i].isContainer && types.indexOf("Folders") != -1) {
            results.spaces[numSpaces++] = children[i];
        } else if (children[i].isDocument && types.indexOf("Documents") != -1) {
            results.documents[numDocs++] = children[i];
        }
    }
    results.totalCount = results.spaces.length + results.documents.length;  
    model.results = results;
}

main();