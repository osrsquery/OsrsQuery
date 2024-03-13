import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class OutputStream extends java.io.OutputStream
{
    private ByteBuffer buffer;

    public OutputStream(int capacity)
    {
        buffer = ByteBuffer.allocate(capacity);
    }

    public OutputStream()
    {
        this(16);
    }

    public byte[] getArray()
    {
        assert buffer.hasArray();
        return buffer.array();
    }

    private void ensureRemaining(int remaining)
    {
        while (remaining > buffer.remaining())
        {
            int newCapacity = buffer.capacity() * 2;

            ByteBuffer old = buffer;
            old.flip();

            buffer = ByteBuffer.allocate(newCapacity);

            buffer.put(old);
        }
    }

    public void skip(int length)
    {
        int pos = buffer.position();
        pos += length;
        buffer.position(pos);
    }

    public int getOffset()
    {
        return buffer.position();
    }

    public void setOffset(int offset)
    {
        buffer.position(offset);
    }

    public void writeBytes(byte[] b)
    {
        writeBytes(b, 0, b.length);
    }

    public void writeBytes(byte[] b, int offset, int length)
    {
        ensureRemaining(length);
        buffer.put(b, offset, length);
    }

    public void writeByte(int i)
    {
        ensureRemaining(1);
        buffer.put((byte) i);
    }

    public void writeBigSmart(int value)
    {

        if (value >= 32768)
        {
            ensureRemaining(4);
            this.writeInt((1 << 31) | value);
        }
        else
        {
            ensureRemaining(2);
            this.writeShort(value);
        }
    }

    public void writeShort(int i)
    {
        ensureRemaining(2);
        buffer.putShort((short) i);
    }

    public void writeShortSmart(int value)
    {

        if (value < 128)
        {
            writeByte(value);
        }
        else
        {
            writeShort(0x8000 | value);
        }
    }

    public void write24BitInt(int i)
    {
        ensureRemaining(3);
        buffer.put((byte) (i >>> 16));
        buffer.put((byte) (i >>> 8));
        buffer.put((byte) (i & 0xFF));
    }

    public void writeInt(int i)
    {
        ensureRemaining(4);
        buffer.putInt(i);
    }

    public void writeVarInt(int var1)
    {
        if ((var1 & -128) != 0)
        {
            if ((var1 & -16384) != 0)
            {
                if ((var1 & -2097152) != 0)
                {
                    if ((var1 & -268435456) != 0)
                    {
                        this.writeByte(var1 >>> 28 | 128);
                    }

                    this.writeByte(var1 >>> 21 | 128);
                }

                this.writeByte(var1 >>> 14 | 128);
            }

            this.writeByte(var1 >>> 7 | 128);
        }

        this.writeByte(var1 & 127);
    }

    public void writeLengthFromMark(int var1)
    {
        this.getArray()[this.getOffset() - var1 - 4] = (byte) (var1 >> 24);
        this.getArray()[this.getOffset() - var1 - 3] = (byte) (var1 >> 16);
        this.getArray()[this.getOffset() - var1 - 2] = (byte) (var1 >> 8);
        this.getArray()[this.getOffset() - var1 - 1] = (byte) var1;
    }

    public void writeString(String str)
    {
        writeBytes(str.getBytes(StandardCharsets.ISO_8859_1));
        writeByte(0);
    }

    public byte[] flip()
    {
        buffer.flip();
        byte[] b = new byte[buffer.limit()];
        buffer.get(b);
        return b;
    }

    @Override
    public void write(int b) throws IOException
    {
        buffer.put((byte) b);
    }

}