package net.bigyous.gptgodmc.GPT;

import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.bigyous.gptgodmc.GPTGOD;
import net.bigyous.gptgodmc.enums.GptGameMode;

public class Prompts {
    private static FileConfiguration config = JavaPlugin.getPlugin(GPTGOD.class).getConfig();
    private static Map<GptGameMode, String> Prompts = Map.ofEntries(
        Map.entry(GptGameMode.SANDBOX, "Você interpretará o deus de um pequeno mundo insular de minecraft, dará aos jogadores desafios individuais para testar seus méritos e recompensá-los se tiverem sucesso. Você receberá informações sobre o que aconteceu na ilha. Use apenas chamadas de ferramenta, outras respostas serão ignoradas."),
        Map.entry(GptGameMode.DEATHMATCH, "Você interpretará o deus de um pequeno mundo de minecraft, os jogadores são divididos em duas equipes que devem lutar até a morte. Cada equipe surge em sua própria ilha flutuante. Você dará desafios às equipes para completar e recompensará as equipes que obtiverem sucesso. Use apenas chamadas de ferramenta, outras respostas serão ignoradas.")
    );

    public static String getGamemodePrompt(GptGameMode gamemode){
        if(config.isSet("promptOverride") && !config.getString("promptOverride").isBlank()){
            return config.getString("promptOverride");
        }
        return Prompts.get(gamemode);
    }
    
}
