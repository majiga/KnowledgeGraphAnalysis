
public class CountObject {
	private String className;
	private int count;
	private int classIndegree;
	private int classOutdegree;
	private double classInstanceIndegreeMin;
	private double classInstanceIndegreeAvg;
	private double classInstanceIndegreeMed;
	private double classInstanceIndegreeMax;
	private double classInstanceOutdegreeMin;
	private double classInstanceOutdegreeAvg;
	private double classInstanceOutdegreeMed;
	private double classInstanceOutdegreeMax;
	
	public CountObject(String cN, int count, int in, int out, double iMin, double iAvg, double iMed, double iMax, double oMin, double oAvg, double oMed, double oMax) {
		this.className = cN;
		this.count = count;
		this.classIndegree = in;
		this.classOutdegree = out;
		this.classInstanceIndegreeMin = iMin;
		this.classInstanceIndegreeAvg = iAvg;
		this.classInstanceIndegreeMed = iMed;
		this.classInstanceIndegreeMax = iMax;
		this.classInstanceOutdegreeMin = oMin;
		this.classInstanceOutdegreeAvg = oAvg;
		this.classInstanceOutdegreeMed = oMed;
		this.classInstanceOutdegreeMax = oMax;
	}

	public CountObject(String className) {
		this.className = className;
	}

	public CountObject() {
	}

	public void setCount(Integer iC) {
		this.count = iC;
		
	}

	public int getCount() {
		return this.count;
	}

	public void setIndegree(int indegree) {
		this.classIndegree = indegree;
		
	}


	public void setOutdegree(int outdegree) {
		this.classOutdegree = outdegree;
		
	}

	public void setMedianOutdegree(Double median) {
		this.classInstanceOutdegreeMed = median;
		
	}

	public void setMedianIndegree(Double median) {
		this.classInstanceIndegreeMed = median;
		
	}

	public void setValue(String varName, Double resultD) {
		switch(varName) {
		case "minIndegree":
			this.classInstanceIndegreeMin = resultD;
			break;
		case "avgIndegree":
			this.classInstanceIndegreeAvg = resultD;
			break;
		case "maxIndegree":
			this.classInstanceIndegreeMax = resultD;
			break;
		case "minOutdegree":
			this.classInstanceOutdegreeMin = resultD;
			break;
		case "avgOutdegree":
			this.classInstanceOutdegreeAvg = resultD;
			break;
		case "maxOutdegree":
			this.classInstanceOutdegreeMax = resultD;
			break;
		default:
			System.out.println("error for setting value in CountObject.");
		}
			
	}

	public void printAll() {
		System.out.println(this.className + ", "+ this.count + ", " + this.classIndegree + ", "+ this.classOutdegree  + ", "+
			this.classInstanceIndegreeMin+ ", "+ this.classInstanceIndegreeAvg  + ", "+ this.classInstanceIndegreeMed  + ", "+ this.classInstanceIndegreeMax + ", " +
			this.classInstanceOutdegreeMin + ", "+ this.classInstanceOutdegreeAvg  + ", "+ this.classInstanceOutdegreeMed  + ", " + this.classInstanceOutdegreeMax);
		
	}
	
}
