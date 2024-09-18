package net.bigyous.gptgodmc.GPT;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bukkit.entity.Player;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import com.google.gson.GsonBuilder;

import de.maxhenkel.voicechat.api.VoicechatApi;
import net.bigyous.gptgodmc.GPTGOD;
import net.bigyous.gptgodmc.GPT.Json.TTSRequest;
import net.bigyous.gptgodmc.utils.QueuedAudio;

public class TextToSpeech {
    // private static GsonBuilder gson = new GsonBuilder();
    // private static ExecutorService pool = Executors.newCachedThreadPool();
    // private static String SPEECH_ENDPOINT = "https://api.openai.com/v1/audio/speech";
    // private static VoicechatApi api = GPTGOD.VC_SERVER;
    // private static FileConfiguration config = JavaPlugin.getPlugin(GPTGOD.class).getConfig();

    // public static void makeSpeech(String input, Player player){
    //     Player[] players = player == null ? GPTGOD.SERVER.getOnlinePlayers().toArray(new Player[0]) : new Player[] {player};
    //     makeTTsRequest(new TTSRequest("tts-1", input, config.getString("voice"), "pcm"), players);
    // }
    // private static void makeTTsRequest(TTSRequest body, Player[] players){
    //     CloseableHttpClient client = HttpClientBuilder.create().build();
    //     pool.execute(() -> {
    //         StringEntity data = new StringEntity(gson.create().toJson(body), ContentType.APPLICATION_JSON);
    //         GPTGOD.LOGGER.info("POSTING " + gson.setPrettyPrinting().create().toJson(body));
    //         HttpPost post = new HttpPost(SPEECH_ENDPOINT);
    //         post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getString("openAiKey"));
    //         GPTGOD.LOGGER.info("Making POST request");
    //         post.setEntity(data);
    //         try {
    //             HttpResponse response = client.execute(post);
    //             byte[] rawSamples =  response.getEntity().getContent().readAllBytes();
    //             short[] samples = api.getAudioConverter().bytesToShorts(rawSamples);
    //             QueuedAudio.playAudio(samples, players);
    //         } catch (IOException e) {
    //             GPTGOD.LOGGER.error("There was an error making a request to GPT", e);
    //         }
    //     });
    // }



}
