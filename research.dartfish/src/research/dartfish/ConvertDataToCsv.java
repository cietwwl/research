package research.dartfish;

import research.c3d.C3dToCsv;

public class ConvertDataToCsv
{
	public static void main(String[] args) throws Exception
	{
		C3dToCsv.convert("data/rt1.c3d", "data/rt1.tsv");
	}

}
