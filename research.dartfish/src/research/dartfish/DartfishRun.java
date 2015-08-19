package research.dartfish;

import java.util.List;

import research.c3d.C3dToCsv;
import research.util.Filez;
import research.util.LogOut;

public class DartfishRun
{
	static LogOut log = new LogOut(DartfishRun.class);
	
	static public void convertC3d () throws Exception
	{
		List<String> files = Filez.findAllFilesWithExtension("data/clean/Dartfish Data ALL", "c3d");
		for (String inFile : files)
		{
			String outFile = 
				Filez.replaceExtension(
					Filez.replaceFirstDirectoryPart(inFile, "clean", "converted"),
					"tsv"
				);
			
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
	
	static public void calculateIntermediaryValues () throws Exception
	{
		List<String> files = Filez.findAllFilesWithExtension("data/converted", "tsv");
		for (String inFile : files)
		{
			String outFile = 
				Filez.replaceExtension(
					Filez.replaceFirstDirectoryPart(inFile, "converted", "processed"),
					"tsv"
				);
			
			log.println("Converting " + inFile + " -> " + outFile);
			Filez.ensureDirectory(Filez.getDirectoryPart(outFile));
			try
			{
				CalculateIntermediaryValues calculator = new CalculateIntermediaryValues();
				calculator.process(inFile, outFile);
			}
			catch (Exception e)
			{
				log.println(e);
			}
		}
	}
	
	static public void calculateFinal () throws Exception
	{
		CalculateFinal calculator = new CalculateFinal();

		List<String> files = Filez.findAllFilesWithExtension("data/processed", "tsv");
		for (String inFile : files)
		{
			String outFile = 
				Filez.replaceExtension(
					Filez.replaceFirstDirectoryPart(inFile, "processed", "final"),
					"tsv"
				);
			
			log.println("Converting " + inFile + " -> " + outFile);
			Filez.ensureDirectory(Filez.getDirectoryPart(outFile));
			try
			{
				calculator.process(inFile, outFile);
			}
			catch (Exception e)
			{
				log.println(e);
			}
		}
		
		calculator.finish("data/final/final.tsv");
	}
	
	static public void main(String[] args) throws Exception
	{
		convertC3d();
		calculateIntermediaryValues();
		calculateFinal();
	}
}
