package Hoveopgave.Hovedopgave.exercise;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Hoveopgave.Hovedopgave.session.TrainingSessionExerciseRepository;
import Hoveopgave.Hovedopgave.template.WorkoutTemplateItemRepository;

@Service
public class ExerciseService {

	private final ExerciseRepository exerciseRepository;
	private final WorkoutTemplateItemRepository templateItemRepository;
	private final TrainingSessionExerciseRepository sessionExerciseRepository;

	public ExerciseService(ExerciseRepository exerciseRepository, WorkoutTemplateItemRepository templateItemRepository,
			TrainingSessionExerciseRepository sessionExerciseRepository) {
		this.exerciseRepository = exerciseRepository;
		this.templateItemRepository = templateItemRepository;
		this.sessionExerciseRepository = sessionExerciseRepository;
	}

	@Transactional(readOnly = true)
	public List<Exercise> findAll() {
		return exerciseRepository.findAll().stream()
				.sorted(Comparator.comparing(Exercise::getName, String.CASE_INSENSITIVE_ORDER))
				.toList();
	}

	@Transactional(readOnly = true)
	public Optional<Exercise> findById(Long id) {
		return exerciseRepository.findById(id);
	}

	@Transactional
	public Exercise create(ExerciseCommand command) {
		Exercise exercise = new Exercise();
		applyCommand(exercise, command);
		return exerciseRepository.save(exercise);
	}

	@Transactional
	public Optional<Exercise> update(Long id, ExerciseCommand command) {
		return exerciseRepository.findById(id)
				.map(exercise -> {
					applyCommand(exercise, command);
					return exerciseRepository.save(exercise);
				});
	}

	@Transactional
	public boolean delete(Long id) {
		Optional<Exercise> exercise = exerciseRepository.findById(id);
		if (exercise.isEmpty()) {
			return false;
		}

		templateItemRepository.deleteByExerciseId(id);
		sessionExerciseRepository.clearExerciseReferences(id);
		exerciseRepository.delete(exercise.get());
		return true;
	}

	private static void applyCommand(Exercise exercise, ExerciseCommand command) {
		exercise.setName(command.name().trim());
		exercise.setMuscleGroup(command.muscleGroup().trim());
		exercise.setNotes(normalizeNullable(command.notes()));
	}

	private static String normalizeNullable(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	public record ExerciseCommand(String name, String muscleGroup, String notes) {
	}
}
