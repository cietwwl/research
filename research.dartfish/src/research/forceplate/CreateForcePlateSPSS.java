package research.forceplate;

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

public class CreateForcePlateSPSS
{
	static LogOut log = new LogOut(CreateForcePlateSPSS.class);
	
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
	
	String[] functions = { "cov", "average" };
	String[] tasks = { "DD", "DS" };
	String[] tests = { "Pre", "Post" };
	String[] conditions = { "LL", "NM", "M" };
	String[] runs = { "1", "2", "3" };
	String[] variables = { "averageSpeed", "averageSpeedX", "averageSpeedY", "standardDeviation", "standardDeviationX", "standardDeviationY", "duration" };


	Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, Object>>>>>> personToTaskToTestToConditionToVariableToRunValue = new 
			HashMap<String, Map<String, Map<String, Map<String, Map<String, Map<String, Object>>>>>>();
	
	Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, Object>>>>>> dataMap = personToTaskToTestToConditionToVariableToRunValue;
	
	void feedData (String person, String task, String test, String condition, String run, String fileName) throws IOException
	{
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
								String ftask = task;
								if (subject.equals("MG") && test.equals("Pre"))
								{
									if (task.equals("DD"))
										ftask = "DDd";
									else
									if (task.equals("DS"))
										ftask = "DD";
									else
										throw new RuntimeException("wat");
								}
								
								feedData(subject, task, test, condition, run,
									inFileNameBase + "/" + subject + "/" + test + "/" + ftask + "_" + condition + run + ".tsv"
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
									
									double[] d = Maths.toDoubleArray(doubles);
									
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

					csv.write(outFileNameBase + "/" + function + "_" + task + "_" + variable + ".tsv");
				}
			}
		}
		
	}
}
