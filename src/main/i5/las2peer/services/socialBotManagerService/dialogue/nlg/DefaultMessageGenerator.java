package i5.las2peer.services.socialBotManagerService.dialogue.nlg;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;

public class DefaultMessageGenerator extends LanguageGenerator {

    @Override
    public ResponseMessage parse(DialogueAct act) {

	assert act != null : "dialogue act parameter is null";
	assert act.getIntent() != null : "dialogue act has no intent";
	assert act.getIntentType() != null : "no intent type specified";

	System.out.println("get default response for intent " + act.getIntent() + " of type " + act.getIntentType());

	switch (act.getIntentType()) {
	case HOME:
	    return getMainMenu(act);
	case REQUEST_SLOT:
	    return getRequest(act);
	case REQCONF_FRAME:
	    return getReqConf(act);
	case REQCONF_OPTIONAL:
	    return getReqOptional(act);
	case REQCONF_SLOT:
	    break;
	case INFORM_SLOT:
	    return getInform(act);
	case TALK:
	    break;
	default:
	    break;

	}
	return null;

    }

    // Frame Intents

    public ResponseMessage getReqConf(DialogueAct act) {
	assert act != null : "dialogue act parameter is null";

	String message = "We have all necessary data \n";
	for (Map.Entry<String, String> entry : act.getEntities().entrySet())
	    message = message.concat(entry.getKey()).replaceAll("_", " ").concat(": \t ").concat(entry.getValue())
		    .concat(" \n");
	message = message.concat("is this right? \n");

	ResponseMessage res = new ResponseMessage(message);
	return (res);
    }

    public ResponseMessage getReqOptional(DialogueAct act) {
	assert act != null : "dialogue act parameter is null";

	String message = "There are more optional parameters. \n Do you want to fill them?";

	ResponseMessage res = new ResponseMessage(message);
	return (res);
    }

    // Slot Intents

    public ResponseMessage getRequest(DialogueAct act) {

	assert act != null : "dialogue act parameter is null";
	assert act.getEntities() != null : "dialogue act has no entities";
	assert act.getEntities().containsKey("name") : "dialogue act has no name entitiy";

	String message = "";
	Map<String, String> entities = act.getEntities();
	System.out.println(entities.entrySet());

	String name = entities.get("name");
	message = message.concat("What is the *").concat(name).concat("* \n\n");
	System.out.println(message);

	if (entities.containsKey("description"))
	    message = message.concat("Description:\t" + entities.get("description") + "\n");

	if (entities.containsKey("example"))
	    message = message.concat("Example:    \t" + entities.get("example") + "\n");

	if (act.hasExpected() && act.getExpected().getType() != null)
	    message = message.concat("\n" + this.InputTypeMessage(act.getExpected()) + "\n");
	if (act.getExpected().hasEnums()) {
	    List<String> enums = act.getExpected().getEnums();
	    message = message.concat(enums.get(0));
	    for (String enu : enums.subList(1, enums.size()))
		message = message.concat(", " + enu);
	}

	ResponseMessage res = new ResponseMessage(message);
	return res;
    }

    public ResponseMessage getInform(DialogueAct act) {

	assert act != null : "dialogue act parameter is null";
	assert act.getEntities() != null : "dialogue act has no entities";
	assert act.getEntities().containsKey("name") : "dialogue act has no name entitiy";

	String message = "";
	Map<String, String> entities = act.getEntities();

	String name = entities.get("name");
	message = "*" + name + "*";

	if (entities.containsKey("description"))
	    message = message.concat("description:\t" + entities.get("description") + "\n");

	if (entities.containsKey("value"))
	    message = message.concat("current value:\t" + entities.get("value") + "\n");

	ResponseMessage res = new ResponseMessage(message);
	return res;
    }

    public String InputTypeMessage(ExpectedInput inputType) {
	assert inputType != null : "inputType parameter is null";
	assert inputType.getType() != null : "inputType has no type";

	String message = "";
	switch (inputType.getType()) {

	case Confirmation:
	    message = "Please confirm or deny";
	    break;
	case Date:
	    message = "Please give a date in the format \"yyyy-MM-dd\" ";
	    break;
	case Decimal:
	case Number:
	    message = "Please give a number";
	    break;
	case Enum:
	    message = "Please choose one of this possibilites: ";
	    break;
	case Free:
	    message = "Please answer with a free text message";
	    break;
	case Url:
	    message = "Please answer with a valid url";
	    break;
	case Word:
	    message = "Please answer with one word";
	    break;
	default:
	    break;

	}

	return message;
    }

    public ResponseMessage getMainMenu(DialogueAct act) {

	assert act != null : "dialogue act parameter is null";
	assert act.getEntities() != null : "dialogue act has no entities";

	Map<String, String> entities = act.getEntities();
	String message = "Hi, I am a bot. \n I can perform the following operations: \n ";

	for (Entry<String, String> entity : entities.entrySet()) {
	    assert entity.getKey() != null : "entity no key";
	    assert entity.getValue() != null : "entity no value";
	    System.out.println(entity.toString());
	    message = message.concat("/" + entity.getKey() + " - " + entity.getValue()).concat("\n");
	}

	message = message.concat(
		"\n During conversation you can use the following commands: \n /cancel Aborts the current operation \n /revert Reverts your last input.");

	ResponseMessage res = new ResponseMessage(message);
	return res;
    }

    public String getInvalidValue(ExpectedInput input) {
	
	assert input != null : "expected input parameter is null";
	
	String message = null;
	switch (input.getType()) {

	case Confirmation:
	    message = "please clearly state that you agree (yes) or disagree (no)";
	    break;

	case Enum:
	    message = "Please choose one of this possible answers: \n";
	    for (String enu : input.getEnums()) {
		message = message + enu + "\n";
	    }
	    break;

	case Number:
	    message = "Please answer with a number \n";
	    break;

	case Url:
	    message = "Please answer with a valid url \n";
	    break;

	case Word:
	    message = "Please answer in one word without spaces \n";
	    break;

	default:
	    break;

	}
	
	

	return message;
    }

}
