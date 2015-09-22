package research.balance;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import research.csv.Csv.ColumnType;
import research.csv.CsvIn;
import research.exceptions.DataMissing;
import research.math.Vector3;
import research.util.LogOut;
import research.visualization.Joint;
import research.visualization.Node;

public class Skeleton extends Node
{
	static LogOut log = new LogOut(Skeleton.class);
	
	static interface Delegate {
		public void onUpdate (Skeleton skeleton);
	}
	
	static String[] defaultJointsZ = new String[] {
		S.neck, S.leftShoulder, S.rightShoulder, S.centerHip, null,
		S.centerHip, S.leftHip, S.rightHip, null,
		S.leftHip, S.leftKnee, null,
		S.rightHip, S.rightKnee, null,
		S.leftKnee, S.leftAnkle, null,
		S.rightKnee, S.rightAnkle, null,
		S.leftAnkle, S.leftToe, null,
		S.rightAnkle, S.rightToe, null
	};

	static String[] defaultJointsY = new String[] {
		S.neck, S.leftShoulder, S.rightShoulder, S.centerHip, null,
		S.centerHip, S.leftHip, S.rightHip, null,
		S.leftHip, S.leftKnee, null,
		S.rightHip, S.rightKnee, null,
		S.leftKnee, S.leftAnkle, null,
		S.rightKnee, S.rightAnkle, null,
		S.leftAnkle, S.leftToe, null,
		S.rightAnkle, S.rightToe, null,

		S.Pneck, S.PleftShoulder, S.PrightShoulder, S.PcenterHip, null,
		S.PcenterHip, S.PleftHip, S.PrightHip, null,
		S.PleftHip, S.PleftKnee, null,
		S.PrightHip, S.PrightKnee, null,
		S.PleftKnee, S.PleftAnkle, null,
		S.PrightKnee, S.PrightAnkle, null,
		S.PleftAnkle, S.PleftToe, null,
		S.PrightAnkle, S.PrightToe, null
	};

	static String[] defaultJointsX = new String[] {
		S.Pneck, S.PleftShoulder, S.PrightShoulder, S.PcenterHip, null,
		S.PcenterHip, S.PleftHip, S.PrightHip, null,
		S.PleftHip, S.PleftKnee, null,
		S.PrightHip, S.PrightKnee, null,
		S.PleftKnee, S.PleftAnkle, null,
		S.PrightKnee, S.PrightAnkle, null,
		S.PleftAnkle, S.PleftToe, null,
		S.PrightAnkle, S.PrightToe, null
	};

	static String[] defaultJoints = defaultJointsZ;
	
	Map<String, Joint> joints = new HashMap<String, Joint>();
	
	public Skeleton (String[] jointNames, float timeScale)
	{
		this.timeScale = timeScale;
		
		Joint o = null;
		for (String j : jointNames)
		{
			if (j == null)
			{
				o = null;
			}
			else
			if (o == null)
			{
				o = ensureJoint(j);
			}
			else
			{
				o.connectTo(ensureJoint(j));
			}
		}
	}
	
	public Delegate delegate;
	CsvIn data = null;
	double timeScale;
	double maxTime = 0;
	int frame = 0;
	boolean repeat = false;
	
	public void setData (CsvIn data)
	{
		this.data = data;
		maxTime = (double)data.rows.lastElement().get("time");
		
		for (Entry<String, ColumnType> k : data.columns.entrySet())
		{
			if (k.getValue() == ColumnType.Vector)
			{
				ensureJoint(k.getKey());
			}
		}
	}
	
	public Joint ensureJoint (String name)
	{
		if (!joints.containsKey(name))
		{
			Joint j = new Joint();
			int color = name.hashCode() | 0xFFFF;
			j.c = new Color(color);
			j.name = name;
			children.add(j);
			joints.put(name, j);
		}
		
		return joints.get(name);
	}
	
	public void update(double time, double dt)
	{
		time = timeScale * time;

		if (time > maxTime && !repeat)
			System.exit(0);
		
		time = time - (Math.floor(time / maxTime) * maxTime);
		frame = (int)(time / maxTime * data.rows.size());
		// log.println(time, frame);
		
		if (delegate != null)
			delegate.onUpdate(this);

		for (String k : joints.keySet())
		{
			Joint v = joints.get(k);
			if (data.getRow(frame).get(k) == null)
				continue;
//				throw new DataMissing(k);
			
			Object o = data.getRow(frame).get(k);
			if (o instanceof Vector3)
			{
				Vector3 d = (Vector3)data.getRow(frame).get(k);
				v.transform.set(d);
			}
		}
		
		super.update(time, dt);
	}

	public void setRepeat(boolean repeat)
	{
		this.repeat = repeat;
	}
}
