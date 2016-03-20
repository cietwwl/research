package research.math;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.function.Max;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NotFiniteNumberException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilter;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignFisher;
import research.util.Collectionz;
import research.util.Pair;

public class Maths
{
	public static double[] toDoubleArray (Collection<Double> c)
	{
		double[] a = new double[c.size()];
		int i=0;
		for (Double d : c)
		{
			a[i++] = d; 
		}
		
		return a;
	}

	public static double[] toDoubleArrayIgnoreInvalids (Collection<Double> c)
	{
		double[] a = new double[c.size()];
		int i=0;
		for (Double d : c)
		{
			if (d != null)
			{
				if (!d.isInfinite() && !d.isNaN())
					a[i++] = d; 
			}
		}
		
		return Arrays.copyOf(a,  i);
	}

	public static double[] only (Collection<Vector3> c, int i)
	{
		double[] a = new double[c.size()];
		
		int j=0;
		for (Vector3 d : c)
		{
			a[j++] = d.get(i); 
		}
		
		return a;
	}
	
	public static <Record> double average (ArrayList<Record> comps, String fieldName)
	{
		double[][] values = toXYArrays(comps, fieldName, false, false, null);
		return average(values[1]);
	}
	
	public static double average (double[] v)
	{
		double total = 0;
		for (double s : v)
			total += s;
		
		double average = total / (double)v.length;
		
		return average;
	}
	
	public static Double max (double[] vs)
	{
		Double d = null;
		for (double v : vs)
		{
			if (d == null || v > d)
				d = v;
		}
		
		return d;
	}

	public static Double min (double[] vs)
	{
		Double d = null;
		for (double v : vs)
		{
			if (d == null || v < d)
				d = v;
		}
		
		return d;
	}
	public static <Record> Double max (ArrayList<Record> comps, String fieldName)
	{
		double[][] values = toXYArrays(comps, fieldName, false, false, null);
		return max(values[1]);
	}
	
	public static double stddev (double[] v)
	{
		StandardDeviation stddev = new StandardDeviation();
		double sd = stddev.evaluate(v);
		return sd;
	}

	public static <Record> double stddev (ArrayList<Record> comps, String fieldName)
	{
		double[][] values = toXYArrays(comps, fieldName, false, false, null);
		return stddev(values[1]);
	}

	public static <Record> int findPeak (int direction, ArrayList<Record> comps, int startingIndex, PeakFinder<Record> c)
	{
		int peakIndex = -1;
		Record peak = null;
		boolean correctSideFound = false;
		int failedSide = 0;
		
		for (int i=startingIndex; i<comps.size(); ++i)
		{
			Record comp = comps.get(i);
			if (!c.isValid(comp))
				continue;
			
			if (!correctSideFound)
			{
				if (c.test(direction, comp))
					correctSideFound = true;
			}
			
			if (correctSideFound)
			{
				if (!c.test(direction, comp))
				{
					failedSide++;
					if (failedSide > 10)
						break;
				}
				else
				if (peak == null || c.test(direction, comp, peak))
				{
					peakIndex = i;
					peak = comp;
				}
			}
		}
		
		return peakIndex;
	}

	public static interface PeakFinder <Record>
	{
		boolean test(int direction, Record l, Record r);
		boolean test(int direction, Record r);
		boolean isValid(Record r);
	}
	
	public static abstract class PeakFinderDouble <Record> implements PeakFinder<Record> {

		@Override
		public boolean test(int direction, Record l, Record r)
		{
			if (direction > 0)
			{
				return getValue(l) > getValue(r);
			}
			else
			{
				return getValue(l) < getValue(r);
			}
		}

		@Override
		public boolean test(int direction, Record r)
		{
			if (direction > 0)
			{
				return getValue(r) > getThreshold(r);
			}
			else
			{
				return getValue(r) < getThreshold(r);
			}
		}

		@Override
		public boolean isValid(Record r)
		{
			return getValue(r) != null && getThreshold(r) != null;
		}
		
