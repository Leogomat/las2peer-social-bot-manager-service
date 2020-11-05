package i5.las2peer.services.socialBotManagerService.dialogue.nlg;

import java.util.ArrayList;
import java.util.List;

public class ResponseMessage {

    String message;
    String channel;
    boolean end;
    List<String> buttons = new ArrayList<>();
    MessageFile file;

    public ResponseMessage(String message) {
	this.message = message;
    }

    public String getMessage() {
	return message;
    }

    public void setMessage(String message) {
	this.message = message;
    }

    public List<String> getButtons() {
	return buttons;
    }

    public void addButton(String value) {
	this.buttons.add(value);
    }

    public void setButtons(List<String> buttons) {
	this.buttons = buttons;
    }

    public boolean hasButtons() {
	return (this.buttons != null && !this.buttons.isEmpty());
    }

    public boolean isEnd() {
	return end;
    }

    public void setEnd(boolean end) {
	this.end = end;
    }

    public MessageFile getFile() {
	return file;
    }

    public void setFile(MessageFile file) {
	this.file = file;
    }

    public String getChannel() {
	return channel;
    }

    public void setChannel(String channel) {
	this.channel = channel;
    }

}
