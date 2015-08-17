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
 * Buffer reader that reads ints (shorts) and floats in the DEC (VAX/PDP)
 * (middle-endian) byte order.
 * <p>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
class DECBufferReader extends BufferReader
{
    //----------------------------------------------------------
    // Methods defined by BufferReader
    //----------------------------------------------------------

    public DECBufferReader(RandomAccessFile source)
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
    	start = read (start, 2);
    	
        int ch1 = buffer[start] & 255;
        int ch2 = (buffer[start + 1] & 255);

        return ((ch2 << 8) + ch1);
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

        if(ch1 != 0 || ch2 != 0 || ch3 != 0 || ch4 != 0)
            ch2--;

        return ((ch2 << 24) + (ch1 << 16) + (ch4 << 8) + ch3);
    }
}

