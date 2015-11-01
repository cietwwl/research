package research.balance;

import java.util.ArrayList;
import research.util.Filez;
import research.util.LogOut;

public class FindOneMarkers
{
	static LogOut log = new LogOut(FindOneMarkers.class);
	
	static public void findMarkers (String inFile, String outFile, ArrayList<String> flags) throws Exception
	{
		Filez.ensureDirectory(Filez.getDirectoryPart(outFile));
		try
		{
			CompuSkel calculator = new CompuSkel();
			
			calculator.initializationFlags.addAll(flags);
			calculator.process(inFile, outFile);
		}
		catch (Exception e)
		{
			log.println(e);
		}
	}
	
	static public void main (String[] args) throws Exception
	{
		String inputFile = null;
		String outputFile = null;
		
		ArrayList<String> initializationArgs = new ArrayList<String>();
		for (String a : args)
		{
			String[] arg = a.split("=");
			switch(arg[0])
			{
				case "in":
					inputFile = arg[1];
					if (outputFile == null)
						outputFile = Filez.replaceFirstDirectoryPart(inputFile, "converted", "markers");
				break;
				
				case "out":
					outputFile = arg[1];
				break;
				
				case "init":
					initializationArgs.add(arg[1]);
				break;
				
				default:
					System.out.println("Use like: in=myfile.tsv out=otherFile init=initializationOption");
				break;
			}
		}
			
		findMarkers(inputFile, outputFile, initializationArgs);
	}
}
