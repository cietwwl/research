package research.balance;

import java.util.List;

import research.c3d.C3dToCsv;
import research.util.Filez;
import research.util.LogOut;

public class FindAllAnkleDistances
{
	static LogOut log = new LogOut(FindAllAnkleDistances.class);
	
	static public void findAll () throws Exception
	{
		String source = "markers";
		
		List<String> files = Filez.findAllFilesWithExtension("data/" + source, "tsv");
		for (String inFile : files)
		{
			if (!inFile.toLowerCase().contains("sebt"))
				continue;
			
			String outFile = 
				Filez.replaceExtension(
					Filez.replaceFirstDirectoryPart(inFile, source, "ankles/compute"),
					"tsv"
				).replace(" ", "_");
			
			String peakFileName = 
					Filez.replaceExtension(
						Filez.replaceFirstDirectoryPart(inFile, source, "ankles/peaks"),
						"tsv"
					).replace(" ", "_");

			String maxPeakFileName = 
					Filez.replaceExtension(
						Filez.replaceFirstDirectoryPart(inFile, source, "ankles/max-peaks"),
						"tsv"
					).replace(" ", "_");

			log.println("Processing " + inFile + " -> " + outFile);
			Filez.ensureDirectory(Filez.getDirectoryPart(outFile));
			Filez.ensureDirectory(Filez.getDirectoryPart(peakFileName));
			Filez.ensureDirectory(Filez.getDirectoryPart(maxPeakFileName));
			try
			{
				FindOneAnkleDistances.compute(inFile, outFile, peakFileName, maxPeakFileName, null);
			}
			catch (Exception e)
			{
				log.println(e);
			}
		}
	}
	
	static public void main (String[] args) throws Exception
	{
		findAll();
	}

}
