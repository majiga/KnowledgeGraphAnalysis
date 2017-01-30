import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class InstanceOverlapMain {

	public static void main(String[] args) throws IOException {
		
		boolean useSamples = false;
		
		ArrayList<Double> thresholds = new ArrayList<Double>();
		thresholds.add(1.0);
		thresholds.add(0.9);
		thresholds.add(0.8);
		
		StringMeasures stringMeasures = new StringMeasures(thresholds);
		//StringMeasures stringMeasures = new StringMeasures(exactMatch, jaccard, jaccardT, jaro, jaroT, scaledLevenstein, scaledLevensteinT, tfidf, tfidfT, jaroWinkler, jaroWinklerT,softTfidf, softTfidfT, internalSoftTfidf, internalSoftTfidfS, internalSoftTfidfT);
		
		ArrayList<String> stringM = new ArrayList<String>();
		//stringM.add("all");
		stringM.add("exactMatch");
		stringM.add("jaccard");
		stringM.add("jaro");
		stringM.add("jaroWinkler");
		stringM.add("scaledLevenstein");
		stringM.add("softTfidf");
		
		//configure log4j for secondstring library
		org.apache.log4j.BasicConfigurator.configure();
		//LogManager.getRootLogger().setLevel(Level.OFF); //set console logger off
		
		// PARAMETERS: string similarity measures and thresholds
		/*boolean exactMatch = true;
		boolean jaccard = true;
		double jaccardT = 1.0;
		boolean jaro = true;
		double jaroT = 1.0;
		boolean scaledLevenstein = true;
		double scaledLevensteinT = 1.0;
		boolean tfidf = false;
		double tfidfT = 1.0;
		boolean jaroWinkler = true;
		double jaroWinklerT = 1.0;
		boolean softTfidf = true;
		double softTfidfT = 1.0;
		boolean internalSoftTfidf = false;
		String internalSoftTfidfS = "jaroWinkler"; //"jaroWinkler", "jaccard", or "scaledLevenstein"
		double internalSoftTfidfT = 0.9;
		*/
		
		ClassMapping cM = new ClassMapping();
		ArrayList<String> classNames = getClassNames();
		ArrayList<String> classNamesUserInput = new ArrayList<>();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        boolean newInput = true;
		while (newInput) {
		System.out.print("Enter class name, 'all', or 'start': ");
        String userInput = br.readLine();
        	if (userInput.equals("start")) {
        		if (classNamesUserInput.size()>0) {
        			newInput = false;
        		} else {
        			System.out.println("please enter at least one valid class first.");
        			System.out.println(classNames.toString());
        		}
        	} else if (userInput.equals("all")) {
        		classNamesUserInput = classNames;
        	} else {
        		if (classNames.contains(userInput)) {
        			classNamesUserInput.add(userInput);
        		} else {
        			System.out.println("Class not found. Please enter one of the following classes");
        			System.out.println(classNames.toString());
        		}	
        	}
		}
        
		
		//for (String className : classNames) {
		for (String className : classNamesUserInput) {
			
		
		// SAME AS LINKS
			// PARAMETERS		
			boolean d2y = true;
			boolean d2w = true;
			boolean d2o = true;
			boolean d2n = true;
			boolean y2w = true;
			boolean y2o = true;
			boolean y2n = true;
			boolean w2o = true;
			boolean w2n = true;
			boolean o2n = true;
			CountSameAs same = new CountSameAs();
			same.run(className, cM, d2y, d2w, d2o, d2n, y2w, y2o, y2n, w2o, w2n, o2n);
			
			
		// INSTANCE MATCHES USING STRING SIMILARITY MEASURES
			
		
			CountStringSimilarity stringSim = new CountStringSimilarity();
			HashMap<String, HashMap<String, Integer>> kKgInstanceCount = stringSim.run(className, cM, stringMeasures, useSamples, thresholds);
		//CALCULATE ESTIMATED INSTANCE OVERLAP
			
			EstimatedInstanceOverlap overlap = new EstimatedInstanceOverlap();
			overlap.run(className, cM, stringM, thresholds, kKgInstanceCount);
			System.out.println("DONE");
			
		}
		
	
		
		
	
		

	}
	
	private static ArrayList<String> getClassNames() {
		ArrayList<String> classNames = new ArrayList<String>();
		classNames.addAll(Arrays.asList(
							//PERSON
								"Agent",
								"Person",
								"Politician",
								"Athlete",
								"Actor",
							//ORGANIZATION
								"GovernmentOrganization",
								"Company",
								"PoliticalParty",
							//PLACE
								"Place",
								"PopulatedPlace",
								"City_Village_Town",
								"Country",
							//ART
								"Work",
								"MusicalWork",
								"Album",
								"Song",
								"Single",
								"Movie",
								"Book",
							//EVENT	
								"Event",
								"MilitaryConflict",
								"SocietalEvent",
								"SportsEvent",
							//TRANSPORT
								"Vehicle",
								"Automobile",
								"Ship",
								"Spacecraft",
							//OTHER
								"ChemicalElement_Substance",
								"CelestialBody_Object",
								"Planet"
							));
		return classNames;
	}
	

}
