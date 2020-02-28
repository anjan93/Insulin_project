package application;

public class BloodSugarLevelCalculation {
	private static BloodSugarLevelCalculation BSLCalculation = null;
	public static double k1 = 0.0453;
	public static double k2 = 0.0224;
	//public static double k1 = 0.0453;
	//public static double k2 = 0.0224;
	private double bloodsugar = StaticValuesforBSL.CurrentBSL; 

	public BloodSugarLevelCalculation() {
	}

	public static synchronized BloodSugarLevelCalculation getInstance() {

		if (BSLCalculation == null) {
			BSLCalculation = new BloodSugarLevelCalculation();
		}
		return BSLCalculation;

	}
	
	public double checkBloodGlucose() {
		return getBloodSugar();
	}

	public double bslOnIdeal() {
		bloodsugar = StaticValuesforBSL.CurrentBSL;
		bloodsugar -= (5 * (k1 / (k2 - k1)) * (Math.exp(-k1 * 5) - Math.exp(-k2 * 5)));
		return bloodsugar;
	}

	public double bslAfterActivity(double carbs, int t) {
		if (carbs != 0) {
			//System.out.println("carbs===>"+carbs);
			//bloodsugar = (StaticValues.PreviousBSL * Math.exp(-k2 * t)) +  (2 * carbs * (k1 / (k2 - k1)) * (Math.exp(-k1 * t) - Math.exp(-k2 * t)));
			//bloodsugar = (StaticValuesforBSL.PreviousBSL) +  (2 * carbs * (k1 / (k2 - k1)) * (Math.exp(-k1 * 3 * t) - Math.exp(-k2 * 3 * t)));
			bloodsugar = (StaticValuesforBSL.PreviousBSL) +  ( carbs * (k1 / (k2 - k1)) * (Math.exp(-k1 * 1 * t) - Math.exp(-k2 * 1 * t)));
		}
		return bloodsugar;
	}

	public double bslOnInsulinDosage(double insulin) {
		bloodsugar = StaticValuesforBSL.CurrentBSL;
		if (insulin > 0) {
			bloodsugar -= (StaticValuesforBSL.ISF * insulin);
		}
		
		return bloodsugar;
	}

	public double bslOnGlucagonDosage(double glucagon) {
		if(glucagon < 0) 
			return StaticValuesforBSL.CurrentBSL;
			
		bloodsugar = StaticValuesforBSL.CurrentBSL;
		bloodsugar += glucagon * 3;
		return bloodsugar;
	}

	private double getBloodSugar() {
		return bloodsugar;
	}
}
