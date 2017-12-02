package io.github.lunarwatcher.chatbot.bot.commands

import io.github.lunarwatcher.chatbot.Constants.LEARNED_COMMANDS
import io.github.lunarwatcher.chatbot.Database
import io.github.lunarwatcher.chatbot.bot.chat.BMessage
import org.checkerframework.checker.units.qual.A
import org.omg.CORBA.Object

//Note to self: Any = Object in Kotlin
@Suppress("UNCHECKED_CAST", "UNUSED")
abstract class MemoryBase(cmdName: String, cmdAliases: Array<String>?, cmdDesc: String?, cmdHelp: String?, val db: Database)
    : AbstractCommand(cmdName, cmdAliases, cmdDesc, cmdHelp){

    val commands: MutableList<LearnedCommand>

    init{
        commands = mutableListOf()
        load()
    }

    fun doesCommandExist(name: String) : Boolean{
        return commands.any { it.nm.toLowerCase() == name.toLowerCase() };
    }

    fun save(){

        /*
        The map has a key of the command name, and a map of the attributes contained in the LearnedCommand class
         */
        val map: MutableList<Map<String, Any?>> = mutableListOf()

        commands.forEach{
            lc ->
            val cmdMap = mutableMapOf<String, Any?>()

            cmdMap.put("name", lc.nm);
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

            val name: String? = map["name"] as String?;
            val desc: String? = map["desc"] as String?;
            val output: String? = map["output"] as String?;
            val creator: Long = map["creator"] as Long;
            val reply: Boolean = map["reply"] as Boolean;

            addCommand(LearnedCommand(name ?: return, desc, output ?: return, reply, creator))
        }
        for(item: Any in loaded){

        }
    }

    fun addCommand(command: LearnedCommand) = commands.add(command)
    fun removeCommand(command: LearnedCommand) = commands.remove(command)
    fun removeCommand(name: String) = commands.removeIf{it.nm == name}
    fun removeCommands(creator: Long) = commands.removeIf{it.creator == creator}
}

class LearnedCommand(cmdName: String, cmdDesc: String?, val output: String, val reply: Boolean, val creator: Long) : AbstractCommand(cmdName, null, cmdDesc, null){

    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input)){
            return null;
        }
        return BMessage(output, reply);
    }

    override fun getHelp(): String {
        return "This is a taught command, and doesn't have help";
    }
}