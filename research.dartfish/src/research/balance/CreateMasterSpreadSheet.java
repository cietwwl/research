package research.balance;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import research.csv.CsvIn;
import research.csv.CsvOut;
import research.util.Collectionz;
import research.util.Filez;
import research.util.LogOut;

public class CreateMasterSpreadSheet
{
	static LogOut log = new LogOut(CreateMasterSpreadSheet.class);
	
	String[] keysOrder = { 
			"MaxAnk1", "MaxAnk2", "MaxAnk3", "MaxAnkAvg",
			"MaxKnee1", "MaxKnee2", "MaxKnee3", "MaxKneeAvg", 
			"MaxAnkFrame1", "MaxAnkFrame2", "MaxAnkFrame3",
			"MaxKneeFrame1", "MaxKneeFrame2", "MaxKneeFrame3",
			"PelAvg", "PelMax", "PelStd",
			"TrialAvgAnk", "TrialMaxAnk", "TrialStdAnk", 
			"TrialAvgKnee", "TrialMaxKnee", "TrialStdKnee"
	} ;
	
	Map<String, Map<String, Object>> trialToRow = new HashMap<String, Map<String, Object>>();
	
	void feedData (String fileName) throws IOException
	{
		CsvIn in = new CsvIn(fileName);
		if (in.rows.size() < 1)
		{
			log.println(fileName, "does not have right number of rows");
			return;
		}
		
		String fname = Filez.getFileNamePart(fileName);
		fname = fname.substring(0, fname.indexOf("."));
		
		String[] parts = fname.split("_");
		String type = parts[0];
		String trial = parts[1];
		
		Map<String, Object> row = in.getRow(0);
		row.put("trial", trial);
		trialToRow.put(trial, row);
	}
	
	void writeData (String outFileName, String[] trials) throws IOException
	{
		CsvOut out = new CsvOut();
		
		for (String trial : trials)
		{
			Map<String, Object> sums = new HashMap<String, Object>();
			int count = 0;
			for (Entry<String, Map<String, Object>> tr : trialToRow.entrySet())
			{
				if (!tr.getKey().startsWith(trial))
					continue;
				
				out.addRow(tr.getValue());
				
				for (Entry<String, Object> kv : tr.getValue().entrySet())
				{
					if (!(kv.getValue() instanceof Double))
						continue;
					
					double value = (double) sums.getOrDefault(kv.getKey(), 0.0);
					value += (double) kv.getValue();
					sums.put(kv.getKey(), value);
				}
				
				count++;
			}
			
			for (Entry<String, Object> kv : sums.entrySet())
			{
				kv.setValue(((double)kv.getValue())/count);
			}

			sums.put("trial", "AVG");
			out.addRow(sums);
			out.addRow();
		}
		
		out.write(outFileName);
	}
}
