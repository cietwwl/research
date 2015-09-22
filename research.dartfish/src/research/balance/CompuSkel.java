package research.balance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import research.csv.CsvIn;
import research.csv.CsvOut;
import research.csv.find.Compare;
import research.csv.find.LessThanVector3;
import research.math.Vector3;
import research.math.Vector4;
import research.util.Filez;
import research.util.LogNull;
import research.util.LogOut;
import research.util.Pair;

public class CompuSkel
{
	static LogNull log = new LogNull(CompuSkel.class);
	static LogOut fileLog = new LogOut(CompuSkel.class);
	Set<String> initializationFlags = new HashSet<String>();
	int INITIALIZATION_WINDOW_SIZE=50;

	static class Output
	{
		String label;
		Vector3 position;
		
		Vector3 interpolatedPositionSum = Vector3.Zero();
		double interpolatedPositionWeightSum = 0;
		Vector3 interpolatedPosition = null;
		
		ArrayList<Vector4> interpolatedRadii = new ArrayList<Vector4>();
	}
	
	static class Intermediate
	{
		public ArrayList<Pick> picks = new ArrayList<Pick>();
	}
	
	static class Aggregate
	{
		double averageDistanceFromPredictedPosition = 0;
		double averageDistanceFromPredictedPositionSum = 0;
		double averageDistanceFromPredictedPositionSamples = 0;
		
		Map<String, Vector3> averageJointLength = new HashMap<String, Vector3>();
		
	}
	
	CsvIn inCsv;
	CsvOut outCsv;
	
	Map<String, Aggregate> aggregateColumns = new HashMap<String, Aggregate>();
	Map<Integer, Map<String, Output>> outRows = new HashMap<Integer, Map<String,Output>>();
	Map<Integer, Map<String, Intermediate>> intermediateRows = new HashMap<Integer, Map<String,Intermediate>>();
	int currentRow;
	int direction;
	
	static public class Computer
	{
		public void compute () {}
		public void postCompute () {}
	}
	
	public class CompuPoint
	{
		String dataLabel;
		String predictedLabel;
		Output input;
		Intermediate intermediate;
		Output output;
		Aggregate aggregate;
		
		public CompuPoint (String dataLabel, String predictedLabel)
		{
			this.dataLabel = dataLabel;
			this.predictedLabel = predictedLabel;
		}
		
		public String lastKnownLabelGet ()
		{
			for (int i=currentRow - direction; i>=0 && i<inCsv.rows.size(); i-=direction)
			{
				if (outRows.containsKey(i) && outRows.get(i).containsKey(dataLabel))
					return outRows.get(i).get(dataLabel).label;
			}
			
			return null;
		}
		
		public void prepareForCycle(int inRow, int outRow)
		{
			aggregateColumns.putIfAbsent(dataLabel, new Aggregate());
			outRows.putIfAbsent(inRow, new HashMap<String, Output>());
			outRows.putIfAbsent(outRow, new HashMap<String, Output>());
			outRows.get(inRow).putIfAbsent(dataLabel, new Output());
			outRows.get(outRow).putIfAbsent(dataLabel, new Output());
			
			aggregate = aggregateColumns.get(dataLabel);
			input = outRows.get(inRow).get(dataLabel);
			output = outRows.get(outRow).get(dataLabel);
			intermediate = new Intermediate();
			
			if (output.position == null)
			{
				output.position = validatePoint(inCsv.getRow(outRow).get(input.label));
				if (output.position != null)
					output.label = input.label;
			}
		}
		
		ArrayList<Computer> interpolators = new ArrayList<Computer>();
		
		public void interpolate ()
		{
			for (Computer c : interpolators)
				c.compute();
		}
		
		public void interpolateFinalize ()
		{
			if (this.output.interpolatedPositionWeightSum > 0)
				this.output.interpolatedPosition = 
					this.output.interpolatedPositionSum.divide(
						this.output.interpolatedPositionWeightSum
					);
		}
		
		public void update (double time, Vector3 position)
		{
			output.position = position;
		}
		
