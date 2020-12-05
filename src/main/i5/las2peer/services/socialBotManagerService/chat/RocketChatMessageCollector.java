package i5.las2peer.services.socialBotManagerService.chat;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

import com.rocketchat.core.model.RocketChatMessage;
import com.rocketchat.core.model.RocketChatMessage.Type;

public class RocketChatMessageCollector extends ChatMessageCollector {
	private static HashMap<String, String> UMLAUTE = new HashMap<String, String>();
	static {
		UMLAUTE.put("Ä", "Ae");
		UMLAUTE.put("Ö", "Oe");
		UMLAUTE.put("Ü", "Ue");
		UMLAUTE.put("ä", "ae");
		UMLAUTE.put("ü", "ue");
		UMLAUTE.put("ö", "oe");
		UMLAUTE.put("ß", "ss");
	}

	public static String replaceUmlaute(String orig) {
		if (orig == null) {
			return "";
		}
		String result = orig;
		for (HashMap.Entry<String, String> entry : UMLAUTE.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			result = result.replace(key, value);
		}

		return result;
	}

	public void handle(RocketChatMessage message) {
		Type type = message.getMsgType();
		if (type != null) {
			if (type.equals(Type.TEXT)) {
				try {
					System.out.println("Handling text.");
					JSONArray emails = message.getSender().getEmails();
					// System.out.println(emails.toString());
					String rid = message.getRoomId();
					String user = message.getSender().getUserName();
					String msg = replaceUmlaute(message.getMessage());
					ChatMessage cm = new ChatMessage(rid, user, msg);
					this.addMessage(cm);
					System.out.println("Message added.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Unsupported type: " + type.toString());
			}
		} else {
			System.out.println("Skipped");
		}
	}

	public void handle(RocketChatMessage message, int role, String email) {
		Type type = message.getMsgType();
		if (type != null) {
			if (type.equals(Type.TEXT)) {
				try {
					System.out.println("Handling text.");
					JSONArray emails = message.getSender().getEmails();
					// System.out.println(emails.toString());
					String rid = message.getRoomId();
					String user = message.getSender().getUserName();
					String msg = replaceUmlaute(message.getMessage());
					ChatMessage cm = new ChatMessage(rid, user, msg);
					System.out.println("Email of user is " + email);
					cm.setEmail(email);
					cm.setRole(role);
					this.addMessage(cm);
					System.out.println("Message added.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Unsupported type: " + type.toString());
			}
		} else {
			System.out.println("Skipped");
		}
	}
}
