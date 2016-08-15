package edu.umass.cs.gnscommon.packets;

import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.gnscommon.GNSCommandProtocol;
import edu.umass.cs.gnscommon.GNSProtocol;
import edu.umass.cs.gnsserver.gnsapp.packet.Packet;
import edu.umass.cs.gnsserver.interfaces.InternalRequestHeader;

/**
 * @author arun A utility class for methods relating to {@link Packet}.
 */
public class PacketUtils {

	/**
	 * @param command
	 * @param response
	 * @return CommandPacket {@code command} with {@code response} in it.
	 */
	public static CommandPacket setResult(CommandPacket command,
			ResponsePacket response) {
		return command.setResult(response);
	}

	/**
	 * @param command
	 * @return JSONObject command within CommandPacket {@code command}.
	 */
	public static JSONObject getCommand(CommandPacket command) {
		return command.getCommand();
	}

	private static String getOriginatingGUID(CommandPacket commandPacket) {
		String oguid = null;
		JSONObject command = commandPacket.getCommand();
		try {
			return command.has(GNSProtocol.ORIGINATING_GUID.toString())
					&& !(oguid = command.getString(GNSProtocol.ORIGINATING_GUID
							.toString())).equals(GNSProtocol.UNKNOWN_NAME
							.toString()) ? oguid : command
					.has(GNSCommandProtocol.READER) ? command
					.getString(GNSCommandProtocol.READER) : command
					.has(GNSCommandProtocol.WRITER) ? command
					.getString(GNSCommandProtocol.WRITER) : null;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param commandPacket
	 * @return {@link InternalRequestHeader} if {@code commandPacket} is an
	 *         internal request.
	 */
	public static InternalRequestHeader getInternalRequestHeader(
			CommandPacket commandPacket) {
		return commandPacket instanceof InternalRequestHeader ? (InternalRequestHeader) commandPacket
				/* originatingGUID must be non-null for internal commands to
				 * make sense because it is important for internal commands to
				 * be chargeable to someone; that someone is the originating
				 * GUID. This is especially important for active requests where
				 * the request chain is computed dynamically and is not known a
				 * priori. */
				: getOriginatingGUID(commandPacket) != null ? 
						
						new InternalRequestHeader() {

					@Override
					public long getOriginatingRequestID() {
						try {
							return commandPacket.getCommand().has(
									GNSProtocol.ORIGINATING_QID.toString()) ? commandPacket
									.getCommand().getLong(
											GNSProtocol.ORIGINATING_QID
													.toString())
									: commandPacket.getRequestID();
						} catch (JSONException e) {
							return commandPacket.getRequestID();
						}
					}

					@Override
					public String getOriginatingGUID() {
						return PacketUtils.getOriginatingGUID(commandPacket);
					}

					@Override
					public int getTTL() {
						try {
							return commandPacket.getCommand().getInt(
									GNSProtocol.REQUEST_TTL.toString());
						} catch (JSONException e) {
							return InternalRequestHeader.DEFAULT_TTL;
						}
					}
				}
						: null;
	}
}
