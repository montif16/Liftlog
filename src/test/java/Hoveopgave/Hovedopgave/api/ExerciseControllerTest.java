package Hoveopgave.Hovedopgave.api;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import Hoveopgave.Hovedopgave.exercise.Exercise;
import Hoveopgave.Hovedopgave.exercise.ExerciseRepository;
import Hoveopgave.Hovedopgave.session.TrainingSessionExerciseRepository;
import Hoveopgave.Hovedopgave.template.WorkoutTemplateItemRepository;

@ExtendWith(MockitoExtension.class)
class ExerciseControllerTest {

	@Mock
	private ExerciseRepository exerciseRepository;

	@Mock
	private WorkoutTemplateItemRepository templateItemRepository;

	@Mock
	private TrainingSessionExerciseRepository sessionExerciseRepository;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();

		ExerciseController controller =
				new ExerciseController(exerciseRepository, templateItemRepository, sessionExerciseRepository);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setValidator(validator)
				.build();
	}

	@Test
	void findAllReturnsExercisesSortedByName() throws Exception {
		Exercise squat = exercise(1L, "Squat", "Legs");
		Exercise benchPress = exercise(2L, "Bench press", "Chest");
		when(exerciseRepository.findAll()).thenReturn(List.of(squat, benchPress));

		mockMvc.perform(get("/api/exercises"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].name").value("Bench press"))
				.andExpect(jsonPath("$[1].name").value("Squat"));
	}

	@Test
	void createStoresExerciseAndReturnsCreatedResponse() throws Exception {
		when(exerciseRepository.save(any(Exercise.class))).thenAnswer(invocation -> {
			Exercise exercise = invocation.getArgument(0);
			exercise.setId(10L);
			exercise.setCreatedAt(Instant.parse("2026-05-22T09:00:00Z"));
			exercise.setUpdatedAt(Instant.parse("2026-05-22T09:00:00Z"));
			return exercise;
		});

		mockMvc.perform(post("/api/exercises")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": " Bench press ",
								  "muscleGroup": " Chest ",
								  "notes": "Barbell movement"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.name").value("Bench press"))
				.andExpect(jsonPath("$.muscleGroup").value("Chest"))
				.andExpect(jsonPath("$.notes").value("Barbell movement"));

		ArgumentCaptor<Exercise> exerciseCaptor = ArgumentCaptor.forClass(Exercise.class);
		verify(exerciseRepository).save(exerciseCaptor.capture());
		Exercise savedExercise = exerciseCaptor.getValue();
		org.assertj.core.api.Assertions.assertThat(savedExercise.getName()).isEqualTo("Bench press");
		org.assertj.core.api.Assertions.assertThat(savedExercise.getMuscleGroup()).isEqualTo("Chest");
	}

	@Test
	void createRejectsBlankName() throws Exception {
		mockMvc.perform(post("/api/exercises")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "",
								  "muscleGroup": "Chest",
								  "notes": null
								}
								"""))
				.andExpect(status().isBadRequest());

		verify(exerciseRepository, never()).save(any(Exercise.class));
	}

	@Test
	void findByIdReturnsNotFoundForUnknownExercise() throws Exception {
		when(exerciseRepository.findById(404L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/exercises/404"))
				.andExpect(status().isNotFound());
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
