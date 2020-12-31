function main()
{
    var store_type = "workspace";
    var store_id = "SpacesStore";
    var callbackFunctionName = args["callback"];

    var store = store_type + "://" + store_id;
    var query = "+PATH:\"/app:company_home//*\" AND (TYPE:\"cm:content\" OR TYPE:\"cm:folder\") AND NOT TYPE:\"cm:systemfolder\" AND @cm\\:modified:NOW";
    var itemsCreatedToday = search.luceneSearch(store, query);

    var results = {};
    results.callbackFunctionName = callbackFunctionName;
    results.content = itemsCreatedToday;
    results.content.sort(sortNames);
    results.totalCount = results.content.length;
    model.results = results;
}

function sortNames(a, b) {
    var nameA = a.name.toLowerCase(), nameB = b.name.toLowerCase();
    if (nameA < nameB) //sort string ascending
        return -1;
    if (nameA > nameB)
        return 1;
    return 0; //default return value (no sorting)
}

main();