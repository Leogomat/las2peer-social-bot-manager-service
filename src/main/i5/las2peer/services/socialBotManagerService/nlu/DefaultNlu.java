package i5.las2peer.services.socialBotManagerService.nlu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultNlu extends LanguageUnderstander {

    List<String> confirms = new ArrayList<>();
    List<String> denys = new ArrayList<>();

    public DefaultNlu() {

	confirms.add("yes");
	confirms.add("true");
	confirms.add("okay");
	confirms.add("ok");
	confirms.add("correct");
	confirms.add("\\\\uD83C\\\\uDD97");
	confirms.add("\\\\uD83D\\\\uDC4C");
	confirms.add("\\\\uD83D\\\\uDC4D");

	denys.add("no");
	denys.add("false");

    }

    @Override
    public Intent getIntent(String message) {

	Intent intent = new Intent("unknown", 1.0f);

	for (String conf : confirms) {
	    if (message.contentEquals(conf))
		intent.setKeyword("confirm");

	}

	for (String deny : denys) {
	    if (message.contentEquals(deny))
		intent.setKeyword("deny");
	}

	return intent;

    }

    @Override
    public Collection<String> getIntents() {
	Set<String> intents = new HashSet<>();
	intents.add("confirm");
	intents.add("deny");
	return intents;
    }

	@Override
	public void addIntents(Collection<String> intents) {
		// TODO Auto-generated method stub
		
	}

}
