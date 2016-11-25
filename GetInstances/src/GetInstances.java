import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class GetInstances {

	public static void main(String[] args) {
		
		long startTime = System.nanoTime();
		
		//for each KG
		
		//DBpedia files
		String dbpediaInstanceTypesTransitive = "/Users/curtis/SeminarPaper_KG_files/DBpedia/instance_types_transitive_en.ttl"; //_sample
		String dbpediaInstanceTypes = "/Users/curtis/SeminarPaper_KG_files/DBpedia/instance_types_en.ttl"; //_sample
		String dbpediaLabels = "/Users/curtis/SeminarPaper_KG_files/DBpedia/labels_en.ttl"; //_sample
		
		// get all classes for DBpedia
		HashSet<String> dboClasses = getDBpediaClasses();
		System.out.println(dboClasses);
		HashSet<String> allInstancesSet = new HashSet<String>();
		HashSet<String> labeledInstancesSet = new HashSet<String>();
		
		try {
		// GET ALL INSTANCES FOR ALL CLASSES
			//create stream objects of the files
			//http://www.oracle.com/technetwork/articles/java/ma14-java-se-8-streams-2177646.html
			Stream<String> itTransitive = Files.lines(Paths.get(dbpediaInstanceTypesTransitive));
			Stream<String> it = Files.lines(Paths.get(dbpediaInstanceTypes));
			
			
			// read files
			Map<String, Set<String>> classInstances =
					//Files.lines(Paths.get(fileName))	
					Stream.concat(itTransitive, it)
						.skip(1) //skip first row 
						.filter(line -> containsClassName(line, dboClasses, allInstancesSet)) //check if line contains a className
						//collect: group by className (third argument), set of all instance names (first argument): instance a className
						.collect(Collectors.groupingBy(line -> getO(line), Collectors.mapping(line -> getS(line), Collectors.toSet())));
		
			//System.out.println(linesThatHaveDboClass);
			/*for (Entry<String, Set<String>> entry : classInstances.entrySet()) {
				System.out.println(entry.getKey() + ": " + entry.getValue().size() + " instances");
				//write instances to disk
				Path fileName = Paths.get("results/" + getClassNameOfURI(entry.getKey()) + "Instances.txt");
				Files.write(fileName, entry.getValue(), Charset.forName("UTF-8"));
			}
			*/
			
			
			System.out.println("allInstancesSet.size():" + allInstancesSet.size());
			int instanceCount = 0;
			for (Entry<String, Set<String>> entry : classInstances.entrySet()) {
				instanceCount += entry.getValue().size();
			}
			System.out.println("instanceCount:" + instanceCount);
			
			
		// GET ENGLISH INSTANCE LABELS	
			Stream<String> labels = Files.lines(Paths.get(dbpediaLabels));
			Map<String, String> instancesWithLabel =
					//Stream.of(labels)
					labels
							.skip(1) //skip first row
							.filter(line -> containsInstanceNameEn(line, classInstances, allInstancesSet, labeledInstancesSet))
							.collect(Collectors.toMap(
									line -> getS(line), 
									line -> getLabel(getO(line))
								));
			
			//System.out.println(instancesWithLabel);
			int noLabelCounter = 0;
			//combine the results
			Map<String, Set<String>> classInstancesWithLabel = new HashMap<String, Set<String>>();
			for (Entry<String, Set<String>> entry : classInstances.entrySet()) {
				System.out.println(entry.getKey() + ": " + entry.getValue().size() + " instances");
				Set<String> instanceWithLabel = new HashSet<String>();
				//for each instance in class set
				for (String instance : entry.getValue()) {
					//add instance uri and english label
					instanceWithLabel.add(instance + "\t" + instancesWithLabel.get(instance));
					if (instancesWithLabel.get(instance) == null) {
						noLabelCounter += 1;
					}
				}
				//add map (instance, label) to class map
				classInstancesWithLabel.put(entry.getKey(), instanceWithLabel);
			}
					
			System.out.println("No english label found for "+ noLabelCounter + " instances");
					
			
			//write to file
			//http://stackoverflow.com/questions/2885173/how-to-create-a-file-and-write-to-a-file-in-java
			//for (Entry<String, Set<String>> entry : classInstances.entrySet()) {
			for (Entry<String, Set<String>> entry : classInstancesWithLabel.entrySet()) {
				//write instances to disk
				Path fileName = Paths.get("results/" + getClassNameOfURI(entry.getKey()) + "InstancesWithLabels.txt");		
				Files.write(fileName, entry.getValue(), Charset.forName("UTF-8"));
				
			}
		
		
			
			
		
		} catch (IOException e) {
			System.out.println("ERROR WHILE reading files");
			e.printStackTrace();
		} finally {
			System.out.println("EXECUTION TIME: " +  ((System.nanoTime() - startTime)/1000000000) + " seconds." );
		}
	}

	/**
	  * Check if line contains an instance name with english label
	  * @param line
	  * @returns className (String)
	 */
	 private static boolean containsInstanceNameEn(String line,
			Map<String, Set<String>> classInstances, HashSet<String> allInstancesSet, HashSet<String> labeledInstancesSet) {
		 boolean containsInstanceName = false;
		//get spo triple
		String spo[] = getSPO(line);
		
		//check if instance was already labeled
		if (!labeledInstancesSet.contains(spo[0])) {
			// check if property is rdfs:label
			if (spo[1].equals("<http://www.w3.org/2000/01/rdf-schema#label>")) {
				//check if label is in english
				//if (spo[2].contains("@en")) {
					//check if instanceName is contained in one of the sets
					if(allInstancesSet.contains(spo[0])){
						labeledInstancesSet.add(spo[0]);
						containsInstanceName = true;
					}
					//} else {
					//	System.out.println("label is not english: " + line);
					//}
				//} else {
					//System.out.println("property is not rdfs:label for: " + line);
			}
		}
		return containsInstanceName;
	}

	/**
	  * Get third argument of line
	  * @param line
	  * @returns spo[2] (String)
	 */
	private static String getO(String line) {
		String spo[] = getSPO(line);
		return spo[2];
	}
	/**
	  * Get first argument of line
	  * @param line
	  * @returns spo[0] (String)
	 */
	private static String getS(String line) {
		String spo[] = getSPO(line);
		return spo[0];
	}
	/**
	  * Get label of string
	  * @param o (string)
	  * @returns substring without label
	 */
	private static String getLabel(String o) {
		//LABEL_TO_KEEP@en\s
		return o.substring(0, o.length()-4); //-4 due to whitespace created by getSPO in the end
	}
	
	/**
	  * Check if line contains className
	  * @param line
	  * @param classes (HashSet containing all class names)
	  * @returns boolean
	 */
	private static boolean containsClassName(String line, HashSet<String> dboClasses, HashSet<String> allInstancesSet) {
		boolean containsClass = false;
		//get spo triple
		String spo[] = getSPO(line);
		// check if property is rdfs:type
		if (spo[1].equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>")) {
			//check if className is a dbo class
			if (spo[2].startsWith("<http://dbpedia.org/ontology/")) {
				//get pure class name in line
				String lineClassName = getClassNameOfURI(spo[2]);
				//check if className is in classNameArray
				if (dboClasses.contains(lineClassName)) {
					//add instance to allInstancesSet 
					allInstancesSet.add(spo[0]);
					containsClass = true;
				}		
			}
		}
		return containsClass;
	}

	/**
	  * Get class name of URI
	  * @param uriString
	  * @returns substring
	 */
	private static String getClassNameOfURI(String o) {
		//"<http://dbpedia.org/ontology/CLASSNAME_TO_KEEP>"
		return o.substring(29, o.length()-1);
	}

	/**
	  * Get SPO (subject, predicate, object) triple from line
	  * @param line
	  * @returns triple-Array
	 */
	private static String[] getSPO(String line) {
		String[] words;
		// replace quotes in object and split line to s,p,o triple
		if (line.contains("\"")) {
			String[] allWords = line.replace("\"", "").split("\\s+");
			int wordCounter = 0;
			String s = "";
			String p = "";
			String o = "";
			for (String word : allWords) {
				if (wordCounter == 0) {
					s = word;
				} else if (wordCounter == 1) {
					p = word;
				} else {
					o = o + word + " ";
				}	
				if (word.contains("@en")) {
					break;
				}
				wordCounter += 1;
			}
			
			//
			String[] preWords = {s,p,o};
			words = preWords;
			
			
		} else {
			String[] preWords = line.split("\\s+");
			words = preWords;
		}
		//for (String word : words)
		//	System.out.println(word);
		return words;
	}

	/**
	   * Get HashSet containing all class names in DBpedia 
	   * @return Array of all DBpedia classes
	   */
	private static HashSet<String> getDBpediaClasses() {
		HashSet<String> classNameArray = new HashSet<String>();
		classNameArray.addAll(Arrays.asList(
							//PERSON
								"Agent",
								"Person",
								"Politician",
								"Athlete",
								"Actor",
							//ORGANIZATION
								"GovernmentAgency",
								"Company",
								"PoliticalParty",
							//PLACE
								"Place",
								"PopulatedPlace",
								"City",
								"Village",
								"Town",
								"Country",
							//ART
								"Work",
								"MusicalWork",
								"Album",
								"Song",
								"Single",
								"Film",
								"Book",
							//EVENT	
								"Event",
								"MilitaryConflict",
								"SocietalEvent",
								"SportsEvent",
							//TRANSPORT
								"MeanOfTransportation",
								"Automobile",
								"Ship",
								"Spacecraft",
							//OTHER
								"ChemicalSubstance",
								"ChemicalElement",
								"CelestialBody",
								"Planet")
								);
		return classNameArray;
	}

}
