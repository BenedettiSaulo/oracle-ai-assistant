package com.java.community.assistant.service;

import com.java.community.assistant.model.BrazilJugEvent;
import com.java.community.assistant.repository.BrazilJugEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngestionService {

	private final AiAssistantService aiService;
	private final BrazilJugEventRepository brazilJugEventRepository;

	public void saveAllEvents(List<BrazilJugEvent> brazilJugEvents) {
		for (BrazilJugEvent brazilJugEvent : brazilJugEvents) {
			this.saveEvent(brazilJugEvent);
		}
	}

	public void saveEvent(BrazilJugEvent event) {
		float[] embedding = aiService.generateEmbedding(
			"Event: " + event.name() + ". Description: " + event.description()
		);

		brazilJugEventRepository.saveEvent(
			event.name(),
			event.description(),
			event.date(),
			event.local(),
			event.link(),
			embedding
		);
	}
}
