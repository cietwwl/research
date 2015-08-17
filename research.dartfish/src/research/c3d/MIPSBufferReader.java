/*****************************************************************************
 *                         (c) j3d.org 2002 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package research.c3d;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

// External imports
// None

// Local imports
// None

/**
 * Buffer reader that reads ints (shorts) and floats in the MIPS
 * (big-endian) byte order.
 * <p>
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
class MIPSBufferReader extends BufferReader
{
    //----------------------------------------------------------
    // Methods defined by BufferReader
    //----------------------------------------------------------

    public MIPSBufferReader(RandomAccessFile source)
	{
		super(source);
		// TODO Auto-generated constructor stub
	}

	/**
     * Read an short from the buffer, starting at the given byte.
     *
     * @param start The start index in the array
     * @return The value read
     */
    int readShort(int start) throws IOException
    {
    	start = read(start,2);
    	
        int ch1 = buffer[start] & 255;
        int ch2 = (buffer[start + 1] & 255);

        return ((ch1 << 8) + ch2);
    }

    /**
     * Read an int from the buffer, starting at the given byte.
     *
     * @param start The start index in the array
     * @return The value read
     */
    int readInt(int start) throws IOException
    {
    	start = read(start,4);

    	int ch1 = buffer[start] & 255;
        int ch2 = (buffer[start + 1] & 255);
        int ch3 = (buffer[start + 2] & 255);
        int ch4 = (buffer[start + 3] & 255);

        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4);
    }
}
