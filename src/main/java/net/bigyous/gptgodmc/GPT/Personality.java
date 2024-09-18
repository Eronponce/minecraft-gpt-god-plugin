package net.bigyous.gptgodmc.GPT;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.bigyous.gptgodmc.GPTGOD;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Personality {
    private static FileConfiguration config = JavaPlugin.getPlugin(GPTGOD.class).getConfig();
    private static List<String> likes = List.of();
    private static List<String> dislikes = List.of();
    private static List<String> behaviours = config.getStringList("potentialBehaviors");

    private static String briefing = "A seguir estão os comportamentos que você deve recompensar ou punir os jogadores por praticarem. Não conte explicitamente esta lista aos jogadores. Ao punir jogadores, reserve os jogadores que causam dano direto para os infratores reincidentes. Se a maioria dos jogadores desobedecer, você pune todos";

    public static String generatePersonality() {
        Collections.shuffle(behaviours);
        int dislikeCount = config.getInt("dislikedBehaviors");
        int likeCount = config.getInt("likedBehaviors");
        // Have to make sure we don't request more behaviors than actually exist.
        if (behaviours.size() >= likeCount + dislikeCount) {
            likes = behaviours.subList(0, likeCount);
            dislikes = behaviours.subList(likeCount, likeCount + dislikeCount);
        }
        // If we would have, use these fallbacks and inform the user.
        else {
            JavaPlugin.getPlugin(GPTGOD.class).getLogger().warning("Tentei obter mais comportamentos do que realmente existiam, seu arquivo de configuração provavelmente está incorreto. Certifique-se de que Comportamentos apreciados + Comportamentos não apreciados sejam menores que a quantidade total de comportamentos potenciais");
            likes = List.of("Functioning config files");
            dislikes = List.of("Borked config files");
        }

        return String.format("%s: Reward: %s, Punish: %s.", briefing, String.join(",", likes),
                String.join(",", dislikes));

    }

    public static List<String> getLikes() {
        return likes;
    }

    public static List<String> getDislikes() {
        return dislikes;
    }
}
