import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class GetInstances {

	public static void main(String[] args) {
		
		long startTime = System.nanoTime();
		
		// SPECIFY PARAMETERS
		boolean useSamples = false;
		boolean dbpedia = false;
		boolean yago = true;
		
		String fInstanceTypesTransitive  = "";
		String fInstanceTypes = "";
		String fLabels = "";
		
		if (dbpedia) {
			//DBpedia files
			if (useSamples) {
				fInstanceTypesTransitive = "/Users/curtis/SeminarPaper_KG_files/DBpedia/instance_types_transitive_en_s.ttl";
				fInstanceTypes = "/Users/curtis/SeminarPaper_KG_files/DBpedia/instance_types_en_s.ttl";
				fLabels = "/Users/curtis/SeminarPaper_KG_files/DBpedia/labels_en_s.ttl";
			} else { //full files
				fInstanceTypesTransitive = "/Users/curtis/SeminarPaper_KG_files/DBpedia/instance_types_transitive_en.ttl";
				fInstanceTypes = "/Users/curtis/SeminarPaper_KG_files/DBpedia/instance_types_en.ttl";
				fLabels = "/Users/curtis/SeminarPaper_KG_files/DBpedia/labels_en.ttl";
			}
			// get all classes for DBpedia
			HashSet<String> classes = getDBpediaClasses();
			System.out.println(classes);
			
			runProcess(0, fInstanceTypesTransitive, fInstanceTypes, fLabels, classes);
		}
		if (yago) {
			//YAGO files
			if (useSamples) {
				fInstanceTypesTransitive = "/Users/curtis/SeminarPaper_KG_files/YAGO/yagoTransitiveType_s.ttl";
				fInstanceTypes = "/Users/curtis/SeminarPaper_KG_files/YAGO/yagoTypes_s.ttl";
				fLabels = "/Users/curtis/SeminarPaper_KG_files/YAGO/yagoLabels_s.ttl";
			} else {
				fInstanceTypesTransitive = "/Users/curtis/SeminarPaper_KG_files/YAGO/yagoTransitiveType.ttl";
				fInstanceTypes = "/Users/curtis/SeminarPaper_KG_files/YAGO/yagoTypes.ttl";
				fLabels = "/Users/curtis/SeminarPaper_KG_files/YAGO/yagoLabels.ttl";
			}
			// get all classes for DBpedia
			HashSet<String> classes = getYagoClasses();
			System.out.println(classes);
			
			runProcess(1, fInstanceTypesTransitive, fInstanceTypes, fLabels, classes);
			
		}
		System.out.println("EXECUTION TIME: " +  ((System.nanoTime() - startTime)/1000000000) + " seconds." );
	}
		
		private static void runProcess(int kg, String fInstanceTypesTransitive,
				String fInstanceTypes, String fLabels, HashSet<String> classes) {
			
			int skipRows = 1; //skip first row for dbpedia
			if (kg == 1)
				skipRows = 10; //skip first ten rows for yago 
			
			HashSet<String> allInstancesSet = new HashSet<String>();
			HashSet<String> labeledInstancesSet = new HashSet<String>();
			
			try {
			// GET ALL INSTANCES FOR ALL CLASSES
				//create stream objects of the files
				//http://www.oracle.com/technetwork/articles/java/ma14-java-se-8-streams-2177646.html
				Stream<String> itTransitive = Files.lines(Paths.get(fInstanceTypesTransitive));
				Stream<String> it = Files.lines(Paths.get(fInstanceTypes));
				
				
				// read files
				Map<String, Set<String>> classInstances =
						//Files.lines(Paths.get(fileName))	
						Stream.concat(itTransitive, it)
							.skip(skipRows) //skip rows 
							.filter(line -> containsClassName(kg, line, classes, allInstancesSet)) //check if line contains a className
							//collect: group by className (third argument), set of all instance names (first argument): instance a className
							.collect(Collectors.groupingBy(line -> getO(kg, line), Collectors.mapping(line -> getS(kg, line), Collectors.toSet())));
			
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
				Stream<String> labels = Files.lines(Paths.get(fLabels));
				Map<String, Set<String>> instancesWithLabel =
						//Stream.of(labels)
						labels
								.skip(skipRows) //skip first row
								.filter(line -> containsInstanceNameEn(kg, line, classInstances, allInstancesSet, labeledInstancesSet))
								//.collect(Collectors.toMap(line -> getS(line), Collectors.toSet(line -> getLabel(getO(line)))));
								.collect(Collectors.groupingBy(line -> getS(kg, line), Collectors.mapping(line -> getLabel(kg, getO(kg, line)), Collectors.toSet())));
				
				//System.out.println(classInstances);
				//System.out.println(instancesWithLabel);
				int noLabelCounter = 0;
				//combine the results
				Map<String, Set<String>> classInstancesWithLabel = new HashMap<String, Set<String>>();
				for (Entry<String, Set<String>> entry : classInstances.entrySet()) {
					System.out.println(entry.getKey() + ": " + entry.getValue().size() + " instances");
					Set<String> instanceWithLabel = new HashSet<String>();
					//for each instance in class set
					for (String instance : entry.getValue()) {
						//System.out.println(instance);
						//add instance uri and english label
						String allLabels = null;
						if (instancesWithLabel.get(instance) == null) {
							noLabelCounter += 1;
						} else {
							
							for (String label : instancesWithLabel.get(instance)) {
								if (allLabels == null) {
									allLabels = label;
								} else {
									allLabels = allLabels + "\t" + label;
								}
							}
						}
			
						instanceWithLabel.add(instance + "\t" + allLabels);
						
					}
					//add map (instance, label) to class map
					classInstancesWithLabel.put(entry.getKey(), instanceWithLabel);
				}
						
				System.out.println("No english label found for "+ noLabelCounter + " instances");
						
				
				//write to file
				String resultFolder = "";
				if (kg == 0) {
					resultFolder = "DBpediaResults/";
				} else {
					resultFolder = "yagoResults/";
				}
				//http://stackoverflow.com/questions/2885173/how-to-create-a-file-and-write-to-a-file-in-java
				//for (Entry<String, Set<String>> entry : classInstances.entrySet()) {
				for (Entry<String, Set<String>> entry : classInstancesWithLabel.entrySet()) {
					//write instances to disk
					Path fileName = Paths.get(resultFolder + getClassNameOfURI(kg, entry.getKey()) + "InstancesWithLabels.txt");		
					Files.write(fileName, entry.getValue(), Charset.forName("UTF-8"));
					
				}
			
			} catch (IOException e) {
				System.out.println("ERROR WHILE reading files");
				e.printStackTrace();
			} finally {
				System.out.println("DONE");
			}
		}
	



	/**
	  * Check if line contains an instance name with english label
	  * @param line
	  * @returns className (String)
	 */
	 private static boolean containsInstanceNameEn(int kg, String line,
			Map<String, Set<String>> classInstances, HashSet<String> allInstancesSet, HashSet<String> labeledInstancesSet) {
		 boolean containsInstanceName = false;
		//get spo triple
		String spo[] = getSPO(kg, line);
		//get label and English label string
		String labelString = "";
		String englishLabel = "";
		if (kg == 0) {
			//DBpedia
			labelString = "<http://www.w3.org/2000/01/rdf-schema#label>";
			englishLabel = "@en";
		} else {
			//YAGO
			labelString = "rdfs:label";
			englishLabel = "@eng";
		}
		//check if line was complete (yago contains single element references as line)
		if (spo.length >= 3) {
			//check if instance was already labeled
			if (!labeledInstancesSet.contains(spo[0])) {
				// check if property is rdfs:label
				if (spo[1].equals(labelString)) {
					//check if label is in English
					if (spo[2].contains(englishLabel)) {
						//check if instanceName is contained in one of the sets
						if(allInstancesSet.contains(spo[0])){
							labeledInstancesSet.add(spo[0]);
							containsInstanceName = true;
						}
						}// else {
						//	System.out.println("label is not english: " + line);
						//}
					//} else {
						//System.out.println("property is not rdfs:label for: " + line);
				}
			}
		}
		return containsInstanceName;
	}

	/**
	  * Get third argument of line
	  * @param line
	  * @returns spo[2] (String)
	 */
	private static String getO(int kg, String line) {
		String spo[] = getSPO(kg, line);
		return spo[2];
	}
	/**
	  * Get first argument of line
	  * @param line
	  * @returns spo[0] (String)
	 */
	private static String getS(int kg, String line) {
		String spo[] = getSPO(kg, line);
		return spo[0];
	}
	/**
	  * Get label of string
	  * @param o (string)
	  * @returns substring without label
	 */
	private static String getLabel(int kg, String o) {
		String returnString = "";
		if (kg==0) {
			//DBpedia: LABEL_TO_KEEP@en\s
			returnString = o.substring(0, o.length()-4); //-4 due to whitespace created by getSPO in the end
		} else {
			//YAGO: "LABEL_TO_KEEP"@eng .\n
			returnString = o.substring(1, o.length()-7);			
		}	
		//System.out.println("getLabel: " + returnString);
		return  returnString;
	}
	
	/**
	  * Check if line contains className
	  * @param kg 0:dbpedia, 1:yago
	  * @param line
	  * @param classes (HashSet containing all class names)
	  * @returns boolean
	 */
	private static boolean containsClassName(int kg, String line, HashSet<String> classes, HashSet<String> allInstancesSet) {
		boolean containsClass = false;
		//get spo triple
		String spo[] = getSPO(kg, line);
		String typeString = "";
		String classString = "";
		String classString2 = "";
		if (kg == 0) {
			//DBpedia
			typeString = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
			classString = "<http://dbpedia.org/ontology/";
			classString2 = "<http://dbpedia.org/ontology/";
		} else {
			//YAGO
			typeString = "rdf:type";
			classString = "<wordnet_";
			classString2 = "<yagoLegalActor";
		}
		//check if line was complete (yago contains single element references as line)
		if (spo.length >= 3) {
			// check if property is rdfs:type
			if (spo[1].equals(typeString)) {
				//check if className is a dbo class
				if (spo[2].startsWith(classString) || spo[2].startsWith(classString2)) {
					//get pure class name in line
					String lineClassName = getClassNameOfURI(kg, spo[2]);
					//check if className is in classNameArray
					if (classes.contains(lineClassName)) {
						//add instance to allInstancesSet 
						allInstancesSet.add(spo[0]);
						containsClass = true;
					}		
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
	private static String getClassNameOfURI(int kg, String o) {
		String returnString = "";
		if (kg ==0) {
			//"<http://dbpedia.org/ontology/CLASSNAME_TO_KEEP>"
			returnString = o.substring(29, o.length()-1);
		} else {
			//"<CLASSNAME_TO_KEEP> ."
			returnString = o.substring(1, o.length()-3);
		}
		//System.out.println("getClassNameOfURI: " + returnString);
		return returnString;
	}

	/**
	  * Get SPO (subject, predicate, object) triple from line
	  * @param kg
	  * @param line
	  * @returns triple-Array
	 */
	private static String[] getSPO(int kg, String line) {
		String[] words;
		if (kg == 0) { //DBEPDIA
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
				String[] preWords = line.split("\\s+"); //split on whitespace
				words = preWords;
			}
		} else { //YAGO
			String[] preWords = line.split("\\t"); //split on tab
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
	
	/**
	   * Get HashSet containing all class names in YAGO 
	   * @return Array of all YAGO classes
	   */
	private static HashSet<String> getYagoClasses() {
		HashSet<String> classNameArray = new HashSet<String>();
		classNameArray.addAll(Arrays.asList(
								//PERSON
									"yagoLegalActor",
									"wordnet_causal_agent_100007347",
									"wordnet_person_100007846",
									"wordnet_politician_110450303",
									"wordnet_politician_110451263",
									"wordnet_athlete_109820263",	
									"wordnet_actor_109767197",	
								//ORGANIZATION
									"wordnet_government_108050678",
									"wordnet_stock_company_108383310",
									"wordnet_party_108256968",
								//PLACE
									"wordnet_location_100027167",
									"wordnet_settlement_108672562",		
									"wordnet_city_108524735",
									"wordnet_village_108672738",	
									"wordnet_town_108665504",
									"wordnet_country_108544813",
								//ART
									"wordnet_work_104599396",								
									"wordnet_musical_composition_107037465",	
									"wordnet_album_106591815",
									"wordnet_song_107048000",
									"wordnet_movie_106613686",	
									"wordnet_book_106410904",
									"wordnet_book_102870092",									
								//EVENT	
									"wordnet_event_100029378",
									"wordnet_war_101236296",
									"wordnet_social_event_107288639",
								//TRANSPORT	
									"wordnet_vehicle_104524313",
									"wordnet_conveyance_103100490",
									"wordnet_car_102958343",
									"wordnet_ship_104194289",
									"wordnet_spacecraft_104264914",
								//OTHER	
									"wordnet_chemical_element_114622893",	
									"wordnet_substance_100019613",
									"wordnet_celestial_body_109239740",
									"wordnet_planet_109394007")
									);
		return classNameArray;
	}
}

