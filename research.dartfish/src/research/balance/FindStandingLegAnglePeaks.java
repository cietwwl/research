package research.balance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
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

public class FindStandingLegAnglePeaks
{
	static LogOut log = new LogOut(FindStandingLegAnglePeaks.class);
	
	static public class Comp
	{
		public int frame;
		public Double sacrum;
		public Double sacrumBW;
		public Double sacrumSmooth, sacrumAverage, sacrumTest;
		public double[] ankleAngles = { 0,0 };
		public double[] kneeAngles = { 0,0 };
		
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
		dataset.addSeries(ankleAngleRightBW);
		dataset.addSeries(kneeAngleRightBW);
		dataset.addSeries(ankleAngleLeftBW);
		dataset.addSeries(kneeAngleLeftBW);

		dataset.addSeries(ankleAngleRight);
		dataset.addSeries(kneeAngleRight);
		dataset.addSeries(ankleAngleLeft);
		dataset.addSeries(kneeAngleLeft);
		
		dataset.addSeries(pelvicAngle);
		dataset.addSeries(pelvicAngleBW);
		dataset.addSeries(pelvicAngleAverage);
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
				if (Collectionz.mapContainsAllAndNotNull(row, S.toe[s], S.ankle[s], S.knee[s], S.hip[s]))
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

		Maths.butterworth(comps, "kneeAngleLeft", "kneeAngleLeftBW", filterOrder, fcf1);
		Maths.smooth(comps, "kneeAngleLeft", "kneeAngleLeftSmooth", "kneeAngleLeftAverage", "kneeAngleLeftTest", bandwidth, cycles);
		List<Pair<Integer, Comp>> kneeAngleLeftPeaks = Maths.findPeaks(comps, new Maths.PeakFinderDoubleReflection<Comp>("kneeAngleLeftBW", "kneeAngleLeftTest")); 

		Maths.butterworth(comps, "kneeAngleRight", "kneeAngleRightBW", filterOrder, fcf1);
		Maths.smooth(comps, "kneeAngleRight", "kneeAngleRightSmooth", "kneeAngleRightAverage", "kneeAngleRightTest", bandwidth, cycles);
		List<Pair<Integer, Comp>> kneeAngleRightPeaks = Maths.findPeaks(comps, new Maths.PeakFinderDoubleReflection<Comp>("kneeAngleRightBW", "kneeAngleRightTest")); 
		
		Maths.butterworth(comps, "ankleAngleLeft", "ankleAngleLeftBW", filterOrder, fcf1);
		Maths.smooth(comps, "ankleAngleLeft", "ankleAngleLeftSmooth", "ankleAngleLeftAverage", "ankleAngleLeftTest", bandwidth, cycles);
		List<Pair<Integer, Comp>> ankleAngleLeftPeaks = Maths.findPeaks(comps, new Maths.PeakFinderDoubleReflection<Comp>("ankleAngleLeftBW", "ankleAngleLeftTest")); 
		
		Maths.butterworth(comps, "ankleAngleRight", "ankleAngleRightBW", filterOrder, fcf1);
		Maths.smooth(comps, "ankleAngleRight", "ankleAngleRightSmooth", "ankleAngleRightAverage", "ankleAngleRightTest", bandwidth, cycles);
		List<Pair<Integer, Comp>> ankleAngleRightPeaks = Maths.findPeaks(comps, new Maths.PeakFinderDoubleReflection<Comp>("ankleAngleRightBW", "ankleAngleRightTest")); 

		Maths.butterworth(comps, "pelvicAngle", "pelvicAngleBW", filterOrder, fcf1);
		Maths.smooth(comps, "pelvicAngle", "pelvicAngleSmooth", "pelvicAngleAverage", "pelvicAngleTest", bandwidth, cycles);
		List<Pair<Integer, Comp>> pelvicAnglePeaks = Maths.findPeaks(comps, new Maths.PeakFinderDoubleReflection<Comp>("pelvicAngleBW", "pelvicAngleTest")); 

		CsvOut csvOut = new CsvOut();
		CsvOut sacrumPeaksOut = new CsvOut();
		CsvOut kneeAngleLeftPeaksOut = new CsvOut();
		CsvOut kneeAngleRightPeaksOut = new CsvOut();
		CsvOut ankleAngleLeftPeaksOut = new CsvOut();
		CsvOut ankleAngleRightPeaksOut = new CsvOut();
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
