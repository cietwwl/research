package research.balance;

public class S
{
	public static final String 
		rightShoulder = "_rightShoulder" ,
		neck = "_neck",
		leftShoulder = "_leftShoulder",
		rightHip = "_rightHip", 
		centerHip = "_centerHip",
		leftHip = "_leftHip",
		rightKnee = "_rightKnee",
		leftKnee = "_leftKnee",
		leftAnkle = "_leftAnkle",
		leftToe = "_leftToe",
		rightAnkle = "_rightAnkle",
		rightToe = "_rightToe";
	
	public static final String 
		PrightShoulder = "p_rightShoulder" ,
		Pneck = "p_neck",
		PleftShoulder = "p_leftShoulder",
		PrightHip = "p_rightHip", 
		PcenterHip = "p_centerHip",
		PleftHip = "p_leftHip",
		PrightKnee = "p_rightKnee",
		PleftKnee = "p_leftKnee",
		PleftAnkle = "p_leftAnkle",
		PleftToe = "p_leftToe",
		PrightAnkle = "p_rightAnkle",
		PrightToe = "p_rightToe";

	static String[] 
		toe = { S.leftToe, S.rightToe },
		ankle = { S.leftAnkle, S.rightAnkle },
		knee = { S.leftKnee, S.rightKnee },
		hip = { S.leftHip, S.rightHip };
	
	static final int left = 0, right = 1;
		
	static final String
		time = "time";
	
	public static final String
		switchFeet = "switchFeet",
		switchLeftAnkleToe = "switchLeftAnkleToe",
		switchRightAnkleToe = "switchRightAnkleToe",
		initializationWindowSize = "windowSize";
}
