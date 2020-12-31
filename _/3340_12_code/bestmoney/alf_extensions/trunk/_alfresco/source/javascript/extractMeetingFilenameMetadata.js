if (document.hasAspect("bmc:document_data")) {
   var language = document.name.substring(2, document.name.indexOf("-"));
   var department = document.name.substring(document.name.indexOf("-") + 1);
   department = department.substring(0, department.indexOf("."));
   
   logger.log("Language = " + language);
   logger.log("Department = "+ department);

   document.properties["bmc:language"] = language;
   document.properties["bmc:department"] = department;
   document.save();
} else {
   logger.log("Aspect bmc:document_data is not set for document " + document.name);
}
