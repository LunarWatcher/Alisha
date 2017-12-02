package io.github.lunarwatcher.chatbot.bot.sites.se;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import io.github.lunarwatcher.chatbot.bot.chat.Message;
import io.github.lunarwatcher.chatbot.bot.chat.SEEvents;
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter;
import io.github.lunarwatcher.chatbot.bot.commands.BotConfig;
import io.github.lunarwatcher.chatbot.bot.exceptions.RoomNotFoundException;
import io.github.lunarwatcher.chatbot.utils.Http;
import io.github.lunarwatcher.chatbot.utils.Response;
import io.github.lunarwatcher.chatbot.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.naming.ldap.StartTlsRequest;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SERoom implements Closeable {
    private int id;
    private SEChat parent;
    @Getter
    Session session;
    @Getter
    private String fkey;

    public SERoom(int id, SEChat parent) throws Exception {
        this.id = id;
        this.parent = parent;


        Response connect = parent.getHttp().get(SEEvents.getRoom(parent.getSite().getUrl(), id));
        if(connect.getStatusCode() == 404){
            throw new RoomNotFoundException("SERoom not found!");
        }

        if(!connect.getBody().contains("<textarea id=\"input\">")){
            throw new RoomNotFoundException("No write access in the room!");
        }
        fkey = Utils.parseHtml(connect.getBody());

        ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                .configurator(new ClientEndpointConfig.Configurator() {
                    @Override
                    public void beforeRequest(Map<String, List<String>> headers) {
                        headers.put("Origin", Arrays.asList(parent.getSite().getUrl()));
                    }
                }).build();
        session = parent.getWebSocket().connectToServer(new Endpoint() {
            @Override
            public void onOpen(Session session, EndpointConfig config) {
                session.addMessageHandler(String.class, SERoom.this::receiveMessage);
            }

            @Override
            public void onError(Session session, Throwable error){
                try{
                    throw new Exception(error);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }, config, new URI(getWSURL()));
    }

    public String getWSURL() throws IOException{
        Response response = parent.http.post(parent.getSite().getUrl() + "/ws-auth",
                "roomid", id,
                "fkey", fkey
        );

        String url = null;
        try {
            url = response.getBodyAsJson().get("url").asText();
        }catch(Exception e){
            e.printStackTrace();
        }

        return url + "?l=" + System.currentTimeMillis();
    }

    //TODO add external handlers
    public void receiveMessage(String input){
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(input).get("r" + id);

            if(actualObj == null)
                return;

            actualObj = actualObj.get("e");
            if(actualObj == null)
                return;

            boolean eight = false;
            for(JsonNode event : actualObj){
                JsonNode et = event.get("event_type");
                if(et == null)
                    continue;

                int eventCode = et.asInt();


                if(eventCode == 1 || eventCode == 2){
                    String content = event.get("content").toString();
                    content = removeQuotation(content);
                    content = correctBackslash(content);
                    if(eight){
                        if(isPinged(content, parent.site.getConfig().getUsername())){
                            //The bot has already been pinged (flagged by event ID 8)
                            //So ignore this message and move on, it's already been handled
                            //Disable the flag again to avoid a bug if multiple events come in at
                            //once AND there are two eights
                            eight = false;
                            continue;
                        }
                    }
                    //New message or edited message

                    long messageID = event.get("message_id").asLong();
                    int userid = event.get("user_id").asInt();
                    String username = event.get("user_name").toString();


                    username = removeQuotation(username);
                    Message message = new Message(content, messageID, id, username, userid);

                    parent.newMessages.add(message);
                    return;
                }else if(eventCode == 3 || eventCode == 4){
                    int userid = event.get("user_id").asInt();
                    String username = event.get("user_name").toString();
                    username = removeQuotation(username);

                    UserAction action = new UserAction(eventCode, userid, username, id);
                    parent.actions.add(action);
                    return;
                }else if(eventCode == 6){
                    long messageID = event.get("message_id").asLong();
                    int stars = event.get("message_stars").asInt();

                    StarMessage message = new StarMessage(messageID, id, stars);
                    parent.starredMessages.add(message);
                    return;
                }else if(eventCode == 8){
                    eight = true;

                    String content = event.get("content").toString();
                    content = removeQuotation(content);
                    content = correctBackslash(content);
                    long messageID = event.get("message_id").asLong();
                    int userid = event.get("user_id").asInt();
                    String username = event.get("user_name").toString();


                    username = removeQuotation(username);
                    Message message = new Message(content, messageID, id, username, userid);
                    parent.pingMessages.add(message);
                    return;
                }else if(eventCode == 10){
                    //The message was deleted. Ignore it
                    return;
                }else if(eventCode == 15){
                    close();
                    parent.getRooms().remove(this);
                }else{
                    //These are printed using the error stream to make sure they are easy to spot. These are critical
                    //to find more events in the SE network
                    System.err.println("Unknown event:");
                    System.err.println(event.toString());
                }
                // Event reference sheet:,

                //1: message
                //2: edited
                //3: join
                //4: leave
                //5:
                //6: star
                //7:
                //8: ping - if called, ensure that the content does not contain a ping to the bot name if 1 is called
                //9:
                //10: deleted
                //15: kicked
                //17: Invite

                //19: Moved
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public String removeQuotation(String input){
        return input.substring(1, input.length() - 1);
    }

    public void sendMessage(String message) throws IOException{
        Response response = parent.getHttp().post(parent.getUrl() + "/chats/" + id + "/messages/new",
                "text", message,
                "fkey", fkey
        );
        //@formatter:on

        if (response.getStatusCode() == 404) {
				/*
				 * We already checked to make sure the room exists. So, if a 404
				 * response is returned when trying to send a message, it likely
				 * means that the bot's permission to post messages has been
				 * revoked.
				 *
				 * If a 404 response is returned from this request, the response
				 * body reads:
				 * "The room does not exist, or you do not have permission"
				 */
            System.err.println("Room not found, or you can't access it: " + id);
        }
    }

    public void reply(String message, long targetMessage) throws IOException{
        sendMessage(":" + targetMessage + " " + message);
    }

    @Override
    public void close() throws IOException {
        parent.getHttp().post(SEEvents.leaveRoom(parent.getSite().getUrl(), id),
                "fkey", fkey);
        session.close();
    }

    @AllArgsConstructor
    public class UserAction{
        public int eventID, userID;
        public String username;
        public int room;
    }

    @AllArgsConstructor
    public class StarMessage{
        public long messageID;
        public int room;
        public int stars;

    }

    public boolean isPinged(String message, String username){
        for(int i = 3; i < username.length(); i++){
            if(message.contains("@" + username.substring(0, i))){
                return true;
            }
        }
        return false;
    }

    public int getId(){
        return id;
    }

    public String correctBackslash(String input){
        return input.replace("\\\\", "\\");
    }

}
