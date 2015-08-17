package research.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Filez
{
	public static char Slash = '/';
	
	public static List<String> findAllFilesWithExtension(String path, String extension)
	{
		String extensionLower = extension.toLowerCase();
		List<String> result = new ArrayList<String>();
		File[] files = new File(path).listFiles();
		for (File f : files)
		{
			String fileName = Filez.getFileNamePart(f.getAbsolutePath());
			if (f.isDirectory())
			{
				for (String sub : findAllFilesWithExtension(path + Slash + fileName, extension))
					result.add(sub);
			}
			else
			{
				if (fileName.toLowerCase().endsWith(extensionLower))
				{
					result.add(path + Slash + fileName);
				}
			}
		}
		
		return result;
	}
	
	public static String getFileNamePart (String fullPath)
	{
		return fullPath.substring(fullPath.lastIndexOf(Slash)+1);
	}
	
	public static String getDirectoryPart (String fullPath)
	{
		return fullPath.substring(0,fullPath.lastIndexOf(Slash));
	}

	public static String replaceFirstDirectoryPart(String inFile, String from, String to)
	{
		if (inFile.startsWith(from + Slash))
			return replaceFirstDirectory(inFile, to);
		
		String replace = Slash + from + Slash;
		String replaceWith = Slash + to + Slash;
		int begin = inFile.indexOf(replace);
		int end = begin + replace.length();
		return inFile.substring(0, begin) + replaceWith + inFile.substring(end);
	}

	public static String replaceFirstDirectory(String inFile, String directory)
	{
		return directory + Slash + inFile.substring(inFile.indexOf(Slash) + 1);
	}

	public static String replaceExtension(String inFile, String extension)
	{
		return inFile.substring(0, inFile.lastIndexOf(".")) + "." + extension;
	}
	
	public static void ensureDirectory (String directory)
	{
		File f = new File(directory);
		f.mkdirs();
	}
}
