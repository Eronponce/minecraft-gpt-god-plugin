package net.bigyous.gptgodmc.GPT;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;

import net.bigyous.gptgodmc.EventLogger;
import net.bigyous.gptgodmc.GPTGOD;
import net.bigyous.gptgodmc.StructureManager;
import net.bigyous.gptgodmc.WorldManager;
import net.bigyous.gptgodmc.GPT.Json.Choice;
import net.bigyous.gptgodmc.GPT.Json.GptFunction;
import net.bigyous.gptgodmc.GPT.Json.GptResponse;
import net.bigyous.gptgodmc.GPT.Json.GptTool;
import net.bigyous.gptgodmc.GPT.Json.Parameter;
import net.bigyous.gptgodmc.GPT.Json.ToolCall;
import net.bigyous.gptgodmc.interfaces.Function;
import net.bigyous.gptgodmc.loggables.GPTActionLoggable;
import net.bigyous.gptgodmc.utils.GPTUtils;
import net.bigyous.gptgodmc.utils.GptObjectiveTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class GptActions {
    private int tokens = -1;
    private static Gson gson = new Gson();
    private static JavaPlugin plugin = JavaPlugin.getPlugin(GPTGOD.class);
    private static Boolean useTts = plugin.getConfig().getBoolean("tts");
    private static void staticWhisper(String playerName, String message) {
        Player player = GPTGOD.SERVER.getPlayerExact(playerName);
        player.sendRichMessage("<i>You hear something whisper to you...</i>");
        player.sendMessage(message);
        // if(useTts){
        //     TextToSpeech.makeSpeech(message, player);
        // }
        EventLogger.addLoggable(new GPTActionLoggable(
                String.format("whispered \"%s\" to %s", message, playerName)));
    }

    private static void staticAnnounce(String message) {
        GPTGOD.SERVER.broadcast(Component.text("A Loud voice bellows from the heavens", NamedTextColor.YELLOW)
                .decoration(TextDecoration.BOLD, true));
        GPTGOD.SERVER.broadcast(Component.text(message, NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.BOLD, true));
        // if(useTts){
        //     TextToSpeech.makeSpeech(message, null);
        // }
        EventLogger.addLoggable(new GPTActionLoggable(String.format("announced \"%s\"", message)));
    }
    // in hindsight, I should have used an interface or abstract class to do this but oh well...

    private static Function<String> whisper = (String args) -> {
        TypeToken<Map<String, String>> mapType = new TypeToken<Map<String, String>>() {
        };
        Map<String, String> argsMap = gson.fromJson(args, mapType);
        String message = argsMap.get("message");
        staticWhisper(argsMap.get("playerName"), message);
        return;
    };
    private static Function<String> announce = (String args) -> {
        TypeToken<Map<String, String>> mapType = new TypeToken<Map<String, String>>() {
        };
        Map<String, String> argsMap = gson.fromJson(args, mapType);
        String message = argsMap.get("message");
        staticAnnounce(message);
    };
    private static Function<String> giveItem = (String args) -> {
        JsonObject argObject = JsonParser.parseString(args).getAsJsonObject();
        String playerName = gson.fromJson(argObject.get("playerName"), String.class);
        String itemId = gson.fromJson(argObject.get("itemId"), String.class);
        int count = gson.fromJson(argObject.get("count"), Integer.class);
        // executeCommand(String.format("/give %s %s %d", playerName, itemId, count));
        if (Material.matchMaterial(itemId) == null)
            return;
        Player player = GPTGOD.SERVER.getPlayer(playerName);
        player.getInventory().addItem(new ItemStack(Material.matchMaterial(itemId), count));
        player.sendRichMessage(String.format("<i>A %s appeared in your inventory</i>", itemId));
        EventLogger.addLoggable(new GPTActionLoggable(String.format("gave %d %s to %s", count, itemId, playerName)));
    };
    private static Function<String> command = (String args) -> {
        TypeToken<Map<String, String>> mapType = new TypeToken<Map<String, String>>() {
        };
        Map<String, String> argsMap = gson.fromJson(args, mapType);
        GenerateCommands.generate(argsMap.get("prompt"));
        EventLogger
                .addLoggable(new GPTActionLoggable(String.format("commanded \"%s\" to happen", argsMap.get("prompt"))));
    };
    private static Function<String> smite = (String args) -> {
        JsonObject argObject = JsonParser.parseString(args).getAsJsonObject();
        String playerName = gson.fromJson(argObject.get("playerName"), String.class);
        int power = gson.fromJson(argObject.get("power"), Integer.class);
        Player player = GPTGOD.SERVER.getPlayer(playerName);
        for (int i = 0; i < power; i++) {
            WorldManager.getCurrentWorld().strikeLightning(player.getLocation());
        }
        EventLogger.addLoggable(new GPTActionLoggable(String.format("smited %s", playerName)));
    };
    private static Function<String> spawnEntity = (String args) -> {
        JsonObject argObject = JsonParser.parseString(args).getAsJsonObject();
        String position = gson.fromJson(argObject.get("position"), String.class);
        String entityName = gson.fromJson(argObject.get("entity"), String.class);
        int count = gson.fromJson(argObject.get("count"), Integer.class);
        String customName = gson.fromJson(argObject.get("customName"), String.class);
        Location location = StructureManager.hasStructure(position)
                ? StructureManager.getStructure(position).getLocation()
                : GPTGOD.SERVER.getPlayer(position).getLocation();
        EntityType type = EntityType.fromName(entityName);
        for (int i = 0; i < count; i++) {
            double r = Math.random() / Math.nextDown(1.0);
            double offset = 0 * (1.0 - 1) + 3 * r;
            Entity ent = WorldManager.getCurrentWorld().spawnEntity(
                    location.offset(offset - i, 0, offset + i).toLocation(WorldManager.getCurrentWorld()),
                    type, true);
            TextComponent nameComponent = customName != null
                    ? PlainTextComponentSerializer.plainText()
                            .deserialize(String.format("%s%s", customName, i > 0 ? " " + String.valueOf(i) : ""))
                    : null;
            ent.customName(nameComponent);
        }
        EventLogger.addLoggable(
                new GPTActionLoggable(String.format("summoned %d %s%s near %s", count, entityName,
                        customName != null ? String.format(" named: %s,", customName) : "", position)));
    };
    private static Function<String> summonSupplyChest = (String args) -> {
        TypeToken<List<String>> stringArrayType = new TypeToken<List<String>>() {
        };
        JsonObject argObject = JsonParser.parseString(args).getAsJsonObject();
        String playerName = gson.fromJson(argObject.get("playerName"), String.class);
        List<String> itemNames = gson.fromJson(argObject.get("items"), stringArrayType);
        boolean fullStacks = gson.fromJson(argObject.get("fullStacks"), Boolean.class) != null
                ? gson.fromJson(argObject.get("fullStacks"), Boolean.class)
                : false;
        List<ItemStack> items = itemNames.stream().map((String itemName) -> {
            Material mat = Material.matchMaterial(itemName);
            if (mat == null) {
                return new ItemStack(Material.COBWEB);
            }
            return new ItemStack(mat, fullStacks ? mat.getMaxStackSize() : 1);
        }).toList();
        Location playerLoc = GPTGOD.SERVER.getPlayer(playerName).getLocation();
        Block currentBlock = WorldManager.getCurrentWorld()
                .getBlockAt(playerLoc
                        .offset(playerLoc.getDirection().getBlockX() + 1, 0,
                                playerLoc.getDirection().getBlockZ() + 1)
                        .toLocation(null));
        currentBlock.setType(Material.CHEST);
        Chest chest = (Chest) currentBlock.getState();
        chest.getBlockInventory().addItem(items.toArray(new ItemStack[itemNames.size()]));
        chest.open();
        WorldManager.getCurrentWorld().spawnParticle(Particle.WAX_OFF, chest.getLocation().toCenterLocation(), 100,
                2,
                3, 2);
        EventLogger.addLoggable(new GPTActionLoggable(String.format("summoned a chest with: %s inside next to %s",
                String.join(", ", itemNames), playerName)));

    };
    private static Function<String> transformStructure = (String args) -> {
        JsonObject argObject = JsonParser.parseString(args).getAsJsonObject();
        String structure = gson.fromJson(argObject.get("structure"), String.class);
        String blockType = gson.fromJson(argObject.get("block"), String.class);
        StructureManager.getStructure(structure).getBlocks()
                .forEach((Block b) -> b.setType(Material.matchMaterial(blockType)));
        EventLogger.addLoggable(
                new GPTActionLoggable(
                        String.format("turned all the blocks in Structure %s to %s", structure, blockType)));

    };
    private static Function<String> revive = (String args) -> {
        TypeToken<Map<String, String>> mapType = new TypeToken<Map<String, String>>() {
        };
        Map<String, String> argsMap = gson.fromJson(args, mapType);
        String playerName = argsMap.get("playerName");
        Player player = GPTGOD.SERVER.getPlayer(playerName);
        if (player.getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }
        Location spawn = player.getRespawnLocation() != null ? player.getRespawnLocation()
                : WorldManager.getCurrentWorld().getSpawnLocation();
        player.teleport(spawn);
        player.setGameMode(GameMode.SURVIVAL);
        EventLogger.addLoggable(new GPTActionLoggable(String.format("revived %s", playerName)));
    };
    private static Function<String> teleport = (String args) -> {
        TypeToken<Map<String, String>> mapType = new TypeToken<Map<String, String>>() {
        };
        Map<String, String> argsMap = gson.fromJson(args, mapType);
        String playerName = argsMap.get("playerName");
        String destName = argsMap.get("destination");
        Player player = GPTGOD.SERVER.getPlayer(playerName);
        Location destination = StructureManager.hasStructure(destName)
                ? StructureManager.getStructure(destName).getLocation()
                : GPTGOD.SERVER.getPlayer(destName).getLocation();
        player.teleport(destination);
        EventLogger.addLoggable(new GPTActionLoggable(String.format("teleported %s to %s", playerName, destName)));
    };
    private static Function<String> setObjective = (String args) -> {
        TypeToken<Map<String, String>> mapType = new TypeToken<Map<String, String>>() {
        };
        Map<String, String> argsMap = gson.fromJson(args, mapType);
        String objective = argsMap.get("objective");

        Score score = GPTGOD.GPT_OBJECTIVES.getScore(objective.length() > 45 ? objective.substring(0, 44) : objective );
        score.setScore(plugin.getConfig().getInt("objectiveDecay"));
        // decrement the score by one every minute until the score reaches zero
        GptObjectiveTracker tracker = new GptObjectiveTracker(score);
        tracker.setTaskId(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, tracker, 0, 1200));
        if (GPTGOD.GPT_OBJECTIVES.getDisplaySlot() == null) GPTGOD.GPT_OBJECTIVES.setDisplaySlot(DisplaySlot.SIDEBAR);
        EventLogger.addLoggable(new GPTActionLoggable(String.format("set objective %s", objective)));
        
    };
    private static Function<String> clearObjective = (String args) -> {
        TypeToken<Map<String, String>> mapType = new TypeToken<Map<String, String>>() {
        };
        Map<String, String> argsMap = gson.fromJson(args, mapType);
        String objective = argsMap.get("objective");
        GPTGOD.GPT_OBJECTIVES.getScore(objective).resetScore();
        if(GPTGOD.SCOREBOARD.getEntries().stream().filter(entry -> GPTGOD.SERVER.getPlayer(entry) == null).count() < 1){
            GPTGOD.GPT_OBJECTIVES.setDisplaySlot(null);
        }
        EventLogger.addLoggable(new GPTActionLoggable(String.format("declared objective %s as completed", objective)));
        
    };
    private static Function<String> detonateStructure = (String args) -> {
        JsonObject argObject = JsonParser.parseString(args).getAsJsonObject();
        String structure = gson.fromJson(argObject.get("structure"), String.class);
        boolean setFire = gson.fromJson(argObject.get("setFire"), Boolean.class);
        int power = gson.fromJson(argObject.get("power"), Integer.class);
        StructureManager.getStructure(structure).getLocation().createExplosion(power, setFire, true);
        EventLogger.addLoggable(new GPTActionLoggable(String.format("detonated Structure: %s", structure)));
    };
    private static Map<String, GptFunction> functionMap = Map.ofEntries(
            Map.entry("whisper", new GptFunction("whisper",
                    "privately send a message to a player. Avoid repeating things that have already been said. Keep messages short, concise, and no more than 100 characters.",
                    Map.of("playerName", new Parameter("string", "name of the player to privately send to"),
                            "message", new Parameter("string", "the message")),
                    whisper)),
            Map.entry("announce", new GptFunction("announce",
                    "brodcast a message to all players. Avoid repeating things that have already been said. Keep messages short, concise, and no more than 100 characters.",
                    Map.of("message", new Parameter("string", "the message")),
                    announce)),
            Map.entry("giveItem", new GptFunction("giveItem", "give a player any amount of an item",
                    Map.of("playerName", new Parameter("string", "name of the Player"),
                            "itemId", new Parameter("string", "the name of the minecraft item"),
                            "count", new Parameter("number", "amount of the item")),
                    giveItem)),
            Map.entry("command", new GptFunction("command",
                    "Describe a series of events you would like to take place, taking into consideration the limitations of minecraft",
                    Collections.singletonMap("prompt", new Parameter("string", "a description of what will happen")),
                    command)),
            Map.entry("smite", new GptFunction("smite", "Strike a player down with lightning, reserve this punishment for repeat offenders",
                    Map.of("playerName", new Parameter("string", "the player's name"),
                            "power", new Parameter("number", "the strength of this smiting")),
                    smite)),
            Map.entry("transformStructure",
                    new GptFunction("transformStructure", "replace all the blocks in a structure with any block",
                            Map.of("structure", new Parameter("string", "name of the structure"),
                                    "block", new Parameter("string", "The name of the minecraft block")),
                            transformStructure)),
            Map.entry("spawnEntity", new GptFunction("spawnEntity",
                    "spawn any minecraft entity next to a player or structure",
                    Map.of("position", new Parameter("string", "name of the Player or Structure"),
                            "entity",
                            new Parameter("string",
                                    "the name of the minecraft entity name will be underscore deliminated eg. \"mushroom_cow\""),
                            "count", new Parameter("number", "the amount of the entity that will be spawned"),
                            "customName",
                            new Parameter("string",
                                    "(optional) custom name that will be given to the spawned entities, set to null to leave entities unnamed")),
                    spawnEntity)),
            Map.entry("summonSupplyChest", new GptFunction("summonSupplyChest",
                    "spawn chest full of items for use in a project next to a player",
                    Map.of("items",
                            new Parameter("array",
                                    "names of the minecraft items you would like to put in the chest, each item takes up one of 8 slots",
                                    "string"),
                            "fullStacks", new Parameter("boolean", "put the maximum stack size of each item?"),
                            "playerName",
                            new Parameter("string", "The name of the player that will recieve this chest")),
                    summonSupplyChest)),
            Map.entry("revive",
                    new GptFunction("revive", "bring a player back from the dead",
                            Map.of("playerName", new Parameter("string", "The name of the player")), revive)),
            Map.entry("teleport", new GptFunction("teleport", "teleport a player to another player or a structure",
                    Map.of("playerName", new Parameter("string", "name of the player to be teleported"),
                            "destination",
                            new Parameter("string",
                                    "The name of the player or Structure the player will be sent to")),
                    teleport)),
            Map.entry("setObjective", new GptFunction("setObjective", "set an objective for players to complete. base this off of the behaviors observed in the logs. objectives can't be longer than 45 characters", 
            Map.of("objective", new Parameter("string", "the objective to set, if it's for a specific player, be sure to include their name")), setObjective)),
            Map.entry("clearObjective", new GptFunction("clearObjective", "set an objective as complete. Follow this up with a reward", 
            Map.of("objective", new Parameter("string", "the objective to mark as complete")), clearObjective)),
            Map.entry("detonateStructure", new GptFunction("detonateStructure", "cause an explosion at a Structure",
                    Map.of("structure", new Parameter("string", "name of the structure"),
                            "setFire", new Parameter("boolean", "will this explosion cause fires?"),
                            "power",
                            new Parameter("number", "the strength of this explosion where 4 is the strength of TNT")),
                    detonateStructure)));
    private static Map<String, GptFunction> speechFunctionMap = new HashMap<>(functionMap);
    private static Map<String, GptFunction> actionFunctionMap = new HashMap<>(functionMap);

    private static GptTool[] tools;
    private static GptTool[] actionTools;
    private static GptTool[] speechTools;
    private static final List<String> speechActionKeys = Arrays.asList("announce", "whisper", "setObjective", "clearObjective");
    private static final List<String> persistentActionKeys = Arrays.asList("command");

    public static GptTool[] wrapFunctions(Map<String, GptFunction> functions) {
        GptFunction[] funcList = functions.values().toArray(new GptFunction[functions.size()]);
        GptTool[] toolList = new GptTool[functions.size()];
        for (int i = 0; i < funcList.length; i++) {
            toolList[i] = new GptTool(funcList[i]);
        }
        return toolList;
    }

    public static GptTool[] GetAllTools() {
        if (tools[0] != null) {
            return tools;
        }
        tools = wrapFunctions(functionMap);
        return tools;
    }

    public static GptTool[] GetActionTools() {
        if (actionTools == null || actionTools[0] == null) {
            actionFunctionMap.keySet().removeAll(speechActionKeys);
            actionFunctionMap.keySet().removeAll(persistentActionKeys);
            actionTools = wrapFunctions(actionFunctionMap);
        }
        GptTool[] newTools = GPTUtils.randomToolSubset(actionTools, 3);
        GptTool[] persistentTools = persistentActionKeys.stream().map(key -> {return new GptTool(functionMap.get(key));}).toArray(GptTool[]::new);
        // I could do this nicer, but I don't feel like it
        newTools = GPTUtils.concatWithArrayCopy(newTools, persistentTools);
        return newTools;
    }

    public static GptTool[] GetSpeechTools() {
        if (speechTools != null && speechTools[0] != null) {
            return speechTools;
        }
        speechFunctionMap.keySet().retainAll(speechActionKeys);
        speechTools = wrapFunctions(speechFunctionMap);
        return speechTools;
    }

    private static void dispatch(String command, CommandSender console) {
        // can't let GPT turn off mob spawning
        if(command.contains("doMobSpawning")){
            return;
        }
        command = command.charAt(0) == '/' ? command.substring(1) : command;
        if (command.matches(".*\\bgive\\b.*") || command.contains(" in ")) {
            GPTGOD.SERVER.dispatchCommand(console, command);
        } else {
            if (!(command.contains(" as ") || command.contains(" at "))
                    && (command.contains("~") || command.contains("^"))) {
                command = "execute at @r run " + command;
            }
            GPTGOD.SERVER.dispatchCommand(console,
                    String.format("execute in %s run %s", WorldManager.getDimensionName(), command));
        }
    }

    public static void executeCommands(String[] commands) {
        CommandSender console = GPTGOD.SERVER.getConsoleSender();
        for (String command : commands) {
            dispatch(command, console);
        }
    }

    public static void executeCommand(String command) {
        CommandSender console = GPTGOD.SERVER.getConsoleSender();
        dispatch(command, console);
    }

    public static int run(String functionName, String jsonArgs) {
        GPTGOD.LOGGER.info(String.format("running function \"%s\" with json arguments \"%s\"", functionName, jsonArgs));
        Bukkit.getScheduler().runTask(plugin, () -> {
            functionMap.get(functionName).runFunction(jsonArgs);
        });
        return 1;
    }

    public static void processResponse(String response) {
        GptResponse responseObject = gson.fromJson(response, GptResponse.class);
        for (Choice choice : responseObject.getChoices()) {
            if(choice.getMessage().getTool_calls() == null){
                continue;
            }
            for (ToolCall call : choice.getMessage().getTool_calls()) {
                run(call.getFunction().getName(), call.getFunction().getArguments());
            }
        }
    }

    public static void processResponse(String response, Map<String, GptFunction> functions) {
        GptResponse responseObject = gson.fromJson(response, GptResponse.class);
        for (Choice choice : responseObject.getChoices()) {
            if(choice.getMessage().getTool_calls() == null){
                continue;
            }
            for (ToolCall call : choice.getMessage().getTool_calls()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    functions.get(call.getFunction().getName()).runFunction(call.getFunction().getArguments());
                });
            }
        }
    }

    private int calculateFunctionTokens() {
        int sum = 0;
        for (GptFunction function : functionMap.values()) {
            sum += function.calculateFunctionTokens();
        }
        return sum;
    }

    public int getTokens() {
        if (tokens >= 0) {
            return tokens;
        }
        return calculateFunctionTokens();
    }

}