		public void postCompute () 
		{
			for (Computer c : interpolators)
				c.postCompute();

			if (allNonNull(output.position, output.interpolatedPosition))
			{
				double dv = this.output.position.subtract(this.output.interpolatedPosition).length();
				log.println(dataLabel, dv);
				
				aggregate.averageDistanceFromPredictedPositionSum += dv;
				aggregate.averageDistanceFromPredictedPositionSamples += 1.0;
				aggregate.averageDistanceFromPredictedPosition = 
					aggregate.averageDistanceFromPredictedPositionSum / 
						aggregate.averageDistanceFromPredictedPositionSamples; 
			}
		}
	}
	
	public class Mirroring extends Computer
	{
		CompuPoint self;
		CompuPoint center, other, axis;
		Vector3 overrideAxis;
		double weight = 1.0;
		
		Mirroring (CompuPoint self, CompuPoint center, CompuPoint other, CompuPoint axis, Vector3 overrideAxis)
		{
			this.self = self;
			this.center = center;
			this.other = other;
			this.axis = axis;
			this.overrideAxis = overrideAxis;
		}
		
		@Override
		public void compute ()
		{
			if (allNonNull(center.output.position, axis.output.position, self.output.position))
			{
				Vector3 a = center.output.position.subtract(axis.output.position);
				if (overrideAxis != null)
					a = overrideAxis;
				
				Vector3 v2 = center.output.position.subtract(self.output.position);
				Vector3 perp = a.cross(v2);
				Vector3 mirror = perp.cross(a);
				mirror.normalize();
							
	//			mirror = v2.unitize();
				
				{
					double w = this.weight;
					double dm = v2.dot(mirror);
					Vector3 p = mirror.multiply(dm * 3.0).add(self.output.position);
					other.output.interpolatedPositionSum = other.output.interpolatedPositionSum.add(p.multiply(w));
					other.output.interpolatedPositionWeightSum += w;
				}
			}
		}
	}
	
	public class FixedLengthJoint extends Computer
	{
		FixedLengthJoint (CompuPoint self, CompuPoint other, double weight, boolean createInterpolationRadius)
		{
			this.self = self;
			this.other = other;
			this.weight = weight;
			this.createInterpolationRadius = createInterpolationRadius;
		}

		CompuPoint self;
		CompuPoint other;
		Vector3 lastKnownVector;
		double weight = 1.0;
		boolean createInterpolationRadius;
		
		@Override
		public void postCompute ()
		{
			if (allNonNull(other.output.position,self.output.position))
			{
				lastKnownVector = other.output.position.subtract(self.output.position);
				
				if (createInterpolationRadius)
				{
					self.aggregate.averageJointLength.putIfAbsent(self.dataLabel+other.dataLabel, Vector3.Zero());
					Vector3 averageJointLength = self.aggregate.averageJointLength.get(self.dataLabel+other.dataLabel);
	
					averageJointLength.x += lastKnownVector.length();
					averageJointLength.y += 1.0;
					averageJointLength.z = averageJointLength.x / averageJointLength.y;
//					fileLog.println("averageJointLength", self.dataLabel+other.dataLabel, lastKnownVector.length(), averageJointLength);
				}
			}
		}
		
		@Override
		public void compute ()
		{
			if (allNonNull(lastKnownVector, self.output.position))
			{
				double w = weight;
				Vector3 p = self.output.position.add(lastKnownVector);
				other.output.interpolatedPositionSum = other.output.interpolatedPositionSum.add(p.multiply(w));
				other.output.interpolatedPositionWeightSum += w;
			}
			
			Vector3 averageJointLength = self.aggregate.averageJointLength.getOrDefault(self.dataLabel+other.dataLabel, null);
			if (allNonNull(averageJointLength, self.output.position) && createInterpolationRadius)
			{
				other.output.interpolatedRadii.add(new Vector4(self.output.position, averageJointLength.z));
//				fileLog.println("interpolatedRadii", self.dataLabel+other.dataLabel, lastKnownVector.length(), averageJointLength);
			}
		}
	}
	
	
	CompuPoint
		leftShoulder, neck, rightShoulder,
		leftHip, sacrum, rightHip,
		leftKnee, rightKnee,
		leftAnkle, rightAnkle,
		leftToe, rightToe;
	
	CompuPoint compuPoints[];
	
