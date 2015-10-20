package research.csv;

import java.io.IOException;

public class CsvIn extends Csv
{
	public CsvIn(String inFileName) throws IOException
	{
		super(inFileName, 0, 0);
	}

	public CsvIn(String inFileName, int skipRows, int skipColumns) throws IOException
	{
		super(inFileName, skipRows, skipColumns);
	}
}
