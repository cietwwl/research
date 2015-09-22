package research.balance;

import java.util.List;

import research.c3d.C3dToCsv;
import research.util.Filez;
import research.util.LogOut;

public class FindAllMarkers
{
	static LogOut log = new LogOut(FindAllMarkers.class);
	
	static public void findMarkers () throws Exception
	{
		List<String> files = Filez.findAllFilesWithExtension("data/converted", "tsv");
		for (String inFile : files)
		{
			String outFile = 
				Filez.replaceExtension(
					Filez.replaceFirstDirectoryPart(inFile, "converted", "markers"),
					"tsv"
				).replace(" ", "_");
			
			log.println("Converting " + inFile + " -> " + outFile);
			Filez.ensureDirectory(Filez.getDirectoryPart(outFile));
			try
			{
				CompuSkel calculator = new CompuSkel();
				calculator.process(inFile, outFile);
			}
			catch (Exception e)
			{
				log.println(e);
			}
		}
	}
	
	static public void main (String[] args) throws Exception
	{
		findMarkers();
	}

}
