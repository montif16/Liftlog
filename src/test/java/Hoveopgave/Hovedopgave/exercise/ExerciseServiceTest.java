package Hoveopgave.Hovedopgave.exercise;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Hoveopgave.Hovedopgave.exercise.ExerciseService.ExerciseCommand;
import Hoveopgave.Hovedopgave.session.TrainingSessionExerciseRepository;
import Hoveopgave.Hovedopgave.template.WorkoutTemplateItemRepository;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

	@Mock
	private ExerciseRepository exerciseRepository;

	@Mock
	private WorkoutTemplateItemRepository templateItemRepository;

	@Mock
	private TrainingSessionExerciseRepository sessionExerciseRepository;

	@Test
	void findAllReturnsExercisesSortedByName() {
		Exercise squat = exercise(1L, "Squat", "Legs");
		Exercise benchPress = exercise(2L, "Bench press", "Chest");
		ExerciseService service = service();
		when(exerciseRepository.findAll()).thenReturn(List.of(squat, benchPress));

		List<Exercise> exercises = service.findAll();

		Assertions.assertThat(exercises)
				.extracting(Exercise::getName)
				.containsExactly("Bench press", "Squat");
	}

	@Test
	void createTrimsRequiredFieldsAndNormalizesBlankNotes() {
		ExerciseService service = service();
		when(exerciseRepository.save(any(Exercise.class))).thenAnswer(invocation -> invocation.getArgument(0));

		service.create(new ExerciseCommand(" Bench press ", " Chest ", "   "));

		ArgumentCaptor<Exercise> exerciseCaptor = ArgumentCaptor.forClass(Exercise.class);
		verify(exerciseRepository).save(exerciseCaptor.capture());
		Exercise savedExercise = exerciseCaptor.getValue();
		Assertions.assertThat(savedExercise.getName()).isEqualTo("Bench press");
		Assertions.assertThat(savedExercise.getMuscleGroup()).isEqualTo("Chest");
		Assertions.assertThat(savedExercise.getNotes()).isNull();
	}

	@Test
	void deleteClearsTemplateAndSessionReferencesBeforeDeletingExercise() {
		Exercise exercise = exercise(7L, "Deadlift", "Back");
		ExerciseService service = service();
		when(exerciseRepository.findById(7L)).thenReturn(Optional.of(exercise));

		boolean deleted = service.delete(7L);

		Assertions.assertThat(deleted).isTrue();
		verify(templateItemRepository).deleteByExerciseId(7L);
		verify(sessionExerciseRepository).clearExerciseReferences(7L);
		verify(exerciseRepository).delete(exercise);
	}

	@Test
	void deleteReturnsFalseWhenExerciseDoesNotExist() {
		ExerciseService service = service();
		when(exerciseRepository.findById(404L)).thenReturn(Optional.empty());

		boolean deleted = service.delete(404L);

		Assertions.assertThat(deleted).isFalse();
		verify(templateItemRepository, never()).deleteByExerciseId(any());
		verify(sessionExerciseRepository, never()).clearExerciseReferences(any());
		verify(exerciseRepository, never()).delete(any());
	}

	private ExerciseService service() {
		return new ExerciseService(exerciseRepository, templateItemRepository, sessionExerciseRepository);
	}

	private static Exercise exercise(Long id, String name, String muscleGroup) {
		Exercise exercise = new Exercise();
		exercise.setId(id);
		exercise.setName(name);
		exercise.setMuscleGroup(muscleGroup);
		exercise.setNotes(null);
		exercise.setCreatedAt(Instant.parse("2026-05-22T09:00:00Z"));
		exercise.setUpdatedAt(Instant.parse("2026-05-22T09:00:00Z"));
		return exercise;
	}
}
