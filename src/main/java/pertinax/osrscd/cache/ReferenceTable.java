package pertinax.osrscd.cache;

import java.nio.ByteBuffer;

import static pertinax.osrscd.utils.BufferUtilities.getSmart;

/**
 * Contains data about the files stored inside each cache index.
 *
 * @author Method
 * @author Pertinax
 */
public class ReferenceTable {

	/**
	 * Represents a single entry inside the ReferenceTable.
	 *
	 * @author Method
	 */
	public static class Entry {

		private int index;
		private int identifier;
		private int crc;
		private int version;
		private int childCount;
		private int[] childIndices;
		private int childIndexCount;
		private int[] childIdentifiers;
		private byte[] digest;

		public int getIndex() {
			return index;
		}

		public int getIdentifier() {
			return identifier;
		}

		public int getCRC() {
			return crc;
		}

		public int getVersion() {
			return version;
		}

		public int getChildCount() {
			return childCount;
		}

		public int[] getChildIndices() {
			return childIndices;
		}

		public int[] getChildIdentifiers() {
			return childIdentifiers;
		}

		public byte[] getDigest() {
			return digest;
		}

	}

	private int crc;
	private int protocol;
	private int version;
	private int entryCount = 15;
	private Entry[] entries;
	private boolean hasIdentifiers;
	private boolean hasDigests;

	/**
	 * Creates a new ReferenceTable object that contains information on the other reference tables.
	 *
	 * @param buffer The reference table data
	 */
	public ReferenceTable(ByteBuffer buffer) {
		buffer.position(1);
		entryCount = buffer.getInt() / 8;
		entries = new Entry[entryCount];

		for (int i = 0; i < entryCount; i++) {
			Entry entry = entries[i] = new Entry();
			entry.crc = buffer.getInt();
			entry.version = buffer.getInt();
			//entry.digest = new byte[64];
			//buffer.get(entry.digest);
		}

		// not all of the version table is read here - there is still an RSA-encrypted
		// whirlpool hash of the data after this, used for verification purposes
	}

	/**
	 * Creates a new ReferenceTable object for the specified cache index.
	 *
	 * @param index  The cache index this ReferenceTable holds information for
	 * @param buffer The reference table data
	 * @param main   The main ReferenceTable instance
	 */
	public ReferenceTable(int index, ByteBuffer buffer, ReferenceTable main) {
		FileContainer container = new FileContainer(buffer);
		crc = container.getCRC();
		if (main != null) {
			Entry entry = main.getEntries()[index];
			if (entry.crc != crc) {
				throw new RuntimeException("CRC mismatch: " + index + "," + crc + "," + entry.crc);
			}
		}
		unpack(index, container.unpack(), main);
	}

	/**
	 * Unpacks the data inside this ReferenceTable.
	 *
	 * @param index The cache index this ReferenceTable holds information for
	 * @param data  The reference table data
	 * @param main  The main ReferenceTable instance
	 */
	private void unpack(int index, byte[] data, ReferenceTable main) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		protocol = buffer.get() & 0xff;
		if (protocol >= 6) {
			version = buffer.getInt();
			if (main != null) {
				Entry entry = main.getEntries()[index];
				if (entry.version != version) {
					throw new RuntimeException("Version mismatch " + index + "," + version + "," + entry.version);
				}
			}
		}
		int flags = buffer.get() & 0xff;
		hasIdentifiers = (flags & 1) != 0;
		hasDigests = (flags & 2) != 0;

		if (protocol >= 7) {
			entryCount = getSmart(buffer);
		} else {
			entryCount = buffer.getShort() & 0xffff;
		}
		entries = new Entry[entryCount];

		int off = 0;
		int count = -1;
		for (int i = 0; i < entryCount; i++) {
			Entry entry = entries[i] = new Entry();
			entry.index = off += protocol >= 7 ? getSmart(buffer) : (buffer.getShort() & 0xffff);
			if (entry.index > count) {
				count = entry.index;
			}
		}

		for (int i = 0; i < entryCount; i++) {
			entries[i].identifier = hasIdentifiers ? buffer.getInt() : -1;
		}

		for (int i = 0; i < entryCount; i++) {
			entries[i].crc = buffer.getInt();
		}

		if (hasDigests) {
			for (int i = 0; i < entryCount; i++) {
				Entry entry = entries[i];
				entry.digest = new byte[64];
				buffer.get(entry.digest);
			}
		}

		for (int i = 0; i < entryCount; i++) {
			entries[i].version = buffer.getInt();
		}

		for (int i = 0; i < entryCount; i++) {
			entries[i].childCount = protocol >= 7 ? getSmart(buffer) : (buffer.getShort() & 0xffff);
		}

		for (int i = 0; i < entryCount; i++) {
			Entry entry = entries[i];
			off = 0;
			int children = entry.childCount;
			entry.childIndices = new int[children];
			count = -1;
			for (int j = 0; j < children; j++) {
				entry.childIndices[j] = off += protocol >= 7 ? getSmart(buffer) : (buffer.getShort() & 0xffff);
				if (entry.childIndices[j] > count) {
					count = entry.childIndices[j];
				}
			}
			entry.childIndexCount = count + 1;
			if (count + 1 == children) {
				entry.childIndices = null;
			}
		}

		if (hasIdentifiers) {
			for (int i = 0; i < entryCount; i++) {
				Entry entry = entries[i];
				int children = entry.childCount;
				entry.childIdentifiers = new int[entry.childIndexCount];
				for (int j = 0; j < entry.childIndexCount; j++) {
					entry.childIdentifiers[j] = -1;
				}
				for (int j = 0; j < children; j++) {
					int k;
					if (entry.childIndices != null) {
						k = entry.childIndices[j];
					} else {
						k = j;
					}
					entry.childIdentifiers[k] = buffer.getInt();
				}
			}
		}
	}

	/**
	 * Gets the current version of this ReferenceTable.
	 *
	 * @return The current version of this ReferenceTable.
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Gets the entry count of this ReferenceTable.
	 *
	 * @return The entry count of this ReferenceTable.
	 */
	public int getEntryCount() {
		return entryCount;
	}

	/**
	 * Gets the entries of this ReferenceTable.
	 *
	 * @return The entries of this ReferenceTable.
	 */
	public Entry[] getEntries() {
		return entries;
	}

	/**
	 * Gets the entry with with specified index.
	 *
	 * @param index The index of the entry.
	 * @return The requested entry, or null if it could not be found.
	 */
	public Entry getEntry(int index) {
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].index == index) {
				return entries[i];
			}
		}
		return null;
	}

	/**
	 * Gets the CRC32 value of this ReferenceTable's data.
	 *
	 * @return The CRC32 value of this ReferenceTable.
	 */
	public int getCRC() {
		return crc;
	}

}
