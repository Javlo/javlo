package org.javlo.service.ia;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;

import static dev.langchain4j.data.message.UserMessage.userMessage;

public class ChatBot {

    private static String OPENIA_API_KEY = "###";

    public static void main(String[] args) {

        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(OPENIA_API_KEY)
                .modelName(OpenAiChatModelName.GPT_3_5_TURBO)
                .build();

        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // You have full control over the chat memory.
        // You can decide if you want to add a particular message to the memory
        // (e.g. you might not want to store few-shot examples to save on tokens).
        // You can process/modify the message before saving if required.

        chatMemory.add(userMessage("Hello, my name is Klaus"));
        //AiMessage answer = model.chat(chatMemory.messages()).
       // System.out.println(answer.text()); // Hello Klaus! How can I assist you today?
        //chatMemory.add(answer);

        //chatMemory.add(userMessage("What is my name?"));
        /*AiMessage answerWithName = model.chat(chatMemory.messages()).content().get(0);
        System.out.println(answerWithName.text()); // Your name is Klaus.
        chatMemory.add(answerWithName);*/ 
    }
}