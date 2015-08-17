package research.dartfish;

import java.io.IOException;

import research.csv.CsvIn;
import research.csv.CsvOut;
import research.csv.CsvUtils;
import research.csv.find.All;
import research.csv.find.First;
import research.csv.find.GreaterThan;
import research.csv.find.LessThan;
import research.exceptions.DataMissing;
import research.util.Collectionz;
import research.util.LogOut;
import research.util.Pair;

public class CalculateFinal
{
	static LogOut log = new LogOut(CalculateFinal.class);

	static final double
	THRESHOLD_OF_MAX_START_VELOCITY = 0.05,
	THRESHOLD_OF_MAX_MID_VELOCITY = 0.50,
	THRESHOLD_OF_MAX_STOP_VELOCITY = 0.05;

	public void process(String inFileName, String outFileName) throws IOException
	{
		CsvIn csvIn = new CsvIn(inFileName);
		CsvOut csvOut = new CsvOut();
		csvOut.prefix = csvIn.prefix;

		if (Collectionz.mapContainsAll(csvIn.columns, "sho", "elb", "wr", "troc", "time", "wristSpeed"))
		{
			try
			{
				double maxWristSpeed = 
					CsvUtils.findValueInColumn(
					csvIn, "wristSpeed", -1, 
					new All<Double>(new GreaterThan<Double>())
				).first;

				double wristSpeedStartThreshold = maxWristSpeed * THRESHOLD_OF_MAX_START_VELOCITY;
				double wristSpeedMidThreshold = maxWristSpeed * THRESHOLD_OF_MAX_MID_VELOCITY;
				double wristSpeedStopThreshold = maxWristSpeed * THRESHOLD_OF_MAX_STOP_VELOCITY;

				Pair<Double, Integer> begin = CsvUtils.findValueInColumn(
					csvIn, "wristSpeed", -1,
					new First<Double>(new GreaterThan<Double>(), wristSpeedStartThreshold)
				);

				if (begin == null)
					throw new DataMissing("Could not upper body wrist begin");

				csvOut.addRow(
					csvIn.getRowSubset(begin.second, "elbowAngle", "shoulderAngle", "time", "wristSpeed"), 
					"maxWristSpeed", maxWristSpeed,
					"threshold", wristSpeedStartThreshold
				);

				Pair<Double, Integer> mid = CsvUtils.findValueInColumn(
					csvIn, "wristSpeed", begin.second,
					new First<Double>(new GreaterThan<Double>(), wristSpeedMidThreshold)
				);

				if (mid == null)
					throw new DataMissing("Could not upper body wrist mid point");

				csvOut.addRow(
					csvIn.getRowSubset(mid.second,  "elbowAngle", "shoulderAngle", "time", "wristSpeed"),
					"maxWristSpeed", maxWristSpeed,
					"threshold", wristSpeedMidThreshold
				);

				Pair<Double, Integer> end = CsvUtils.findValueInColumn(csvIn, "wristSpeed", mid.second,
					new First<Double>(new LessThan<Double>(), wristSpeedStopThreshold)
				);

				if (end == null)
					throw new DataMissing("Could not upper body wrist end point");

				csvOut.addRow(
					csvIn.getRowSubset(end.second, "elbowAngle", "shoulderAngle", "time", "wristSpeed"),
					"maxWristSpeed", maxWristSpeed,
					"threshold", wristSpeedStopThreshold
				);
			}
			catch (Exception e)
			{
				log.println("caught exception: " + e);
			}
		}

		csvOut.write(outFileName);
	}

	public static void main(String[] args) throws Exception
	{
		CalculateFinal calculator = new CalculateFinal();
		calculator.process("data/rt1-intermediate.tsv", "data/rt1-final.tsv");
	}

}
