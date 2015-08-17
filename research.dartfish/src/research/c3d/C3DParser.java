/*****************************************************************************
 *                            (c) j3d.org 2002 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package research.c3d;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

/**
 * A low-level parser for the C3D file format.
 * <p>
 *
 * The output of this parser is the parameter block and streams of data
 * expressed as Java float arrays.
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://www.c3d.org">http://www.c3d.org/</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.4 $
 */
public class C3DParser
{
    /** Message when the 2nd byte is not 0x50. */
    private static final String INVALID_FILE_FORMAT =
        "The stream does not represent a C3D file. The binary ID is incorrect";

    /**
     * Message when the user code generates an exception in observer callback
     * for headers.
     */
    private static final String USER_HEADER_ERR =
        "User code implementing C3DParseObserver has generated an exception " +
        "during the headerComplete() callback.";

    /**
     * Message when the user code generates an exception in observer callback
     * for parameter groups.
     */
    private static final String USER_PARAM_ERR =
        "User code implementing C3DParseObserver has generated an exception " +
        "during the parametersComplete() callback.";

    /** Message when the parameter definitions run over the size of the number of
     * blocks that we were told were allocated to the parameter section.
     */
    private static final String PARAM_BLOCK_SIZE_MSG =
        "The file incorrectly states the size of the parameter block for the " +
        "number of parameters we've attempted to read. Some parameters will " +
        "be invalid or truncated";

    /** The label block is missing from the parameters  */
    private static final String LABEL_PARAM_MISSING_ERR =
        "The label parameter values are missing from the file";

    /** The label description block is missing from the parameters  */
    private static final String DESC_PARAM_MISSING_ERR =
        "The description parameter values are missing from the file";

    /**
     * The stream used to fetch the data from. Must be set before calling
     * any methods in this base class. Either the streem or the reader will
     * be set, not bother.
     */
    private RandomAccessFile inputStream;
    private BufferReader reader;

    private C3DMetaData meta;
    C3DParserDelegate delegate;
    
    /**
     * Construct a new parser using the given stream to source the data from.
     *
     * @param is The stream to read data from
     */
    public C3DParser(RandomAccessFile is)
    {
        inputStream = is;
    }

 
    /**
     * Parse the stream now and start generating the output. This will
     * automatically have the first parameter block queued in the reader
     * because we need it to know which file format type is to be processed.
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally
     * @throws Exception 
     */
    public void parse(C3DParserDelegate delegate) throws Exception
    {
    	this.delegate = delegate;
    	meta = new C3DMetaData();
    	
        // The header data is always kept until parsing is complete because it
        // tells us how to find chunks of the rest of the file.
        parseHeader();
        parseParams();
        delegate.onHeader(meta);
    	parseTrajectories(delegate);
    	delegate.onEnd();
    	
    	meta = null;
    }

