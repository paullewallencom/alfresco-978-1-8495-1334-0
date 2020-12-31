logger.log("Starting check of Review Period for folders"); 

var today = new Date();
var docReviewList = new Array();
var store = "workspace://SpacesStore";
var query = "+PATH:\"/app:company_home//*\" +ASPECT:\"bmc:reviewable\"";
var reviewableFolders = search.luceneSearch(store, query);

for each (reviewableFolder in reviewableFolders) {
   var reviewPeriod = reviewableFolder.properties["bmc:reviewPeriod"]; 
   var lastReviewDate =  new Date(new Date(today).setYear(1900 + today.getYear() - reviewPeriod));
   var includeSubfolders = reviewableFolder.properties["bmc:includeSubFolders"]; 
   logger.log("Checking Review Period for documents in folder [name=" + reviewableFolder.displayPath + "/" +  reviewableFolder.name + "][reviewPeriod="+reviewPeriod+"][lastReviewDate="+lastReviewDate+"][includeSubfolders="+includeSubfolders+"]"); 

   var wildcard = includeSubfolders ? "//*" : "/*";
   query = "+PATH:\""+reviewableFolder.qnamePath + wildcard + "\"";
   var nodes = search.luceneSearch(store, query);
   for each (node in nodes) {
      if (node.isDocument) {
         var modifiedDate = node.properties["cm:modified"];
         logger.log("Checking if doc should be reviewed: "+node.name + ", modifiedDate = " + modifiedDate);
         if (modifiedDate <= lastReviewDate) {
            var docToReview = node.name + " ("+reviewableFolder.displayPath + "/" +  reviewableFolder.name + ")";
            logger.log("Adding doc to review list: "+docToReview);
            docReviewList.push(docToReview);
            // Update modified date so doc is not reviewed again next month
            node.properties["cm:modified"] = today;
            node.save();
         }
      }
   }
}

// Setup email body text 
var emailBodyText = "The following documents need to be reviewed:\n\r\n\r";
for each (docToReview in docReviewList) {
   emailBodyText = emailBodyText + docToReview + "\n\r";
}
logger.log("email = " + emailBodyText);

// Create an array with all users and groups that the email should be sent to
var reveiwersGroupName = "GROUP_DOC_REVIEWERS";
var reviewerGroups = new Array(reveiwersGroupName);

// Create mail action and send emails
var mail = actions.create("mail");
mail.parameters.to_many=reviewerGroups;
mail.parameters.subject = "Documents For Reviewing";
mail.parameters.from = "do-not-reply@bestmoney.com";
mail.parameters.text = emailBodyText;
// execute action against current space    
mail.execute(space);

logger.log("End check of Review Period for folders"); 
