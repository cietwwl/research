package research.balance.kill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import research.balance.S;
import research.balance.Vector3WithLabel;
import research.csv.Csv.ColumnType;
import research.csv.CsvIn;
import research.csv.CsvOut;
import research.csv.find.LessThanVector3;
import research.exceptions.DataMissing;
import research.math.Vector3;
import research.util.Filez;
import research.util.LogNull;
import research.util.LogOut;
import research.util.Pair;

public class FindMarkersBasedOnPosition
{
	static LogNull log = new LogNull(FindMarkersBasedOnPosition.class);
	static LogOut unmovingLog = new LogOut(FindMarkersBasedOnPosition.class);
	
	Set<String> ignore = new HashSet<String>();
	
	double SLOW_THRESHOLD_POINT_CAN_BECOME_ANOTHER = 50.0;
	double MEDIUM_THRESHOLD_POINT_CAN_BECOME_ANOTHER = 100.0;
	double FAST_THRESHOLD_POINT_CAN_BECOME_ANOTHER = 400.0;
	double UNMOVING_THRESHOLD = 10.0;

	public void log(List<Vector3WithLabel> torso)
	{
		for (Vector3WithLabel v : torso)
		{
			log.println(v.label, v);
		}
	}

	Map<String,Map<String,Integer>> votes = new HashMap<String, Map<String,Integer>>();
	
	public double getThreshold(String part)
	{
		switch (part)
		{
			case S.leftAnkle:
			case S.rightAnkle:
			case S.leftToe:
			case S.rightToe:
				return FAST_THRESHOLD_POINT_CAN_BECOME_ANOTHER;
			case S.leftKnee:
			case S.rightKnee:
				return MEDIUM_THRESHOLD_POINT_CAN_BECOME_ANOTHER;
		}
		
		return SLOW_THRESHOLD_POINT_CAN_BECOME_ANOTHER;
	}
	
	public void addVote(String part, String column)
	{
		if (!votes.containsKey(part))
			votes.put(part, new HashMap<String,Integer>());
		
		if (!votes.get(part).containsKey(column))
			votes.get(part).put(column, 0);
		
		log.println(part, "->", column);
		votes.get(part).put(column, votes.get(part).get(column) + 1);
	}

	public Map<String, List<Pair<String, Integer>>> getWinners ()
	{
		Map<String, List<Pair<String, Integer>>> winners = new HashMap<String, List<Pair<String, Integer>>>();
		
		for (Entry<String, Map<String, Integer>> e : votes.entrySet())
		{
			List<Pair<String, Integer>> l = new ArrayList<Pair<String,Integer>>();
			for (Entry<String, Integer> v : e.getValue().entrySet())
			{
				l.add(Pair.create(v.getKey(), v.getValue()));
			}

			l.sort(new Comparator<Pair<String,Integer>>() {

				@Override
				public int compare(Pair<String, Integer> o1,
						Pair<String, Integer> o2)
				{
					return -o1.second.compareTo(o2.second);
				}
			});
			
			if (l.size() > 1)
			{
				for (Pair<String, Integer> x : l)
				{
					log.println(x.first, x.second);
				}
			}
			
			winners.put(e.getKey(), l);
		}
		
		return winners;
	}
	
