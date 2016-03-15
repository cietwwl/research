package research.forceplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import research.util.Filez;
import research.util.LogOut;

public class CreateForcePlateSPSSAll
{
	static LogOut log = new LogOut(CreateForcePlateSPSSAll.class);
	
	static public void main (String[] args) throws Exception
	{
		CreateForcePlateSPSS spreadSheeter = new CreateForcePlateSPSS();
		Filez.ensureDirectory("data/forceplate/final");

		spreadSheeter.readData("data/forceplate");
		spreadSheeter.writeData("data/forceplate/final");
	}

}
