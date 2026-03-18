package com.java.community.assistant.controller;

import com.java.community.assistant.repository.BrazilJugEventRepository;
import com.java.community.assistant.service.AiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assistant")
@Tag(name = "Community Assistent", description = "Endpoints for interacting with Oracle Cloud AI")
public class AssistantController {

	private final AiAssistantService aiService;
	private final BrazilJugEventRepository brazilJugEventRepository;

	@PostMapping("/ask")
	@Operation(
		summary = "Send a question to the assistant.",
		description = "It sends a prompt to the OCI Generative AI model and returns the processed response."
	)
	public ResponseEntity<String> ask(@RequestBody String question) {
		float[] questionVector = aiService.generateEmbedding(question);

		List<String> contextSnippets = brazilJugEventRepository.findRelevantContext(questionVector);
		String context = String.join("\n", contextSnippets);

		String response = aiService.getAiResponse(question, context);

		return ResponseEntity.ok(response);
	}
}
