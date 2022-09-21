package pertinax.osrscd.utils;

import java.nio.ByteBuffer;

/**
 *
 * Miscellaneous NIO ByteBuffer utilities.
 *
 * @author Pertinax
 * @date 05/01/16 at 6:13 PM.
 */
public class BufferUtilities {

    public static int getSmart(ByteBuffer buffer) {
        return buffer.get(buffer.position()) >= 0 ? buffer.getShort() & 0xffff : buffer.getInt() & 0x7fffffff;
    }

    public static int getMediumInt(ByteBuffer buffer) {
        return ((buffer.get() & 0xff) << 16) | ((buffer.get() & 0xff) << 8) | (buffer.get() & 0xff);
    }

    public static void putMediumInt(ByteBuffer buffer, int val) {
        buffer.put((byte) (val >> 16));
        buffer.put((byte) (val >> 8));
        buffer.put((byte) val);
    }

}
