package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import java.util.ArrayList;
import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.model.Slot;

public class SlotSet extends ArrayList<Slot> {

	private static final long serialVersionUID = 2835382998066194672L;

	public boolean contains(String name) {
		for (Slot slot : this) {
			if (slot.getName().equals(name) || slot.getIntents().contains(name))
				return true;
		}
		return false;
	}

	public Collection<String> getIntents() {
		Collection<String> intents = new ArrayList<String>();
		for (Slot slot : this) {
			intents.addAll(slot.getIntents());
		}
		return intents;
	}

	public Slot get(String name) {
		for (Slot slot : this) {
			if (slot.getName().equals(name) || slot.getIntents().contains(name))
				return slot;
		}
		return null;
	}

	@Override
	public String toString() {
		String res = "SlotSet [";
		for (Slot slot : this) {
			res = res.concat(slot.toString());
		}
		res = res.concat("]");
		return res;
	}

}