    /**
     * Parse the header block from the file.
     *
     * @return true if the parsing should continue to the next section
     * @throws IOException some problem reading the basic file.
     */
    private void parseHeader() throws IOException
    {
        // Read the first blocks of 512 bytes

        // First two bytes are a pointer to the start of the parameter block,
        // followed, by 0x50. Look for the magic number first.
    	int param_block = inputStream.read();
    	int fileFormat = inputStream.read();
    	
        if(fileFormat != 0x50)
            throw new IOException(INVALID_FILE_FORMAT);

        // We need to read the first block of the parameter section to find
        // out what format the numbers are in.
        inputStream.seek((param_block - 1) * 512);

        // 83 decimal + processor type.
        // Processor type 1 = Intel
        // Processor type 2 = DEC (VAX, PDP-11)
        // Processor type 3 = MIPS processor (SGI/MIPS)
        inputStream.skipBytes(3);
        int proc_type = inputStream.read() - 83;

        switch(proc_type)
        {
            case 1:
                reader = new IntelBufferReader(inputStream);
                break;

            case 2:
                reader = new DECBufferReader(inputStream);
                break;

            case 3:
                reader = new MIPSBufferReader(inputStream);
                break;
        }

        C3DHeader header = new C3DHeader();
        header.processorType = proc_type;
        header.startParamBlock = param_block;
        
        // go back to the beginning
        inputStream.seek(0);
        
        // skip the param block and file format we read earlier
        inputStream.skipBytes(2);
        header.numTrajectories = reader.readShort(2);
        header.numAnalogChannels = reader.readShort(4);

        header.start3DFrame = reader.readShort(6) - 1;
        header.end3DFrame = reader.readShort(8) - 1;

        header.numTrajectorySamples = header.end3DFrame - header.start3DFrame + 1;

        header.maxInterpolationGap = reader.readShort(10);
        header.scaleFactor = reader.readFloat(12);
        header.startDataBlock = reader.readShort(16);
        header.numAnalogSamplesPer3DFrame = reader.readShort(18);
        header.trajectorySampleRate = reader.readFloat(20);

        // A key value (12345 decimal) is written here if Label and Range data is
        // present, otherwise write 0x00.
        header.hasRangeData = (reader.readShort(294) == 12345);
        header.rangeDataStart = reader.readShort(296);

        // A key value (12345 decimal) present if this file supports 4 char
        // event labels. An older format supported only 2 character labels.
        boolean old_labels = (reader.readShort(298) != 12345);

        header.numTimeEvents = reader.readShort(300);

        // Probably want to bitch if this is greater than 18, but for now,
        // we'll just just truncate.
        if(header.numTimeEvents > 18)
            header.numTimeEvents = 18;

        header.eventTimes = new float[header.numTimeEvents];
        header.eventLabels = new String[header.numTimeEvents];
        header.eventDisplayFlag = new boolean[header.numTimeEvents];

        for(int i = 0; i < header.numTimeEvents; i++)
            header.eventTimes[i] = reader.readFloat(304 + (i << 2));

        // Read display flags. Single byte each. 0x00 is on, 0x01 is off.
        // ie backwards to normal C conventions.
        for(int i = 0; i < header.numTimeEvents; i++)
            header.eventDisplayFlag[i] = reader.readByte(376 + i) == 0;

        for(int i = 0; i < header.numTimeEvents; i++)
            header.eventLabels[i] = reader.readString(396 + (i << 2), 4);

        // Readjust the read values to be more useful
        header.numAnalogChannels /= header.numAnalogSamplesPer3DFrame;
        header.analogSampleRate = header.trajectorySampleRate *
                                  header.numAnalogSamplesPer3DFrame;

        // fast forward to param block
        inputStream.seek((param_block-1) * 512);
        
        meta.header = header;
    }

