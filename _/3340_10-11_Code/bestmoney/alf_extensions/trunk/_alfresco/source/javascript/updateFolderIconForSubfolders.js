// Get all children under current space
var children = space.children;

// Loop through and set icon for sub-folders
for (var i = 0; i < children.length; i++) {
   if (children[i].isContainer) {
      children[i].properties["app:icon"] = "space-icon-doc";
      children[i].save();
   }
}