	public void process(String inFileName, String outFileName) throws IOException
	{
		CsvIn csvIn = new CsvIn(inFileName);
		CsvOut csvOut = new CsvOut();
		
		final Set<String> unmovingPoints = new HashSet<String>();
		final Set<String> mirrorPoints = new HashSet<String>();
		for (Entry<String, ColumnType> e : csvIn.columns.entrySet())
		{
			if (e.getValue() != ColumnType.Vector)
				continue;
			
			unmovingPoints.add(e.getKey());

			Vector3 value = null;
			for (int i=0; i<csvIn.rows.size(); ++i)
			{
				Map<String, Object> row = csvIn.getRow(i);
				
				Vector3 v = (Vector3) row.get(e.getKey());
				if (v.x < -1200)
				{
					mirrorPoints.add(e.getKey());
				}
				if (v.y > 898)
				{
					mirrorPoints.add(e.getKey());
				}
				
				if (!v.equals(Vector3.Zero))
				{
					if (value == null)
					{
						value = v;
					}
					else
					{
						double ds = v.subtract(value).lengthSquared();
						if (ds > UNMOVING_THRESHOLD)
						{
							unmovingPoints.remove(e.getKey());
							break;
						}
					}
				}
			}
		}
		
		for (String u : unmovingPoints)
		{
			unmovingLog.println("unmoving", u);
		}
		
		for (String u : mirrorPoints)
		{
			unmovingLog.println("mirror", u);
		}

		for (int i=0; i<csvIn.rows.size(); ++i)
		{
			Map<String, Object> row = csvIn.getRow(i);
			
			ArrayList<Vector3WithLabel> vectors = new ArrayList<Vector3WithLabel>();
			for (String label : row.keySet())
			{
				Object o = row.get(label);
				if (o instanceof Vector3)
				{
					vectors.add(new Vector3WithLabel(label, (Vector3)o));
				}
			}
		
			vectors.removeIf(new Predicate<Vector3WithLabel>() {
				@Override
				public boolean test(Vector3WithLabel t)
				{
					return t.equals(Vector3.Zero);
				}
			});
			
			vectors.removeIf(new Predicate<Vector3WithLabel>() {
				@Override
				public boolean test(Vector3WithLabel t)
				{
					return unmovingPoints.contains(t.label);
				}
			});
			
			vectors.removeIf(new Predicate<Vector3WithLabel>() {
				@Override
				public boolean test(Vector3WithLabel t)
				{
					return ignore.contains(t.label);
				}
			});

			vectors.removeIf(new Predicate<Vector3WithLabel>() {
				@Override
				public boolean test(Vector3WithLabel t)
				{
					return mirrorPoints.contains(t.label);
				}
			});

			if (vectors.size() != 12)
			{
				log.println("vector quantity", vectors.size(), "should be", 12, "continuing to next row");
				for (Vector3WithLabel v : vectors)
				{
					log.println(v.label);
				}
				continue;
			}
			
			vectors.sort(new LessThanVector3(Vector3.UnitZ));
//			log(vectors);
			
			List<Vector3WithLabel> torso = vectors.subList(0, 3);
			List<Vector3WithLabel> pelvis = vectors.subList(3, 6);
			List<Vector3WithLabel> knees = vectors.subList(6, 8);
			List<Vector3WithLabel> feet = vectors.subList(8, 12);
			
			torso.sort(new LessThanVector3(Vector3.UnitY));
			pelvis.sort(new LessThanVector3(Vector3.UnitY));
			knees.sort(new LessThanVector3(Vector3.UnitY));
			feet.sort(new LessThanVector3(Vector3.UnitY));
			
			List<Vector3WithLabel> feetRight = feet.subList(0, 2);
			List<Vector3WithLabel> feetLeft = feet.subList(2, 4);
			feetRight.sort(new LessThanVector3(Vector3.UnitX));
			feetLeft.sort(new LessThanVector3(Vector3.UnitX));
			
			double distanceFromCenter = 0;
			Vector3 center = pelvis.get(1);
			for (Vector3 v : vectors)
			{
				double dv = v.subtract(center).length();
				if (dv > distanceFromCenter )
					distanceFromCenter = dv;
			}
			
			log.println("DISTANCE from center", distanceFromCenter);
			
			addVote(S.rightShoulder, torso.get(0).label);
			addVote(S.neck, torso.get(1).label);
			addVote(S.leftShoulder, torso.get(2).label);
			addVote(S.rightHip, pelvis.get(0).label);
			addVote(S.centerHip, pelvis.get(1).label);
			addVote(S.leftHip, pelvis.get(2).label);

			addVote(S.rightKnee, knees.get(0).label);
			addVote(S.leftKnee, knees.get(1).label);
			
			addVote(S.leftAnkle, feetLeft.get(0).label);
			addVote(S.leftToe, feetLeft.get(1).label);
			addVote(S.rightAnkle, feetRight.get(0).label);
			addVote(S.rightToe, feetRight.get(1).label);
			
			boolean calibrated = true;
			Map<String, List<Pair<String, Integer>>> w = getWinners();
			for (Entry<String, List<Pair<String, Integer>>> e : w.entrySet())
			{
				if (e.getValue().get(0).second < 10)
				{
					calibrated = false;
					break;
				}
			}
			
			if (calibrated == true)
				break;
		}
		
		Map<String, List<Pair<String, Integer>>> w = getWinners();
		Map<String, String> dataLabelToJoint = new HashMap<String, String>();
		Map<String, String> jointToDataLabel = new HashMap<String, String>();
		Map<String, Vector3> jointToVector = new HashMap<String, Vector3>();

		for (Entry<String, List<Pair<String, Integer>>> e : w.entrySet())
		{
			jointToDataLabel.put(e.getKey(), e.getValue().get(0).first);
			dataLabelToJoint.put(e.getValue().get(0).first, e.getKey());
		}
		
		// search for first row with winners
		int i=0;
		for (i=0; i<csvIn.rows.size(); ++i)
		{
			Map<String, Object> row = csvIn.getRow(i);
			boolean allNonZero = true;
			for (String key : dataLabelToJoint.keySet())
			{
				Vector3 v = (Vector3) row.get(key);
				jointToVector.put(key, v);
				if (v.equals(Vector3.Zero))
					allNonZero = false;
			}
			
			if (allNonZero)
				break;
		}
		
		String[] orderedJoints = new String[] {
			S.neck,
			S.rightShoulder,
			S.leftShoulder,
			S.centerHip,
			S.rightHip,
			S.leftHip,
			S.rightKnee,
			S.leftKnee,
			S.leftAnkle,
			S.leftToe,
			S.rightAnkle,
			S.rightToe
		};
		
		// from here on out, we use the winners, but if the winner turns into zero
		// we search for the closest point not taken and not zero
		for (; i<csvIn.rows.size(); ++i)
		{
			Map<String, Object> row = csvIn.getRow(i);
			
			for (String key : orderedJoints)
			{
				String dataKey = jointToDataLabel.get(key);
				Vector3 p = jointToVector.get(key);
				Vector3 v = (Vector3) row.get(dataKey);
				
				if (v.equals(Vector3.Zero))
				{
					double distanceSquared = Double.MAX_VALUE;
					Vector3 winner = null;
					String winnerKey = null;

					for (Entry<String, Object> e : row.entrySet())
					{
						if (e.getValue() instanceof Vector3 && !dataLabelToJoint.containsKey(e.getKey()))
						{
							Vector3 n = (Vector3)e.getValue();
							if (n.equals(Vector3.Zero))
								continue;
							
							if (unmovingPoints.contains(e.getKey()))
								continue;
							
							if (ignore.contains(e.getKey()))
								continue;
							
							if (mirrorPoints.contains(e.getKey()))
								continue;
							
							double l = n.subtract(p).lengthSquared();
							if (l < distanceSquared)
							{
								winner = n;
								winnerKey = e.getKey();
								distanceSquared = l;
							}
						}
					}
					
					double THRESHOLD_SQUARED = getThreshold(key);
					if (distanceSquared < THRESHOLD_SQUARED && winner != null)
					{
						v = winner;
						dataLabelToJoint.remove(dataKey);
						dataLabelToJoint.put(winnerKey, key);
						jointToDataLabel.put(key, winnerKey);
					}
					else
					{
						v = p;
					}
				}
				
				jointToVector.put(key, v);
			}
			
			csvOut.addRow(
				jointToVector,
				row
			);
		}

		csvOut.write(outFileName);
	}
	
