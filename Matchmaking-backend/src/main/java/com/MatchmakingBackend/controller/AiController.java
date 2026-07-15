package com.MatchmakingBackend.controller;

import com.MatchmakingBackend.service.AiAdvisorService;
import com.MatchmakingBackend.dto.AiChatRequest;
import com.MatchmakingBackend.dto.AiChatResponse;
import com.MatchmakingBackend.dto.MatchReport;
import com.MatchmakingBackend.service.MatchReportService;
import com.MatchmakingBackend.service.SemanticMatchService;
import jakarta.validation.Valid;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {
	private final MatchReportService matchReportService;
	private final AiAdvisorService aiAdvisorService;
	private final SemanticMatchService semanticMatchService;

	public AiController(
			MatchReportService matchReportService,
			AiAdvisorService aiAdvisorService,
			SemanticMatchService semanticMatchService) {

		this.matchReportService = matchReportService;
		this.aiAdvisorService = aiAdvisorService;
		this.semanticMatchService = semanticMatchService;
	}

	@PostMapping("/chat")
	public AiChatResponse chat(@Valid @RequestBody AiChatRequest request) {

		MatchReport report =
				matchReportService.createCalculatedReport(
						request.profileOneId(),
						request.profileTwoId());

		List<Document> retrievedContext =
				semanticMatchService.findComparableContext(
						report.profileOne(),
						report.profileTwo());

		return aiAdvisorService.chat(
				report.profileOne(),
				report.profileTwo(),
				request.message(),
				report,
				retrievedContext);
	}
}
