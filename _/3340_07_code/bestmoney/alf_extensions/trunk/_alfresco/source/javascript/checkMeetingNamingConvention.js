// 
// Naming Convention:
//
// Example: 06En-FM.02_3_annex1.doc
// 06 = last 2 digits of the year	
// En = language - English
// FM = section code - Finance
// 02 = section sequence number  
// 3 = Agenda item
// annex1 = annex for that agenda item
//

// Regulars Expression Definition   
var re = new RegExp("^\\d{2}(Ar|Ch|En|Fr|Ge|In|Jp|Po|Ru|Sp|Sw|Ta|Tu)-(A|HR|FM|FS|FU|IT|M|L)\\.\\d{2}_\\d{1,3}_annex.*");

logger.log("Check Meeting Naming Convention RegExp = "+ re);

if (re.test(document.name) == false) {
   var exampleNamingConvention = "06En-FM.02_3_annex1.doc";
   var errorMsg = "<<ERROR: Filename " + document.name + " does not follow naming convention for this folder. Example of naming convention: " + exampleNamingConvention + ". Regular Expression used: " + re + ">>";
   logger.log(errorMsg);

    // Cancel Transaction so document is not stored
    throw errorMsg;
 } 
