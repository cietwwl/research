package research.dartfish;

import java.io.IOException;
import java.util.Map;

import research.csv.CsvIn;
import research.csv.CsvOut;
import research.csv.CsvUtils;
import research.csv.find.All;
import research.csv.find.Compare;
import research.csv.find.First;
import research.csv.find.GreaterThan;
import research.csv.find.IsNot;
import research.csv.find.LessThan;
import research.csv.find.LessThanVector3;
import research.csv.find.RunningAverageVector3;
import research.exceptions.DataMissing;
import research.math.Vector3;
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

	CsvOut finalCsv = new CsvOut();
	
	public CalculateFinal () throws Exception
	{
		
	}
	
	public void process(String inFileName, String outFileName) throws IOException
	{
		CsvIn csvIn = new CsvIn(inFileName);
		CsvOut csvOut = new CsvOut();
		csvOut.prefix = csvIn.prefix;

		if (inFileName.toLowerCase().contains("/rt") &&
			Collectionz.mapContainsAll(csvIn.columns, "sho", "elb", "wr", "troc", "time", "wristSpeed"))
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
				
				//----
				
				finalCsv.addRow(
					"Subject", csvIn.prefix, 
					"FileName", inFileName,
					"VSHON", csvIn.getRow(begin.second).get("shoulderAngle"),
					"VSHOFF", csvIn.getRow(end.second).get("shoulderAngle"),
					"VELON", csvIn.getRow(begin.second).get("elbowAngle"),
					"VELOFF", csvIn.getRow(end.second).get("elbowAngle")
				);
			}
			catch (Exception e)
			{
				log.println("caught exception: " + e);
			}
		}
		
		if (inFileName.toLowerCase().contains("/gt") &&
			Collectionz.mapContainsAll(csvIn.columns, "c7", "sac", "troc", "knee", "ank", "heel", "toe"))
		{
			try
			{
				Pair<Double, Integer> maxTime = CsvUtils.findValueInColumn(
					csvIn, "time", -1,
					new All<Double>(new GreaterThan<Double>())
				);
				
				Pair<Vector3, Integer> stat = CsvUtils.findValueInColumn(
						csvIn, "stat", -1,
						new RunningAverageVector3(
							new IsNot<Vector3>(new Compare<Vector3>(), Vector3.Zero)
						)
					);
				
				Pair<Vector3, Integer> toePassesStat = CsvUtils.findValueInColumn(
					csvIn, "toe", -1,
					new First<Vector3>(
						new LessThanVector3(Vector3.UnitX), 
						stat.first, 
						new IsNot<Vector3>(new Compare<Vector3>(), Vector3.Zero)
					)
				);
				
				Pair<Double, Integer> heelStop = CsvUtils.findValueInColumn(
						csvIn, "_heelSpeed", toePassesStat.second,
						new First<Double>(
							new LessThan<Double>(), 
							(double) 50,
							new IsNot<Double>(new Compare<Double>(), 0.0)
						)
					);

				if (heelStop == null)
					throw new DataMissing("heel doesn't stop");

				Pair<Double, Integer> heelMoving = CsvUtils.findValueInColumn(
						csvIn, "_heelSpeed", heelStop.second,
						new First<Double>(
							new GreaterThan<Double>(), 
							(double) 3000,
							new IsNot<Double>(new Compare<Double>(), 0.0)
						)
					);
				
				if (heelMoving == null)
					throw new DataMissing("heel doesn't move after stopping");

				Pair<Double, Integer> heelStop2 = CsvUtils.findValueInColumn(
						csvIn, "_heelSpeed", heelMoving.second,
						new First<Double>(
							new LessThan<Double>(), 
							(double) 50,
							new IsNot<Double>(new Compare<Double>(), 0.0)
						)
					);

				if (heelStop2 == null)
					throw new DataMissing("heel doesn't stop again after moving");
				
				finalCsv.addRow(
					"Subject", csvIn.prefix, 
					"FileName", inFileName,
					"statFinal", stat.first,
					"statFinalFrames", stat.second,
					"toePassesStatFinal", toePassesStat.first,
					"toePassesStatFinalFrame", toePassesStat.second,
					"heelStop", heelStop.first,
					"heelStopFrame", heelStop.second,
					"heelMoving", heelMoving.first,
					"heelMovingFrame", heelMoving.second,
					"heelStop2", heelStop2.first,
					"heelStop2Frame", heelStop2.second
				);

			}
			catch (Exception e)
			{
				log.println(e);
			}
		}

		csvOut.write(outFileName);
	}

	public void finish (String outFileName) throws IOException
	{
		finalCsv.write(outFileName);
	}
	
	public static void main(String[] args) throws Exception
	{
		CalculateFinal calculator = new CalculateFinal();
		calculator.process("data/rt1-intermediate.tsv", "data/rt1-final.tsv");
	}

}
