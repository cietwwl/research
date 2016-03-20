package research.balance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.geometry.partitioning.utilities.AVLTree;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import research.balance.FindOneAnkleDistances.Comp;
import research.csv.CsvIn;
import research.csv.CsvOut;
import research.math.Maths;
import research.math.Maths.PeakFinder;
import research.math.Vector3;
import research.math.Vectors;
import research.util.Collectionz;
import research.util.Filez;
import research.util.LogOut;
import research.util.Pair;
import research.util.RegE;

public class FindStandingLegAnglePeaks
{
	static LogOut log = new LogOut(FindStandingLegAnglePeaks.class);
	
	
	static Map<String,Integer> fixes = Collectionz.toMap(
			/*
			"BL_PRE_DD_LL1_ANKLE", -1,
			"CS_PRE_DD_LL3_KNEE", -1,
			"EB_POST_DD_LL2_ANKLE", -1,
			"EB_POST_DD_LL2_KNEE", -1,
			"EB_PRE_DD_LL3_ANKLE", -1,
			"EB_PRE_DD_LL3_KNEE", -1,
			"HR_PRE_DD_LL1_ANKLE", -1,
			"JB_PRE_DD_LL2_ANKLE", -2,
			"JB_PRE_DD_LL2_KNEE", -3,
			"JB_PRE_DD_LL3_ANKLE", -1,
			"JTB_POST_DD_NM2_KNEE", -1,
			"LK_PRE_DD_NM1_ANKLE", -1,
			"LK_PRE_DD_M2_ANKLE", -1,
			"LS_POST_DD_LL1_ANKLE", -1,
			"MG_POST_DD_LL1_ANKLE", -1,
			"MG_POST_DD_LL3_ANKLE", -1,
			"MG_POST_DD_LL3_KNEE", -1,
			"MG_PRE_DD_LL1_ANKLE", -1,
			"SK_POST_DD_LL1_ANKLE", -1,
			"SD_POST_DD_LL3_ANKLE", -1,
			"SK_POST_DD_LL3_KNEE", -1,
			
			"SG_POST_DD_LL1_ANKLE", -2,
			"SG_POST_DD_LL2_ANKLE", -2,
			"SG_POST_DD_LL3_ANKLE", -2,
			"SG_POST_DD_NM1_ANKLE", -2,
			"SG_POST_DD_NM2_ANKLE", -2,
			"SG_POST_DD_NM3_ANKLE", -2,
			"SG_POST_DD_M1_ANKLE", -2,
			"SG_POST_DD_M2_ANKLE", -2,
			"SG_POST_DD_M3_ANKLE", -2,

			"SG_PRE_DD_LL1_ANKLE", -2,
			"SG_PRE_DD_LL2_ANKLE", -2,
			"SG_PRE_DD_LL3_ANKLE", -2,
			"SG_PRE_DD_NM1_ANKLE", -2,
			"SG_PRE_DD_NM2_ANKLE", -2,
			"SG_PRE_DD_NM3_ANKLE", -2,
			"SG_PRE_DD_M1_ANKLE", -2,
			"SG_PRE_DD_M2_ANKLE", -2,
			"SG_PRE_DD_M3_ANKLE", -2
			*/
		);
		
	
	static public class Comp
	{
		public int frame;
		public Double sacrum;
		public Double sacrumBW;
		public Double sacrumSmooth, sacrumAverage, sacrumTest;
		public Double[] ankleAngles = { null,null };
		public Double[] kneeAngles = { null,null };
		
		public Double ankleAngleLeft;
		public Double ankleAngleLeftBW;
		public Double ankleAngleLeftSmooth;
		public Double ankleAngleLeftAverage;
		public Double ankleAngleLeftTest;

		public Double ankleAngleRight;
		public Double ankleAngleRightBW;
		public Double ankleAngleRightSmooth;
		public Double ankleAngleRightAverage;
		public Double ankleAngleRightTest;