	public void setup ()
	{
		leftShoulder = new CompuPoint(S.leftShoulder, S.PleftShoulder);
		neck = new CompuPoint(S.neck, S.Pneck);
		rightShoulder = new CompuPoint(S.rightShoulder, S.PrightShoulder);
		leftHip = new CompuPoint(S.leftHip, S.PleftHip);
		sacrum = new CompuPoint(S.centerHip, S.PcenterHip);
		rightHip = new CompuPoint(S.rightHip, S.PrightHip);
		leftKnee = new CompuPoint(S.leftKnee, S.PleftKnee);
		rightKnee = new CompuPoint(S.rightKnee, S.PrightKnee);
		leftAnkle = new CompuPoint(S.leftAnkle, S.PleftAnkle);
		rightAnkle = new CompuPoint(S.rightAnkle, S.PrightAnkle);
		leftToe = new CompuPoint(S.leftToe, S.PleftToe);
		rightToe = new CompuPoint(S.rightToe, S.PrightToe);

//		leftShoulder.computers.add(new Mirroring(leftShoulder, neck, rightShoulder, sacrum, Vector3.UnitZ));
//		rightShoulder.computers.add(new Mirroring(rightShoulder, neck, leftShoulder, sacrum, Vector3.UnitZ));
		leftShoulder.interpolators.add(new FixedLengthJoint(leftShoulder, rightShoulder, 1.0, false));
		rightShoulder.interpolators.add(new FixedLengthJoint(rightShoulder, leftShoulder, 1.0, false));
		leftShoulder.interpolators.add(new FixedLengthJoint(leftShoulder, neck, 1.0, false));
		rightShoulder.interpolators.add(new FixedLengthJoint(rightShoulder, neck, 1.0, false));
		neck.interpolators.add(new FixedLengthJoint(neck, sacrum, 0.1, false));
		
		leftShoulder.interpolators.add(new FixedLengthJoint(leftShoulder, sacrum, .05, false));
		rightShoulder.interpolators.add(new FixedLengthJoint(rightShoulder, sacrum, .05, false));
		leftShoulder.interpolators.add(new FixedLengthJoint(leftShoulder, leftHip, .05, false));
		rightShoulder.interpolators.add(new FixedLengthJoint(rightShoulder, rightHip, .05, false));

//		leftHip.computers.add(new Mirroring(leftHip, sacrum, rightHip, neck, null));
//		rightHip.computers.add(new Mirroring(rightHip, sacrum, leftHip, neck, null));
		leftHip.interpolators.add(new FixedLengthJoint(leftHip, sacrum, 1.0, false));
		rightHip.interpolators.add(new FixedLengthJoint(rightHip, sacrum, 1.0, false));
		sacrum.interpolators.add(new FixedLengthJoint(sacrum, leftHip, 1.0, false));
		sacrum.interpolators.add(new FixedLengthJoint(sacrum, rightHip, 1.0, false));
		
		leftHip.interpolators.add(new FixedLengthJoint(leftHip, rightHip, 1.0, false));
		rightHip.interpolators.add(new FixedLengthJoint(rightHip, leftHip, 1.0, false));

		leftHip.interpolators.add(new FixedLengthJoint(leftHip, leftKnee, 1.0, false));
		rightHip.interpolators.add(new FixedLengthJoint(rightHip, rightKnee, 1.0, false));
		leftKnee.interpolators.add(new FixedLengthJoint(leftKnee, leftHip, 1.0, false));
		rightKnee.interpolators.add(new FixedLengthJoint(rightKnee, rightHip, 1.0, false));
		
		leftKnee.interpolators.add(new FixedLengthJoint(leftKnee, leftAnkle, 1.0, true));
		rightKnee.interpolators.add(new FixedLengthJoint(rightKnee, rightAnkle, 1.0, true));
		leftAnkle.interpolators.add(new FixedLengthJoint(leftAnkle, leftKnee, 1.0, false));
		rightAnkle.interpolators.add(new FixedLengthJoint(rightAnkle, rightKnee, 1.0, false));

		leftAnkle.interpolators.add(new FixedLengthJoint(leftAnkle, leftToe, 1.0, true));
		rightAnkle.interpolators.add(new FixedLengthJoint(rightAnkle, rightToe, 1.0, true));
		leftToe.interpolators.add(new FixedLengthJoint(leftToe, leftAnkle, 1.0, true));
		rightToe.interpolators.add(new FixedLengthJoint(rightToe, rightAnkle, 1.0, true));
		
		compuPoints = new CompuPoint[] {
			leftShoulder, neck, rightShoulder,
			leftHip, sacrum, rightHip,
			leftKnee, rightKnee,
			leftAnkle, rightAnkle,
			leftToe, rightToe
		};
	}
	
