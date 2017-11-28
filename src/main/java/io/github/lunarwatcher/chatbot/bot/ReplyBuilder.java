package io.github.lunarwatcher.chatbot.bot;

public class ReplyBuilder {
    StringBuilder builder = new StringBuilder();
    public ReplyBuilder() {
        builder = new StringBuilder();
    }

    public ReplyBuilder(String initial){
        builder = new StringBuilder(initial);
    }

    public ReplyBuilder fixedInput(){
        //For-loop as adding four spaces directly doesn't work for some reason
        for(int i = 0; i < 4; i++)
            builder.append(" ");
        return this;
    }

    public ReplyBuilder newLine(){
        builder.append("\n");
        return this;
    }
    public ReplyBuilder nl(){
        return newLine();
    }

    public ReplyBuilder append(String appendObject){
        builder.append(appendObject);
        return this;
    }

    public ReplyBuilder append(Object appendObject){
        try{
            builder.append(appendObject);
        }catch(Exception e){
            System.err.println("Could not append object");
        }

        return this;
    }

    public String build(){
        return builder.toString();
    }

    public String toString(){
        return builder.toString();
    }
}
