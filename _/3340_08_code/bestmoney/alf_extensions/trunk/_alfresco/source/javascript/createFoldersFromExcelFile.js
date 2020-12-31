// Read content from current document and split it into separate lines
var affiliateLines=document.properties.content.content.split("\r\n");
		   
if (affiliateLines == null) {
   logger.log("No text lines available");
} else {
   logger.log(affiliateLines.length + " lines available for processing");
}

var primaryName = null;
var shortName = null;
var number = null;
var status = null;
var country = null;
			
if (affiliateLines != null) {
   for each (affiliateLine in affiliateLines) {
      // Extract the different affiliate fields
      var affiliateTokens = affiliateLine.split(",");
      primaryName = affiliateTokens[0].trim();
      shortName = affiliateTokens[1].trim();
      number = affiliateTokens[2];
      status = affiliateTokens[3].trim();
      country = affiliateTokens[4].trim();
					
      logger.log("Processing : " + primaryName + ", " + shortName + "," + number + "," + status + "," + country);

      // Setup Affiliate folder name
      var numberString = "";
      if (number != null && number != "") {
          numberString =  " - " + number;
      }
      var affiliateFolderName = primaryName + numberString;
      // Replace stuff that is not valid in a filename
      affiliateFolderName = affiliateFolderName.replaceAll("/", "-"); 
      affiliateFolderName = affiliateFolderName.replaceAll("\"", ""); 

      // Find folder to create Affiliate folder under 
     // /Company Home/Affiliates/[Countries*]/[Affiliate Status*]
      var affiliateParentFolderPath = "Affiliates/" + country + "/" + status;
      affiliateParentFolder = companyhome.childByNamePath(affiliateParentFolderPath);

      if (affiliateParentFolder != null) {
         // Check if folder already exists
         var affiliateFolder = companyhome.childByNamePath(affiliateParentFolderPath + "/" + affiliateFolderName);
         if (affiliateFolder == null) {
            // Create Affiliate folder
            affiliateFolder = affiliateParentFolder.createFolder(affiliateFolderName);
            if (affiliateFolder != null) {
               affiliateFolder.properties["app:icon"] = "space-icon-doc";
               affiliateFolder.properties["cm:title"] = shortName;
               affiliateFolder.properties["cm:description"] = "The top folder for this affiliate.";
               affiliateFolder.save();

               affiliateFolder.setPermission("Coordinator", "SYSTEM_ADMINS");
            } else {
               logger.log("Affiliate folder: " + affiliateFolderName + " could not be created");
            }
         } else {
            logger.log("Affiliate folder: " + affiliateFolderName + " already exists, no need to create it");
         }
      } else {
         logger.log("Could not find affiliate parent folder: " + affiliateParentFolderPath + ", affilate folder " + affiliateFolderName  + " has not been created");
      }
   }
}
