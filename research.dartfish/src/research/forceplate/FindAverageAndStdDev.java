package research.forceplate;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import research.csv.CsvIn;
import research.csv.CsvOut;
import research.math.Maths;
import research.math.Vector3;
import research.util.Collectionz;
import research.util.Filez;

public class FindAverageAndStdDev
{
	public void compute (String inFileName, String outFileName) throws Exception
	{
		CsvIn csvIn = new CsvIn(inFileName, 1, 13);
		CsvOut csvOut = new CsvOut();
		csvOut.prefix = csvIn.prefix;
		
		ArrayList<Vector3> speedsList = new ArrayList<Vector3>();
		
		double FPS = 360.0;
		double SPF = 1.0 / FPS;

		for (int i=1; i<csvIn.rows.size(); ++i)
		{
			// COP:X, COP:Y
			// framerate 360 per second
			
			int lastRow = i-1;
			int currentRow = i;
			Vector3 last = (Vector3) csvIn.rows.get(lastRow).get("COP");
			Vector3 current = (Vector3) csvIn.rows.get(currentRow).get("COP");
			
			Vector3 dp = current.subtract(last);
			Vector3 v = dp.multiply(FPS);
			Vector3 s = new Vector3(Math.abs(v.x), Math.abs(v.y), v.length());
			
			speedsList.add(s);
		}
		
		double duration = csvIn.rows.size() * SPF;
		
		double[] speedsX = Maths.only(speedsList, 0);
		double[] speedsY = Maths.only(speedsList, 1);
		double[] speeds = Maths.only(speedsList, 2);
		
		double averageSpeedX = Maths.average(speedsX);
		double averageSpeedY = Maths.average(speedsY);
		double averageSpeed = Maths.average(speeds);
		
		double stddevX = Maths.stddev(speedsX);
		double stddevY = Maths.stddev(speedsY);
		double stddev = Maths.stddev(speeds);
		
		CsvOut out = new CsvOut();
		out.addRow(
			"averageSpeedX", averageSpeedX,
			"averageSpeedY", averageSpeedY,
			"averageSpeed", averageSpeed, 
			"standardDeviationX", stddevX, 
			"standardDeviationY", stddevY, 
			"standardDeviation", stddev, 
			"duration", duration
		);
		out.write(outFileName);
	}

	public static void main (String args[]) throws Exception
	{
		FindAverageAndStdDev computer = new FindAverageAndStdDev();
		computer.compute ("data/fp-in.tsv", "data/fp-out.tsv");
	}
	
}
