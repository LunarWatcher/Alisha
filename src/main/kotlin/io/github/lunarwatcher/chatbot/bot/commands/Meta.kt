package io.github.lunarwatcher.chatbot.bot.commands

class BotConfig{
    val site: String;
    val admins: MutableList<Long>;
    val privelege: MutableList<Long>;
    val banned: MutableList<Long>;
    val homes: MutableList<Int>;

    constructor(site: String){
        this.site = site;

        admins = mutableListOf()
        privelege = mutableListOf()
        banned = mutableListOf()
        homes = mutableListOf()
    }

    fun addHomeRoom(newRoom: Int) : Boolean{
        for(home in homes){
            if(home == newRoom)
                return false;
        }

        homes.add(newRoom);
        return true;
    }

    fun removeHomeRoom(rr: Int) : Boolean{
        for(i in (homes.size - 1)downTo 0){
            if(homes[i] == rr){
                homes.removeAt(i)
                return true;
            }
        }

        return false;
    }

    fun addAdmin(newUser: Long){
        admins.filter { it == newUser }
                .forEach { return }

        admins.add(newUser)
    }

    fun removeAdmin(rr: Long){
        for(i in (admins.size - 1) downTo 0){
            if(admins[i] == rr){
                admins.removeAt(i)
            }
        }
    }

    fun addPriv(newUser: Long){
        privelege.filter { it == newUser }
                .forEach { return }

        privelege.add(newUser)
    }

    fun removePriv(rr: Long){
        for(i in (privelege.size - 1) downTo 0){
            if(privelege[i] == rr){
                privelege.removeAt(i)
            }
        }
    }

    fun ban(newUser: Long){
        banned.filter { it == newUser }
                .forEach { return }

        banned.add(newUser)
    }

    fun unban(rr: Long){
        for(i in (banned.size - 1) downTo 0){
            if(banned[i] == rr){
                banned.removeAt(i)
            }
        }
    }

    /**
     * Since the database can return null for objects, allow support for nullable variables. These are simply ignored
     * in the function
     */
    fun set(homes: List<Int>?, admins: List<Long>?, prived: List<Long>?, banned: List<Long>?){
        if(homes != null) {
            this.homes.addAll(homes)
        }
        if(admins != null) {
            for(x in admins){
                this.admins.add(x.toLong());
            }
        }
        if(prived != null){
            for(x in privelege){
                this.privelege.add(x.toLong());
            }
        }
        if(banned != null){
            for(x in banned){
                this.banned.add(x.toLong());
            }
        }
    }

}
