package pertinax.osrscd.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static pertinax.osrscd.net.CacheRequester.State.*;
import static pertinax.osrscd.utils.BufferUtilities.putMediumInt;


/**
 * Manages the requesting of files from the game server.
 *
 * @author Method
 * @author Pertinax
 */
public class CacheRequester {

	public enum State {
		DISCONNECTED, ERROR, OUTDATED, CONNECTING, CONNECTED
	}

	private boolean fallback;
	private Queue<FileRequest> requests;
	private Map<Long, FileRequest> waiting;
	private Socket socket;
	private InputStream input;
	private OutputStream output;
	private State state;
	private String host;
	private int revision;
	private FileRequest current;
	private long lastUpdate;
	private ByteBuffer outputBuffer;
	private ByteBuffer inputBuffer;

	/**
	 * Creates a new CacheRequester instance.
	 */
	public CacheRequester(boolean fallback) {
		this.fallback = fallback;
		requests = new LinkedList<>();
		waiting = new HashMap<>();
		state = DISCONNECTED;
		outputBuffer = ByteBuffer.allocate(4);
		inputBuffer = ByteBuffer.allocate(8);
	}

	/**
	 * Connects to the specified host on port 43594 and initiates the update
	 * protocol handshake.
	 *
	 * @param host  The world to connect to
	 * @param major The client's revision
	 */
	public void connect(String host, int major) {
		this.host = host;
		this.revision = major;

		try {
			socket = new Socket(host, fallback ? 443 : 43594);
			input = socket.getInputStream();
			output = socket.getOutputStream();

			ByteBuffer buffer = ByteBuffer.allocate(5);
			buffer.put((byte) 15); // handshake type
			buffer.putInt(major); // client's revision version
			output.write(buffer.array());
			output.flush();

			state = CONNECTING;
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}

	/**
	 * Submits a request to be sent to the server.
	 *
	 * @param index The cache index the file belongs to
	 * @param file  The file number
	 * @return A FileRequest object representing the requested file.
	 */
	public FileRequest request(int index, int file) {
		FileRequest request = new FileRequest(index, file);
		requests.offer(request);
		return request;
	}

	/**
	 * Gets the current state of the requester.
	 *
	 * @return The requester's current
	 */
	public State getState() {
		return state;
	}

	/**
	 * Handles the bulk of the processing for the requester. This method uses
	 * the current state of the requester to choose the correct action.
	 * <p/>
	 * When connected, this method will send up to 20 requests to the server at
	 * one time, reading and processing them as they are sent back from the
	 * server.
	 */
	public void process() {
		if (state == CONNECTING) {
			try {
				if (input.available() > 0) {
					int response = input.read();
					if (response == 0) {
						System.out.println("Correct version: " + revision);
						System.out.println();
						sendConnectionInfo();
						lastUpdate = System.currentTimeMillis();
						state = CONNECTED;
					} else if (response == 6) {
						state = OUTDATED;
						System.out.println("Invalid version " + revision + ", trying again");
					} else {
						state = ERROR;
					}
				}
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
		} else if (state == OUTDATED) {
			reset();
			connect(host, ++revision);
		} else if (state == ERROR) {
			throw new RuntimeException("Unexpected server response");
		} else if (state == DISCONNECTED) {
			reset();
			connect(host, revision);
		} else {
			if (lastUpdate != 0
					&& System.currentTimeMillis() - lastUpdate > 30000) {
				System.out.println("Server timeout, dropping connection");
				state = DISCONNECTED;
				return;
			}
			try {
				while (!requests.isEmpty() && waiting.size() < 20) {
					FileRequest request = requests.poll();
					outputBuffer.put(request.getIndex() == 255 ? (byte) 1 : (byte) 0);
					putMediumInt(outputBuffer, (int) request.hash());
					output.write(outputBuffer.array());
					output.flush();
					outputBuffer.clear();
					System.out.println("Requested " + request.getIndex() + "," + request.getFile());
					waiting.put(request.hash(), request);
				}
				for (int i = 0; i < 100; i++) {
					int available = input.available();
					if (available < 0) throw new IOException();
					if (available == 0) break;
					lastUpdate = System.currentTimeMillis();
					int needed = 0;
					if (current == null)
						needed = 8;
					else if (current.getPosition() == 0)
						needed = 1;
					if (needed > 0) {
						if (available >= needed) {
							if (current == null) {
								inputBuffer.clear();
								input.read(inputBuffer.array());
								int index = inputBuffer.get() & 0xff;
								int file = inputBuffer.getShort() & 0xffff;
								int compression = (inputBuffer.get() & 0xff) & 0x7f;
								int fileSize = inputBuffer.getInt();
								long hash = ((long) index << 16) | file;
								current = waiting.get(hash);
								if (current == null) {
									throw new IOException();
								}

								int size = fileSize
										+ (compression == 0 ? 5 : 9)
										+ (index != 255 ? 2 : 0);
								current.setSize(size);
								ByteBuffer buffer = current.getBuffer();
								buffer.put((byte) compression);
								buffer.putInt(fileSize);
								current.setPosition(8);
								inputBuffer.clear();
							} else if (current.getPosition() == 0) {
								if (input.read() != 0xff) {
									current = null;
								} else {
									current.setPosition(1);
								}
							} else {
								throw new IOException();
							}
						}
					} else {
						ByteBuffer buffer = current.getBuffer();
						int totalSize = buffer.capacity()
								- (current.getIndex() != 255 ? 2 : 0);
						int blockSize = 512 - current.getPosition();
						int remaining = totalSize - buffer.position();
						if (remaining < blockSize)
							blockSize = remaining;
						if (available < blockSize)
							blockSize = available;
						int read = input.read(buffer.array(),
								buffer.position(), blockSize);
						buffer.position(buffer.position() + read);
						current.setPosition(current.getPosition() + read);
						if (buffer.position() == totalSize) {
							current.setComplete(true);
							waiting.remove(current.hash());
							buffer.flip();
							current = null;
						} else {
							if (current.getPosition() == 512) {
								current.setPosition(0);
							}
						}
					}
				}
			} catch (IOException exception) {
				exception.printStackTrace();
				sendConnectionInfo();
				state = DISCONNECTED;
				System.exit(1);
			}
		}
	}

	/**
	 * Sends the initial connection status and login packets to the server. By
	 * default, this downloader indicates that it is logged out.
	 */
	private void sendConnectionInfo() {
		try {
			outputBuffer.put((byte) 3);
			putMediumInt(outputBuffer, 0);
			output.write(outputBuffer.array());
			output.flush();
			outputBuffer.clear();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Resets the state of the requester. Files that have been sent and are
	 * waiting to be processed will be requested again once the connection is
	 * reestablished.
	 */
	private void reset() {
		try {
			for (FileRequest request : waiting.values()) {
				requests.offer(request);
			}
			waiting.clear();
			socket.close();
			socket = null;
			input = null;
			output = null;
			current = null;
			lastUpdate = 0;
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

}
