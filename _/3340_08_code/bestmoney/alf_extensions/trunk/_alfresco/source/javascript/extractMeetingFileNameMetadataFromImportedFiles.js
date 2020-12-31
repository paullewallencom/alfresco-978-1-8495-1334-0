//
// Naming Convention Circulars:
//
// Example: 10Eng-FM.02_3_annex1.doc
//

// Regulars Expression Definition
var re = new RegExp("^\\d{2}(Arabic|Chinese|Eng|eng|F|Fr|G|Ger|Indonesian|Ital|Jpn|Port|Rus|Russian|Sp|Sw|Tagalog|Turkish)-(A|HR|FM|FS|FU|IT|M|L).*");

var store = "workspace://SpacesStore";
var query = "+PATH:\"/app:company_home/cm:Meetings//*\" +TYPE:\"cm:content\"";
var legacyContentFiles = search.luceneSearch(store, query);

for each (legacyContentFile in legacyContentFiles) {
    if (re.test(legacyContentFile.name) == true) {
        var language = getLanguageCode(RegExp.$1);
        var department = RegExp.$2;
        logger.log("Extracted and updated metadata (language=" + language + ")(department=" + department + ") for file: " + legacyContentFile.name);
        if (legacyContentFile.hasAspect("bmc:document_data")) {
            // Set some metadata extracted from file name
            legacyContentFile.properties["bmc:language"] = language;
            legacyContentFile.properties["bmc:department"] = department;

            // Make sure versioning is not enabled for property updates
            legacyContentFile.properties["cm:autoVersionOnUpdateProps"] = false;

            legacyContentFile.save();
        } else {
            logger.log("Aspect bmc:document_data is not set for document " + legacyContentFile.name);
        }
    } else {
        logger.log("Did NOT extract metadata from file: " + legacyContentFile.name);
    }
}

/**
 * Convert from legacy language code to new 2 char language code
 *
 * @param parsedLanguage legacy language code
 */
function getLanguageCode(parsedLanguage) {
    if (parsedLanguage == "Arabic") {
        return "Ar";
    } else if (parsedLanguage == "Chinese") {
        return "Ch";
    } else if (parsedLanguage == "Eng" || parsedLanguage == "eng") {
        return "En";
    } else if (parsedLanguage == "F" || parsedLanguage == "Fr") {
        return "Fr";
    } else if (parsedLanguage == "G" || parsedLanguage == "Ger") {
        return "Ge";
    } else if (parsedLanguage == "Indonesian") {
        return "In";
    } else if (parsedLanguage == "Ital") {
        return "";
    } else if (parsedLanguage == "Jpn") {
        return "Jp";
    } else if (parsedLanguage == "Port") {
        return "Po";
    } else if (parsedLanguage == "Rus" || parsedLanguage == "Russian") {
        return "Ru";
    } else if (parsedLanguage == "Sp") {
        return "Sp";
    } else if (parsedLanguage == "Sw") {
        return "Sw";
    } else if (parsedLanguage == "Tagalog") {
        return "Ta";
    } else if (parsedLanguage == "Turkish") {
        return "Tu";
    } else {
        logger.log("Invalid parsed language code: " + parsedLanguage);
        return "";
    }
}