		abstract public Double getValue(Record r);
		abstract public Double getThreshold(Record r);
		
	}
	
	public static class PeakFinderDoubleReflection <Record> extends PeakFinderDouble<Record> {

		String v, t;
		
		public PeakFinderDoubleReflection(String v, String t)
		{
			this.v = v;
			this.t = t;
		}
		
		public Double getValue(Record r)
		{
			try
			{
				Object x = r.getClass().getDeclaredField(v).get(r);
				return (Double)x;
			} 
			catch (Exception e)
			{
				return null;
			}
		}
		
		public Double getThreshold(Record r)
		{
			try
			{
				Object x = r.getClass().getDeclaredField(t).get(r);
				return (Double)x;
			}
			catch (Exception e)
			{
				return null;
			}
		}
	}

	public static <Record> List<Pair<Integer, Record>> findPeaks (ArrayList<Record> comps, PeakFinder<Record> c)
	{
		ArrayList<Pair<Integer, Record>> peaks = new ArrayList<Pair<Integer, Record>>();
		
		int peak = 0;
		int direction = 1;
		while (peak >= 0)
		{
			peak = findPeak(direction, comps, peak, c);
			if (peak != -1)
			{
				peaks.add(Pair.create(direction, comps.get(peak)));
			}
			
			direction = -direction;
		}
		
		return peaks;
	}
	