    /**
     * Parse the parameter block(s).
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally
     * @return true if the parsing should continue to the next section
     * @throws Exception 
     */
    private void parseParams() throws Exception
    {
    	inputStream.skipBytes(2);

    	// The group name as the key, linking to the C3DParameterGroup object
        HashMap<Integer, C3DParameterGroup> param_groups = new HashMap<Integer, C3DParameterGroup>();

        // Read all the param blocks into one big array and process from there.
        int num_param_blocks = reader.readByte(2) & 0xFF;
        int analog_group_id = 0;
        int point_group_id = 0;

        // Start reading the param block. First param or group starts as byte 4
        boolean have_params = true;
        int offset = 4;

        reader.skipBytes(1); // this is the proc_type read before
        
        while(have_params)
        {
            // if num_name_chars is negative, this indicates a locked parameter
            // record.
            int num_name_chars = reader.readByte(offset);
            boolean locked = false;
            int id = (byte)reader.readByte(offset + 1);

            if(num_name_chars < 0)
            {
                locked = true;
                num_name_chars = -num_name_chars;
            }

            String name = reader.readString(offset + 2, num_name_chars);
            name = name.toUpperCase();

            int next_record = reader.readShort(offset + 2 + num_name_chars);
            if(next_record == 0)
                have_params = false;

            // process a group or a parameter
            if(id < 0)
            {
                int desc_size = reader.readByte(offset + 4 + num_name_chars);
                String desc = reader.readString(offset + 5 + num_name_chars,
                                                desc_size);

                // It is possible that one or more params were declared for
                // this group before we got here in the file. If that is the
                // case then we will have a proxy already stored in the map.
                // If there is a proxy, just fill in the extra details.
                C3DParameterGroup grp = (C3DParameterGroup)param_groups.get(id);

                if(grp != null)
                {
                    grp.setName(name);
                    grp.setDescription(desc);
                    grp.setLocked(locked);
                }
                else
                {
                    grp = new C3DParameterGroup(name, locked, id, desc);
                    param_groups.put(id, grp);
                }

                if(name.equals("ANALOG"))
                    analog_group_id = id;
                else if(name.equals("POINT"))
                    point_group_id = id;

                offset += 5 + num_name_chars + desc_size;
            }
            else
            {
                int data_size = (byte)reader.readByte(offset + 4 + num_name_chars);
                int num_dimensions = reader.readByte(offset + 5 + num_name_chars);

                int[] dimensions = new int[num_dimensions];

                for(int i = 0; i < num_dimensions; i++)
                    dimensions[i] = reader.readByte(offset + 6 + num_name_chars + i) & 255;

                offset += 6 + num_name_chars + num_dimensions;

                // Group ID is negative value of parameter ID
                C3DParameterGroup grp =
                    (C3DParameterGroup)param_groups.get(-id);

                // It is possible that we've run across the group before it was
                // defined in the file. This is allowed by C3D. If this is the
                // case, the grp variable will still be null. So, let's create
                // a proxy group object to hold this and any other values for
                // now.
                if(grp == null)
                {
                    grp = new C3DParameterGroup(null, false, -id, null);
                    param_groups.put(-id, grp);
                }

                try
                {
                    switch(data_size)
                    {
                        case -1:
                            C3DStringParameter sp =
                                new C3DStringParameter(name, false, id);
                            offset = readStringParams(sp, dimensions, offset);
                            sp.setLocked(locked);
                            grp.addParameterUnlocked(sp);
                            break;

                        case 1:
                            C3DByteParameter bp =
                                new C3DByteParameter(name, false, id);
                            offset = readByteParams(bp, dimensions, offset);
                            bp.setLocked(locked);
                            grp.addParameterUnlocked(bp);
                            break;

                        case 2:
                            C3DIntParameter ip =
                                new C3DIntParameter(name, false, id);
                            offset = readIntParams(ip, dimensions, offset);
                            ip.setLocked(locked);
                            grp.addParameterUnlocked(ip);
                            break;

                        case 4:
                            C3DFloatParameter fp =
                                new C3DFloatParameter(name, false, id);
                            offset = readFloatParams(fp, dimensions, offset);
                            fp.setLocked(locked);
                            grp.addParameterUnlocked(fp);
                            break;
                    }
                }
                catch(ArrayIndexOutOfBoundsException aioobe)
                {
                    have_params = false;
                	throw aioobe;
//                    delegate.onError(PARAM_BLOCK_SIZE_MSG, null);
                }

                int desc_size = reader.readByte(offset);
                String desc = reader.readString(offset + 1, desc_size);

                offset += 1 + desc_size;
            }
        }

        C3DHeader header = meta.header;
        C3DDataDescription dataDescription = new C3DDataDescription();
        
        // Finally go through and extract all the info we need for the various
        // points and labels, particularly for trajectory data.
        if(meta.header.numTrajectories != 0)
        {
            dataDescription.markerLabels = new String[header.numTrajectories];
            dataDescription.markerDescriptions = new String[header.numTrajectories];

            C3DParameterGroup grp =
                (C3DParameterGroup)param_groups.get(point_group_id);

            // Go looking for the LABEL and DESCRIPTION parameters
            C3DStringParameter label_param =
                (C3DStringParameter)grp.getParameter("LABELS");

            if(label_param == null)
            {
                // delegate.onError(LABEL_PARAM_MISSING_ERR, null);
            	// what to do
            }
            else
            {
                String[] labels = (String[])label_param.getValue();

                int size = (labels.length < header.numTrajectories) ?
                           labels.length : header.numTrajectories;

                System.arraycopy(labels,
                                 0,
                                 dataDescription.markerLabels,
                                 0,
                                 size);
            }

            C3DStringParameter desc_param =
                (C3DStringParameter)grp.getParameter("DESCRIPTIONS");

            if(desc_param == null)
            {
                // delegate.onError(DESC_PARAM_MISSING_ERR, null);
            }
            else
            {
                String[] descriptions = (String[])desc_param.getValue();

                int size = (descriptions.length < header.numTrajectories) ?
                           descriptions.length : header.numTrajectories;

                System.arraycopy(descriptions,
                                 0,
                                 dataDescription.markerDescriptions,
                                 0,
                                 size);
            }
        }

        if(header.numAnalogChannels != 0)
        {
        	dataDescription.analogLabels = new String[header.numAnalogChannels];
            dataDescription.analogDescriptions = new String[header.numAnalogChannels];

            C3DParameterGroup grp =
                (C3DParameterGroup)param_groups.get(analog_group_id);

            // Go looking for the LABEL and DESCRIPTION parameters
            C3DStringParameter label_param =
                (C3DStringParameter)grp.getParameter("LABELS");

            if(label_param == null)
            {
                // delegate.onError(LABEL_PARAM_MISSING_ERR, null);
            }
            else
            {
                String[] labels = (String[])label_param.getValue();

                int size = (labels.length < header.numAnalogChannels) ?
                           labels.length : header.numAnalogChannels;

                System.arraycopy(labels,
                                 0,
                                 dataDescription.analogLabels,
                                 0,
                                 size);
            }

            C3DStringParameter desc_param =
                (C3DStringParameter)grp.getParameter("DESCRIPTIONS");

            if(desc_param == null)
            {
                // delegate.onError(DESC_PARAM_MISSING_ERR, null);
            }
            else
            {
                String[] descriptions = (String[])desc_param.getValue();

                int size = (descriptions.length < header.numAnalogChannels) ?
                            descriptions.length : header.numAnalogChannels;

                System.arraycopy(descriptions,
                                 0,
                                 dataDescription.analogDescriptions,
                                 0,
                                 size);
            }
        }
        
        meta.dataDescription = dataDescription;
    }

