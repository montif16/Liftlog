package Hoveopgave.Hovedopgave.api;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import Hoveopgave.Hovedopgave.exercise.Exercise;
import Hoveopgave.Hovedopgave.exercise.ExerciseRepository;
import Hoveopgave.Hovedopgave.session.TrainingSessionExercise;
import Hoveopgave.Hovedopgave.session.TrainingSessionExerciseRepository;
import Hoveopgave.Hovedopgave.session.TrainingSessionSet;

@RestController
@RequestMapping("/api/progression")
public class ProgressionController {

	private final ExerciseRepository exerciseRepository;
	private final TrainingSessionExerciseRepository sessionExerciseRepository;

	public ProgressionController(ExerciseRepository exerciseRepository,
			TrainingSessionExerciseRepository sessionExerciseRepository) {
		this.exerciseRepository = exerciseRepository;
		this.sessionExerciseRepository = sessionExerciseRepository;
	}

	@GetMapping("/exercises/{exerciseId}")
	@Transactional(readOnly = true)
	public ExerciseProgressionResponse findExerciseProgression(@PathVariable Long exerciseId) {
		Exercise exercise = exerciseRepository.findById(exerciseId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Exercise not found: " + exerciseId));

		List<ProgressionEntryResponse> entries = sessionExerciseRepository.findProgressionByExerciseId(exerciseId)
				.stream()
				.map(ProgressionController::toEntryResponse)
				.toList();

		return new ExerciseProgressionResponse(exercise.getId(), exercise.getName(), exercise.getMuscleGroup(),
				entries);
	}

	private static ProgressionEntryResponse toEntryResponse(TrainingSessionExercise sessionExercise) {
		List<ProgressionSetResponse> sets = sessionExercise.getSets().stream()
				.sorted(Comparator.comparing(TrainingSessionSet::getSetNumber))
				.map(sessionSet -> new ProgressionSetResponse(sessionSet.getSetNumber(), sessionSet.getWeight(),
						sessionSet.getReps()))
				.toList();

		BigDecimal totalVolume = calculateTotalVolume(sessionExercise.getSets());
		TrainingSessionSet bestSet = findBestSet(sessionExercise.getSets());
		BigDecimal bestWeight = bestSet == null ? null : bestSet.getWeight();
		Integer bestReps = bestSet == null ? null : bestSet.getReps();

		return new ProgressionEntryResponse(sessionExercise.getSession().getId(),
				sessionExercise.getSession().getStartedAt().toString(), sessionExercise.getSession().getTemplateName(),
				totalVolume, bestWeight, bestReps, sets);
	}

	private static BigDecimal calculateTotalVolume(List<TrainingSessionSet> sets) {
		return sets.stream()
				.filter(sessionSet -> sessionSet.getWeight() != null)
				.map(sessionSet -> sessionSet.getWeight().multiply(BigDecimal.valueOf(sessionSet.getReps())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private static TrainingSessionSet findBestSet(List<TrainingSessionSet> sets) {
		return sets.stream()
				.filter(sessionSet -> sessionSet.getWeight() != null)
				.max(Comparator.comparing(TrainingSessionSet::getWeight)
						.thenComparing(TrainingSessionSet::getReps))
				.orElse(null);
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
