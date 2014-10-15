import com.endeca.navigation.*

def assembleEneQuery() {

  query = new UrlENEQuery("", "UTF-8")
  query.d = "suit*"
  query.dx = "matchallpartial+spell+nospell"
  query.dn = "0"
  query.dr = "P_in_stock:T"
  return query
}

/** Example of an Endeca dimension search **/

conn = new HttpENEConnection("host", "port")
results = conn.query(assembleEneQuery())
dimensionSearchGroups = results.getDimensionSearch().getResults();
int numberOfGroups = dimensionSearchGroups.size()
println "Dimension search groups size: $numberOfGroups"

dimensionsToIgnore = ["brand", "Size Chart"]

      genders = [
              "Men" : 1,
              "Women" : 2,
              "Boys" : 3,
              "Girls" : 4
      ]

dimensionSearchGroups.each { DimensionSearchResultGroup group ->


  group.each { DimLocationList dimLocationList ->
    dimLocationList.each { DimLocation dimLocation ->

      dimLocation.ancestors.each { DimVal ancestor ->
        println "Dimension ancestor: $ancestor.name"
      }

      DimVal dimVal = dimLocation.dimValue

      if(dimensionsToIgnore.contains(dimVal.dimensionName)){
        return
      }

      String label = ""

      gender = dimVal.properties['gender'] as String
      genderId = genders[gender]
      isFactory = dimVal.properties['isFactory']
      count = dimVal.properties['Dgraph.AggrBins']

      if (dimVal.dimensionName.startsWith("Category_")) {
        parentCategoryId = dimVal.properties['categoryDimValId']
        if (parentCategoryId != null) {
          label= "Subcategory > $dimVal.name for $gender > N=$genderId+$parentCategoryId+$dimVal.id"
        } else {
          label = "Parent category not found for $dimVal.name ($parentCategoryId)"
        }
      }
      else if (dimVal.dimensionName.equals("Category")) {
        label = "$dimVal.dimensionName > $dimVal.name for $gender > N=$genderId+$dimVal.id"
      }
      else {
        label = "$dimVal.dimensionName > $dimVal.name > N=$dimVal.id"
      }

      println label

    }
  }
}
