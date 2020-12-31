// Get Set of subfolders to copy
var tempFolder = companyhome.childByNamePath("CopyFrom");
var foldersToCopy = tempFolder.children;

// Get all children under current space
var children = space.children;

// Loop thru subfolders and add set of folders to each one
for each (child in children) {
   if (child.isContainer) {
      for each (folderToCopy in foldersToCopy) {
       // Copy folder and its properties
        var copy = folderToCopy.copy(child);

       // Copy permissions
       // List of "[ALLOWED|DENIED];[USERNAME|GROUPNAME];PERMISSION"
       // For example "ALLOWED;martin;Consumer"
       // var permissions = folderToCopy.permissions; // all permissions including inherited
       var permissions = folderToCopy.directPermissions;
       if (permissions != undefined) {
         for each (permission in permissions) {
           if (permission != undefined) {
              var permissionTokens = permission.split(";");
              var authorityId = permissionTokens[1];
              var permissionName = permissionTokens[2];
              copy.setPermission(permissionName, authorityId);
            }
         }
       }
       copy.setInheritsPermissions(folderToCopy.inheritsPermissions());

       // Rules are copied automatically
      }
   }
}