    /**
     * Parse the trajectories and analog data values.
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally
     * @throws Exception 
     */
    private void parseTrajectories(C3DParserDelegate delegate) throws Exception
    {
        inputStream.seek((meta.header.startDataBlock - 1) * 512);
        if(meta.header.scaleFactor < 0)
            parseFloatTrajectories(delegate);
        else
            parseIntTrajectories(delegate);

    }

    /**
     * Parse the trajectories and analog data values in floating point format.
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally
     * @throws Exception 
     */
    private void parseFloatTrajectories(C3DParserDelegate delegate) throws Exception
    {
    	C3DHeader header = meta.header;
    	C3DRow row = new C3DRow(header.numTrajectories, header.numAnalogSamplesPer3DFrame, header.numAnalogChannels);
    	int offset = 0;

        for(int i = 0; i < header.numTrajectorySamples; i++)
        {
            for(int j = 0; j < header.numTrajectories; j++)
            {
            	row.v[j].x = reader.readFloat(offset);
            	row.v[j].y = reader.readFloat(offset + 4);
            	row.v[j].z = reader.readFloat(offset + 8);

                int mask = reader.readInt(offset + 12);
                row.v[j].cameraMask = (short)(mask & 0xFF);
                row.v[j].residual = (int)(mask & 0xFF00) * header.scaleFactor;
            }
            
            for(int j = 0; j < header.numAnalogSamplesPer3DFrame; j++)
            {
                for(int k = 0; k < header.numAnalogChannels; k++)
                {
                    row.analog[j][k] =
                        reader.readFloat(offset);
                }
            }
            
            row.setFrameTime(i,  header.trajectorySampleRate);
            delegate.onRow(row);
        }
    }

