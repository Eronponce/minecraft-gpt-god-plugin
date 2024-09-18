package net.bigyous.gptgodmc.GPT;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.bigyous.gptgodmc.EventLogger;
import net.bigyous.gptgodmc.GPTGOD;
import net.bigyous.gptgodmc.GPT.Json.GptFunction;
import net.bigyous.gptgodmc.GPT.Json.GptFunctionReference;
import net.bigyous.gptgodmc.GPT.Json.GptTool;
import net.bigyous.gptgodmc.GPT.Json.Parameter;
import net.bigyous.gptgodmc.interfaces.Function;

import java.util.Map;

public class SummarizeLogs {
    private static String context = """
        Você é um assistente útil que receberá um registro de eventos de um servidor Minecraft, \
        ou um resumo histórico e um registro de eventos. \
        Você criará um pequeno resumo baseado nessas informações que preserva o enredo detalhado por ambos, você está visualizando esses logs sob a perspectiva de um deus que recompensa %s e pune %s \
        Acompanhe a reputação de cada jogador, se as informações nos logs não forem importantes para a trama, omita-as. Não adicione nenhum floreio extra, apenas declare os fatos, preste atenção às ações que se alinham com quaisquer objetivos listados nos objetivos e promessas que Deus faz aos jogadores.
        Esses logs são o histórico do servidor, portanto, mantenha tudo no passado.
        """;
    private static Gson gson = new Gson();
    private static Function<String> submitSummary = (String args) ->{
        JsonObject argObject = JsonParser.parseString(args).getAsJsonObject();
        GPTGOD.LOGGER.info("summary submitted with args: ", args);
        EventLogger.setSummary(gson.fromJson(argObject.get("summary"), String.class));
    };
    private static Map<String, GptFunction> functionMap = Map.of("submitSummary", 
        new GptFunction("submitSummary", "input the summary, keep the summary below 1000 tokens", 
            Map.of("summary", new Parameter("string","the summary")), 
            submitSummary));
    private static GptTool[] tools = GptActions.wrapFunctions(functionMap);
    private static GptAPI gpt = new GptAPI(GPTModels.getMainModel(), tools)
    .addContext(String.format(context, String.join(",",Personality.getLikes()), String.join(",",Personality.getDislikes())), "prompt").setToolChoice(new GptFunctionReference(functionMap.get("submitSummary")));

    public static void summarize(String log, String summary){
        String content = String.format("Write a short summary that summarizes the events of these logs: %s%s", log, 
            summary != null? String.format(":and this History Summary %s", summary): "");
        gpt.addLogs(content, "logs").send(functionMap);
    }
}