	public void initializeLabels (Map<String, String> labels, int rowIndex)
	{
		Map<String, Object> row = inCsv.getRow(rowIndex);
		for (CompuPoint p : compuPoints)
		{
			p.prepareForCycle(rowIndex, rowIndex);
			p.output.label = labels.get(p.dataLabel);
			p.output.position = (Vector3) row.get(p.dataLabel);
		}
		
		cycle(rowIndex, rowIndex);
		cycle(rowIndex, rowIndex);
	}
	
	static class Pick implements Comparable<Pick>
	{
		String label;
		CompuPoint p;
		double distance = 0;
		double score = 0;
		double scoreSum = 0;
		double scoreQuantity = 0;
		
		Pick (CompuPoint p, String l, double d)
		{
			double probability = 1.0/(1.0+d);
			distance = d;
			scoreSum += probability;
			scoreQuantity += 1.0;
			score = scoreSum / scoreQuantity;
			label = l;
			this.p = p;
		}
		
		@Override
		public int compareTo(Pick o)
		{
			double ds = o.score - score;
			return ds > 0 ?
					1 : ds < 0 ? -1 : 0;
		}
	}
	
	Set<String> taken = new HashSet<String>();
	Set<String> possibilities = new HashSet<String>();
	Set<String> interference = new HashSet<String>();
	
	public boolean pickInitialize (int i)
	{
		taken.clear();
		Set<String> labels = inCsv.columns.keySet();
		
		possibilities.clear();
		possibilities.addAll(labels);
		boolean needsPick = false;
		
		for (CompuPoint p : compuPoints)
		{
			p.intermediate.picks.clear();
			
			if (p.output.position != null)
			{
				taken.add(p.output.label);
			}
			else
			{
				needsPick = true;
			}
		}

		possibilities.removeAll(taken);
		possibilities.removeAll(interference);
		
		return needsPick;
	}
	
	public boolean pickProbabilities (int i)
	{		
		Map<String, Object> row = inCsv.getRow(i);
		for (CompuPoint p : compuPoints)
		{
			if (p.output.position == null)
			{
				for (String l : possibilities)
				{
					Object o = row.get(l);
					if (!(o instanceof Vector3))
						continue;
					
					Vector3 v = (Vector3)o;
					if (isPointValid(v))
					{
						if (allNonNull(p.output.interpolatedPosition))
						{
	//						if (!p.pick.containsKey(l))
		//						p.pick.put(l, new Pick());
							
		//					Pick pick = p.pick.get(l);

							
							double d = v.subtract(p.output.interpolatedPosition).length();
							Pick pick = new Pick(p, l, d);
							p.intermediate.picks.add(pick);
						}
						for (Vector4 r : p.output.interpolatedRadii)
						{
							Vector3 rc = new Vector3(r.x, r.y, r.z);
							double rd = r.w;
							
							double d = v.subtract(rc).length();
							double dw = Math.abs(d-r.w);
							Pick pick = new Pick(p, l, dw);
							// fileLog.println(p.dataLabel, dw, d, r.w);
							p.intermediate.picks.add(pick);
						}
					}
				}
			}
		}

		boolean pickFinished = true;
		Map<String, Pick> r = doPick();
		for (Entry<String, Pick> e : r.entrySet())
		{
			Pick p = e.getValue();
			if (p.score < 0.2)
			{
				return pickFinished = false;
			}
		}
		
		if (r.size() == 0)
			return true;
		
		return pickFinished;
	}
	
	public Map<String, Pick> doPick ()
	{
		Set<String> taken = new HashSet<String>(this.taken);
		Map<String, Pick> result = new HashMap<String, Pick>();
		
		ArrayList<Pick> finalPicks = new ArrayList<Pick>();
		for (CompuPoint p : compuPoints)
		{
			finalPicks.addAll(p.intermediate.picks);
		}
		
		finalPicks.sort(new Compare<Pick>());
		
		for (Pick p : finalPicks)
		{
			if (p.p.output.position != null)
				continue;
			
			if (taken.contains(p.label))
				continue;
			
			if (result.containsKey(p.p.dataLabel))
				continue;
			
			result.put(p.p.dataLabel, p);
			taken.add(p.label);
		}
		
		return result;
	}
	
