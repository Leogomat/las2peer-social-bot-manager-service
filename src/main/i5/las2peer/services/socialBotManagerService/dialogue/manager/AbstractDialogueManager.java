package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public abstract class AbstractDialogueManager {

	public abstract DialogueAct handle(Intent inputSemantic);

	public abstract boolean hasIntent(String intent);
	
	public boolean hasIntent(Intent intent) {
	    return this.hasIntent(intent.getKeyword());
	}

	public abstract DialogueAct handleDefault();
}