package research.dartfish;

import java.io.IOException;
import java.util.Map;

import research.csv.CsvIn;
import research.csv.CsvOut;
import research.math.Vector3;
import research.math.Vectors;
import research.util.Collectionz;

public class CalculateIntermediaryValues
{
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
				
		        double elbowAngleDegrees = Vectors.findAngleBetweenLimbsOfThreeJoints(shoulder, elbow, wrist);
		        double shoulderAngleDegrees = Vectors.findAngleBetweenLimbsOfThreeJoints(trocanter, shoulder, elbow);
		        Vector3 velocity = null;
		        Double speed = null;
		        
		        Map<String, Object> prevRow = csvIn.getRow(i-1);
		        if (Collectionz.mapContainsAll(prevRow, "sho", "elb", "wr", "troc", "time"))
		        {
					Vector3 prevWrist = csvIn.get(prevRow, "wr");
					double prevTime = csvIn.get(prevRow, "time");
					
					double dt = time - prevTime;
					velocity = wrist.subtract(prevWrist).divide(dt);
					speed = velocity.length();
		        }
		        
		        upperBody = new Object[] {
		        	"elbowAngle", elbowAngleDegrees, 
		        	"shoulderAngle", shoulderAngleDegrees,
		        	"wristVelocity", velocity,
		        	"wristSpeed", speed
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
		calculator.process("data/rt1.tsv", "data/rt1-intermediate.tsv");
	}
	

}
