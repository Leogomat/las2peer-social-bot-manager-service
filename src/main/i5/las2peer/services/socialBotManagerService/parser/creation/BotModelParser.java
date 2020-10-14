package i5.las2peer.services.socialBotManagerService.parser.creation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import i5.las2peer.services.socialBotManagerService.model.BotModel;
import i5.las2peer.services.socialBotManagerService.model.BotModelEdge;
import i5.las2peer.services.socialBotManagerService.model.BotModelNode;
import i5.las2peer.services.socialBotManagerService.model.BotModelNodeAttribute;
import i5.las2peer.services.socialBotManagerService.model.BotModelValue;

public class BotModelParser {

    Map<BotModelNode, String> nodes = new LinkedHashMap<>();
    List<BotModelNode> messengers = new ArrayList<>();

    LinkedHashMap<String, BotModelEdge> edgeList = new LinkedHashMap<>();
    LinkedHashMap<String, BotModelNode> nodeList = new LinkedHashMap<>();


    public BotModel parse(Bot bot) {

	BotModel model = new BotModel();

	// VLE Instance
	String vleAddress = "http://127.0.0.1:8070/";
	String vleName = "vleName";
	BotModelNode vleNode = addNode("Instance");
	addAttribute(vleNode, "name", vleName);
	addAttribute(vleNode, "address", vleAddress);

	// Bot
	BotModelNode botNode = addNode("Bot");
	assert bot.getName() != null : "bot has no name";
	addAttribute(botNode, "name", bot.getName());
	addEdge(vleNode, botNode, "has");

	// NLU Knowledge
	String nluName = "defaultNLU";
	String nluID = "0";
	String nluURL = "http://localhost:5005";
	BotModelNode nluNode = addNode("NLU Knowledge");
	addAttribute(nluNode, "Name", nluName);
	addAttribute(nluNode, "ID", nluID);
	addAttribute(nluNode, "URL", nluURL);
	addEdge(botNode, nluNode, "has");

	// Messenger
	for (Messenger messenger : bot.getMessenger()) {
	    BotModelNode messengerNode = addNode("Messenger");
	    addEdge(botNode, messengerNode, "has");

	    assert (messenger.getType() != null) : "messenger type is null";
	    addAttribute(messengerNode, "Messenger Type", messenger.getType().toString());
	    switch (messenger.getType()) {

	    case TELEGRAM:
		TelegramMessenger tele = (TelegramMessenger) messenger;
		addAttribute(messengerNode, "Authentication Token", tele.getToken());
		addAttribute(messengerNode, "Name", "Telegram");
		break;

	    case SLACK:
		SlackMessenger slack = (SlackMessenger) messenger;
		addAttribute(messengerNode, "Authentication Token", slack.getToken());
		addAttribute(messengerNode, "Name", slack.getAppId());
		break;

	    default:
		assert false : "no known messenger " + messenger.getType();
		break;
	    }
	}

	// Function
	for (Function function : bot.getFunction()) {
	    switch (function.getType()) {

	    case CHIT_CHAT:
		ChitChatFunction fn = (ChitChatFunction) function;
		for (Message message : fn.getMessages()) {

		    BotModelNode inNode = addNode("Incoming Message");
		    addAttribute(inNode, "Intent Keyword", message.getIntent());

		    BotModelNode outNode = addNode("Chat Response");
		    addAttribute(outNode, "Message", "hi");

		    addEdge(inNode, outNode, "triggers");
		    addMessengerEdges(inNode, "generates");
		}
		break;

	    case SERVICE_ACCESS:
		AccessServiceFunction as = (AccessServiceFunction) function;

		BotModelNode frameNode = addNode("Frame");
		addAttribute(frameNode, "intent", as.getIntent());

		BotModelNode actionNode = addNode("Bot Action");
		addAttribute(actionNode, "Action Type", "Service");
		addAttribute(actionNode, "Function Name", as.getOperationID());
		addAttribute(actionNode, "Service Alias", as.getServiceURL());

		addEdge(frameNode, actionNode, "triggers");
		addMessengerEdges(frameNode, "generates");
		break;

	    default:
		assert false : "no known function" + function.getType();
		break;

	    }
	}

	model.setEdges(edgeList);
	model.setNodes(nodeList);
	return model;

    }

    public BotModelNode addNode(String type) {

	BotModelNode node = new BotModelNode();
	node.setType(type);
	node.setAttributes(new LinkedHashMap<>());
	String id = getID();
	nodes.put(node, id);
	nodeList.put(id, node);

	if (node.getType().contentEquals("Messenger"))
	    messengers.add(node);

	return node;
    }

    public void addAttribute(BotModelNode node, String name, String value) {

	if (node.getAttributes() == null)
	    node.setAttributes(new LinkedHashMap<String, BotModelNodeAttribute>());

	LinkedHashMap<String, BotModelNodeAttribute> attributes = node.getAttributes();
	BotModelNodeAttribute attr = getAttribute(name, value);
	attributes.put(attr.getId(), attr);
	node.setAttributes(attributes);

    }

    public BotModelNodeAttribute getAttribute(String name, String value) {

	BotModelNodeAttribute res = new BotModelNodeAttribute();
	res.setName(name);
	String id = getID();
	res.setId(id);
	res.setValue(getValue(id, name, value));
	return res;
    }

    public BotModelValue getValue(String id, String name, String value) {

	BotModelValue res = new BotModelValue();
	res.setId(id);
	res.setName(name);
	res.setValue(value);
	return res;
    }

    public BotModelEdge addEdge(BotModelNode source, BotModelNode target, String type) {

	BotModelEdge edge = new BotModelEdge();
	edge.setSource(nodes.get(source));
	edge.setTarget(nodes.get(target));
	edge.setType(type);
	String id = getID();
	edgeList.put(id, edge);
	return edge;

    }

    public void addMessengerEdges(BotModelNode target, String type) {

	for (BotModelNode messenger : this.messengers) {
	    assert messenger.getType().contentEquals("Messenger") : "node should be a Messenger but is "
		    + messenger.getType();
	    addEdge(messenger, target, type);
	}
    }

    public String getID() {
	return UUID.randomUUID().toString();
    }

}