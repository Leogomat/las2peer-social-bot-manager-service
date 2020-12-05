package i5.las2peer.services.socialBotManagerService.nlu;

import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import net.minidev.json.JSONObject;

public class TrainingHelper implements Runnable {
	String url;
	String config;
	String markdownTrainingData;

	boolean success = false;
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

	public TrainingHelper(String url, String config, String markdownTrainingData) {
		this.url = url;
		this.config = config;
		this.markdownTrainingData = replaceUmlaute(markdownTrainingData);
	}

	@Override
	// Trains and loads the model trained with the data given in the constructor.
	public void run() {
		MiniClient client = new MiniClient();
		client.setConnectorEndpoint(url);

		JSONObject json = new JSONObject();
		json.put("config", config);
		json.put("nlu", markdownTrainingData);

		HashMap<String, String> headers = new HashMap<String, String>();
		ClientResponse response = client.sendRequest("POST", "model/train", json.toJSONString(),
				MediaType.APPLICATION_JSON + ";charset=utf-8", MediaType.APPLICATION_JSON + ";charset=utf-8", headers);

		String filename = response.getHeader("filename");
		if (filename == null) {
			this.success = false;
			return;
		}

		json = new JSONObject();
		json.put("model_file", "models/" + filename);

		response = client.sendRequest("PUT", "model", json.toString(), MediaType.APPLICATION_JSON + ";charset=utf-8",
				MediaType.APPLICATION_JSON + ";charset=utf-8", headers);
		this.success = response.getHttpCode() == 204;
	}

	public boolean getSuccess() {
		return this.success;
	}
}
