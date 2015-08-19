package research.csv;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import research.math.Vector3;
import research.util.Sortz;

public class Csv
{
	public static enum ColumnType {
		Stringable,
		Vector
	}
	
	public static class Field {
		public int inIndex;
		public String prefix;
		public String name;
		public String subscript;
		
		public Field(String fieldName, int inIndex)
		{
			String[] p = fieldName.split(":");
			prefix = p[0];
			name = p[1];
			subscript = p.length > 2 ? p[2] : null;

			this.inIndex = inIndex;
		}
	}

	public String prefix = null;
	public Map<String, ColumnType > columns = new HashMap<String, ColumnType >();
	public Vector< Map<String, Object> > rows = new Vector < Map<String, Object> >();
	
	protected Csv (String inFileName) throws IOException
	{
		CSVParser parser = CSVFormat.TDF.parse(new FileReader(inFileName));
		
		ArrayList<Field> fields = new ArrayList<Field>();
		
		for (CSVRecord csvRecord : parser) 
		{
			if (fields.isEmpty())
			{
				for (int i=0; i<csvRecord.size(); ++i)
				{
					Field f = new Field(csvRecord.get(i), i);
					if (f.prefix != null)
						prefix = f.prefix;
					
					fields.add(f);
					addColumn(f.name, f.subscript == null ? Double.class : Vector3.class);
				}
			}
			else
			{
				Map<String, Object> row = new HashMap<String, Object>();
				
				for (Field f : fields)
				{
					String s = csvRecord.get(f.inIndex);
					if (s.isEmpty())
					{
						row.put(f.name, null);
					}
					else
					{
						double value = Double.parseDouble(s);

						if (f.subscript != null)
						{
							if (row.get(f.name) == null)
								row.put(f.name, new Vector3());
							
							Vector3 v = (Vector3)row.get(f.name);
							if (f.subscript.equals("X"))
								v.x = value;
							if (f.subscript.equals("Y"))
								v.y = value;
							if (f.subscript.equals("Z"))
								v.z = value;
						}
						else
						{
							row.put(f.name, value);
						}
					}
				}
				
				rows.add(row);
			}
		}
	}
	
	private void addColumn(String name, Class<?> clazz)
	{
		if (clazz == null)
		{
			if (!columns.containsKey(name))
			{
				columns.put(name, ColumnType.Stringable);
			}
		}
		else
		{
			columns.put(
				name, 
				clazz == Vector3.class ? 
					ColumnType.Vector : 
					ColumnType.Stringable
			);
		}
	}

	protected Csv () throws IOException
	{
		
	}
		
	protected void addToRow (Map<String, Object> row, Object[] keyValues)
	{
		String k = null;
		for (Object o : keyValues)
		{
			if (o instanceof HashMap<?,?>)
			{
				@SuppressWarnings("unchecked")
				HashMap<String,Object> h = (HashMap<String,Object>)o;
				for (String l : h.keySet())
				{
					Object v = h.get(l);
					addColumn(l, v != null ? v.getClass() : null);
					row.put(l, v);
				}
			}
			else
			if (o instanceof Object[])
			{
				addToRow(row, (Object[])o);
			}
			else
			{
				if (k == null)
				{
					k = (String)o;
				}
				else
				{
					addColumn(k, o != null ? o.getClass() : null);
					row.put(k, o);
					k = null;
				}
			}
		}		
		
	}
	
	public void addRow (Object... objects)
	{
		Map<String, Object> row = new HashMap<String, Object>();
		addToRow(row, objects);
		rows.add(row);
	}
	
	public Map<String,Object> getRow(int i)
	{
		if (i<0)
			return null;
		if (i>= rows.size())
			return null;
		
		return rows.get(i);
	}

	public Map<String,Object> getRowSubset(int i, String ... keys)
	{
		Map<String, Object> r = new HashMap<String, Object>();
		for (String k : keys)
		{
			r.put(k, rows.get(i).get(k));
		}
		
		return r;
	}
	
	public void write (String outFileName) throws IOException
	{
		FileWriter out = new FileWriter(outFileName);
		String[] allColumns = getExplodedColumns();
		
		Map<String,Integer> columnIndexes = new HashMap<String, Integer>();
		for (String s : allColumns)
			columnIndexes.put(s, columnIndexes.size());
		
		String[] sortedColumns = getExplodedColumns();
		Arrays.sort(sortedColumns, new Sortz.SortIgnoreCase());
		
		CSVPrinter csvPrinter = CSVFormat.TDF.withHeader(sortedColumns).print(out);
		
		for (Map<String, Object> row : rows)
		{
			ArrayList<Object> r = new ArrayList<Object>();
			for (Entry<String, ColumnType> e : columns.entrySet())
			{
				Object o = row.get(e.getKey());
				if (e.getValue() == ColumnType.Vector)
				{
					if (o == null)
					{
						r.add("");
						r.add("");
						r.add("");
					}
					else
					{
						Vector3 v = (Vector3)o;
						r.add(v.x);
						r.add(v.y);
						r.add(v.x);
					}
					
				}
				else
				{
					if (o==null)
					{
						r.add("");
					}
					else
					{
						r.add(o);
					}
				}
			}
			
			for (String c : sortedColumns)
				csvPrinter.print(r.get(columnIndexes.get(c)));
				
			csvPrinter.println();
		}
		
		csvPrinter.close();
		out.close();
	}
	
	public String fullKeyValue(String key)
	{
		if (prefix == null)
			return key;
		else
			return prefix + ":" + key;
	}

	public String[] getExplodedColumnsForKey (String k)
	{
		String key = fullKeyValue(k);
		ColumnType value = columns.get(k);
		if (value == ColumnType.Vector)
		{
			return new String[] {
				key + ":X",
				key + ":Y",
				key + ":Z"
			};
		}

		return new String[] {
			key
		};
	}
	
	public String[] getExplodedColumns ()
	{
		ArrayList<String> c = new ArrayList<String>();
		for (Entry<String, ColumnType> e : columns.entrySet())
		{
			for (String column : getExplodedColumnsForKey(e.getKey()))
				c.add(column);
		}
		
		return c.toArray(new String[0]);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Map<String, Object> row, String k)
	{
		if (row == null)
			return null;
		
		return (T)row.get(k);
	}
	
}
