package pertinax.osrscd;

/**
 * Defines default runtime parameters.
 *
 * @author Pertinax
 */

public final class Defaults {

	private static final int REVISION = 208;
	private static final int WORLDID = 18;
	private static final boolean FALLBACK = false;

	public static int getRevision() {
		return REVISION;
	}

	public static int getWorldID() {
		return WORLDID;
	}

	public static boolean getFallback() {
		return FALLBACK;
	}

}