		public Double kneeAngleLeft;
		public Double kneeAngleLeftBW;
		public Double kneeAngleLeftSmooth;
		public Double kneeAngleLeftAverage;
		public Double kneeAngleLeftTest;
		
		public Double kneeAngleRight;
		public Double kneeAngleRightBW;
		public Double kneeAngleRightSmooth;
		public Double kneeAngleRightAverage;
		public Double kneeAngleRightTest;
		
		public Double pelvicAngle;
		public Double pelvicAngleBW;
		public Double pelvicAngleSmooth;
		public Double pelvicAngleAverage;
		public Double pelvicAngleTest;
	}
	
	static void makeChart (ArrayList<Comp> comps, String outFileName) throws IOException
	{

		final XYSeries sacrum = new XYSeries("Sacrum");
		final XYSeries sacrumBW = new XYSeries("Sacrum BW");
		final XYSeries sacrumAverage = new XYSeries("SacrumAverage");
		final XYSeries sacrumSmooth = new XYSeries("SacrumSmooth");
		final XYSeries sacrumTest = new XYSeries("SacrumTest");
		
		final XYSeries ankleAngleLeft = new XYSeries("Left - AnkleAngle");
		final XYSeries ankleAngleLeftSmooth = new XYSeries("Left - AnkleAngle S");
		final XYSeries ankleAngleLeftTest = new XYSeries("Left - AnkleAngle T");
		final XYSeries ankleAngleLeftBW = new XYSeries("Left - AnkleAngle BW");

		final XYSeries ankleAngleRight = new XYSeries("Right - ANKLE");
		final XYSeries ankleAngleRightSmooth = new XYSeries("Right - AnkleAngle S");
		final XYSeries ankleAngleRightTest = new XYSeries("Right - AnkleAngle T");
		final XYSeries ankleAngleRightBW = new XYSeries("Right - AnkleAngle BW");

		final XYSeries kneeAngleLeft = new XYSeries("Left - KneeAngle");
		final XYSeries kneeAngleLeftBW = new XYSeries("Left - KneeAngle BW");
		final XYSeries kneeAngleLeftSmooth = new XYSeries("Left - KneeAngle S");
		final XYSeries kneeAngleLeftTest = new XYSeries("Left - KneeAngle T");

		final XYSeries kneeAngleRight = new XYSeries("Right - KneeAngle");
		final XYSeries kneeAngleRightBW = new XYSeries("Right - KneeAngle BW");
		final XYSeries kneeAngleRightSmooth = new XYSeries("Right - KneeAngle S");
		final XYSeries kneeAngleRightTest = new XYSeries("Right - KneeAngle T");
		
		final XYSeries pelvicAngle = new XYSeries("Pelvic Angle");
		final XYSeries pelvicAngleBW = new XYSeries("Pelvic Angle BW");
		final XYSeries pelvicAngleSmooth = new XYSeries("Pelvic Angle Smooth");
		final XYSeries pelvicAngleAverage = new XYSeries("Pelvic Angle Average");
		final XYSeries pelvicAngleTest = new XYSeries("Pelvic Angle Test");

		for (Comp c: comps)
		{
			double f = c.frame;
			
			sacrum.add(f, c.sacrum != null ? ((c.sacrum - 500.0)/5.0) : null);
			sacrumBW.add(f, c.sacrumBW);
			sacrumAverage.add(f, c.sacrumAverage);
			sacrumSmooth.add(f, c.sacrumSmooth);
			sacrumTest.add(f, c.sacrumTest);
			
			ankleAngleLeft.add(f, c.ankleAngleLeft);
			ankleAngleLeftBW.add(f, c.ankleAngleLeftBW);
			ankleAngleLeftSmooth.add(f, c.ankleAngleLeftSmooth);
			ankleAngleLeftTest.add(f, c.ankleAngleLeftTest);

			ankleAngleRight.add(f, c.ankleAngleRight);
			ankleAngleRightBW.add(f, c.ankleAngleRightBW);
			ankleAngleRightSmooth.add(f, c.ankleAngleRightSmooth);
			ankleAngleRightTest.add(f, c.ankleAngleRightTest);
			
			kneeAngleLeft.add(f, c.kneeAngleLeft);
			kneeAngleLeftBW.add(f, c.kneeAngleLeftBW);
			kneeAngleLeftSmooth.add(f, c.kneeAngleLeftSmooth);
			kneeAngleLeftTest.add(f, c.kneeAngleLeftTest);

			kneeAngleRight.add(f, c.kneeAngleRight);
			kneeAngleRightBW.add(f, c.kneeAngleRightBW);
			kneeAngleRightSmooth.add(f, c.kneeAngleRightSmooth);
			kneeAngleRightTest.add(f, c.kneeAngleRightTest);
			
			pelvicAngle.add(f, c.pelvicAngle);
			pelvicAngleBW.add(f, c.pelvicAngleBW);
			pelvicAngleSmooth.add(f, c.pelvicAngleSmooth);
			pelvicAngleAverage.add(f, c.pelvicAngleAverage);
			pelvicAngleTest.add(f, c.pelvicAngleTest);
		}
		
		final XYSeriesCollection dataset = new XYSeriesCollection();
//		dataset.addSeries(sacrumAverage);
//		dataset.addSeries(sacrumSmooth);
//		dataset.addSeries(sacrumTest);
//		dataset.addSeries(ankleAngleLeft);
//		dataset.addSeries(kneeAngleLeft);

		dataset.addSeries(sacrum);
//		dataset.addSeries(ankleAngleRightBW);
//		dataset.addSeries(kneeAngleRightBW);
//		dataset.addSeries(ankleAngleLeftBW);
//		dataset.addSeries(kneeAngleLeftBW);

		dataset.addSeries(ankleAngleRight);
		dataset.addSeries(kneeAngleRight);
		dataset.addSeries(ankleAngleLeft);
		dataset.addSeries(kneeAngleLeft);
		
//		dataset.addSeries(pelvicAngle);
//		dataset.addSeries(pelvicAngleBW);
//		dataset.addSeries(pelvicAngleAverage);
//		dataset.addSeries(pelvicAngleTest);
		
		final JFreeChart chart = ChartFactory.createXYLineChart(
	            outFileName,      // chart title
	            "X",                      // x axis label
	            "Y",                      // y axis label
	            dataset,                  // data
	            PlotOrientation.VERTICAL,
	            true,                     // include legend
	            false,                     // tooltips
	            false                     // urls
	        );		

		String outFileNameComp = outFileName.replaceAll("/", "_");
		Filez.ensureDirectory("data/standingleg-pictures/");
		ChartUtilities.saveChartAsPNG(new File("data/standingleg-pictures/" + outFileNameComp), chart, 1024, 768);
	}
	
