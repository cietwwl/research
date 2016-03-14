package research.balance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import research.util.Filez;
import research.util.LogOut;

public class CreateMasterSpreadSheetAll
{
	static LogOut log = new LogOut(FindStandingLegAnglePeaksAll.class);
	
	static String[] trials = {
		"LL", "NM", "M",
		
	};
	
	static public void findAll () throws Exception
	{
		Map<String, CreateMasterSpreadSheet> spreadSheets = new HashMap<String, CreateMasterSpreadSheet>();
		
		String source = "standingleg/max-peaks/";
		String fullSource = "data/" + source;
		List<String> files = Filez.findAllFilesWithExtension("data/" + source, "tsv");
		for (String inFile : files)
		{
			if (!inFile.toLowerCase().contains("dd") && !inFile.toLowerCase().contains("ds"))
				continue;

			String subject = inFile.substring(fullSource.length()+1);
			String prepost = subject.substring(subject.indexOf("/")+1);
			prepost = prepost.substring(0, prepost.indexOf("/")).toUpperCase();
			subject = subject.substring(0, subject.indexOf("/")).toUpperCase();
			
			String fileName = Filez.replaceExtension(Filez.getFileNamePart(inFile), "");
			String parts[] = fileName.split("_");
			String ddords = parts[0];
			
			String outFileName = subject + "_" + prepost + "_" + ddords;
			
			String outFile = "data/standingleg/final/" + outFileName + ".tsv";
			
			log.println("Processing " + inFile + " -> " + outFile);
			Filez.ensureDirectory(Filez.getDirectoryPart(outFile));
			
			if (!spreadSheets.containsKey(outFile))
				spreadSheets.put(outFile, new CreateMasterSpreadSheet());
			
			spreadSheets.get(outFile).feedData(inFile);
		}
		
		for (Entry<String, CreateMasterSpreadSheet> s : spreadSheets.entrySet())
		{
			s.getValue().writeData(s.getKey(), trials);
		}
	}
	
	static public void main (String[] args) throws Exception
	{
		findAll();
	}

}
