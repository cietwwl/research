package research.balance;

import java.util.List;

import research.c3d.C3dToCsv;
import research.util.Filez;
import research.util.LogOut;

public class ConvertAllC3dFiles
{
	static LogOut log = new LogOut(ConvertAllC3dFiles.class);
	
	static public void convertC3d () throws Exception
	{
		List<String> files = Filez.findAllFilesWithExtension("data/clean", "c3d");
		for (String inFile : files)
		{
			String outFile = 
				Filez.replaceExtension(
					Filez.replaceFirstDirectoryPart(inFile, "clean", "converted"),
					"tsv"
				).replace(" ", "_");
			
			log.println("Converting " + inFile + " -> " + outFile);
			Filez.ensureDirectory(Filez.getDirectoryPart(outFile));
			try
			{
				C3dToCsv.convert(inFile, outFile);
			}
			catch (Exception e)
			{
				log.println(e);
			}
		}
	}
		
	static public void main(String[] args) throws Exception
	{
		convertC3d();
	}
}
