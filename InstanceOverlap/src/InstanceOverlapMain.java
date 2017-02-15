import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class InstanceOverlapMain {

	public static void main(String[] args) throws IOException {
		
		boolean useSamples = false;
		
		ArrayList<Double> thresholdsH = new ArrayList<Double>();
		thresholdsH.add(1.0);
		thresholdsH.add(0.95);
		thresholdsH.add(0.9);
		
		ArrayList<Double> thresholdsL = new ArrayList<Double>();
		thresholdsL.add(1.0);
		thresholdsL.add(0.9);
		thresholdsL.add(0.8);
		
		ArrayList<Double> thresholdsJaccard = new ArrayList<Double>();
		thresholdsJaccard.add(1.0);
		thresholdsJaccard.add(0.8);
		thresholdsJaccard.add(0.6);
		
		HashSet<String> simMeasuresThresholdH = new HashSet<>();
		simMeasuresThresholdH.add("jaro");
		simMeasuresThresholdH.add("jaroWinkler");
		simMeasuresThresholdH.add("mongeElkan");
		
		StringMeasures stringMeasures = new StringMeasures(thresholdsH, thresholdsL, thresholdsJaccard);
		//StringMeasures stringMeasures = new StringMeasures(exactMatch, jaccard, jaccardT, jaro, jaroT, scaledLevenstein, scaledLevensteinT, mongeElkan, mongeElkanT, tfidf, tfidfT, jaroWinkler, jaroWinklerT,softTfidf, softTfidfT, internalSoftTfidf, internalSoftTfidfS, internalSoftTfidfT);
		
		ArrayList<String> stringM = new ArrayList<String>();
		//stringM.add("all");
		stringM.add("exactMatch");
		stringM.add("jaccard");
		stringM.add("scaledLevenstein");
		stringM.add("jaro");
		stringM.add("jaroWinkler");
		stringM.add("mongeElkan");
		
		//stringM.add("softTfidf");
		
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
		
		UserInput ui = new UserInput();
		ArrayList<String> classNamesUserInput = ui.getClassNames(classNames);
		//int maxBlockSize = ui.getMaxBlockSize();
		
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
			HashMap<String, HashMap<String, Integer>> kKgInstanceCount = stringSim.run(className, cM, stringMeasures, useSamples, thresholdsH, thresholdsL, thresholdsJaccard, simMeasuresThresholdH);
		//CALCULATE ESTIMATED INSTANCE OVERLAP
			EstimatedInstanceOverlap overlap = new EstimatedInstanceOverlap();
			overlap.run(className, cM, stringM, thresholdsH, thresholdsL, thresholdsJaccard, simMeasuresThresholdH, kKgInstanceCount);
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
