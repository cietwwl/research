package research.forceplate;

import java.util.List;

import research.forceplate.FindAverageAndStdDev;
import research.util.Filez;
import research.util.LogOut;

public class FindAverageAndStdDevAll
{
	static LogOut log = new LogOut(FindAverageAndStdDevAll.class);
	
	static public void convertAll () throws Exception
	{
		List<String> files = Filez.findAllFilesWithExtension("data/clean", "txt");
		for (String inFile : files)
		{
			String outFile = 
				Filez.replaceExtension(
					Filez.replaceFirstDirectoryPart(inFile, "clean", "forceplate"),
					"tsv"
				).replace(" ", "_");
			
			log.println("Converting " + inFile + " -> " + outFile);
			Filez.ensureDirectory(Filez.getDirectoryPart(outFile));
			try
			{
				FindAverageAndStdDev computer = new FindAverageAndStdDev();
				computer.compute(inFile, outFile);
			}
			catch (Exception e)
			{
				log.println(e);
			}
		}
	}
		
	static public void main(String[] args) throws Exception
	{
		convertAll();
	}
}
