package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.List;
import java.util.Random;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class SimpleDialogueManager extends AbstractDialogueManager {

    MultivaluedMap<String, String> intentMessageMap;

    public SimpleDialogueManager() {
	super();
	this.intentMessageMap = new MultivaluedHashMap<String, String>();
    }

    @Override
    public String handle(Intent intent) {

	List<String> responses = this.intentMessageMap.get(intent.getKeyword());
	if (responses.isEmpty()) {
	    System.out.println("no responses for intent keyword: " + intent.getKeyword());
	    return null;
	}

	String response = responses.get(new Random().nextInt(responses.size()));
	System.out.println(response);
	return response;

    }

    public void addIntent(String intent, String message) {
	this.intentMessageMap.add(intent, message);
    }

    @Override
    public boolean hasIntent(String intent) {
	return this.intentMessageMap.containsKey(intent);
    }

    @Override
    public String handleDefault() {
	
	if(intentMessageMap.get("default") != null) {
	    List<String> responses = intentMessageMap.get("default");
	    return responses.get(new Random().nextInt(responses.size()));
	}
	
	return "I am a bot";	
    }

}
