package i5.las2peer.services.socialBotManagerService.nlu;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.*;
import java.util.ArrayList;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class Intent {
	private String intentKeyword;
	private float confidence;

	private HashMap<String, Entity> entities;

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

	// Constructor for intent extraction through Rasa NLU.
	public Intent(JSONObject json) {
		JSONObject intentInner = (JSONObject) json.get("intent");
		float confidence = intentInner.getAsNumber("confidence").floatValue();
		this.intentKeyword = replaceUmlaute(intentInner.getAsString("name"));
		this.confidence = confidence;

		JSONArray entities = (JSONArray) json.get("entities");
		HashMap<String, Entity> entitiesMap = new HashMap<String, Entity>();

		entities.forEach(o -> {
			Entity entity = new Entity((JSONObject) o);
			entitiesMap.put(entity.getEntityName(), entity);
		});
		this.entities = entitiesMap;
	}

	// Constructor for bypassing intent extraction. Used for '!'-commands, for
	// example.
	public Intent(String intentKeyword, String entityName, String entityValue) {
		this.intentKeyword = replaceUmlaute(intentKeyword);
		this.confidence = 1.0f;
		this.entities = new HashMap<String, Entity>();
		this.entities.put(entityName, new Entity(entityName, entityValue));
	}

	public String getKeyword() {
		return this.intentKeyword;
	}

	public float getConfidence() {
		return this.confidence;
	}

	public Entity getEntity(String entity) {
		return this.entities.get(entity);
	}

	/*
	 * public ArrayList<String> getEntities(){ ArrayList<String> extractedEntities=
	 * new ArrayList<String>(); for(Entry<String, Entity> entry :
	 * entities.entrySet()) { String key = entry.getKey();
	 * extractedEntities.add(key); } return extractedEntities; }
	 */
	public ArrayList<String> getEntitieValues() {
		ArrayList<String> extractedEntitieValues = new ArrayList<String>();
		for (Entry<String, Entity> entry : entities.entrySet()) {
			String value = entry.getValue().getValue();
			extractedEntitieValues.add(value);
		}
		return extractedEntitieValues;
	}

	public Collection<Entity> getEntities() {
		return this.entities.values();
	}
}
