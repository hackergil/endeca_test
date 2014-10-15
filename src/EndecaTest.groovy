import com.endeca.navigation.*

/**
 * Print aggregated record results
 * @param navigation Navigation
 */
def printResults(navigation) {
  total = navigation.totalNumAggrERecs
  println "Results: $total"

  for (eRec in navigation.ERecs) {
    println "Printing record: $eRec.spec"
    for (property in eRec.properties) {
      println("$property.key :$property.value | ")
    }
  }
}

/**
 * Prints the returned dimensions
 * @param dimensionList DimensionList
 */
def printDimensions(dimensionList) {

  dimensionsToIgnore = ["Size Chart"]
  Map refinementsMap = new TreeMap()

  for (Dimension dimension in dimensionList) {

    if (!dimensionsToIgnore.contains(dimension.name)) {
      Refinement refinement = refinementsMap[dimension.name]
      if(refinement == null) {
        refinement = new Refinement(id: dimension.id, name: dimension.name)
        refinementsMap[dimension.name] = refinement
      }

      for (DimVal dimVal in dimension.refinements) {
        recordCount = dimVal.properties['DGraph.Bins']
        disabled = false
        RefinementValue refinementValue = new RefinementValue(id: dimVal.id, name: dimVal.name, selected: false, count: recordCount, disabled: disabled)
        refinement.refinementValues << refinementValue
      }
      for (DimVal dimVal : dimension.descriptor){
        recordCount = dimVal.properties['DGraph.Bins']
        disabled = false
        selected = true
        RefinementValue refinementValue = new RefinementValue(id: dimVal.id, name: dimVal.name, selected: selected, count: recordCount, disabled: disabled)
        refinement.refinementValues << refinementValue
      }
    }
  }

  refinementsMap.each { key, value ->

    println "Refinement: ($key)"

    value.refinementValues.sort { a, b -> a.name <=> b.name}

    value.refinementValues.each { refinementValue ->
      println refinementValue
    }
  }

}

def printDescriptors(dimensionList) {
  for (Dimension dimension in dimensionList) {
    println "Dimension: ($dimension.id) $dimension.name"
    DimVal dimVal = dimension.descriptor
    String count = dimVal.properties['DGraph.AggrBins']
    println("* $dimVal.id | $dimVal.name ($count)")
  }
}

def assembleEneQuery() {

  query = new UrlENEQuery("", "UTF-8")
  query.n = "1"
  query.navAllRefinements = true
  query.navRollupKey = "compositekey"
  query.navERecsPerAggrERec = 2
  query.nr = "NOT(P_dimension:omit)"

  DisabledRefinementsConfig config = new DisabledRefinementsConfig();
  config.setTextSearchesInBase(true);
  config.setDimensionInBase(15, true)
  config.setDimensionInBase(10001, true)
  query.navDisabledRefinementsConfig = config
  return query
}

/** Example of a regular Endeca search **/
conn = new HttpENEConnection("host", "port")
results = conn.query(assembleEneQuery())
navigation = results.navigation

println "Results: $navigation.totalNumAggrERecs"
printDimensions(navigation.integratedDimensions)

println "\nSelected refinements:"
printDescriptors(navigation.descriptorDimensions)

class Refinement{

  long id
  String name
  List refinementValues = []

  @Override
  String toString() {
    "$id: $name"
  }

}

class RefinementValue{

  long id
  String name
  boolean disabled
  boolean selected
  String count

  @Override
  String toString(){
    if (selected){
       "$id: - $name"
    } else {
      "$id: $name"
    }
  }

}
