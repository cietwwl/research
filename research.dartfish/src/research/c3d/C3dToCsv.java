package research.c3d;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import research.exceptions.DataMissing;

public class C3dToCsv
{
	public static class Delegate implements C3DParserDelegate 
	{
		FileWriter out;
		CSVPrinter writer;
		double[] row;
		
		public String findPrefix (String field)
		{
			String[] parts = field.split(":");
			if (parts.length>0)
				return parts[0] + ":";
			
			return "";
		}
		
		public Delegate(String outFileName) throws IOException
		{
			out = new FileWriter(outFileName);
		}
		
		@Override
		public void onHeader(C3DMetaData metaData) throws IOException
		{
			if (metaData.dataDescription.markerLabels == null)
				throw new DataMissing("No marker labels in C3D file");
			
			int nullFields = 0;
			ArrayList<String> fields = new ArrayList<String>();
			for (String label : metaData.dataDescription.markerLabels)
			{
				if (label == null)
					label = "NULL+"+nullFields++;
				
				fields.add(label.trim() + ":X");
				fields.add(label.trim() + ":Y");
				fields.add(label.trim() + ":Z");
			}
			
			String prefix = findPrefix(fields.get(0));
			
			fields.add(prefix + "frame");
			fields.add(prefix + "time");
			
			row = new double[fields.size()];
			writer = CSVFormat.TDF.withHeader(fields.toArray(new String[0])).print(out);
		}

		@Override
		public void onRow(C3DRow in) throws IOException
		{			
			int i=0;
			for (C3DPoint p : in.v)
			{
				row[i++] = p.x;
				row[i++] = p.y;
				row[i++] = p.z;
			}
			
			row[i++] = in.frame;
			row[i++] = in.time;
			
			for (double d : row)
				writer.print(d);
			
			writer.println();
		}
		
		@Override
		public void onEnd() throws IOException
		{
			writer.close();
			out.close();
		}
		
	}
	
	static public void convert (String inFileName, String outFileName) throws Exception
	{
		RandomAccessFile file = new RandomAccessFile(inFileName, "r");
		C3DParser parser = new C3DParser(file);
		parser.parse(new Delegate(outFileName));
	}
	
	public static void main(String[] args) throws Exception
	{
		 C3dToCsv.convert(args[0], args[1]);
	}

}
