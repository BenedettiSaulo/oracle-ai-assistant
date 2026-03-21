package com.java.community.assistant.service;

import com.java.community.assistant.repository.BrazilJugEventRepository;
import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import com.oracle.bmc.generativeaiinference.model.*;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.bmc.generativeaiinference.requests.EmbedTextRequest;
import com.oracle.bmc.generativeaiinference.responses.ChatResponse;
import com.oracle.bmc.generativeaiinference.responses.EmbedTextResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiAssistantService {

	private final GenerativeAiInferenceClient aiClient;
	private final BrazilJugEventRepository repository;

	@Value("${oci.compartment.id}")
	private String compartmentId;

	public String processUserQuestion(String question) {
		float[] questionVector = generateEmbedding(question);

		List<String> contextSnippets = repository.findRelevantContext(questionVector);
		String context = String.join("\n", contextSnippets);

		return getAiResponse(question, context);
	}

	public String getAiResponse(String userQuestion, String context) {
		String modelId = "meta.llama-3.3-70b-instruct";

		String systemText = """
			You are the Virtual Assistant for the Brasil JUG (Java User Group) community.
		
			CURRENT DATE: %s
		
			CONTEXT ABOUT EVENTS:
			---
			%s
			---
		
			CRITICAL RULES:
			1. USE THE CONTEXT: Answer questions about community events based ONLY on the context provided above. Use the CURRENT DATE to determine if an event is in the past or future.
		    2. FALLBACK: If the answer is not in the context, politely state you don't know and invite the user to join the Brasil JUG WhatsApp group: https://chat.whatsapp.com/LehW8kDjNcDDROKYt49HSS or Discord chanel: https://discord.gg/bW2Camnr.
		    3. TONE: Maintain a professional and friendly tone.
		    4. LANGUAGE: Pay close attention to the language of the user's question. You MUST answer entirely in the EXACT SAME LANGUAGE as the user. If the user asks in English, translate the context information internally and answer in English. Do not mix languages.
		""".formatted(LocalDate.now().toString(), context);

		SystemMessage systemInstruction = SystemMessage.builder()
			.content(Collections.singletonList(TextContent.builder()
				.text(systemText)
				.build()))
			.build();

		UserMessage userMessage = UserMessage.builder()
			.content(Collections.singletonList(TextContent.builder().text(userQuestion).build()))
			.build();

		ChatDetails chatDetails = ChatDetails.builder()
			.compartmentId(compartmentId)
			.servingMode(OnDemandServingMode.builder().modelId(modelId).build())
			.chatRequest(GenericChatRequest.builder()
				.messages(Arrays.asList(systemInstruction, userMessage))
				.maxTokens(500)
				.temperature(0.2)
				.build())
			.build();

		ChatRequest request = ChatRequest.builder()
			.chatDetails(chatDetails)
			.build();

		try {
			ChatResponse response = aiClient.chat(request);

			GenericChatResponse chatResponse = (GenericChatResponse) response.getChatResult().getChatResponse();
			Message responseMessage = chatResponse.getChoices().get(0).getMessage();

			TextContent responseContent = (TextContent) responseMessage.getContent().get(0);
			return responseContent.getText();

		} catch (Exception e) {
			return "Error querying Oracle AI: " + e.getMessage();
		}
	}

	public float[] generateEmbedding(String text) {
		EmbedTextDetails details = EmbedTextDetails.builder()
				.compartmentId(compartmentId)
				.servingMode(OnDemandServingMode.builder().modelId("cohere.embed-multilingual-v3.0").build())
				.inputs(Collections.singletonList(text))
				.build();

		EmbedTextRequest request = EmbedTextRequest.builder().embedTextDetails(details).build();
		EmbedTextResponse response = aiClient.embedText(request);

		List<Float> embedding = response.getEmbedTextResult().getEmbeddings().get(0);
		float[] vector = new float[embedding.size()];
		for (int i = 0; i < embedding.size(); i++) vector[i] = embedding.get(i);

		return vector;
	}
}
