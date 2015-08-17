package research.c3d;

import java.util.Vector;

import javax.vecmath.Vector3d;

public class C3DRow
{
	public int frame;
	public double time;
	public C3DPoint[] v;
	public double[][] analog;
	
	public C3DRow (int width, int analogSampleWidth, int numAnalogChannels)
	{
		v = new C3DPoint[width];
		for (int i=0; i<width; ++i)
			v[i] = new C3DPoint();
		
		analog = new double[analogSampleWidth][numAnalogChannels];
	}
	
	public void setFrameTime(int frame, double sampleRate)
	{
		this.frame = frame;
		this.time = (double)(frame+1) / sampleRate;	
	}
}
