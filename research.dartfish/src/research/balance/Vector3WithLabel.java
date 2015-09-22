package research.balance;

import research.math.Vector3;

public class Vector3WithLabel extends Vector3
{
	public Vector3WithLabel(String label, Vector3 o)
	{
		super(o);
		this.label = label;
	}

	public String label;
}
