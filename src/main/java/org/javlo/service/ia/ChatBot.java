package org.javlo.service.ia;



public class ChatBot {

    private static String OPENIA_API_KEY = "###";

    public static void main(String[] args) {

        /*ChatLanguageModel model = OpenAiChatModel.withApiKey(OPENIA_API_KEY);

        ChatMemory chatMemory = TokenWindowChatMemory.withMaxTokens(300, new OpenAiTokenizer(GPT_3_5_TURBO));

        // You have full control over the chat memory.
        // You can decide if you want to add a particular message to the memory
        // (e.g. you might not want to store few-shot examples to save on tokens).
        // You can process/modify the message before saving if required.

        chatMemory.add(userMessage("Hello, my name is Klaus"));
        AiMessage answer = model.generate(chatMemory.messages()).content();
        System.out.println(answer.text()); // Hello Klaus! How can I assist you today?
        chatMemory.add(answer);

        chatMemory.add(userMessage("What is my name?"));
        AiMessage answerWithName = model.generate(chatMemory.messages()).content();
        System.out.println(answerWithName.text()); // Your name is Klaus.
        chatMemory.add(answerWithName);*/
    }
}


