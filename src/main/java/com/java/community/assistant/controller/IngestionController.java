package com.java.community.assistant.controller;

import com.java.community.assistant.model.BrazilJugEvent;
import com.java.community.assistant.service.IngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Tag(name = "Ingestion", description = "Endpoints for interacting with Oracle Database")
public class IngestionController {

	private final IngestionService ingestionService;

	@PostMapping("/saveAll")
	@Operation(
		summary = "Sends events to be saved in the database.",
		description = "It sends a list of community events, which are then processed and saved in the Oracle database."
	)
	public String saveAll(@RequestBody List<BrazilJugEvent> brazilJugEvents) {

		ingestionService.saveAllEvents(brazilJugEvents);

		return "Database successfully populated!";
	}
}
