package com.java.community.assistant.service;

import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import com.oracle.bmc.generativeaiinference.model.*;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.bmc.generativeaiinference.requests.EmbedTextRequest;
import com.oracle.bmc.generativeaiinference.responses.ChatResponse;
import com.oracle.bmc.generativeaiinference.responses.EmbedTextResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiAssistantService {

	private final GenerativeAiInferenceClient aiClient;

	@Value("${oci.compartment.id}")
	private String compartmentId;

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

	public String getAiResponse(String userQuestion, String context) {
		String modelId = "meta.llama-3.3-70b-instruct";

		SystemMessage systemInstruction = SystemMessage.builder()
				.content(Collections.singletonList(TextContent.builder()
						.text("Você é o Assistente Virtual da comunidade Brasil JUG (Java User Group). " +
								"Use o contexto abaixo para responder perguntas sobre os eventos da comunidade, tanto os que já aconteceram quanto os novos. " +
								"Se a resposta não estiver no contexto, diga que não sabe e convide o usuário para o Whatsapp da comunidade Brasil JUG com o link: https://chat.whatsapp.com/LehW8kDjNcDDROKYt49HSS. " +
								"Mantenha um tom profissional e amigável. Contexto: " + context)
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
						.temperature(0.5)
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
}