	public static <Record> void smooth (
		List<Record> comps, 
		String valueFieldName, 
		String smoothFieldName, 
		String averageFieldName, 
		String testFieldName,
		double bandwidth, int robustness
	)
	{
		try
		{
			Field valueField = valueFieldName != null ? comps.get(0).getClass().getField(valueFieldName) : null;
			Field smoothField = smoothFieldName != null ? comps.get(0).getClass().getField(smoothFieldName) : null;
			Field averageField = averageFieldName != null ? comps.get(0).getClass().getField(averageFieldName) : null;
			Field testField = testFieldName != null ? comps.get(0).getClass().getField(testFieldName) : null;
			
			double[] xd, yd;
			
			{
				ArrayList<Double> x = new ArrayList<Double>();
				ArrayList<Double> y = new ArrayList<Double>();
				int i=0; 
				for (Record c : comps)
				{
					Double value = (Double)valueField.get(c);
					if (value != null)
					{
						x.add((double)i);
						y.add(value);
					}
					
					++i;
				}
				
				xd = Maths.toDoubleArray(x);
				yd = Maths.toDoubleArray(y);
				
			}
			

			double a = Maths.average(yd);
	
			LoessInterpolator li = new LoessInterpolator(bandwidth, robustness);
			double[] smooth = null;
			try
			{
				 smooth = li.smooth(xd, yd);
			}
			catch (NumberIsTooSmallException e)
			{
				smooth = yd;
			}
			catch (NoDataException e)
			{
				smooth = yd;
			}
			catch (NotFiniteNumberException e)
			{
				e.printStackTrace();
				smooth = yd;
			}
			
			for (int i=0; i<smooth.length; ++i)
			{
				int frame = (int)xd[i];
				double s = smooth[i];
				double sw = 1.0;
				double aw = 1.5;
				double t = (s * sw + a * aw) / (sw + aw);
				
				Record comp = comps.get(frame);
				if (smoothField != null)
					smoothField.set(comp, s);
				if (averageField != null)
					averageField.set(comp, a);
				if (testField != null)
					testField.set(comp, t);
			}
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

	public static <Record> double[][] toXYArrays (List<Record> comps, String inFieldName, Double defaultValue)
	{
		return toXYArrays(comps, inFieldName, true, true, defaultValue);
	}
	
	public static <Result, Record> Collection<Result> values (List<Record> comps, String inFieldName)
	{
		try
		{
			Field inField = comps.get(0).getClass().getField(inFieldName);
	
			ArrayList<Result> c = new ArrayList<Result>();
			for (Record r : comps)
			{
				Result value = (Result)inField.get(r);
				if (value != null)
					c.add(value);
			}
			
			return c;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public static <Record> double[][] toXYArrays (List<Record> comps, String inFieldName, boolean useForward, boolean useDefaultValue, Double defaultValue)
	{
		try
		{
			Field inField = comps.get(0).getClass().getField(inFieldName);
			
			double[] xd, yd;
			
			{
				ArrayList<Double> x = new ArrayList<Double>();
				ArrayList<Double> y = new ArrayList<Double>();
				for (int i=0; i<comps.size(); ++i)
				{
					boolean found = false;
					
					Record c = comps.get(i);
					Double value = (Double)inField.get(c);
					if (value != null)
					{
						x.add((double)i);
						y.add(value);
						found = true;
					}
					
					if (!found && useForward)
					{
						for (int j=i; j<comps.size(); j++)
						{
							Double forwardValue = (Double)inField.get(comps.get(j));
							if (forwardValue != null)
							{
								found = true;
								x.add((double)i);
								y.add(forwardValue);
								break;
							}
						}
					}
					
					if (!found && useDefaultValue)
					{
						x.add((double) i);
						y.add(defaultValue);
						found = true;
					}
				}
				
				xd = Maths.toDoubleArray(x);
				yd = Maths.toDoubleArray(y);
			}
			
			return new double[][] { xd, yd };
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public static <Record> void setMemberField (Record comp, String outFieldName, Object value)
	{
		try
		{
			Field outField;
			outField = comp.getClass().getField(outFieldName);
			outField.set(comp,  value);
		} 
		catch (Exception e)
		{
			throw new RuntimeException(e);
		} 
	}
	
	public static <Record> Object getMemberField (Record comp, String outFieldName)
	{
		try
		{
			Field outField;
			outField = comp.getClass().getField(outFieldName);
			return outField.get(comp);
		} 
		catch (Exception e)
		{
			throw new RuntimeException(e);
		} 
	}

	public static <Record> void butterworth(List<Record> comps, String inFieldName, String outFieldName,
			int filterOrder, double fcf1)
	{
		double defaultValue = 0;
		double[][] xy = toXYArrays(comps, inFieldName, defaultValue);

		/**
		* Designs an IIR filter and returns the IIR filter coefficients.
		*
		* <p>
		* The cutoff frequencies are specified relative to the sampling rate and must be
		* between 0 and 0.5.<br>
		* The following formula can be used to calculate the relative frequency values:
		* <pre>   frequencyInHz / samplingRateInHz</pre>
		*
		* <p>
		* For Bessel filters, matched Z-transform is used to design the filter.
		*
		* @param filterPassType
		*    The filter pass type (Lowpass, highpass, bandpass, bandstop).
		* @param filterCharacteristicsType
		*    The filter characteristics type.
		*    The following filters are implemented: Butterworth, Chebyshev (type 1), Bessel.
		* @param filterOrder
		*    The filter order.
		* @param ripple
		*    Passband ripple in dB. Must be negative. Only used for Chebyshev filter, ignored for other filters.
		* @param fcf1
		*    The relative filter cutoff frequency for lowpass/highpass, lower cutoff frequency for bandpass/bandstop.
		*    This value is relative to the sampling rate (see above for more details).
		* @param fcf2
		*    Ignored for lowpass/highpass, the relative upper cutoff frequency for bandpass/bandstop,
		*    This value is relative to the sampling rate (see above for more details).
		* @return
		*    The IIR filter coefficients.
		*/
		double ripple = 0; // ignored
		double fcf2 = 0; // ignored
		IirFilterCoefficients coefficients = IirFilterDesignFisher.design(FilterPassType.lowpass, FilterCharacteristicsType.butterworth, filterOrder, ripple, fcf1, fcf2);
		IirFilter filter = new IirFilter(coefficients);
		for (int i=0; i<1000; ++i)
		{
			filter.step(xy[1][0]);
		}
		
		for (int i=0; i<xy[0].length; ++i)
		{
			double v = filter.step(xy[1][i]);
			
			setMemberField(comps.get(i), outFieldName, v);
		}
	}
}
