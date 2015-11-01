package research.balance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import research.csv.CsvIn;
import research.csv.CsvOut;
import research.math.Maths;
import research.math.Vector3;
import research.util.Collectionz;
import research.util.Filez;
import research.util.LogOut;
import research.util.Pair;

public class FindOneAnkleDistances
{
	static LogOut log = new LogOut(FindOneAnkleDistances.class);
	
	static class Comp
	{
		int frame;
		Vector3 l, r;
		Double d;
		Double s;
		Double p;
		Double a;
		Double sa;
		Integer direction;
	}
	
	static void makeChart (ArrayList<Comp> comps, String outFileName) throws IOException
	{
		final XYSeries distance = new XYSeries("Distance");
		final XYSeries average = new XYSeries("Average");
		final XYSeries peak = new XYSeries("Peak");
		final XYSeries loes = new XYSeries("Loes");
		final XYSeries test = new XYSeries("Test");
		for (Comp c: comps)
		{
			if (c.d == null)
				continue;

			double f = c.frame;
			
			distance.add(f, c.d);
			average.add(f, c.a);
			if (c.p != null)
				peak.add(f, c.p);
			loes.add(f, c.s);
			test.add(f, c.sa);
		}
		
		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(distance);
		dataset.addSeries(average);
		dataset.addSeries(peak);
		dataset.addSeries(loes);
		dataset.addSeries(test);
		
		final JFreeChart chart = ChartFactory.createXYLineChart(
	            outFileName,      // chart title
	            "X",                      // x axis label
	            "Y",                      // y axis label
	            dataset,                  // data
	            PlotOrientation.VERTICAL,
	            true,                     // include legend
	            true,                     // tooltips
	            false                     // urls
	        );		
		
		ChartUtilities.saveChartAsPNG(new File(outFileName), chart, 640, 480);
	}
	
	static boolean test(int direction, double l, double r)
	{
		if (direction > 0)
			return l > r;
		else
			return r > l;
	}
	
	public static int findPeak (int direction, ArrayList<Comp> comps, int startingIndex)
	{
		int peakIndex = -1;
		double peak = test(direction,0,Float.MAX_VALUE) ? Float.MAX_VALUE : 0;
		boolean correctSideFound = false;
		int failedSide = 0;
		for (int i=startingIndex; i<comps.size(); ++i)
		{
			Comp comp = comps.get(i);
			if (!Collectionz.allNotNull(comp.d, comp.sa))
				continue;
			
			if (!correctSideFound)
			{
				if (test(direction, comp.d, comp.sa))
					correctSideFound = true;
			}
			
			if (correctSideFound)
			{
				if (!test(direction, comp.d, comp.sa))
				{
					failedSide++;
					if (failedSide > 10)
						break;
				}
				else
				if (test(direction, comp.d, peak))
				{
					peakIndex = i;
					peak = comp.d;
				}
			}
		}
		
		return peakIndex;
	}
	
	public static void compute (String inFileName, String outFileName, String peakOutFileName, String maxPeakFileName, ArrayList<String> flags) throws Exception
	{
		Filez.ensureDirectory(Filez.getDirectoryPart(outFileName));

		CsvIn csvIn = new CsvIn(inFileName);
		
		
		ArrayList<Comp> comps = new ArrayList<Comp>();

		for (int i=0; i<csvIn.rows.size(); ++i)
		{
			int currentRow = i;
			Comp c = new Comp();
			c.frame = i;
			c.l = (Vector3) csvIn.rows.get(currentRow).get(S.leftAnkle);
			c.r = (Vector3) csvIn.rows.get(currentRow).get(S.rightAnkle);
			c.d = Collectionz.allNotNull(c.l, c.r) ?
					c.l.subtract(c.r).length() : null;
					
			comps.add(c);
		}
		
		ArrayList<Double> x = new ArrayList<Double>();
		ArrayList<Double> y = new ArrayList<Double>();
		
		for (Comp c : comps)
		{
			if (c.d != null)
			{
				x.add((double)c.frame);
				y.add(c.d);
			}
		}
		
		double[] xd, yd;
		xd = Maths.toDoubleArray(x);
		yd = Maths.toDoubleArray(y);
		double a = Maths.average(yd);

		LoessInterpolator li = new LoessInterpolator(0.2, 4);
		double[] smooth = li.smooth(xd, yd);
		for (int i=0; i<smooth.length; ++i)
		{
			int f = (int)xd[i];
			double s = smooth[i];
			
			comps.get(f).s = s;
			comps.get(f).a = a;
			
			double sw = 1.0;
			double aw = 1.5;
			comps.get(f).sa = (s * sw + a * aw) / (sw + aw);
		}

		// ok now we have the smoothing
		// so we look for the peaks as: find next peak which is above the interpolation spline

		int peak = 0;
		int direction = 1;
		while (peak >= 0)
		{
			peak = findPeak(direction, comps, peak);
			if (peak != -1)
			{
				comps.get(peak).p = comps.get(peak).d;
				comps.get(peak).direction = direction;
				log.println("found peak", peak, direction,  comps.get(peak).d);
			}
			
			direction = -direction;
		}
		
		CsvOut csvOut = new CsvOut();
		CsvOut peaksOut = new CsvOut();
		CsvOut maxPeaksOut = new CsvOut();
		csvOut.prefix = csvIn.prefix;

		for (Comp c : comps)
		{
			csvOut.addRow(
				"frame", c.frame,
				"anklePeak", c.p,
				"ankleLeft", c.l,
				"ankleRight", c.r,
				"ankleDistance", c.d,
				"ankleDistanceLoessInterpolator", c.s,
				"ankleAverage", c.a,
				"ankleTestLine", c.sa
			);
			
			if (c.p != null)
			{
				peaksOut.addRow(
					"frame", c.frame,
					"anklePeak", c.p
				);
			}
			
			if (c.p != null && c.direction == 1)
			{
				maxPeaksOut.addRow(
					"frame", c.frame,
					"anklePeak", c.p
				);
			}
		}
		
		maxPeaksOut.write(maxPeakFileName);
		peaksOut.write(peakOutFileName);
		csvOut.write(outFileName);
		
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