	public boolean pickFinalize (Map<String, Pick> picks, int i)
	{
		boolean madeChange = false;
		
		for (Pick p : picks.values())
		{
			Vector3 v =  (Vector3) inCsv.getRow(i).get(p.label);
			double dv = p.distance;
			
			double maxDist = Math.max(50.0,p.p.aggregate.averageDistanceFromPredictedPosition * 2.0);
			if (dv < maxDist)
			{
				madeChange = true;
				
				p.p.output.label = p.label;
				p.p.output.position = v;

				double ds = p.p.output.position.subtract(p.p.output.interpolatedPosition).length();
				log.println("****", i,  
					dv, 
					p.p.aggregate.averageDistanceFromPredictedPosition, 
					p.p.dataLabel, p.p.output.label, ds, 
					p.p.output.position, p.p.output.interpolatedPosition
				);
			}
			else
			{
				log.println(i, "skipping point because too far away", 
					p.p.dataLabel, p.label, dv, 
					p.p.aggregate.averageDistanceFromPredictedPosition, v, 
					p.p.output.position
				);
			}
		}
		
		for (CompuPoint p : compuPoints)
		{
			if (p.output.position != null && p.output.interpolatedPosition != null)
			{
				double ds = p.output.position.subtract(p.output.interpolatedPosition).length();
				log.println(p.dataLabel, p.output.label, ds, p.output.position, p.output.interpolatedPosition);
			}
		}
		
		return madeChange;
	}
	
	public boolean cycle (int inRowIndex, int outRowIndex)
	{
		boolean madeChange = false;
		
		for (CompuPoint p : compuPoints)
		{
			p.prepareForCycle(inRowIndex, outRowIndex);
		}
		
		for (CompuPoint p : compuPoints)
		{
			p.interpolate();
		}
		
		for (CompuPoint p : compuPoints)
		{
			p.interpolateFinalize();
		}

		int i = outRowIndex;
		if (pickInitialize(i))
		{
			for (int j=i; j < i+1 && j<inCsv.rows.size(); ++j)
			{
				if (pickProbabilities(j))
				{
//					break;
				}
			}
			
			madeChange = pickFinalize(doPick(), i);
		}

		for (CompuPoint p : compuPoints)
		{
			p.postCompute();
		}
		
		return madeChange;
	}
		
	public void process (String inFile, String outFile) throws IOException
	{
		inCsv = new CsvIn(inFile);
		outCsv = new CsvOut();
		
		run();
		outCsv.write(outFile);
	}
	
	public void postProcessInitialization (Map<String,String> init)
	{
		for (String s : initializationFlags)
		{
			fileLog.println("initialization", s);
			switch (s)
			{
				case S.switchFeet:
				{
					String l0 = init.get(S.leftAnkle);
					String l1 = init.get(S.leftToe);
					String r0 = init.get(S.rightAnkle);
					String r1 = init.get(S.rightToe);
				
					init.put(S.rightAnkle, l0);
					init.put(S.rightToe, l1);
					init.put(S.leftAnkle, r0);
					init.put(S.leftToe, r1);
				} break;
				
				case S.switchLeftAnkleToe:
				{
					String l0 = init.get(S.leftAnkle);
					String l1 = init.get(S.leftToe);

					init.put(S.leftAnkle, l1);
					init.put(S.leftToe, l0);
				} break;
				
				case S.switchRightAnkleToe:
				{
					String r0 = init.get(S.rightAnkle);
					String r1 = init.get(S.rightToe);

					init.put(S.rightAnkle, r1);
					init.put(S.rightToe, r0);
				} break;
				
				default:
					if (!s.startsWith(S.initializationWindowSize))
						fileLog.println("initialization argument not known!");
			}
		}
	}
	
