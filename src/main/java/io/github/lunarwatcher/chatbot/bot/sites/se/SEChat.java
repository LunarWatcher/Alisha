package io.github.lunarwatcher.chatbot.bot.sites.se;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lunarwatcher.chatbot.Database;
import io.github.lunarwatcher.chatbot.Site;
import io.github.lunarwatcher.chatbot.bot.chat.BMessage;
import io.github.lunarwatcher.chatbot.bot.chat.Message;
import io.github.lunarwatcher.chatbot.bot.chat.SEEvents;
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter;
import io.github.lunarwatcher.chatbot.bot.sites.Chat;
import io.github.lunarwatcher.chatbot.utils.Http;
import io.github.lunarwatcher.chatbot.utils.Utils;
import lombok.Getter;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Stack Exchange network is a massive blob of communities, and there are at least three known chat domains:
 * * chat.stackoverflow.com
 * * chat.meta.stackexchange.com
 * * chat.stackexchange.com
 *
 * The last one is mostly universal for all the sites, while the others are specific for each site. MSE and SO has
 * more or less the same core architecture, but the login on regular SE is different from SO and MSE. The general
 * system in chat is the same, but because there are differences, this is made abstract to allow for customization
 * for a specific site.
 */
public class SEChat implements Chat {
    @Getter
    Site site;
    @Getter
    String fKey;
    @Getter
    CloseableHttpClient httpClient;
    @Getter
    WebSocketContainer webSocket;
    @Getter
    Http http;

    public List<Message> newMessages = new ArrayList<>();
    public List<SERoom.StarMessage> starredMessages = new ArrayList<>();
    public List<SERoom.UserAction> actions = new ArrayList<>();
    public List<Message> pingMessages = new ArrayList<>();

    List<SERoom> rooms = new ArrayList<>();
    SEThread thread;
    CommandCenter commands;
    List<Integer> joining = new ArrayList<>();
    @Getter
    private Database db;
    @Getter
    private volatile boolean killed = false;

    public SEChat(Site site, CloseableHttpClient httpClient, WebSocketContainer webSocket, Properties botProps, Database database) throws IOException {
        this.site = site;
        this.db = database;
        this.httpClient = httpClient;
        this.webSocket = webSocket;

        for(Map.Entry<Object, Object> props : botProps.entrySet()){
            if(props.getKey().toString().equals("bot.site.homes." + site.getName())){
                String[] values = props.getValue().toString().split(",");
                for(String x : values){
                    System.out.println(x);
                    joining.add(Integer.parseInt(x));
                }
            }
        }

        //Ignore unchecked cast warning
        List<Integer> data = (List<Integer>) database.get(getName() + "-rooms");
        if(data != null){
            joining.addAll(data);
        }
        data = null;

        commands = new CommandCenter(botProps);
        http = new Http(httpClient);
        logIn();
        run();
    }

    public void logIn() throws IOException {
        if(site == null)
            return;
        String targetUrl = (site.getName().equals("stackexchange") ? SEEvents.getSELogin(site.getUrl()) : SEEvents.getLogin(site.getUrl()));
        System.out.println(targetUrl);

        if (site.getName().equals("stackexchange")) {
            Http.Response se = http.post(targetUrl, "from", "https://stackexchange.com/users/login#log-in");
            targetUrl = se.getBody();
        }

        String fKey = Utils.parseHtml(http.get(targetUrl).getBody());

        Http.Response response = null;

        if(fKey == null){
            System.out.println("No fKey found!");
            return;
        }

        if(site.getName().equals("stackexchange")){
            targetUrl = "https://openid.stackexchange.com/affiliate/form/login/submit";
            response = http.post(targetUrl, "email", site.getConfig().getEmail(), "password", site.getConfig().getPassword(), "fkey", fKey, "affId", "11");
            String TUREG = "(var target = .*?;)";
            Pattern pattern = Pattern.compile(TUREG);
            Matcher m = pattern.matcher(response.getBody());
            response = http.get(m.find() ? m.group(0).replace("var target = ", "").replace("'", "").replace(";", "") : null);
        }else{
           response = http.post(targetUrl, "email", site.getConfig().getEmail(), "password", site.getConfig().getPassword(), "fkey", fKey);
        }


        int statusCode = response.getStatusCode();

        if(statusCode != 200 && statusCode != 302){
            throw new IllegalAccessError();
        }

        try {
            for(int i = joining.size() - 1; i >= 0; i--){
                rooms.add(new SERoom(joining.get(i), this));
            }
        }catch(IllegalArgumentException e){
            System.out.println("Room not available!");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void run(){
        thread = new SEThread();
        thread.start();
    }

    public void kill(){
        killed = true;
    }

    public void unkill(){
        killed = false;
        run();
    }

    public String getUrl(){
        return site.getUrl();
    }

    public String getName(){
        return site.getName();
    }

    private class SEThread extends Thread {
        int retries;
        public void run() {
            try {
                while (!killed) {
                    for (Message m : newMessages) {
                        if (CommandCenter.isCommand(m.content)) {
                            List<BMessage> replies = commands.parseMessage(m.content);
                            if(replies != null){
                                for(BMessage bm : replies){
                                    if(bm.replyIfPossible){
                                        getRoom(m.roomID).reply(bm.content, m.messageID);
                                    }else{
                                        getRoom(m.roomID).sendMessage(bm.content);
                                    }
                                }
                            }else{
                                SERoom r = getRoom(m.roomID);
                                if(r != null){
                                    r.reply("Maybe you should consider looking up the manual", m.messageID);
                                }else{
                                    System.err.println("Room is null!");
                                }
                            }
                        }
                    }
                    newMessages.clear();

                    for(SERoom.StarMessage sm : starredMessages){

                    }
                    try {
                        //Update once every second to avoid CPU eating
                        Thread.sleep(1000);
                        retries--;
                    } catch (InterruptedException e) {
                    }
                }
            }catch(IOException e){
                if(retries < 10) {
                    run();
                    retries++;
                }else{
                    System.err.println("Fatal error: ");
                    e.printStackTrace();
                }
            }
        }
    }

    public SERoom getRoom(int id){
        for(SERoom r : rooms){
            if(r.getId() == id)
                return r;
        }
        return null;
    }

    public void save(){
        if(db != null){
            List<Integer> rooms = new ArrayList<>();
            for(SERoom room : this.rooms){
                rooms.add(room.getId());
            }

            db.put(getName() + "-rooms", rooms);
        }

    }

    public void load(){

    }
}
