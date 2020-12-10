package i5.las2peer.services.socialBotManagerService.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.Vector;

import javax.websocket.DeploymentException;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.services.socialBotManagerService.chat.ChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.chat.RocketChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.SlackChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.MoodleForumMediator;
import i5.las2peer.services.socialBotManagerService.chat.MoodleChatMediator;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.RasaNlu;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

public class Messenger {
	private String name;

	private ChatMediator chatMediator;
	// private RasaNlu rasa;
	// private RasaNlu rasaAssessment;

	// Key: intent keyword
	private HashMap<String, IncomingMessage> knownIntents;

	// Used for keeping conversation state per channel
	private HashMap<String, HashMap<String, IncomingMessage>> stateMap;
	// Used for keeping context between assessment and non-assessment states
	// Key is the channelId
	private HashMap<String, HashMap<String, String>> currentNluModel;
	// Used to know to which Function the received intents/messages are to be sent
	// Is additionally used to check if we are currently communicating with a service(if set, then yes otherwise no)
	private HashMap<String, String> triggeredFunction;
	// dont think i need this one
	// private HashMap<String, Bool> contextWithService;

	private Random random;

	public Messenger(String id, String chatService, String token, SQLDatabase database)
			throws IOException, DeploymentException, ParseBotException {

//		this.rasa = new RasaNlu(rasaUrl);
//        this.rasaAssessment = new RasaNlu(rasaAssessmentUrl);
		if (chatService.contentEquals("Slack")) {
			this.chatMediator = new SlackChatMediator(token);
		} else if (chatService.contentEquals("Rocket.Chat")) {
			this.chatMediator = new RocketChatMediator(token, database, new RasaNlu("rasaUrl"));
		} else if (chatService.contentEquals("Moodle Forum")) {
			this.chatMediator = new MoodleForumMediator(token);
		} else if (chatService.contentEquals("Moodle Chat")) {
			this.chatMediator = new MoodleChatMediator(token);
		} else { // TODO: Implement more backends
			throw new ParseBotException("Unimplemented chat service: " + chatService);
		}
		this.name = id;
		this.knownIntents = new HashMap<String, IncomingMessage>();
		this.stateMap = new HashMap<String, HashMap<String, IncomingMessage>>();
		this.random = new Random();
		// Initialize the assessment setup
		this.currentNluModel = new HashMap<String, HashMap<String, String>>();
		this.triggeredFunction = new HashMap<String, String>();
	}

	public String getName() {
		return name;
	}

	public void addMessage(IncomingMessage msg) {
		this.knownIntents.put(msg.getIntentKeyword(), msg);
	}

	public ChatMediator getChatMediator() {
		return this.chatMediator;
	}
	// set the context of the specified channel
	/*public void setContext(String channel, String contextName){
	    context.put(channel, contextName);
	    
	}*/

	/*  public String getEmail(String channel) throws IOException, SlackApiException {
		return  chatMediator.getEmail(channel);
	};*/

	public void setContextToBasic(String channel, String userid) {
		triggeredFunction.remove(channel);
		
		IncomingMessage state = this.stateMap.get(channel).get(userid);
		if (state != null) {
			if (state.getFollowingMessages() == null) {
				System.out.println("Conversation flow ended now");
			} else	if(state.getFollowingMessages().get("") != null) {
				state = state.getFollowingMessages().get("");
				stateMap.get(channel).put(userid, state);
				this.chatMediator.sendMessageToChannel(channel, state.getResponse(random).getResponse(), Optional.of(userid));

			} else {}
		}
	}

	public String getContext(String channel) {
		return this.triggeredFunction.get(channel);
	}