	public void run ()
	{
		setup();
		
		Map<String,String> initialization = null;
		
		for (String s : initializationFlags)
		{
			if (s.startsWith(S.initializationWindowSize))
			{
				INITIALIZATION_WINDOW_SIZE = new Integer(s.split(":")[1]);
				fileLog.println("Set initialization window size to", INITIALIZATION_WINDOW_SIZE);
			}
		}
		
		int i;
		for (i=0; i<inCsv.rows.size(); ++i)
		{
			initialization = attemptInitialSkeleton(inCsv.rows.get(i), i);
			if (initialization != null)
				break;
		}
		
		if (i >= inCsv.rows.size())
		{
			fileLog.println("could not find initialization vector.");
			return;
		}
		postProcessInitialization(initialization);
		initializeLabels(initialization, i);
		
		for (int j=i; j>=0; --j)
			cycle(j+1, j);

		initializeLabels(initialization, i);
		for (int j=i+1; j<inCsv.rows.size(); ++j)
			cycle(j-1, j);

		int pass = 2;
		boolean madeChange = false;
		do
		{
			// fileLog.println("pass", pass);
			madeChange = false;
			for (int j=inCsv.rows.size()-1; j>=0; --j)
				madeChange = madeChange || cycle(j+1, j);
					
			for (int j=1; j<inCsv.rows.size(); ++j)
				madeChange = madeChange || cycle(j-1, j);
			
			pass++;
		} while (madeChange);
		
		for (int j=0; j<inCsv.rows.size(); ++j)
		{
			Map<String, Output> row = outRows.get(j);
			Map<String, Object> output = new HashMap<String, Object>();
			for (Entry<String, Output> e : row.entrySet())
			{
				if (allNonNull(e.getValue().position))
				{
					output.put(e.getKey(), e.getValue().position);
					output.put(e.getKey()+"+source", e.getValue().label);
				}
			}
			
			outCsv.addRow(output, inCsv.rows.get(j));
		}
	}
	
	public boolean isPointValid (Vector3 v)
	{
		if (v.isNearZero())
			return false;
		
		if (v.x < -800)
			return false;
		
		if (v.x > 1500)
			return false;

		if (v.y < -800)
			return false;
		
		if (v.y > 1300)
			return false;
		
		// there is this one point annoying the fuck out of me
		if (v.x > 700 && v.y < -200 && v.z > 1000)
			return false;
		
		// and there is this other point annoying the fuck out of me
		if (v.x < -380 && v.y > 900 && v.z < 50)
			return false;
		
		return true;

	}
	
	public Vector3 validatePoint (Object o)
	{
		if (o instanceof Vector3)
		{
			Vector3 v = (Vector3)o;
			if (isPointValid(v))
				return v;
		}
		
		return null;
	}
	
	public boolean allNonNull (Object ... a)
	{
		for (Object o : a)
		{
			if (o==null)
				return false;
		}
		return true;
	}
	
	Map<String,Map<String,Integer>> votes = new HashMap<String, Map<String,Integer>>();
	public void addVote(String part, String column)
	{
		if (!votes.containsKey(part))
			votes.put(part, new HashMap<String,Integer>());
		
		if (!votes.get(part).containsKey(column))
			votes.get(part).put(column, 0);
		
		votes.get(part).put(column, votes.get(part).get(column) + 1);
	}
	
	public Map<String, String> getInitialWinners ()
	{
		Map<String, String> winners = new HashMap<String, String>();
		
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
			
			Pair<String, Integer> winner = l.get(0);
			if (l.size() > 1)
			{
				Pair<String, Integer> runnerUp = l.get(1);
				if (winner.second > 10 && winner.second > runnerUp.second/2)
					winners.put(e.getKey(), winner.first);
			}
			else
			{
				if (winner.second > 10)
					winners.put(e.getKey(), winner.first);
			}
		}
		
		if (winners.keySet().size() == compuPoints.length)
			return winners;
		