	public static void compute (String inFileName, String outFileName, String peakOutFileName, String maxPeakFileName, ArrayList<String> flags) throws Exception
	{
		CsvIn csvIn = new CsvIn(inFileName);
		ArrayList<Comp> comps = new ArrayList<Comp>();

		for (int i=0; i<csvIn.rows.size(); ++i)
		{
			Comp c = new Comp();
			c.frame = i;
			comps.add(c);

			int currentRow = i;
			Map<String, Object> row = csvIn.rows.get(currentRow);

			if (Collectionz.mapContainsAllAndNotNull(row, S.centerHip))
				c.sacrum = ((Vector3) row.get(S.centerHip)).z;
			
			for (int s=S.left; s<=S.right; ++s)
			{
				if (Collectionz.mapContainsAllAndNotNullAndNotZero(row, S.toe[s], S.ankle[s], S.knee[s], S.hip[s]))
				{
					c.ankleAngles[s] = Vectors.findAngleAndAxisBetweenLimbsOfThreeJoints(
						(Vector3) row.get(S.toe[s]),
						(Vector3) row.get(S.ankle[s]),
						(Vector3) row.get(S.knee[s])					
					).first;
	
					c.kneeAngles[s] = Vectors.findAngleAndAxisBetweenLimbsOfThreeJoints(
						(Vector3) row.get(S.ankle[s]),
						(Vector3) row.get(S.knee[s]),
						(Vector3) row.get(S.hip[s])					
					).first;
				}
			}

			String[] possiblePoints = { S.leftHip, S.rightHip };
			String left = null, right = null;
			for (String p : possiblePoints)
			{
				if (row.get(p) != null)
				{
					if (left == null)
						left = p;
					else
						right = p;
				}
			}
			
			if (left != null && right != null)
			{
				Vector3 belowLeft = new Vector3((Vector3) row.get(left));
				belowLeft.z -= 100;
				c.pelvicAngle = Vectors.findAngleBetweenLimbsOfThreeJoints(
					belowLeft, 
					(Vector3) row.get(left),
					(Vector3) row.get(right)
				);
			}
				
			c.ankleAngleLeft = c.ankleAngles[S.left];
			c.ankleAngleRight = c.ankleAngles[S.right];
			c.kneeAngleLeft = c.kneeAngles[S.left];
			c.kneeAngleRight = c.kneeAngles[S.right];
		}
		
		double bandwidth = 0.2; // for creating test
//		double bandwidth = 0.025;
		int cycles = 4;
		
		int filterOrder = 1;
		double fcf1 = 0.05;
		
		Maths.butterworth(comps, "sacrum", "sacrumBW", filterOrder, fcf1);		
		Maths.smooth(comps, "sacrum", "sacrumSmooth", "sacrumAverage", "sacrumTest", bandwidth, cycles);
		List<Pair<Integer, Comp>> sacrumPeaks = Maths.findPeaks(comps, new Maths.PeakFinderDoubleReflection<Comp>("sacrum", "sacrumTest")); 

		double kneeAngleLeftAverage = Maths.average(comps, "kneeAngleLeft");
		double kneeAngleLeftStdDev = Maths.stddev(comps, "kneeAngleLeft");
		Double kneeAngleLeftMax = Maths.max(comps, "kneeAngleLeft");
		Maths.butterworth(comps, "kneeAngleLeft", "kneeAngleLeftBW", filterOrder, fcf1);
		Maths.smooth(comps, "kneeAngleLeft", "kneeAngleLeftSmooth", "kneeAngleLeftAverage", "kneeAngleLeftTest", bandwidth, cycles);
		List<Pair<Integer, Comp>> kneeAngleLeftPeaks = Maths.findPeaks(comps, new Maths.PeakFinderDoubleReflection<Comp>("kneeAngleLeftBW", "kneeAngleLeftTest")); 

		double kneeAngleRightAverage = Maths.average(comps, "kneeAngleRight");
		double kneeAngleRightStdDev = Maths.stddev(comps, "kneeAngleRight");
		Double kneeAngleRightMax = Maths.max(comps, "kneeAngleRight");
		Maths.butterworth(comps, "kneeAngleRight", "kneeAngleRightBW", filterOrder, fcf1);
		Maths.smooth(comps, "kneeAngleRight", "kneeAngleRightSmooth", "kneeAngleRightAverage", "kneeAngleRightTest", bandwidth, cycles);
		List<Pair<Integer, Comp>> kneeAngleRightPeaks = Maths.findPeaks(comps, new Maths.PeakFinderDoubleReflection<Comp>("kneeAngleRightBW", "kneeAngleRightTest")); 
		
		double ankleAngleLeftAverage = Maths.average(comps, "ankleAngleLeft");
		double ankleAngleLeftStdDev = Maths.stddev(comps, "ankleAngleLeft");
		Double ankleAngleLeftMax = Maths.max(comps, "ankleAngleLeft");
		Maths.butterworth(comps, "ankleAngleLeft", "ankleAngleLeftBW", filterOrder, fcf1);
		Maths.smooth(comps, "ankleAngleLeft", "ankleAngleLeftSmooth", "ankleAngleLeftAverage", "ankleAngleLeftTest", bandwidth, cycles);
		List<Pair<Integer, Comp>> ankleAngleLeftPeaks = Maths.findPeaks(comps, new Maths.PeakFinderDoubleReflection<Comp>("ankleAngleLeftBW", "ankleAngleLeftTest")); 
		
		double ankleAngleRightAverage = Maths.average(comps, "ankleAngleRight");
		double ankleAngleRightStdDev = Maths.stddev(comps, "ankleAngleRight");
		Double ankleAngleRightMax = Maths.max(comps, "ankleAngleRight");
		Maths.butterworth(comps, "ankleAngleRight", "ankleAngleRightBW", filterOrder, fcf1);
		Maths.smooth(comps, "ankleAngleRight", "ankleAngleRightSmooth", "ankleAngleRightAverage", "ankleAngleRightTest", bandwidth, cycles);
		List<Pair<Integer, Comp>> ankleAngleRightPeaks = Maths.findPeaks(comps, new Maths.PeakFinderDoubleReflection<Comp>("ankleAngleRightBW", "ankleAngleRightTest")); 

		double pelvicAngleAverage = Maths.average(comps, "pelvicAngle");
		double pelvicAngleStdDev = Maths.stddev(comps, "pelvicAngle");
		Double pelvicAngleMax = Maths.max(comps, "pelvicAngle");
		Maths.butterworth(comps, "pelvicAngle", "pelvicAngleBW", filterOrder, fcf1);
		Maths.smooth(comps, "pelvicAngle", "pelvicAngleSmooth", "pelvicAngleAverage", "pelvicAngleTest", bandwidth, cycles);
		List<Pair<Integer, Comp>> pelvicAnglePeaks = Maths.findPeaks(comps, new Maths.PeakFinderDoubleReflection<Comp>("pelvicAngleBW", "pelvicAngleTest")); 

		CsvOut csvOut = new CsvOut();
		CsvOut sacrumPeaksOut = new CsvOut();
		CsvOut kneeAngleLeftPeaksOut = new CsvOut();
		CsvOut kneeAngleRightPeaksOut = new CsvOut();
		CsvOut ankleAngleLeftPeaksOut = new CsvOut();
		CsvOut ankleAngleRightPeaksOut = new CsvOut();
		CsvOut characteristicsPeaksOut = new CsvOut();
		csvOut.prefix = csvIn.prefix;

		for (Comp c : comps)
		{
			csvOut.addRow(
				"frame", c.frame,
				"sacrum", c.sacrum,
				"ankleAngleLeft", c.ankleAngleLeft,
				"ankleAngleRight", c.ankleAngleRight,
				"kneeAngleLeft", c.kneeAngleLeft,
				"kneeAngleRight", c.kneeAngleRight
			);
		}
		csvOut.write(outFileName);
		
		for (Pair<Integer, Comp> p : sacrumPeaks)
		{
			sacrumPeaksOut.addRow(
				"direction", p.first,
				"frame", p.second.frame,
				"sacrum", p.second.sacrum
			);
		}
		sacrumPeaksOut.write(peakOutFileName);

		for (Pair<Integer, Comp> p : kneeAngleLeftPeaks)
		{
			kneeAngleLeftPeaksOut.addRow(
				"direction", p.first,
				"frame", p.second.frame,
				"kneeAngleLeft", p.second.kneeAngleLeft
			);
		}
		kneeAngleLeftPeaksOut.write(peakOutFileName, true);

		for (Pair<Integer, Comp> p : kneeAngleRightPeaks)
		{
			kneeAngleRightPeaksOut.addRow(
				"direction", p.first,
				"frame", p.second.frame,
				"kneeAngleRight", p.second.kneeAngleRight
			);
		}
		kneeAngleRightPeaksOut.write(peakOutFileName, true);
		
		for (Pair<Integer, Comp> p : ankleAngleLeftPeaks)
		{
			ankleAngleLeftPeaksOut.addRow(
				"direction", p.first,
				"frame", p.second.frame,
				"ankleAngleLeft", p.second.ankleAngleLeft
			);
		}
		ankleAngleLeftPeaksOut.write(peakOutFileName, true);

		for (Pair<Integer, Comp> p : ankleAngleRightPeaks)
		{
			ankleAngleRightPeaksOut.addRow(
				"direction", p.first,
				"frame", p.second.frame,
				"ankleAngleRight", p.second.ankleAngleRight
			);
		}
		ankleAngleRightPeaksOut.write(peakOutFileName, true);

		characteristicsPeaksOut.addRow(
			"ankleAngleRightAverage", ankleAngleRightAverage, 
			"ankleAngleRightStdDev", ankleAngleRightStdDev, 
			"ankleAngleRightMax", ankleAngleRightMax,
			"kneeAngleRightAverage", kneeAngleRightAverage, 
			"kneeAngleRightStdDev", kneeAngleRightStdDev, 
			"kneeAngleRightMax", kneeAngleRightMax,
			"pelvicAngleAverage", pelvicAngleAverage,
			"pelvicAngleStdDev", pelvicAngleStdDev,
			"pelvicAngleMax", pelvicAngleMax
		);
		characteristicsPeaksOut.write(peakOutFileName, true);
		
		characteristicsPeaksOut = new CsvOut();
		characteristicsPeaksOut.addRow(
			"ankleAngleLeftAverage", ankleAngleLeftAverage, 
			"ankleAngleLeftStdDev", ankleAngleLeftStdDev, 
			"ankleAngleLeftMax", ankleAngleLeftMax,
			"kneeAngleLeftAverage", kneeAngleLeftAverage, 
			"kneeAngleLeftStdDev", kneeAngleLeftStdDev, 
			"kneeAngleLeftMax", kneeAngleLeftMax
		);
		
		characteristicsPeaksOut.write(peakOutFileName, true);
		
		// MaxKnee1	MaxKnee2	MaxKnee3	AvgKnee	MaxAnk1	MaxAnk2	MaxAnk3	AvgAnk	PelAvg	PelMax	PelSD
		Map<String,Double> finalData = new HashMap<String,Double>();

		boolean reversedSubject = false;
		String[] reversed = { "ap", "gs" };
		for (String r : reversed)
		{
			if (inFileName.toLowerCase().contains("/"+r+"/"))
				reversedSubject = true;
		}
		
		List<Pair<Integer, Comp>> anklePeaks = reversedSubject ? ankleAngleRightPeaks : ankleAngleLeftPeaks;
		List<Pair<Integer, Comp>> kneePeaks = reversedSubject ? kneeAngleRightPeaks : kneeAngleLeftPeaks;
		String ankleField = reversedSubject ? "ankleAngleRight" : "ankleAngleLeft";
		String kneeField = reversedSubject ? "kneeAngleRight" : "kneeAngleLeft";
		
		String[] m = RegE.match(inFileName, ".*/(.+?)/(.+?)/(.+?)[_ ]+(.+?)\\..*?");
		String key = 
			m[0].toUpperCase() + "_" +
			m[1].toUpperCase() + "_" +
			m[2].toUpperCase() + "_" +
			m[3].toUpperCase() + "_";
		
		{
			int ankleCorrect = fixes.getOrDefault(key+"ANKLE",0);
			int kneeCorrect = fixes.getOrDefault(key+"KNEE",0);
			
			if (ankleCorrect != 0 || kneeCorrect != 0)
				log.println("ankleCorrect", ankleCorrect, "kneeCorrect", kneeCorrect);
			
			double sumAnk=0, countAnk=0;
			double sumKnee = 0, countKnee = 0;
			for (int i=anklePeaks.size()-2, max=3-ankleCorrect; i>=0 && max>0; --i)
			{
				Pair<Integer, Comp> peak = anklePeaks.get(i);
				if (peak.first > 0)		
				{
					if (max <= 3)
					{
						double v = (Double)Maths.getMemberField(peak.second, ankleField);
						finalData.put("MaxAnk"+max, v);
						finalData.put("MaxAnkFrame"+max, (double)peak.second.frame);
						sumAnk += v;
						countAnk += 1.0;
					}
					max--;
				}
			}

			for (int i=kneePeaks.size()-2, max=3-kneeCorrect; i>=0 && max>0; --i)
			{
				Pair<Integer, Comp> peak = kneePeaks.get(i);
				if (peak.first > 0)				
				{
					if (max <= 3)
					{
						double v = (Double)Maths.getMemberField(peak.second, kneeField);
						finalData.put("MaxKnee"+max, v);
						finalData.put("MaxKneeFrame"+max, (double)peak.second.frame);
						sumKnee += v;
						countKnee += 1.0;
					}
					max--;
				}
			}

			double avgAnk = sumAnk/countAnk;
			double avgKnee = sumKnee/countKnee;
			
			finalData.put("MaxAnkAvg", avgAnk);
			finalData.put("MaxKneeAvg", avgKnee);
		}
		
		finalData.put("PelAvg", pelvicAngleAverage);
		finalData.put("PelMax", pelvicAngleMax);
		finalData.put("PelStd", pelvicAngleStdDev);
		
		String[] keysOrder = { 
			"MaxAnk1", "MaxAnk2", "MaxAnk3", "MaxAnkAvg",
			"MaxKnee1", "MaxKnee2", "MaxKnee3", "MaxKneeAvg", 
			"MaxAnkFrame1", "MaxAnkFrame2", "MaxAnkFrame3",
			"MaxKneeFrame1", "MaxKneeFrame2", "MaxKneeFrame3",
			"PelAvg", "PelMax", "PelStd",
		};
		
		Object[] otherDatas = {
			"TrialAvgAnk", reversedSubject ? ankleAngleRightAverage : ankleAngleLeftAverage,
			"TrialMaxAnk", reversedSubject ? ankleAngleRightMax : ankleAngleLeftMax,
			"TrialStdAnk", reversedSubject ? ankleAngleRightStdDev : ankleAngleLeftStdDev,
			"TrialAvgKnee", reversedSubject ? kneeAngleRightAverage : kneeAngleLeftAverage,
			"TrialMaxKnee", reversedSubject ? kneeAngleRightMax : kneeAngleLeftMax,
			"TrialStdKnee", reversedSubject ? kneeAngleRightStdDev : kneeAngleLeftStdDev
		} ;
		
		CsvOut finalPeaks = new CsvOut();
		ArrayList<Object> row = new ArrayList<Object>();
		for (String k : keysOrder)
		{
			row.add(k);
			row.add(finalData.get(k));
		}
		
		for (Object o : otherDatas)
		{
			row.add(o);
		}
		
		finalPeaks.addRow(row.toArray());
		finalPeaks.write(maxPeakFileName);
		
		makeChart(comps, outFileName + ".png");
	}

	static public void main (String[] args) throws Exception
	{
		String inputFile = null;
		String outputFile = null;
		String peakOutFileName = null;
		String maxPeakFileName = null;
		
		ArrayList<String> initializationArgs = new ArrayList<String>();
		for (String a : args)
		{
			String[] arg = a.split("=");
			switch(arg[0])
			{
				case "in":
					inputFile = arg[1];
					if (outputFile == null)
						outputFile = Filez.replaceFirstDirectoryPart(inputFile, "markers", "ankles");
				break;
				
				case "out":
					outputFile = arg[1];
				break;
				
				case "peaksOut":
					peakOutFileName = arg[1];
				break;
				
				case "maxPeaksOut":
					maxPeakFileName = arg[1];
				break;
					
				case "init":
					initializationArgs.add(arg[1]);
				break;
				
				default:
					System.out.println("Use like: in=myfile.tsv out=otherFile init=initializationOption");
				break;
			}
		}
			
		compute(inputFile, outputFile, peakOutFileName, maxPeakFileName, initializationArgs);
	}
}
