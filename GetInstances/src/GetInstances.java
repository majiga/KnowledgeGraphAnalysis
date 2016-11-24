import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		
		// get all classes for DBpedia
		HashSet<String> dboClasses = getDBpediaClasses();
		System.out.println(dboClasses);
		
		try {
			//create stream objects of the files
			Stream<String> itTransitive = Files.lines(Paths.get(dbpediaInstanceTypesTransitive));
			Stream<String> it = Files.lines(Paths.get(dbpediaInstanceTypes));
			
			// read files
			Map<String, Set<String>>linesThatHaveDboClass =
					//Files.lines(Paths.get(fileName))	
					Stream.concat(itTransitive, it)
						.skip(1) //skip first row 
						.filter(line -> containsClassName(line, dboClasses)) //check if line contains a className
						//collect: group by className, set of all instance names
						.collect(Collectors.groupingBy(line -> getClassName(line), Collectors.mapping(line -> getInstanceName(line), Collectors.toSet())));
		
			//System.out.println(linesThatHaveDboClass);
			for (Entry<String, Set<String>> entry : linesThatHaveDboClass.entrySet()) {
				System.out.println(entry.getKey() + ": " + entry.getValue().size() + " instances");
				//write instances to disk
				Path fileName = Paths.get("results/" + getClassNameOfURI(entry.getKey()) + "Instances.txt");
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
	  * Get class name of line (third argument of line)
	  * @param line
	  * @returns className (String)
	 */
	private static String getClassName(String line) {
		String spo[] = getSPO(line);
		return spo[2];
	}
	/**
	  * Get instance name of line (first argument of line)
	  * @param line
	  * @returns instanceName (String)
	 */
	private static String getInstanceName(String line) {
		String spo[] = getSPO(line);
		return spo[0];
	}
	
	/**
	  * Check if line contains className
	  * @param line
	  * @param classes (HashSet containing all class names)
	  * @returns boolean
	 */
	private static boolean containsClassName(String line, HashSet<String> dboClasses ) {
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
		String words[] = line.split("\\s+");
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
