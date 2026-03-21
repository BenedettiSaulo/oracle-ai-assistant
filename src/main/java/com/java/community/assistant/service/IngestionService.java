package com.java.community.assistant.service;

import com.java.community.assistant.model.BrazilJugEvent;
import com.java.community.assistant.repository.BrazilJugEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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
		OffsetDateTime dt = OffsetDateTime.parse(event.date());

		String dateInEnglish = dt.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd 'at' HH:mm", Locale.ENGLISH));

		String textToEmbed = String.format(
			"Community Event Details -> Name: %s. Description: %s. Scheduled for: %s. Location: %s. Access: %s.",
			event.name(),
			event.description(),
			dateInEnglish,
			event.local(),
			event.local().equalsIgnoreCase("remote") ? "Online/Virtual" : "In-person/Physical"
		);

		float[] embedding = aiService.generateEmbedding(textToEmbed);

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
