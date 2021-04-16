package demo;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class MessageClient {

	HttpClient httpClient = HttpClient.newHttpClient();
	URI server = URI.create("http://training.spirit-indianer.com/");
	Gson gson = new GsonBuilder().registerTypeAdapter(MessageInfo.class, new JsonDeserializer<MessageInfo>() {

		@Override
		public MessageInfo deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			MessageInfo messageInfo = new MessageInfo();
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			messageInfo.id = jsonObject.get("id").getAsInt();
			messageInfo.message = jsonObject.get("message").getAsString();
			messageInfo.read = jsonObject.get("read").getAsBoolean();
			messageInfo.timestamp = LocalDateTime.parse(jsonObject.get("timestamp").getAsString());
			messageInfo.from = jsonObject.get("from").isJsonPrimitive() ? jsonObject.get("from").getAsInt() : jsonObject.get("from").getAsJsonObject().get("id").getAsInt();
			messageInfo.to = jsonObject.get("to").isJsonPrimitive() ? jsonObject.get("to").getAsInt() : jsonObject.get("to").getAsJsonObject().get("id").getAsInt();
			return messageInfo;
		}
	}).create();
	String bearerToken;

	// GET api/users/{id}
	UserInfo readUserDetails(int id) throws Exception {
		HttpRequest httpRequest = HttpRequest.newBuilder(server.resolve("api/users/" + id)).GET()
				.header("Authorization", bearerToken).build();

		HttpResponse<String> httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
		return gson.fromJson(httpResponse.body(), UserInfo.class);
	}

	// GET api/users
	List<UserInfo> readAllUsers() throws Exception {
		HttpRequest httpRequest = HttpRequest.newBuilder(server.resolve("api/users")).GET().build();

		HttpResponse<String> httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
		String jsonBody = httpResponse.body();
		return gson.fromJson(jsonBody, new TypeToken<List<UserInfo>>() {
		}.getType());
	}

	// POST api/users
	public String registerUser(String name, String password) throws Exception {
		String jsonNewUser = "{\"name\":\"" + name + "\",\"password\":\"" + password + "\"}";
		HttpRequest httpRequest = HttpRequest.newBuilder(server.resolve("api/users"))
				.POST(BodyPublishers.ofString(jsonNewUser)).header("Content-Type", "application/json").build();

		HttpResponse<Void> httpResponse = httpClient.send(httpRequest, BodyHandlers.discarding());
		Optional<String> firstValue = httpResponse.headers().firstValue("location");
		return firstValue.orElseThrow();
	}

	// POST api/users/login
	String login(int id, String password) throws Exception {
		String login = "{\"id\":\"" + id + "\",\"password\":\"" + password + "\"}";
		HttpRequest httpRequest = HttpRequest.newBuilder(server.resolve("api/users/login"))
				.POST(BodyPublishers.ofString(login)).header("Content-Type", "application/json").build();
		HttpResponse<Void> httpResponse = httpClient.send(httpRequest, BodyHandlers.discarding());
		Optional<String> firstValue = httpResponse.headers().firstValue("Authorization");
		this.bearerToken = firstValue.orElseThrow();
		return firstValue.orElseThrow();
	}

	// GET api/messages
	List<MessageInfo> readAllMessages() throws Exception {
		return doGetRequest("api/messages", new TypeToken<List<MessageInfo>>() {}.getType());
		
//		HttpRequest httpRequest = HttpRequest.newBuilder(server.resolve("api/messages")).GET()
//				.header("Authorization", bearerToken).build();
//
//		HttpResponse<String> httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
//		String jsonBody = httpResponse.body();
//		return gson.fromJson(jsonBody, new TypeToken<List<MessageInfo>>() {
//		}.getType());
	}

	<T> T doGetRequest(String endpoint, Class<T> responseType) throws Exception {
		return doGetRequest(endpoint, new TypeToken<Class<T>>() {}.getType());
	}
	
	<T> T doGetRequest(String endpoint, Type responseType) throws Exception {
		Builder builder = HttpRequest.newBuilder(server.resolve(endpoint));
		if (bearerToken != null)  builder.header("Authorization", bearerToken);
		HttpRequest httpRequest =  builder.GET().build();
		
		HttpResponse<String> httpResponse = httpClient.send(httpRequest,  BodyHandlers.ofString());
		return gson.fromJson(httpResponse.body(), responseType);
	}
	
	// POST api/messages
	void sendMessage() {
	}

	class UserInfo {
		int id;
		String name;

		@Override
		public String toString() {
			return name + " [" + id + "]";
		}
	}

	class MessageInfo {
		int id;
		int from;
		int to;
		String message;
		LocalDateTime timestamp;
		boolean read;
		@Override
		public String toString() {
			return "MessageInfo [id=" + id + ", from=" + from + ", to=" + to + ", message=" + message + ", timestamp="
					+ timestamp + ", read=" + read + "]";
		}
		
	}
}
