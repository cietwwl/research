package research.balance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import research.util.Filez;
import research.util.LogOut;

public class CreateStandingLegSPSSAll
{
	static LogOut log = new LogOut(CreateStandingLegSPSSAll.class);
	
	static public void main (String[] args) throws Exception
	{
		CreateStandingLegSPSS spreadSheeter = new CreateStandingLegSPSS();
		Filez.ensureDirectory("data/standingleg/spss");

		spreadSheeter.readData("data/standingleg/max-peaks");
		spreadSheeter.writeData("data/standingleg/spss");
	}

}
