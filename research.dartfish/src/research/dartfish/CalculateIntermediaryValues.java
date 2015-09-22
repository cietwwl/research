package research.dartfish;

import java.io.IOException;
import java.util.Map;

import research.csv.CsvIn;
import research.csv.CsvOut;
import research.math.Vector3;
import research.math.Vectors;
import research.util.Collectionz;
import research.util.LogOut;
import research.util.Pair;

public class CalculateIntermediaryValues
{
	static LogOut log = new LogOut(CalculateIntermediaryValues.class);
	
	static final double
	THRESHOLD_OF_MAX_START_VELOCITY = 0.05,
	THRESHOLD_OF_MAX_MID_VELOCITY = 0.50,
	THRESHOLD_OF_MAX_STOP_VELOCITY = 0.05;

	public void process(String inFileName, String outFileName) throws IOException
	{
		CsvIn csvIn = new CsvIn(inFileName);
		CsvOut csvOut = new CsvOut();
		csvOut.prefix = csvIn.prefix;
		
		for (int i=0; i<csvIn.rows.size(); ++i)
		{
			Map<String, Object> row = csvIn.getRow(i);
			
			Object[] upperBody = new Object[0];
			Object[] lowerBody = new Object[0];
			
			if (Collectionz.mapContainsAll(row, "sho", "elb", "wr", "troc", "time"))
			{
				Vector3 shoulder = csvIn.get(row, "sho");
				Vector3 elbow = csvIn.get(row, "elb");
				Vector3 wrist = csvIn.get(row, "wr");
				Vector3 trocanter = csvIn.get(row, "troc");
				double time = csvIn.get(row, "time");
				
//				Vectors.multiply(Vector3.UnitX_Z, shoulder, elbow, wrist, trocanter);
				
		        Pair<Double, Vector3> elbowAngleDegrees = Vectors.findAngleAndAxisBetweenLimbsOfThreeJoints(shoulder, elbow, wrist);
		        Pair<Double, Vector3> shoulderAngleDegrees = Vectors.findAngleAndAxisBetweenLimbsOfThreeJoints(trocanter, shoulder, elbow);

		        shoulderAngleDegrees.first *= Vectors.unitOnAxis(shoulderAngleDegrees.second, Vector3.UnitNegY);

		        Vector3 velocity = null;
		        Double speed = null;
		        
		        Map<String, Object> prevRow = csvIn.getRow(i-1);
		        if (Collectionz.mapContainsAll(prevRow, "sho", "elb", "wr", "troc", "time"))
		        {
					Vector3 prevWrist = csvIn.get(prevRow, "wr");
					double prevTime = csvIn.get(prevRow, "time");
					
//					Vectors.multiply(Vector3.UnitX_Z, prevWrist);
					
					double dt = time - prevTime;
					velocity = wrist.subtract(prevWrist).divide(dt);
					speed = velocity.length();
		        }
		        
		        upperBody = new Object[] {
		        	"elbowAngle", elbowAngleDegrees.first, 
		        	"shoulderAngle", shoulderAngleDegrees.first,
		        	"wristVelocity", velocity,
		        	"wristSpeed", speed
		        };
			}
			
			if (Collectionz.mapContainsAll(row, "c7", "sac", "troc", "knee", "ank", "heel", "toe", "time"))
			{
				Vector3 c7 = csvIn.get(row, "c7");
				Vector3 sacrum = csvIn.get(row, "sac");
				Vector3 trocanter = csvIn.get(row, "troc");
				Vector3 knee = csvIn.get(row, "knee");
				Vector3 ankle = csvIn.get(row, "ank");
				Vector3 heel = csvIn.get(row, "heel");
				Vector3 toe = csvIn.get(row, "toe");
				double time = csvIn.get(row, "time");
				
//				Vectors.multiply(Vector3.UnitX_Z, c7, sacrum, trocanter, knee, ankle, heel, toe);
				
				Vector3 distanceTrocanterToSacrum = trocanter.subtract(sacrum);
				
		        Pair<Double, Vector3> hipAngleDegrees = Vectors.findAngleAndAxisBetweenTwoLimbsOfFourJoints(c7, sacrum, trocanter, knee);
		        Pair<Double, Vector3> kneeAngleDegrees = Vectors.findAngleAndAxisBetweenLimbsOfThreeJoints(trocanter, knee, ankle);
		        Pair<Double, Vector3> footAngleDegrees = Vectors.findAngleAndAxisBetweenTwoLimbsOfFourJoints(knee, ankle, heel, toe);
		        
		        hipAngleDegrees.first *= Vectors.unitOnAxis(hipAngleDegrees.second, Vector3.UnitNegY);
		        kneeAngleDegrees.first *= Vectors.unitOnAxis(kneeAngleDegrees.second, Vector3.UnitNegY);
		        footAngleDegrees.first *= Vectors.unitOnAxis(footAngleDegrees.second, Vector3.UnitNegY);

		        Vector3 velocity = null;
		        Double speed = null;
		        
		        Map<String, Object> prevRow = csvIn.getRow(i-1);
		        if (Collectionz.mapContainsAll(prevRow, "heel", "time"))
		        {
					Vector3 prevHeel = csvIn.get(prevRow, "heel");
					double prevTime = csvIn.get(prevRow, "time");
					
//					Vectors.multiply(Vector3.UnitX_Z, prevHeel);
					
					double dt = time - prevTime;
					velocity = heel.subtract(prevHeel).divide(dt);
					speed = velocity.length();
		        }
		        
		        lowerBody = new Object[] {
		        	"_hipAngle", hipAngleDegrees.first, 
		        	"_kneeAngle", kneeAngleDegrees.first,
		        	"_footAngle", footAngleDegrees.first,
		        	"_distanceTrocanterToSacrum", distanceTrocanterToSacrum.length(),
		        	"_heelVelocity", velocity,
		        	"_heelSpeed", speed
		        };
			}

			csvOut.addRow(
	        	csvIn.getRow(i),
	        	upperBody,
	        	lowerBody
	        );
		}
		
		csvOut.write(outFileName);
	}
	
	public static void main(String[] args) throws Exception
	{
		CalculateIntermediaryValues calculator = new CalculateIntermediaryValues();
		calculator.process(
			"data/converted/Dartfish Data ALL/Pilot 6_6_13/MR/Session 1/Rt1.tsv", 
			"data/processed/Dartfish Data ALL/Pilot 6_6_13/MR/Session 1/Rt1.tsv");
	}
	

}
