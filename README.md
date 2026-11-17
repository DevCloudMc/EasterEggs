# EasterEggs3.0

This plugin allows you to create easter eggs (blocks, entities, ~~NPC~~). By clicking  
them u can run various actions and events.

## Installation

Optional: [VaultUnlocked](https://github.com/TheNewEconomy/VaultUnlocked) 2.20.3 (or Vault) is needed for the `money` action, PlaceholderAPI 2.12.3 is needed for the placeholders below.

Copy the **.jar** file to your plugins folder. Run the server. 
Now, go to the **config.conf**.  
You will see the following:

```kotlin
database {
  type: "sqlite" // sqlite or postgresql
  // The keys below are used only by postgresql
  host: "localhost"
  port: 5432
  database: "eastereggs"
  user: "eastereggs"
  password: "123123"
}

languageFile = "en"
```

`sqlite` is the default and needs no setup: found eggs are kept in `plugins/EasterEggs/eastereggs.db`. With `postgresql` the plugin connects to the server from the keys above. Paper downloads HikariCP and the PostgreSQL driver on the first start, whatever the type. Any other `type` disables the plugin with an error in the log. If the database cannot be opened at startup, the plugin disables itself instead of running without saving finds. `/ee reload` also re-reads the `database` block.

Breaking change in 4.0.0: MySQL support is removed and data from an existing MySQL database is not migrated, so players start with no found eggs. Set `type` to `sqlite` or `postgresql` before the first start.

To get help about the plugin type `/ee help`.

## Categories
To create a new group type `/ee category create <category_name>`.

To delete any category type `/ee category delete <category_name>`. If easter eggs from this category were already found by someone
they will be deleted from the database.

## Creating easter eggs
Once you've created the group u can start editing it and adding new easter eggs by 
typing `/ee edit <category_name>`.

To add a new easter egg just click RMB on any block/armorstand/painting/frame/~~npc~~.
A new easter eggs with unique ID will be created. 

To remove any easter egg just click LBM on it while editing.

## Adding actions to easter eggs
Easter eggs actions are adding in edit mode. To add an action just type 
`/ee action add <easter_egg_id> <action_name> <action_data>`

`<action_name>` value is the name of one of the following action types:

`message` - Chat/actiobar/title message.  
`commands` - Running commands by the player or server
`sound` - Playing sound for the player
`money` - Gives any amount of money  
`firework` - Launching a firework  

Those actions are in the json format. Data depends on the variables of 
specified action. E.g. `message` action can take the following variables:

#### message
`messages` - List of messages displaying in the chat. That's how it looks in  
json - {messages:["Message 1", "Message 2"]}  
`title` - Title message  
`subtitle`- Subtitle message  
`actionbar` - Actionbar message  
`fadeIn` - Title fade in period in ticks (1 second = 20 ticks)  | Default 10 ticks
`fadeOut` - Title fade out period in ticks | Default 10 ticks
`stay` - Delay period before fade out starts in ticks | Default 30 ticks

For example, if u want to display something in actionbar, you should type:  
`/ee action add <egg_id> message {actionbar:"Hello, world!"}`  
or  
`/ee action add <egg_id> message {messages=["&eFirst", "&cSecond"]}`

Here is the list of all actions' variables.

#### commands  
`console` - Commands list. Specifies whether run a command by console  
`player` - Commands list. Specifies whether run a command by player  
Example:  
`/ee action add <egg_id> commands {player=["/home", "/say Hello"] console=["broadcast Hi!"]}` 

#### sound
`type` - Sound name. Here you can find all of them: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html   
`location` - Sound format playback location: "world,x,y,z"  | Default player location  
`pitch` - The pitch of the volume | Default 1.0  
`volume` - How loud the sound | Default 1.0  
Example:  
`/ee action add <egg_id> sound {type="ENTITY_PLAYER_LEVELUP" location="world,0,60,-133"}` 

#### money
`money` - Amount of money given to the player 
Example:  
At the moment, the team is not working properly.  

#### firework
`power` - Distance. 0 by default (~4-5 blocks)  
`effects` - Effects list. If param isn't set the effect will be random. 

It's a way easier to configure firework effects in the config:

```kotlin
firework{
    power=0
    effects:[
        {
            type=BALL // Available types: BALL, BALL_LARGE, STAR, BURST, CREEPER
            trail=false
            colors:[
                "FFFFFF",
                "FF0000"
            ]
            fadeColors:[
                "000000",
                "00FF00"
            ]
        }
    ]
}
```

Colors must be set in RGB. The number of effects is unlimited.

Here is an example with each type of easter eggs and actions:

```kotlin
title="example"
hidePlayerData=false

"0"{
    location="world, 0.0, 0.0, 0.0"
    type="BLOCK"
    actions{
        firework{
            power=0
            effects:[
                {
                    type=BALL // Available types: BALL, BALL_LARGE, STAR, BURST, CREEPER
                    trail=false
                    colors:[
                        "FFFFFF",
                        "FF00FF"
                    ]
                    fadeColors:[
                        "000000",
                        "FF0000"
                    ]
                }
            ]
        }
        message{
            messages:[
                "Hello world!",
                "..."
            ]
            title="Title text"
            subtitle="subtitle text"
            actionbar="Actionbar text"
            fadeIn=10
            stay=40
            fadeOut=10
        }
        command{
            console:[
                "ban %player%",
                "time set 0"
            ]
        }
        sound={type: "ENTITY_PLAYER_LEVELUP"}
        money=1000
    }
}
```

## Finish

For each category you can add actions which performs when a player finds all easter eggs from that group.
You just need to add the `finishAction` to the group file and add actions that you need to perform. 

```kotlin
title="example"
hidePlayerData=false

...

onFinish{
    sound="UI_TOAST_CHALLENGE_COMPLETE"
    message{
        title="Congratulations!"
        subtitle="You found them all!"
        fadeIn=10
        fadeOut=10
        stay=50
    }
}
```

## Placeholders
PlaceholderAPI support has been added to work with third-party plugins Available placeholders:  
`%ee_found_<name>%` - Number of found easter eggs in the group `<name>`  
`%ee_count_<name>%` - Total number of easter eggs in the group `<name>`  

## Permissions
`ee.type.<type>` - acces to someone type (example: `ee.type.default`);

`ee.user` - acces to stats;

`ee.admin` - acces to admin commands (`action, category, clear, edit, list, reload, tp`).