	private Vector3 getBestValue(CsvIn csvIn, int i, Map<String, List<Pair<String, Integer>>> w, String key)
	{
		Map<String, Object> r = csvIn.getRow(i);
		if (w.get(key) == null)
		{
			throw new DataMissing(key);
		}
		for (Pair<String, Integer> e : w.get(key))
		{
			Vector3 v = (Vector3) r.get(e.first);
			if (!v.equals(Vector3.Zero))
			{
				return v;
			}
			else
			{
				log.println("missing");
			}
		}
		
		return Vector3.Zero;
	}

	public static void main(String[] args) throws Exception
	{
		FindMarkersBasedOnPosition calculator = new FindMarkersBasedOnPosition();
		
		String inputFile = null;
		String outputFile = null;
		
		for (String a : args)
		{
			String[] arg = a.split("=");
			switch(arg[0])
			{
				case "in":
					inputFile = arg[1];
					outputFile = Filez.replaceFirstDirectoryPart(inputFile, "converted", "markers");
				break;
				case "out":
					outputFile = arg[1];
				break;
				case "ignore":
					calculator.ignore.add(arg[1]);
				break;
				case "slowSpeedThreshold":
					calculator.SLOW_THRESHOLD_POINT_CAN_BECOME_ANOTHER = new Double(arg[1]);
				break;
				case "fastSpeedThreshold":
					calculator.FAST_THRESHOLD_POINT_CAN_BECOME_ANOTHER = new Double(arg[1]);
				break;
				case "unmovingThreshold":
					calculator.UNMOVING_THRESHOLD = new Double(arg[1]);
				break;
				default:
					System.out.println("Unknown argument " + arg[0]);
				break;
			}
		}
		
		calculator.process(inputFile, outputFile);
	}

}