    /**
     * Parse the trajectories and analog data values in integer format.
     *
     * @param retainData true if the parser should maintain a copy of all the
     *   data read locally
     * @throws Exception 
     */
    private void parseIntTrajectories(C3DParserDelegate delegate) throws Exception
    {
    	C3DHeader header = meta.header;
    	C3DRow row = new C3DRow(header.numTrajectories, header.numAnalogSamplesPer3DFrame, header.numAnalogChannels);

    	int offset = 0;

        for(int i = 0; i < header.numTrajectorySamples; i++)
        {
            for(int j = 0; j < header.numTrajectories; j++)
            {
                row.v[j].x = reader.readShort(offset) * header.scaleFactor;
                row.v[j].y = reader.readShort(offset + 2) * header.scaleFactor;
                row.v[j].z = reader.readShort(offset + 6) * header.scaleFactor;

                int mask = reader.readShort(offset + 8);
                row.v[j].cameraMask = (short)(mask & 0xFF);
                row.v[j].residual = (int)(mask & 0xFF00) * header.scaleFactor;
            }

            for(int j = 0; j < header.numAnalogSamplesPer3DFrame; j++)
            {
                for(int k = 0; k < header.numAnalogChannels; k++)
                {
                    row.analog[j][k] =
                        reader.readFloat(offset);
                }
            }
            
            row.setFrameTime(i,  header.trajectorySampleRate);
            delegate.onRow(row);
        }
    }