		return null;
	}
	
	public Map<String, String> vote (Map<String, String > assign, int i)
	{
		for (Entry<String, String> e : assign.entrySet())
		{
			addVote(e.getKey(), e.getValue());
		}
		
		if (i>INITIALIZATION_WINDOW_SIZE)
		{
			for (Entry<String, Map<String, Integer>> e : votes.entrySet())
			{
				for (Entry<String, Integer> v : e.getValue().entrySet())
				{
					if (assign.getOrDefault(e.getKey(), "N/A").equals(v.getKey()))
						continue;
					
					if (v.getValue() > 0)
						v.setValue(v.getValue() - 1);
				}
			}
		}
		
		return getInitialWinners();
	}
	
	public Map<String, String> attemptInitialSkeleton (Map<String, Object> row, int i)
	{
		ArrayList<Vector3WithLabel> vectors = new ArrayList<Vector3WithLabel>();
		for (String label : row.keySet())
		{
			Object o = row.get(label);
			if (!(o instanceof Vector3))
				continue;
			
			Vector3WithLabel v = new Vector3WithLabel(label, (Vector3)o);
			
			if (isPointValid(v))
				vectors.add(v);
		}
		
		if (vectors.size() == 12)
		{
			vectors.sort(new LessThanVector3(Vector3.UnitZ));
			
			for (Vector3WithLabel v : vectors)
			{
				log.println(v.label, v);
			}
			
			List<Vector3WithLabel> torso = new ArrayList<Vector3WithLabel>(vectors.subList(0, 3));
			List<Vector3WithLabel> pelvis = new ArrayList<Vector3WithLabel>(vectors.subList(3, 6));
			List<Vector3WithLabel> knees = new ArrayList<Vector3WithLabel>(vectors.subList(6, 8));
			List<Vector3WithLabel> feet = new ArrayList<Vector3WithLabel>(vectors.subList(8, 12));
			
			torso.sort(new LessThanVector3(Vector3.UnitY));
			pelvis.sort(new LessThanVector3(Vector3.UnitY));
			knees.sort(new LessThanVector3(Vector3.UnitY));
			feet.sort(new LessThanVector3(Vector3.UnitY));
			
			List<Vector3WithLabel> feetRight = feet.subList(0, 2);
			List<Vector3WithLabel> feetLeft = feet.subList(2, 4);
			feetRight.sort(new LessThanVector3(Vector3.UnitX));
			feetLeft.sort(new LessThanVector3(Vector3.UnitX));

			Map<String, String> assign = new HashMap<String,String>();
			
			assign.put(S.rightShoulder, torso.get(0).label);
			assign.put(S.neck, torso.get(1).label);
			assign.put(S.leftShoulder, torso.get(2).label);
			assign.put(S.rightHip, pelvis.get(0).label);
			assign.put(S.centerHip, pelvis.get(1).label);
			assign.put(S.leftHip, pelvis.get(2).label);

			assign.put(S.rightKnee, knees.get(0).label);
			assign.put(S.leftKnee, knees.get(1).label);
			
			assign.put(S.leftAnkle, feetLeft.get(0).label);
			assign.put(S.leftToe, feetLeft.get(1).label);
			assign.put(S.rightAnkle, feetRight.get(0).label);
			assign.put(S.rightToe, feetRight.get(1).label);
			
			Map<String, String> winners = vote(assign, i);
			
			if (winners == null)
				return null;
			
			for (String s : assign.keySet())
				if (!winners.get(s).equals(assign.get(s)))
					return null;
			
			return assign;
		}
		
		return null;
	}
	
	static public void doAll () throws Exception
	{
		List<String> files = Filez.findAllFilesWithExtension("data/converted", "tsv");
		for (String inFile : files)
		{
			String outFile = 
				Filez.replaceExtension(
					Filez.replaceFirstDirectoryPart(inFile, "converted", "markers_2"),
					"tsv"
				).replace(" ", "_");
			
			fileLog.println("Processing " + inFile + " -> " + outFile);
			Filez.ensureDirectory(Filez.getDirectoryPart(outFile));
//			try
			{
				CompuSkel compuSkel = new CompuSkel();
				compuSkel.process(inFile, outFile);
			}
//			catch (Exception e)
			{
//				fileLog.println(e);
			}
		}
	}

	public static void doOne () throws Exception
	{
		CompuSkel compuSkel = new CompuSkel();
		compuSkel.process("data/converted/AB/Pre/SEBTt_M2.tsv", "out.tsv");

		SkeletonCsvViewer.main(new String[] { "out.tsv", "1.0" });
	}
	
	static public void main (String[] args) throws Exception
	{
		//doOne();
		doAll();
	}
}
