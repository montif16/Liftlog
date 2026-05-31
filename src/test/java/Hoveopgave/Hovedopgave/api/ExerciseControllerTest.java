package Hoveopgave.Hovedopgave.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.time.Instant;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import Hoveopgave.Hovedopgave.exercise.Exercise;
import Hoveopgave.Hovedopgave.exercise.ExerciseRepository;
import Hoveopgave.Hovedopgave.session.TrainingSession;
import Hoveopgave.Hovedopgave.session.TrainingSessionExercise;
import Hoveopgave.Hovedopgave.session.TrainingSessionExerciseRepository;
import Hoveopgave.Hovedopgave.session.TrainingSessionRepository;
import Hoveopgave.Hovedopgave.template.WorkoutTemplate;
import Hoveopgave.Hovedopgave.template.WorkoutTemplateItem;
import Hoveopgave.Hovedopgave.template.WorkoutTemplateItemRepository;
import Hoveopgave.Hovedopgave.template.WorkoutTemplateRepository;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@SpringBootTest
class ExerciseControllerTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private ExerciseRepository exerciseRepository;

	@Autowired
	private WorkoutTemplateRepository templateRepository;

	@Autowired
	private WorkoutTemplateItemRepository templateItemRepository;

	@Autowired
	private TrainingSessionRepository sessionRepository;

	@Autowired
	private TrainingSessionExerciseRepository sessionExerciseRepository;

	@BeforeEach
	void setUp() {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		RestAssuredMockMvc.mockMvc(mockMvc);
		deleteTestData();
	}

	@AfterEach
	void tearDown() {
		RestAssuredMockMvc.reset();
	}

	@Test
	void findAllReturnsExercisesSortedByName() {
		exerciseRepository.save(exercise("Squat", "Legs"));
		exerciseRepository.save(exercise("Bench press", "Chest"));

		given()
				.when()
				.get("/api/exercises")
				.then()
				.statusCode(200)
				.body("$", hasSize(2))
				.body("name", contains("Bench press", "Squat"));
	}

	@Test
	void createStoresExerciseAndReturnsCreatedResponse() {
		given()
				.contentType("application/json")
				.body("""
						{
						  "name": " Bench press ",
						  "muscleGroup": " Chest ",
						  "notes": "Barbell movement"
						}
						""")
				.when()
				.post("/api/exercises")
				.then()
				.statusCode(201)
				.body("name", equalTo("Bench press"))
				.body("muscleGroup", equalTo("Chest"))
				.body("notes", equalTo("Barbell movement"));

		Assertions.assertThat(exerciseRepository.findAll())
				.singleElement()
				.satisfies(savedExercise -> {
					Assertions.assertThat(savedExercise.getName()).isEqualTo("Bench press");
					Assertions.assertThat(savedExercise.getMuscleGroup()).isEqualTo("Chest");
					Assertions.assertThat(savedExercise.getNotes()).isEqualTo("Barbell movement");
				});
	}

	@Test
	void createNormalizesBlankNotes() {
		given()
				.contentType("application/json")
				.body("""
						{
						  "name": "Deadlift",
						  "muscleGroup": "Back",
						  "notes": "   "
						}
						""")
				.when()
				.post("/api/exercises")
				.then()
				.statusCode(201)
				.body("notes", equalTo(null));

		Assertions.assertThat(exerciseRepository.findAll())
				.singleElement()
				.extracting(Exercise::getNotes)
				.isNull();
	}

	@Test
	void createRejectsBlankName() {
		given()
				.contentType("application/json")
				.body("""
						{
						  "name": "",
						  "muscleGroup": "Chest",
						  "notes": null
						}
						""")
				.when()
				.post("/api/exercises")
				.then()
				.statusCode(400);

		Assertions.assertThat(exerciseRepository.count()).isZero();
	}

	@Test
	void findByIdReturnsNotFoundForUnknownExercise() {
		given()
				.when()
				.get("/api/exercises/404")
				.then()
				.statusCode(404);
	}

	@Test
	void deleteClearsTemplateAndSessionReferencesBeforeDeletingExercise() {
		Exercise exercise = exerciseRepository.save(exercise("Deadlift", "Back"));
		WorkoutTemplate template = templateRepository.save(template("Pull day"));
		WorkoutTemplateItem templateItem = templateItemRepository.save(templateItem(template, exercise));
		TrainingSession session = sessionRepository.save(session());
		TrainingSessionExercise sessionExercise = sessionExerciseRepository.save(sessionExercise(session, exercise));

		given()
				.when()
				.delete("/api/exercises/{id}", exercise.getId())
				.then()
				.statusCode(204);

		Assertions.assertThat(exerciseRepository.findById(exercise.getId())).isEmpty();
		Assertions.assertThat(templateItemRepository.findById(templateItem.getId())).isEmpty();
		Optional<TrainingSessionExercise> savedSessionExercise = sessionExerciseRepository.findById(sessionExercise.getId());
		Assertions.assertThat(savedSessionExercise).isPresent();
		Assertions.assertThat(savedSessionExercise.get().getExercise()).isNull();
	}

	@Test
	void deleteReturnsNotFoundWhenExerciseDoesNotExist() {
		given()
				.when()
				.delete("/api/exercises/404")
				.then()
				.statusCode(404);
	}

	private void deleteTestData() {
		templateItemRepository.deleteAll();
		sessionExerciseRepository.deleteAll();
		sessionRepository.deleteAll();
		templateRepository.deleteAll();
		exerciseRepository.deleteAll();
	}

	private static Exercise exercise(String name, String muscleGroup) {
		Exercise exercise = new Exercise();
		exercise.setName(name);
		exercise.setMuscleGroup(muscleGroup);
		exercise.setNotes(null);
		return exercise;
	}

	private static WorkoutTemplate template(String name) {
		WorkoutTemplate template = new WorkoutTemplate();
		template.setName(name);
		template.setDescription(null);
		return template;
	}

	private static WorkoutTemplateItem templateItem(WorkoutTemplate template, Exercise exercise) {
		WorkoutTemplateItem item = new WorkoutTemplateItem();
		item.setTemplate(template);
		item.setExercise(exercise);
		item.setOrderIndex(0);
		item.setTargetSets(3);
		item.setTargetReps(5);
		return item;
	}

	private static TrainingSession session() {
		TrainingSession session = new TrainingSession();
		session.setTemplateName("Pull day");
		session.setStartedAt(Instant.parse("2026-05-22T09:00:00Z"));
		session.setEndedAt(Instant.parse("2026-05-22T10:00:00Z"));
		session.setDurationSeconds(3600L);
		return session;
	}

	private static TrainingSessionExercise sessionExercise(TrainingSession session, Exercise exercise) {
		TrainingSessionExercise sessionExercise = new TrainingSessionExercise();
		sessionExercise.setSession(session);
		sessionExercise.setExercise(exercise);
		sessionExercise.setOrderIndex(0);
		sessionExercise.setExerciseName(exercise.getName());
		sessionExercise.setMuscleGroup(exercise.getMuscleGroup());
		sessionExercise.setNote(null);
		return sessionExercise;
	}
}