    /**
     * Read the string data from the parameter block.
     *
     * @param param The parameter object to stored the parsed value in
     * @param d The dimension sizes
     * @param offset The initial offset into the read buffer
     * @throws IOException 
     */
    private int readStringParams(C3DStringParameter param, int[] d, int offset) throws IOException
    {
        switch(d.length)
        {
            case 0:
                // A string with 0 dimensions is just a single character
                String sd0 = reader.readString(offset, 1);
                offset++;
                param.setValue(sd0);
                break;

            case 1:
                String sd1 = reader.readString(offset, d[0]);
                offset += d[0];
                param.setValue(sd1);
                break;

            case 2:
                String[] sd2 = new String[d[1]];
                for(int i = 0; i < d[1]; i++)
                    sd2[i] = reader.readString(offset + d[0] * i, d[0]);

                offset += d[0] * d[1];
                param.setValue(sd2, d);
                break;

            case 3:
                String[][] sd3 = new String[d[2]][d[1]];
                for(int i = 0; i < d[2]; i++)
                    for(int j = 0; j < d[1]; j++)
                        sd3[i][j] = reader.readString(offset + d[0] * i * j, d[0]);

                offset += d[0] * d[1] * d[2];
                param.setValue(sd3, d);
                break;

            case 4:
                String[][][] sd4 = new String[d[3]][d[2]][d[1]];
                for(int i = 0; i < d[3]; i++)
                    for(int j = 0; j < d[2]; j++)
                        for(int k = 0; k < d[1]; k++)
                            sd4[i][j][k] = reader.readString(offset + d[0] * i * j * k, d[0]);

                offset += d[0] * d[1] * d[2] * d[3];
                param.setValue(sd4, d);
                break;

            case 5:
                String[][][][] sd5 = new String[d[4]][d[3]][d[2]][d[1]];
                for(int i = 0; i < d[4]; i++)
                    for(int j = 0; j < d[3]; j++)
                        for(int k = 0; k < d[2]; k++)
                            for(int l = 0; l < d[1]; l++)
                                sd5[i][j][k][l] = reader.readString(offset + d[0] * i * j * k * l, d[0]);

                offset += d[0] * d[1] * d[2] * d[3] * d[4];
                param.setValue(sd5, d);
                break;

            case 6:
                String[][][][][] sd6 = new String[d[5]][d[4]][d[3]][d[2]][d[1]];
                for(int i = 0; i < d[5]; i++)
                    for(int j = 0; j < d[4]; j++)
                        for(int k = 0; k < d[3]; k++)
                            for(int l = 0; l < d[2]; l++)
                                for(int m = 0; m < d[1]; m++)
                                    sd6[i][j][k][l][m] = reader.readString(offset + d[0] * i * j * k * l * m, d[0]);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5];
                param.setValue(sd6, d);
                break;

            case 7:
                String[][][][][][] sd7 = new String[d[6]][d[5]][d[4]][d[3]][d[2]][d[1]];
                for(int i = 0; i < d[6]; i++)
                    for(int j = 0; j < d[5]; j++)
                        for(int k = 0; k < d[4]; k++)
                            for(int l = 0; l < d[3]; l++)
                                for(int m = 0; m < d[2]; m++)
                                    for(int n = 0; n < d[1]; n++)
                                        sd7[i][j][k][l][m][n] = reader.readString(offset + d[0] * i * j * k * l * m * n, d[0]);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5] * d[6];
                param.setValue(sd7, d);
                break;
        }

        return offset;
    }

    /**
     * Read the string data from the parameter block.
     *
     * @param param The parameter object to stored the parsed value in
     * @param d The dimension sizes
     * @param offset The initial offset into the read buffer
     * @throws IOException 
     */
    private int readByteParams(C3DByteParameter param, int[] d, int offset) throws IOException
    {
        switch(d.length)
        {
            case 0:
                // A string with 0 dimensions is just a single character
                byte sd0 = (byte)reader.readByte(offset);
                offset++;
                param.setValue(sd0);
                break;

            case 1:
                byte[] sd1 = new byte[d[0]];
                reader.readBytes(offset, sd1);
                offset += d[0];
                param.setValue(sd1, d);
                break;

            case 2:
                byte[][] sd2 = new byte[d[1]][d[0]];
                for(int i = 0; i < d[1]; i++)
                	reader.readBytes(offset + d[0] * i, sd2[i]);

                offset += d[0] * d[1];
                param.setValue(sd2, d);
                break;

            case 3:
                byte[][][] sd3 = new byte[d[2]][d[1]][d[0]];
                for(int i = 0; i < d[2]; i++)
                    for(int j = 0; j < d[1]; j++)
                    	reader.readBytes(offset + d[0] * i * j, sd3[i][j]);

                offset += d[0] * d[1] * d[2];
                param.setValue(sd3, d);
                break;

            case 4:
                byte[][][][] sd4 = new byte[d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[3]; i++)
                    for(int j = 0; j < d[2]; j++)
                        for(int k = 0; k < d[1]; k++)
                        	reader.readBytes(offset + d[0] * i * j * k, sd4[i][j][k]);

                offset += d[0] * d[1] * d[2] * d[3];
                param.setValue(sd4, d);
                break;

            case 5:
                byte[][][][][] sd5 = new byte[d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[4]; i++)
                    for(int j = 0; j < d[3]; j++)
                        for(int k = 0; k < d[2]; k++)
                            for(int l = 0; l < d[1]; l++)
                            	reader.readBytes(offset + d[0] * i * j * k * l, sd5[i][j][k][l]);

                offset += d[0] * d[1] * d[2] * d[3] * d[4];
                param.setValue(sd5, d);
                break;

            case 6:
                byte[][][][][][] sd6 = new byte[d[5]][d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[5]; i++)
                    for(int j = 0; j < d[4]; j++)
                        for(int k = 0; k < d[3]; k++)
                            for(int l = 0; l < d[2]; l++)
                                for(int m = 0; m < d[1]; m++)
                                	reader.readBytes(offset + d[0] * i * j * k * l * m, sd6[i][j][k][l][m]);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5];
                param.setValue(sd6, d);
                break;

            case 7:
                byte[][][][][][][] sd7 = new byte[d[6]][d[5]][d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[6]; i++)
                    for(int j = 0; j < d[5]; j++)
                        for(int k = 0; k < d[4]; k++)
                            for(int l = 0; l < d[3]; l++)
                                for(int m = 0; m < d[2]; m++)
                                    for(int n = 0; n < d[1]; n++)
                                    	reader.readBytes(offset + d[0] * i * j * k * l * m * n, sd7[i][j][k][l][m][n]);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5] * d[6];
                param.setValue(sd7, d);
                break;
        }

        return offset;
    }

    /**
     * Read the string data from the parameter block.
     *
     * @param param The parameter object to stored the parsed value in
     * @param d The dimension sizes
     * @param offset The initial offset into the read buffer
     * @throws IOException 
     */
    private int readIntParams(C3DIntParameter param, int[] d, int offset) throws IOException
    {
        switch(d.length)
        {
            case 0:
                // A string with 0 dimensions is just a single character
                int sd0 = reader.readShort(offset);
                offset += 2;
                param.setValue(sd0);
                break;

            case 1:
                int[] sd1 = new int[d[0]];
                for(int i = 0; i < d[0]; i++)
                    sd1[i] = reader.readShort(offset + 2 * i);

                offset += d[0] * 2;
                param.setValue(sd1, d);
                break;

            case 2:
                int[][] sd2 = new int[d[1]][d[0]];
                for(int i = 0; i < d[1]; i++)
                    for(int j = 0; j < d[0]; j++)
                        sd2[i][j] = reader.readShort(offset + 2 * i * j);

                offset += d[0] * d[1] * 2;
                param.setValue(sd2, d);
                break;

            case 3:
                int[][][] sd3 = new int[d[2]][d[1]][d[0]];
                for(int i = 0; i < d[2]; i++)
                    for(int j = 0; j < d[1]; j++)
                        for(int k = 0; k < d[0]; k++)
                            sd3[i][j][k] = reader.readShort(offset + 2 * i * j * k);

                offset += d[0] * d[1] * d[2] * 2;
                param.setValue(sd3, d);
                break;

            case 4:
                int[][][][] sd4 = new int[d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[3]; i++)
                    for(int j = 0; j < d[2]; j++)
                        for(int k = 0; k < d[1]; k++)
                            for(int l = 0; l < d[0]; l++)
                                sd4[i][j][k][l] = reader.readShort(offset + 2 * i * j * k * l);

                offset += d[0] * d[1] * d[2] * d[3] * 2;
                param.setValue(sd4, d);
                break;

            case 5:
                int[][][][][] sd5 = new int[d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[4]; i++)
                    for(int j = 0; j < d[3]; j++)
                        for(int k = 0; k < d[2]; k++)
                            for(int l = 0; l < d[1]; l++)
                                for(int m = 0; m < d[0]; m++)
                                    sd5[i][j][k][l][m] = reader.readShort(offset + 2 * i * j * k * l * m);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * 2;
                param.setValue(sd5, d);
                break;

            case 6:
                int[][][][][][] sd6 = new int[d[5]][d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[5]; i++)
                    for(int j = 0; j < d[4]; j++)
                        for(int k = 0; k < d[3]; k++)
                            for(int l = 0; l < d[2]; l++)
                                for(int m = 0; m < d[1]; m++)
                                    for(int n = 0; n < d[0]; n++)
                                        sd6[i][j][k][l][m][n] = reader.readShort(offset + 2 * i * j * k * l * m * n);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5] * 2;
                param.setValue(sd6, d);
                break;

            case 7:
                int[][][][][][][] sd7 = new int[d[6]][d[5]][d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[6]; i++)
                    for(int j = 0; j < d[5]; j++)
                        for(int k = 0; k < d[4]; k++)
                            for(int l = 0; l < d[3]; l++)
                                for(int m = 0; m < d[2]; m++)
                                    for(int n = 0; n < d[1]; n++)
                                        for(int p = 0; p < d[0]; p++)
                                            sd7[i][j][k][l][m][n][p] = reader.readShort(offset + 2 * i * j * k * l * m * n * p);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5] * d[6] * 2;
                param.setValue(sd7, d);
                break;
        }

        return offset;
    }

    /**
     * Read the string data from the parameter block.
     *
     * @param param The parameter object to stored the parsed value in
     * @param d The dimension sizes
     * @param offset The initial offset into the read buffer
     * @throws IOException 
     */
    private int readFloatParams(C3DFloatParameter param, int[] d, int offset) throws IOException
    {
        switch(d.length)
        {
            case 0:
                // A string with 0 dimensions is just a single character
                float sd0 = reader.readFloat(offset);
                offset += 4;
                param.setValue(sd0);
                break;

            case 1:
                float[] sd1 = new float[d[0]];
                for(int i = 0; i < d[0]; i++)
                    sd1[i] = reader.readFloat(offset + 4 * i);

                offset += d[0] * 4;
                param.setValue(sd1, d);
                break;

            case 2:
                float[][] sd2 = new float[d[1]][d[0]];
                for(int i = 0; i < d[1]; i++)
                    for(int j = 0; j < d[0]; j++)
                        sd2[i][j] = reader.readFloat(offset + 4 * i * j);

                offset += d[0] * d[1] * 4;
                param.setValue(sd2, d);
                break;

            case 3:
                float[][][] sd3 = new float[d[2]][d[1]][d[0]];
                for(int i = 0; i < d[2]; i++)
                    for(int j = 0; j < d[1]; j++)
                        for(int k = 0; k < d[0]; k++)
                            sd3[i][j][k] = reader.readFloat(offset + 4 * i * j * k);

                offset += d[0] * d[1] * d[2] * 4;
                param.setValue(sd3, d);
                break;

            case 4:
                float[][][][] sd4 = new float[d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[3]; i++)
                    for(int j = 0; j < d[2]; j++)
                        for(int k = 0; k < d[1]; k++)
                            for(int l = 0; l < d[0]; l++)
                                sd4[i][j][k][l] = reader.readFloat(offset + 4 * i * j * k * l);

                offset += d[0] * d[1] * d[2] * d[3] * 4;
                param.setValue(sd4, d);
                break;

            case 5:
                float[][][][][] sd5 = new float[d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[4]; i++)
                    for(int j = 0; j < d[3]; j++)
                        for(int k = 0; k < d[2]; k++)
                            for(int l = 0; l < d[1]; l++)
                                for(int m = 0; m < d[0]; m++)
                                    sd5[i][j][k][l][m] = reader.readFloat(offset + 4 * i * j * k * l * m);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * 4;
                param.setValue(sd5, d);
                break;

            case 6:
                float[][][][][][] sd6 = new float[d[5]][d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[5]; i++)
                    for(int j = 0; j < d[4]; j++)
                        for(int k = 0; k < d[3]; k++)
                            for(int l = 0; l < d[2]; l++)
                                for(int m = 0; m < d[1]; m++)
                                    for(int n = 0; n < d[0]; n++)
                                        sd6[i][j][k][l][m][n] = reader.readFloat(offset + 4 * i * j * k * l * m * n);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5] * 4;
                param.setValue(sd6, d);
                break;

            case 7:
                float[][][][][][][] sd7 = new float[d[6]][d[5]][d[4]][d[3]][d[2]][d[1]][d[0]];
                for(int i = 0; i < d[6]; i++)
                    for(int j = 0; j < d[5]; j++)
                        for(int k = 0; k < d[4]; k++)
                            for(int l = 0; l < d[3]; l++)
                                for(int m = 0; m < d[2]; m++)
                                    for(int n = 0; n < d[1]; n++)
                                        for(int p = 0; p < d[0]; p++)
                                            sd7[i][j][k][l][m][n][p] = reader.readFloat(offset + 4 * i * j * k * l * m * n * p);

                offset += d[0] * d[1] * d[2] * d[3] * d[4] * d[5] * d[6] * 4;
                param.setValue(sd7, d);
                break;
        }

        return offset;
    }
}
