package i5.las2peer.services.socialBotManagerService.dialogue;

public class DialogueAct {

    String intent;
    String message;
    ExpectedInput expected;
    boolean full;
    
    public boolean isFull() {
		return full;
	}

	public void setFull(boolean full) {
		this.full = full;
	}

	public DialogueAct() {

    }

    public DialogueAct(String message) {
	this.message = message;
    }

    public DialogueAct(String message, ExpectedInput expected) {
	super();
	this.message = message;
	this.expected = expected;
    }

    public DialogueAct(String intent, String message, ExpectedInput expected) {
	super();
	this.intent = intent;
	this.message = message;
	this.expected = expected;
    }

    public String getMessage() {
	return message;
    }

    public void setMessage(String message) {
	this.message = message;
    }

    public ExpectedInput getExpected() {
	return expected;
    }

    public void setExpected(ExpectedInput expected) {
	this.expected = expected;
    }

    public String getIntent() {
	return intent;
    }

    public void setIntent(String intent) {
	this.intent = intent;
    }

    public boolean hasIntent() {
	return (this.intent != null);
    }

    public boolean hasMessage() {
	return this.message != null;
    }

    public boolean hasExpected() {
	return this.expected != null;
    }

    public DialogueAct concat(DialogueAct act) {
	
	if (act.hasIntent())
	    this.intent = act.intent;
	if (act.hasMessage())
	    this.message = this.message.concat(act.getMessage());
	if (act.hasExpected())
	    this.expected = act.getExpected();
	return this;
    }

    @Override
    public String toString() {
	return "DialogueAct [intent=" + intent + ", message=" + message + ", expected=" + expected + "]";
    }


}
