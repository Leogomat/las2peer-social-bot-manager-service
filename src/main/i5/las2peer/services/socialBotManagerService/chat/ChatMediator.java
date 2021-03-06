package i5.las2peer.services.socialBotManagerService.chat;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Vector;
import java.io.File;

public abstract class ChatMediator {
	protected String authToken;

	public ChatMediator(String authToken) {
		this.authToken = authToken;
	}

	/**
	 * Sends a chat message to a channel.
	 *
	 * @param channel A channel ID valid for interacting with the chat service's API
	 * @param text The content of the chat message
	 * @param id An ID for the sent chat message, e.g. to be able to recognize replies to it later on.
	 */
	public abstract void sendMessageToChannel(String channel, String text, OptionalLong id);

	/**
	 * Sends a chat message to a channel.
	 *
	 * @param channel A channel ID valid for interacting with the chat service's API
	 * @param text The content of the chat message
	 */
	public void sendMessageToChannel(String channel, String text) {
		sendMessageToChannel(channel, text, OptionalLong.empty());
	}

	public abstract void sendFileMessageToChannel(String channel, File f, String text, OptionalLong id);

	public void sendFileMessageToChannel(String channel, File f, String text) {
		sendFileMessageToChannel(channel, f, text, OptionalLong.empty());
	}

	/**
	 * Gets messages the mediator received since the last time the method was called.
	 *
	 * @return A Vector containing the ChatMessages received since the last time the method was called.
	 */
	public abstract Vector<ChatMessage> getMessages();

	/**
	 * Gets the IM channel ID for the user registered under the given E-Mail address.
	 *
	 * @param email The E-Mail address of the user to query
	 * @return If user was found, their IM channel ID, null otherwise.
	 */
	public abstract String getChannelByEmail(String email);

	protected String getDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (first)
				first = false;
			else
				result.append("&");
			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}
		return result.toString();
	}

	public abstract void close();
}
