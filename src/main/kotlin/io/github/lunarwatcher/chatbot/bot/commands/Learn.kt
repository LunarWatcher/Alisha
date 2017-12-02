package io.github.lunarwatcher.chatbot.bot.commands

import io.github.lunarwatcher.chatbot.Constants.LEARNED_COMMANDS
import io.github.lunarwatcher.chatbot.Database
import io.github.lunarwatcher.chatbot.bot.chat.BMessage
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter
import io.github.lunarwatcher.chatbot.utils.Utils
import jdk.nashorn.internal.runtime.Undefined
import org.checkerframework.checker.units.qual.A
import org.omg.CORBA.Object

//Note to self: Any = Object in Kotlin
@Suppress("UNCHECKED_CAST", "UNUSED")
class TaughtCommands(val db: Database){

    val commands: MutableList<LearnedCommand>

    init{
        commands = mutableListOf()
        load()
    }

    fun doesCommandExist(name: String) : Boolean{
        return commands.any { it.name.toLowerCase() == name.toLowerCase() };
    }

    fun save(){

        /*
        The map has a key of the command name, and a map of the attributes contained in the LearnedCommand class
         */
        val map: MutableList<Map<String, Any?>> = mutableListOf()

        commands.forEach{
            lc ->
            val cmdMap = mutableMapOf<String, Any?>()

            cmdMap.put("name", lc.name);
            cmdMap.put("desc", lc.desc);
            cmdMap.put("output", lc.output);
            cmdMap.put("creator", lc.creator);
            cmdMap.put("reply", lc.reply);

            map.add(cmdMap)

        }

        db.put("learned", map);
    }

    fun load(){
        val loaded: MutableList<Any>? = db.getList(LEARNED_COMMANDS);

        if(loaded == null){
            println("No learned commands found")
            return;
        }

        loaded.forEach{
            val map: MutableMap<String, Any?> = it as MutableMap<String, Any?>

            val name: String = map["name"] as String;
            val desc: String = map["desc"] as String;
            val output: String = map["output"] as String;

            //Keep these in case a user-implemented database doesn't work as it is supposed to.
            val creator: Long = try{
                map["creator"] as Long;
            }catch(e: ClassCastException){
                (map["creator"] as Int).toLong();
            }
            val reply: Boolean = try{
                map["reply"] as Boolean
            } catch(e: ClassCastException){
                (map["reply"] as String).toBoolean();
            }

            addCommand(LearnedCommand(name, desc, output, reply, creator))
        }
        for(item: Any in loaded){

        }
    }

    fun addCommand(command: LearnedCommand) = commands.add(command)
    fun removeCommand(command: LearnedCommand) = commands.remove(command)
    fun removeCommand(name: String) = commands.removeIf{it.name == name}
    fun removeCommands(creator: Long) = commands.removeIf{it.creator == creator}

    fun commandExists(cmdName: String) : Boolean{
        commands.forEach{
            if(it.name == cmdName)
                return true;
        }
        return false;
    }
}

class LearnedCommand(cmdName: String, cmdDesc: String = "No description supplied", val output: String, val reply: Boolean, val creator: Long)
    : AbstractCommand(cmdName, listOf(), cmdDesc, "This is a learned command and does not have help"){

    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input)){
            return null;
        }
        return BMessage(output, reply);
    }

}

class Learn(val commands: TaughtCommands, val center: CommandCenter) : AbstractCommand("learn", listOf(), "Teaches the bot a new command"){

    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input)) return null;

        val args: List<String> = parseArguments(input) ?: return BMessage("You have to pass at least the command name", true);

        println(args)
        if(args.size < 2)
            return BMessage("You have to supply at least two arguments!", true);

        var name = "undefined";
        var desc = "No description was supplied";
        var creator = user.userID;
        var output = "undefined";
        var reply = false;


        for(i in 0 until args.size){
            if(i == 0){
                name = args[i]
            }else if(i == 1){
                output = args[i]
            }else if(i == 2){
                try {
                    reply = args[i].toBoolean()
                }catch(e: ClassCastException){
                    return BMessage("The 3rd argument has to be a valid boolean!", true);
                }
            }else if(i == 3){
                desc = args[i]
            }
        }

        if(name == "undefined" || output == "undefined")
            return BMessage("Something went wrong. Command not added", true)

        if(commands.doesCommandExist(name) || center.isBuiltIn(name)){
            return BMessage("That command already exists", true);
        }

        commands.addCommand(LearnedCommand(name, desc, output, reply, creator))

        return BMessage(Utils.getRandomLearnedMessage(), true);
    }
}

class UnLearn(val commands: TaughtCommands, val center: CommandCenter) : AbstractCommand("unlearn", listOf("forget"), "Forgets a taught command"){

    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input)) return null;

        val name = input.replace(name + " ", "");

        if(center.isBuiltIn(name)){
            return BMessage("You can't make me forget something that's hard-coded :>", true);
        }

        if(!commands.doesCommandExist(name)){
            return BMessage("I can't forget what I never knew", true);
        }

        commands.removeCommand(name);

        return BMessage(Utils.getRandomForgottenMessage(), true);
    }
}