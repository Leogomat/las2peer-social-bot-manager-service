package i5.las2peer.services.socialBotManagerService.chat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.files.FilesUploadRequest;
import com.slack.api.methods.request.files.FilesUploadRequest.FilesUploadRequestBuilder;
import com.slack.api.methods.response.auth.AuthTestResponse;
import com.slack.api.methods.response.bots.BotsInfoResponse;
import com.slack.api.methods.response.bots.BotsInfoResponse.Bot;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.files.FilesUploadResponse;

import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import net.minidev.json.JSONObject;

public class SlackChatMediator extends EventChatMediator {

	/**
	 * Main object of the Slack API Java library
	 */
	private Slack slack;

	/**
	 * Team ID of Slack workspace (e.g T018LTPPG1J)
	 */
	private String teamID;

	/**
	 * App ID of Slack application (e.g A018LTPPG1J)
	 */
	private String appID;

	/**
	 * @param authToken used to authenticate the bot when accessing the slack API
	 */
	public SlackChatMediator(String authToken) {
		super(authToken);
		this.slack = Slack.getInstance();
		this.requestAuthTest();
	}

	@Override
	public ChatMessage handleEvent(JSONObject event) {

		ChatMessage message = new ChatMessage();

		String type = (String) event.get("type");
		switch (type) {
		case "message":
			System.out.println("slack event: message");
			if (event.get("bot_id") != null)
				break;
			message = this.parseMessage(event);
			break;
		case "app_mention":
			System.out.println("slack event: app mention");
			String channel = (String) event.get("channel");
			String user = (String) event.get("user");
			this.sendMessageToChannel(channel, "hello " + user);
			break;
		case "team_join":
			System.out.println("slack event: team_join");
			this.sendMessageToChannel("C01880R2NPQ", "hello");
			break;
		default:
			System.out.println("unknown slack event received");
		}
		return message;
	}

	/**
	 * Sends an auth.test request to the slack event API. Receives the
	 * team/workspace id of the authenticated bot.
	 */
	private void requestAuthTest() {

		try {
			AuthTestResponse response = slack.methods().authTest(req -> req.token(authToken));
			if (response.isOk()) {
				System.out.println("Bot Authentication: " + response.isOk());
				this.setTeamID(response.getTeamId());
			} else {
				System.out.println(response.getError());
			}
		} catch (SlackApiException requestFailure) {
			System.out.println("Slack API responded with unsuccessful status code");
		} catch (IOException connectivityIssue) {
			System.out.println("Failed to connect to Slack API");
		}
	}

	/**
	 * Sends an bots.info request to the slack event API. Requires the users:read
	 * scope in the slack app. Receives the application id of the authenticated bot.
	 */
	private void requestBotInfo() {

		try {
			BotsInfoResponse response = slack.methods().botsInfo(req -> req.token(authToken));
			if (response.isOk()) {
				System.out.println("Bot Info received: " + response.isOk());
				System.out.println(response.toString());
				Bot botInfo = response.getBot();
				System.out.println(botInfo.toString());
				this.setAppID(botInfo.getAppId());
			} else {
				System.out.println(response.getError());
			}
		} catch (SlackApiException requestFailure) {
			System.out.println("Slack API responded with unsuccessful status code");
		} catch (IOException connectivityIssue) {
			System.out.println("Failed to connect to Slack API");
		}
	}

	/**
	 * Parses a message event
	 * 
	 * @param parsedMessage of the slack message event in JSON format
	 */
	public ChatMessage parseMessage(JSONObject parsedMessage) {

		try {
			String type = parsedMessage.getAsString("type");
			if (type == null || !type.equals("message"))
				throw new InvalidChatMessageException("not a message type");

			String channel = parsedMessage.getAsString("channel");
			String user = parsedMessage.getAsString("user");
			String text = parsedMessage.getAsString("text");
			String timestamp = parsedMessage.getAsString("ts");

			if (channel == null || user == null || text == null || timestamp == null) {
				throw new InvalidChatMessageException("missing message fields");
			}

			ChatMessage message = new ChatMessage(channel, user, text, timestamp);
			// this.addMessage(message);
			return message;

		} catch (InvalidChatMessageException e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public void sendMessageToChannel(ResponseMessage response) {

		assert response != null;
		assert response.getChannel() != null;

		if (response.getFile() != null)
			sendFileMessageToChannel(response);
		
		String channel = response.getChannel();
		String text = response.getMessage();

		sendMessageToChannel(channel, text);

	}

	@Override
	public void sendMessageToChannel(String channel, String text, OptionalLong id) {

		System.out.println("send message to slack channel " + channel);
		assert channel != null;
		assert text != null;

		try {
			ChatPostMessageResponse response = slack.methods(authToken)
					.chatPostMessage(req -> req.channel(channel).text(text));
			if (response.isOk()) {
				System.out.println("Message sent: " + response.isOk());
			} else {
				System.out.println(response.getError());
			}
		} catch (SlackApiException requestFailure) {
			System.out.println("Slack API responded with unsuccessful status code");
		} catch (IOException connectivityIssue) {
			System.out.println("Failed to connect to Slack API");
		}

	}

	public void sendBlockToChannel(String channel, String text) {

		try {
			ChatPostMessageResponse response = slack.methods(authToken)
					.chatPostMessage(req -> req.channel(channel).blocksAsString(text));
			if (response.isOk()) {
				System.out.println("Message sent: " + response.isOk());
			} else {
				System.out.println(response.getError()); // e.g., "invalid_auth", "channel_not_found"
			}
		} catch (SlackApiException requestFailure) {
			// Slack API responded with unsuccessful status code (= not 20x)
			System.out.println("Slack API responded with unsuccessful status code");
		} catch (IOException connectivityIssue) {
			System.out.println("Failed to connect to Slack API");
		}

	}

	public void sendFileMessageToChannel(ResponseMessage responseMessage) {

		assert responseMessage != null;
		assert responseMessage.getChannel() != null;
		assert responseMessage.getFile() != null;

		String channel = responseMessage.getChannel();
		String fileData = responseMessage.getFile().getDataString();
		String fileName = responseMessage.getFile().getName();
		String fileType = responseMessage.getFile().getType();

		System.out.println("send file to slack channel" + channel);

		FilesUploadRequestBuilder builder = FilesUploadRequest.builder();
		List<String> channels = new ArrayList<>();
		channels.add(channel);
		builder.channels(channels);
		builder.content(fileData);		
		if (fileName != null)
			builder.filename(fileName);
		if (fileType != null)
			builder.filetype(fileType);

		FilesUploadRequest request = builder.build();

		try {

			FilesUploadResponse response = slack.methods(authToken).filesUpload(request);
			if (response.isOk()) {
				System.out.println("Message sent: " + response.isOk());
			} else {
				System.out.println(response.getError());
			}
		} catch (SlackApiException requestFailure) {
			System.out.println("Slack API responded with unsuccessful status code");
		} catch (IOException connectivityIssue) {
			System.out.println("Failed to connect to Slack API");
		}
	}

	public String getTeamID() {
		return this.teamID;
	}

	public void setTeamID(String teamID) {
		this.teamID = teamID;
	}

	public String getAppID() {
		return appID;
	}

	public void setAppID(String appID) {
		this.appID = appID;
	}
}
