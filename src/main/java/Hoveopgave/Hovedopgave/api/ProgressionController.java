package Hoveopgave.Hovedopgave.api;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import Hoveopgave.Hovedopgave.progression.ProgressionService;
import Hoveopgave.Hovedopgave.progression.ProgressionService.ExerciseProgression;
import Hoveopgave.Hovedopgave.progression.ProgressionService.ProgressionEntry;
import Hoveopgave.Hovedopgave.progression.ProgressionService.ProgressionSet;

@RestController
@RequestMapping("/api/progression")
public class ProgressionController {

	private final ProgressionService progressionService;

	public ProgressionController(ProgressionService progressionService) {
		this.progressionService = progressionService;
	}

	@GetMapping("/exercises/{exerciseId}")
	public ExerciseProgressionResponse findExerciseProgression(@PathVariable Long exerciseId) {
		return progressionService.findExerciseProgression(exerciseId)
				.map(ProgressionController::toResponse)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Exercise not found: " + exerciseId));
	}

	private static ExerciseProgressionResponse toResponse(ExerciseProgression progression) {
		List<ProgressionEntryResponse> entries = progression.entries().stream()
				.map(ProgressionController::toEntryResponse)
				.toList();

		return new ExerciseProgressionResponse(progression.exerciseId(), progression.exerciseName(),
				progression.muscleGroup(), entries);
	}

	private static ProgressionEntryResponse toEntryResponse(ProgressionEntry entry) {
		List<ProgressionSetResponse> sets = entry.sets().stream()
				.map(ProgressionController::toSetResponse)
				.toList();

		return new ProgressionEntryResponse(entry.sessionId(), entry.startedAt().toString(), entry.templateName(),
				entry.totalVolume(), entry.bestWeight(), entry.bestReps(), sets);
	}

	private static ProgressionSetResponse toSetResponse(ProgressionSet set) {
		return new ProgressionSetResponse(set.setNumber(), set.weight(), set.reps());
	}

	public record ExerciseProgressionResponse(Long exerciseId, String exerciseName, String muscleGroup,
			List<ProgressionEntryResponse> entries) {
	}

	public record ProgressionEntryResponse(Long sessionId, String startedAt, String templateName,
			BigDecimal totalVolume, BigDecimal bestWeight, Integer bestReps, List<ProgressionSetResponse> sets) {
	}

	public record ProgressionSetResponse(Integer setNumber, BigDecimal weight, Integer reps) {
	}
}
