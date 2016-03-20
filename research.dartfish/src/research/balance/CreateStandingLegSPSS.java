package research.balance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import research.csv.CsvIn;
import research.csv.CsvOut;
import research.math.Maths;
import research.util.Collectionz;
import research.util.Filez;
import research.util.LogOut;
import research.util.Maps;

public class CreateStandingLegSPSS
{
	static LogOut log = new LogOut(CreateStandingLegSPSS.class);
	
	String[][] subjectGroups = 
	{
		{
			"AP",
			"BL",
			"EB",
			"JTB",
			"LG",
			"MG",
			"SG"
		},
		{ 
			"CS",
			"DG",
			"HR",
			"JB",
			"KL",
			"LS",
			"SK"
		}
	};
	
	// person per dd/ds pre/post per condition (LL/NM/M)
	
	String[] functions = { "average" };
	String[] tasks = { "DD", "DS" };
	String[] tests = { "Post", "Pre" };
	String[] conditions = { "LL", "NM", "M" };
	String[] runs = { "1", "2", "3" };
	String[] variables = { 
			"MaxAnk1", "MaxAnk2", "MaxAnk3", "MaxAnkAvg",
			"MaxKnee1", "MaxKnee2", "MaxKnee3", "MaxKneeAvg", 
			"MaxAnkFrame1", "MaxAnkFrame2", "MaxAnkFrame3",
			"MaxKneeFrame1", "MaxKneeFrame2", "MaxKneeFrame3",
			"PelAvg", "PelMax", "PelStd",
			"SacrumDX", "SacrumDY",
			"TrialAvgAnk",
			"TrialMaxAnk", 
			"TrialStdAnk", 
			"TrialAvgKnee", 
			"TrialMaxKnee", 
			"TrialStdKnee"
	};
			
	Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, Object>>>>>> personToTaskToTestToConditionToVariableToRunValue = new 
			HashMap<String, Map<String, Map<String, Map<String, Map<String, Map<String, Object>>>>>>();
	
	Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, Object>>>>>> dataMap = personToTaskToTestToConditionToVariableToRunValue;
	
	void feedData (String person, String task, String test, String condition, String run, String fileName) throws IOException
	{
		log.println("reading ", fileName);
		
		CsvIn in = new CsvIn(fileName);
		if (in.rows.size() < 1)
		{
			log.println(fileName, "does not have right number of rows");
			return;
		}
		
		Map<String, Object> row = in.getRow(0);
		
		Maps.ensureKey(dataMap, person, new HashMap<String, Map<String, Map<String, Map<String, Object>>>>());
		Maps.ensureKey(dataMap.get(person), task, new HashMap<String, Map<String, Map<String, Object>>>());
		Maps.ensureKey(dataMap.get(person).get(task), test,  new HashMap<String, Map<String, Object>>());
		Maps.ensureKey(dataMap.get(person).get(task).get(test), condition, new HashMap<String, Object>());
		dataMap.get(person).get(task).get(test).get(condition).put(run, row);
	}
	
	void readData (String inFileNameBase) throws IOException
	{
		// different spread sheet for DD/DS
		for (String task : tasks)
		{
			for (String[] group : subjectGroups)
			{
				for (String subject : group)
				{
					for (String condition : conditions)
					{
						for (String test : tests)
						{
							for (String run : runs)
							{								
								feedData(subject, task, test, condition, run,
									inFileNameBase + "/" + subject + "/" + test + "/" + task + "_" + condition + run + ".tsv"
								);
							}
						}
					}
				}
			}
		}
		
	}
	
	void writeData (String outFileNameBase) throws IOException
	{
		for (String function : functions)
		{
			// different spread sheet for DD/DS
			for (String task : tasks)
			{
				// different spread sheet for each variable
				for (String variable : variables)
				{
					CsvOut csv = new CsvOut();
					
					for (String[] group : subjectGroups)
					{
						if (!Collectionz.isFirst(subjectGroups, group))
						{
							csv.addRow();
						}
						
						for (String subject : group)
						{
							Map<String, Object> row = new HashMap<String, Object>();
							row.put("Subject", subject);
							
							for (String condition : conditions)
							{
								for (String test : tests)
								{
									// compile data
									ArrayList<Double> doubles = new ArrayList<Double>();
									for (Entry<String, Map<String, Object>> run : dataMap.get(subject).get(task).get(test).get(condition).entrySet())
									{
										doubles.add((Double)run.getValue().get(variable));
									}
									
									double[] d = Maths.toDoubleArrayIgnoreInvalids(doubles);
									
									double value = 0;
									if (function.equals("cov"))
									{
										value = Maths.stddev(d)/Maths.average(d);
									}
									else
									if (function.equals("average"))
									{
										value = Maths.average(d);
									}
									else
									{
										throw new RuntimeException("function not recogniazed");
									}
									
									row.put(condition + " " + test, value);
								}
							}
							
							csv.addRow(row);
							
						}
					}

					String fileName = outFileNameBase + "/" + function + "_" + task + "_" + variable + ".tsv";
					log.println("writing ", fileName);
					csv.write(fileName);
				}
			}
		}
		
	}
}
