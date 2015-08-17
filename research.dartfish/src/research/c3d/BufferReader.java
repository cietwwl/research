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
import java.io.RandomAccessFile;

// External imports
// None

// Local imports
// None

/**
 * Base representation of the buffer reader that does not do any reading
 * itself.
 * <p>
 *
 * Derived versions of this interface do platform-specific reordering of the
 * bytes to generate the right value from the buffer.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
abstract class BufferReader
{
    /** The buffer to read values from */
    protected byte[] buffer = new byte[512];
    
    RandomAccessFile source;
    long currentOffset;
    
    public BufferReader(RandomAccessFile inputStream)
    {
    	this.source = inputStream;
    	currentOffset = 0;
    }
    
    public int read(long offset, int readSize) throws IOException
    {
    	currentOffset += source.read(buffer, 0, readSize);
    	return 0;
    }
    
    /**
     * Read an short from the buffer, starting at the given byte.
     *
     * @param start The start index in the array
     * @return The value read
     */
    abstract int readShort(int start) throws IOException;

    /**
     * Read an int from the buffer, starting at the given byte.
     *
     * @param start The start index in the array
     * @return The value read
     */
    abstract int readInt(int start) throws IOException;

    /**
     * Read a string from the current buffer starting at the given position.
     *
     * @param start The start index in the array
     * @param len The number of bytes (characters) to read
     * @return The string containing that many characters
     */
    String readString(int start, int len) throws IOException
    {
        char[] val = new char[len];
        int end = start + len;

        int idx = 0;
        for(int i = start; i < end; i++)
            val[idx++] = (char)readByte(i);

        return new String(val);
    }

    /**
     * Read an float from the buffer, starting at the given byte.
     *
     * @param start The start index in the array
     * @return The value read
     */
    float readFloat(int start) throws IOException
    {
        int bits = readInt(start);

        return Float.intBitsToFloat(bits);
    }
    
    int readByte(int start) throws IOException
    {
    	currentOffset += 1;
    	return source.read();
    }

	public void readBytes(int offset, byte[] sd1) throws IOException
	{
		int start = read(offset, sd1.length);
		System.arraycopy(buffer, start, sd1, 0, sd1.length);
	}

	public void skipBytes(int i) throws IOException
	{
		source.skipBytes(i);
	}
}

