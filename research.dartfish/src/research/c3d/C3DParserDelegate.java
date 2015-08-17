package research.c3d;

public interface C3DParserDelegate
{
	public void onHeader (C3DMetaData metaData) throws Exception;
	public void onRow (C3DRow row) throws Exception;
	public void onEnd() throws Exception;
}