	// Handles simple responses ("Chat Response") directly, logs all messages and
	// extracted intents into `messageInfos` for further processing later on.
	// TODO: This would be much nicer if we could get a las2peer context here, but this
	// is usually called from the routine thread. Maybe a context can be shared across
	// threads somehow?
	public void handleMessages(ArrayList<MessageInfo> messageInfos, Bot bot) {
		Vector<ChatMessage> newMessages = this.chatMediator.getMessages();
		for (ChatMessage message : newMessages) {
			String userid = message.getUser();
			String channel = message.getChannel();
			
			
			try {
				// If a channel/user pair still isn't assigned to a state, assign it to null
				if (this.stateMap.get(channel) == null) {
					HashMap<String, IncomingMessage> initMap = new HashMap<String, IncomingMessage>();
					initMap.put(userid, null);
					this.stateMap.put(message.getChannel(), initMap);
				}
				
				// If a channel/user pair still isn't assigned to a NLU Model, assign it to the Model 0 
				if (this.currentNluModel.get(channel) == null) {
					HashMap<String, String> initMap = new HashMap<String, String>();
					initMap.put(userid, "0");
					this.currentNluModel.put(message.getChannel(), initMap);
				}
				if (this.currentNluModel.get(channel).get(userid) == null) {
					this.currentNluModel.get(channel).put(userid, "0");
				}
				
				Intent intent = null;
				// Special case: `!` commands
				// System.out.println(this.knownIntents.toString());
				if (message.getText().startsWith("!")) {
					// Split at first occurring whitespace
					System.out.println("This was a command");

					String splitMessage[] = message.getText().split("\\s+", 2);

					// First word without '!' prefix
					String intentKeyword = splitMessage[0].substring(1);
					IncomingMessage incMsg = this.knownIntents.get(intentKeyword);
					// TODO: Log this? (`!` command with unknown intent / keyword)
					if (incMsg == null) {
						if (this.currentNluModel.get(channel).get(userid) == "0") {
							continue;
						} else {
							incMsg = new IncomingMessage(intentKeyword, "");
							incMsg.setEntityKeyword("newEntity");
						}
					}

					String entityKeyword = incMsg.getEntityKeyword();
					String entityValue = null;
					// Entity value is the rest of the message. The whole rest
					// is in the second element, since we only split it into two parts.
					if (splitMessage.length > 1) {
						entityValue = splitMessage[1];
					}

					intent = new Intent(intentKeyword, entityKeyword, entityValue);
				} else {
					// what if you want to start an assessment with a command?
					System.out.println("Intent Extraction now with  : " + this.currentNluModel.get(message.getChannel()));
					intent = bot.getRasaServer(currentNluModel.get(channel).get(userid)).getIntent(message.getText());

				}
				System.out.println(intent.getKeyword());
				String triggeredFunctionId = null;
				IncomingMessage state = this.stateMap.get(channel).get(userid);
				if (state != null) {
					System.out.println("State: " + state.getIntentKeyword());
				}
				// No conversation state present, starting from scratch
				// TODO: Tweak this
				if (!this.triggeredFunction.containsKey(message.getChannel())) {
					if (intent.getConfidence() >= 0.40f) {
						// If there is no previous state, assign the new state tied to the identified intent
						if (state == null) {
							state = this.knownIntents.get(intent.getKeyword());
							System.out.println(
									intent.getKeyword() + " detected with " + intent.getConfidence() + " confidence.");
							stateMap.get(channel).put(userid, state);
						} else {
							// any is a static forward
							// TODO include entities of intents
							// If there is no next state, stay in the same state
							if (state.getFollowingMessages() == null || state.getFollowingMessages().isEmpty()) {
								System.out.println("no follow up messages");
								state = this.knownIntents.get(intent.getKeyword());
								this.currentNluModel.get(channel).put(userid, "0");
								System.out.println(intent.getKeyword() + " detected with " + intent.getConfidence()
										+ " confidence.");
								stateMap.get(channel).put(userid, state);
							// If there is a next state, set the current state to it	
							} else if (state.getFollowingMessages().get(intent.getKeyword()) != null) {
								System.out.println("try follow up message");
								// check ratings
								String keyword = intent.getKeyword();
								String txt = message.getText();
								if (keyword.equals("highrating")
										&& (txt.equals("1") || txt.equals("2") || txt.equals("3"))) {
									keyword = "lowrating";
								} else if (keyword.equals("lowrating") && (txt.equals("4") || txt.equals("5"))) {
									keyword = "highrating";
								}
								state = state.getFollowingMessages().get(intent.getKeyword());
								stateMap.get(channel).put(userid, state);
							} else {
								System.out.println("\u001B[33mDebug --- Followups: " + state.getFollowingMessages() + "\u001B[0m");
								System.out.println("\u001B[33mDebug --- Emptiness: " + state.getFollowingMessages().keySet().isEmpty() + "\u001B[0m");
								System.out.println("\u001B[33mDebug --- State: " + state.getIntentKeyword() + "\u001B[0m");
								System.out.println(intent.getKeyword() + " not found in state map. Confidence: "
										+ intent.getConfidence() + " confidence.");
								// try any
								if (state.getFollowingMessages().get("any") != null) {
									String tmp = message.getText().replaceAll("[^0-9]", "");
									if (tmp.length() > 0 && state.getIntentKeyword().contains("showtasks")) {
										// try to get tasknumber
										int t = Integer.parseInt(tmp);
										if ((message.getRole() % 2) == (t % 2) && t < 9) {
											state = knownIntents.get("t" + tmp);
										} else {
											state = state.getFollowingMessages().get("any");
										}
									} else if (state.getIntentKeyword().contains("functions")) {
										if (message.getText().equals("a") || message.getText().equals("a)")
												|| message.getText().contains("anzeigen")) {
											state = knownIntents.get("showtasks" + message.getRole());
										} else if (message.getText().equals("b") || message.getText().equals("b)")
												|| message.getText().contains("abgeben")) {
											state = knownIntents.get("submission");
										} else if (message.getText().equals("c") || message.getText().equals("c)")
												|| message.getText().contains("Feedback")) {
											state = knownIntents.get("userfeedback");
										}
									} else {
										state = state.getFollowingMessages().get("any");
									}
									stateMap.get(channel).put(userid, state);
								} else  // tud
								if ((intent.getKeyword().equals("zeige") || intent.getKeyword().equals("hast")
										|| intent.getKeyword().equals("will"))
										&& !this.triggeredFunction.containsKey(message.getChannel())) {
									if (intent.getEntity("muster") != null) {
										state = this.knownIntents.get("mustertext");
									} else if (intent.getEntity("video") != null) {
										state = this.knownIntents.get("video");
									} else if (intent.getEntity("help") != null) {
										state = this.knownIntents.get("help");
									} else if (intent.getEntity("pause") != null) {
										state = this.knownIntents.get("pause");
									} else if (intent.getEntity("upload") != null) {
										state = this.knownIntents.get("upload");
									} else if (intent.getEntity("schreibaufgabe") != null) {
										state = this.knownIntents.get("beschreibung");
									} else {
										state = this.knownIntents.get("default");
									}
								}

								// ul
								else if (intent.getEntities().size() > 0 && !this.triggeredFunction.containsKey(message.getChannel())) {
									Collection<Entity> entities = intent.getEntities();
									System.out.println("try to use entity...");
									for (Entity e : entities) {
										System.out.println(e.getEntityName() + " (" + e.getValue() + ")");
										state = this.knownIntents.get(e.getEntityName());
										stateMap.get(channel).put(userid, state);
									}
								}
								else{
									state = this.knownIntents.get("default");
								}
							}
						}
					} else {
						System.out.println(
								intent.getKeyword() + " not detected with " + intent.getConfidence() + " confidence.");
						state = this.knownIntents.get("default");
						System.out.println(state.getIntentKeyword() + " set");
					}
				// No matching intent found, perform default action
				} else if (intent.getConfidence() < 0.40f) {
					intent = new Intent("default", "", "");
				}

				Boolean contextOn = false;
				if (this.triggeredFunction.containsKey(message.getChannel())) {
					triggeredFunctionId = this.triggeredFunction.get(message.getChannel());
					contextOn = true;
				} else {
					if (state != null) {
						ChatResponse response = null;
						if (intent.getEntitieValues().size() == 1) {
							for (ChatResponse res : state.getResponseArray()) {
								System.out.println(res.getTriggerEntity());
								if (res.getTriggerEntity().equals(intent.getEntitieValues().get(0))) {
									response = res;
								}
							}
						}
						if (response == null) {
							response = state.getResponse(this.random);
						}
						if (state.getNluID() != "") {
							System.out.println("New NluId is : " + state.getNluID());
							this.currentNluModel.get(channel).put(userid, state.getNluID());
						}
						if (response != null) {
							System.out.println("Debug - Response : " + response.getResponse());
							if (response.getResponse() != "") {
								if (intent.getEntity("schreibaufgabe") != null
										|| intent.getKeyword().equals("beschreibung")) {
									File f = new File("Schreibauftrag.pdf");
									this.chatMediator.sendFileMessageToChannel(message.getChannel(), f,
											response.getResponse());
								} else if (intent.getEntity("muster") != null
										|| intent.getKeyword().equals("mustertext")) {
									File f = new File("Mustertext.pdf");
									this.chatMediator.sendFileMessageToChannel(message.getChannel(), f,
											response.getResponse());
								} else if (state.getIntentKeyword().equals("suggestMaterial")) {
									// chatbot wl
									String text = message.getText();
									String[] words = text.split(",");
									MiniClient client = new MiniClient();
									client.setConnectorEndpoint("http://137.226.232.175:32303");

									HashMap<String, String> headers = new HashMap<String, String>();
									int counter = 0;
									String s = "";
									for (int i = 0; i < words.length; i++) {
										JSONObject body = new JSONObject();
										JSONArray terms = new JSONArray();
										terms.add(words[i].trim());
										body.put("terms", terms);
										ClientResponse r = client.sendRequest("POST", "materials", body.toJSONString(),
												"application/json", "application/json", headers);

										JSONParser p = new JSONParser();
										JSONObject result = (JSONObject) p.parse(r.getResponse());
										if (result.keySet().size() > 1) {
											counter++;
											JSONArray materials = (JSONArray) result.get("@graph");
											for (Object j : materials) {
												JSONObject jo = (JSONObject) j;
												s += "\\n" + words[i] + ": [" + jo.getAsString("title") + "]("
														+ jo.getAsString("link") + ")";
											}
										}
									}
									this.chatMediator.sendMessageToChannel(message.getChannel(),
											response.getResponse().replace("$X", "" + s));
								} else if (state.getIntentKeyword().equals("liste")) {
									String text = message.getText();
									String[] words = text.split(",");
									JSONArray wordsCleaned = new JSONArray();
									for (int i = 0; i < words.length; i++) {
										wordsCleaned.add(words[i].trim());
									}
									MiniClient client = new MiniClient();
									client.setConnectorEndpoint("http://137.226.232.175:32303");

									HashMap<String, String> headers = new HashMap<String, String>();

									JSONObject body = new JSONObject();
									body.put("terms", wordsCleaned);
									ClientResponse r = client.sendRequest("POST", "compare", body.toJSONString(),
											"application/json", "application/json", headers);

									JSONParser p = new JSONParser();
									JSONObject result = (JSONObject) p.parse(r.getResponse());

									// response = response.replace("$X", result.getAsString("matchCount"));
									this.chatMediator.sendMessageToChannel(message.getChannel(),
											response.getResponse().replace("$X", result.getAsString("matchCount")));
								} else if (state.getIntentKeyword().equals("showtasks")) {
									if (message.getRole() % 2 == 1) {
										state = this.knownIntents.get("showtasks1");
									} else {
										state = this.knownIntents.get("showtasks2");
									}
									response = state.getResponse(this.random);
									this.chatMediator.sendMessageToChannel(message.getChannel(),
											response.getResponse());
								} else {
									String split = "";
									// allows users to use linebreaks \n during the modelling for chat responses
									for (int i = 0; i < response.getResponse().split("\\\\n").length; i++) {
										System.out.println(i);
										split += response.getResponse().split("\\\\n")[i] + " \n ";
									}
									System.out.println(split);
									this.chatMediator.sendMessageToChannel(message.getChannel(), split, Optional.of(message.getUser()));
								}
							} else {
								if (response.getTriggeredFunctionId() != "") {
									this.triggeredFunction.put(message.getChannel(), response.getTriggeredFunctionId());
									contextOn = true;
								} else {
									System.out.println("No Bot Action was given to the Response");
								}
							}
						}
						if (this.triggeredFunction.containsKey(message.getChannel())) {
							triggeredFunctionId = this.triggeredFunction.get(message.getChannel());
						} else
							triggeredFunctionId = state.getTriggeredFunctionId();
						// If conversation flow is terminated, reset state
						if (state.getFollowingMessages().isEmpty()) {
							HashMap<String, IncomingMessage> resetState = new HashMap<String, IncomingMessage>();
							resetState.put(userid, null);
							this.stateMap.put(channel, resetState);
						}
					}
				}
				messageInfos.add(new MessageInfo(message, intent, triggeredFunctionId, bot.getName(),
						bot.getVle().getName(), contextOn));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void close() {
		chatMediator.close();
	}
}