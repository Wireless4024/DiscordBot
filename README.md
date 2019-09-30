# DiscordBot  

### Credits  
  + [ExprK](https://github.com/Keelar/ExprK)
    > *Arithmetic expression parser*
  + [BigMath](https://github.com/eobermuhlner/big-math)
    > *Large precision math*
  + [JDA](https://github.com/DV8FromTheWorld/JDA)
    > *Discord api*
  + [JDA-NAS](https://github.com/sedmelluq/jda-nas)
    > *Native Audio System for JDA*
  + [LavaPlayer](https://github.com/sedmelluq/lavaplayer)
    > *Music player*
  + [Reflection](https://github.com/ronmamo/reflections)
    > *Classes scanner*
  + [Common-CLI](https://github.com/apache/commons-cli)
    > *Parameters parser*
  + [KMongo](https://github.com/Litote/kmongo)
    > *Mongodb driver
  + [Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
    > *Multithreading api for kotlin*
  + [Rhino-Sandbox](https://github.com/javadelight/delight-rhino-sandbox)
    > *Sandboxes Javascript engine*  
 
### Quick start
```
git clone https://github.com/Wireless4024/DiscordBot.git
cd DiscordBot
gradlew jar
```
+ now wait until finish.  
+ then your `DiscordBot-all-xxx.jar` will be in `jar` folder. then move it to somewhere you want  
+ first time when you run<sup><a href="#to-run-bot-on-your-computer">[1]</a></sup> it nothing happen but it will generate `discordbot-config.json` (if it doesn't generate please run as root or administrator)

|key|example|description|  
|---|-----|-----------|
|token   | "token" | your bot token ([discord developer](https://discordapp.com/developers/applications/))|
|~~yttoken~~ | "token" | ~~youtube token but~~ not used. so you can leave it alone|
|debug   | false | print all stacktrace |
|adminlist| [298273616704045057,...] | user id list who can access all command|
|executionTimeout|5| number of second to limit execution time of arithmetic expression (`=expression`) |
|messageDeleteDelay|30| number of second before delete command message |
|prefix|"--"| default prefix for your bot |

+ now run `DiscordBot-all-xxx.jar` again TADA!
---
##### To run bot on your computer
on Windows you can double click `DiscordBot-all-xxx.jar` file to run if java installed  
or if you running on linux
```
java -jar DiscordBot-all-xxx.jar
```
and you can *suppress all message* you can add argument `silent` like so
```
java -jar DiscordBot-all-xxx.jar silent
```
or if you want to run bot in *background* on **linux** use this
```
java -jar DiscordBot-all-xxx.jar silent & disown
```