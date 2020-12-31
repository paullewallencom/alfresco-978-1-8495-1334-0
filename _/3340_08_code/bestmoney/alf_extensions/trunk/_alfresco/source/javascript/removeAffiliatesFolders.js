function removeAllChildren(folder) {
   var nodes = folder.children;
   for each (node in nodes) {
      node.remove();
   }
}

var countries = space.children;
for each (country in countries) {
   var affiliated = country.childByNamePath("Affiliated");
   var disaffiliated = country.childByNamePath("Disaffiliated");
   var lapsed = country.childByNamePath("Lapsed");
   var prospect = country.childByNamePath("Prospect");
   var suspended = country.childByNamePath("Suspended");
	  
   removeAllChildren(affiliated);
   removeAllChildren(disaffiliated);
   removeAllChildren(lapsed);
   removeAllChildren(prospect);
   removeAllChildren(suspended);
}
