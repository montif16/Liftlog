package Hoveopgave.Hovedopgave.progression;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Hoveopgave.Hovedopgave.exercise.Exercise;
import Hoveopgave.Hovedopgave.exercise.ExerciseRepository;
import Hoveopgave.Hovedopgave.session.TrainingSessionExercise;
import Hoveopgave.Hovedopgave.session.TrainingSessionExerciseRepository;
import Hoveopgave.Hovedopgave.session.TrainingSessionSet;

@Service
public class ProgressionService {

	private final ExerciseRepository exerciseRepository;
	private final TrainingSessionExerciseRepository sessionExerciseRepository;

	public ProgressionService(ExerciseRepository exerciseRepository,
			TrainingSessionExerciseRepository sessionExerciseRepository) {
		this.exerciseRepository = exerciseRepository;
		this.sessionExerciseRepository = sessionExerciseRepository;
	}

	@Transactional(readOnly = true)
	public Optional<ExerciseProgression> findExerciseProgression(Long exerciseId) {
		return exerciseRepository.findById(exerciseId)
				.map(exercise -> {
					List<ProgressionEntry> entries = sessionExerciseRepository.findProgressionByExerciseId(exerciseId)
							.stream()
							.map(ProgressionService::toEntry)
							.toList();

					return new ExerciseProgression(exercise.getId(), exercise.getName(), exercise.getMuscleGroup(),
							entries);
				});
	}

	private static ProgressionEntry toEntry(TrainingSessionExercise sessionExercise) {
		List<ProgressionSet> sets = sessionExercise.getSets().stream()
				.sorted(Comparator.comparing(TrainingSessionSet::getSetNumber))
				.map(sessionSet -> new ProgressionSet(sessionSet.getSetNumber(), sessionSet.getWeight(),
						sessionSet.getReps()))
				.toList();

		BigDecimal totalVolume = calculateTotalVolume(sessionExercise.getSets());
		TrainingSessionSet bestSet = findBestSet(sessionExercise.getSets());
		BigDecimal bestWeight = bestSet == null ? null : bestSet.getWeight();
		Integer bestReps = bestSet == null ? null : bestSet.getReps();

		return new ProgressionEntry(sessionExercise.getSession().getId(),
				sessionExercise.getSession().getStartedAt(), sessionExercise.getSession().getTemplateName(),
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

	public record ExerciseProgression(Long exerciseId, String exerciseName, String muscleGroup,
			List<ProgressionEntry> entries) {
	}

	public record ProgressionEntry(Long sessionId, Instant startedAt, String templateName, BigDecimal totalVolume,
			BigDecimal bestWeight, Integer bestReps, List<ProgressionSet> sets) {
	}

	public record ProgressionSet(Integer setNumber, BigDecimal weight, Integer reps) {
	}
}
