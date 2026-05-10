package Hoveopgave.Hovedopgave.api;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import Hoveopgave.Hovedopgave.exercise.Exercise;
import Hoveopgave.Hovedopgave.exercise.ExerciseRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

	private final ExerciseRepository exerciseRepository;

	public ExerciseController(ExerciseRepository exerciseRepository) {
		this.exerciseRepository = exerciseRepository;
	}

	@GetMapping
	public List<ExerciseResponse> findAll() {
		return exerciseRepository.findAll().stream()
				.sorted(Comparator.comparing(Exercise::getName, String.CASE_INSENSITIVE_ORDER))
				.map(ExerciseController::toResponse)
				.toList();
	}

	@GetMapping("/{id}")
	public ExerciseResponse findById(@PathVariable Long id) {
		return toResponse(getExerciseOrThrow(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ExerciseResponse create(@Valid @RequestBody ExerciseUpsertRequest request) {
		Exercise exercise = new Exercise();
		applyRequest(exercise, request);
		return toResponse(exerciseRepository.save(exercise));
	}

	@PutMapping("/{id}")
	public ExerciseResponse update(@PathVariable Long id, @Valid @RequestBody ExerciseUpsertRequest request) {
		Exercise exercise = getExerciseOrThrow(id);
		applyRequest(exercise, request);
		return toResponse(exerciseRepository.save(exercise));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		Exercise exercise = getExerciseOrThrow(id);
		exerciseRepository.delete(exercise);
	}

	private Exercise getExerciseOrThrow(Long id) {
		return exerciseRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found: " + id));
	}

	private static void applyRequest(Exercise exercise, ExerciseUpsertRequest request) {
		exercise.setName(request.name().trim());
		exercise.setMuscleGroup(request.muscleGroup().trim());
		exercise.setNotes(normalizeNullable(request.notes()));
	}

	private static String normalizeNullable(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static ExerciseResponse toResponse(Exercise exercise) {
		return new ExerciseResponse(exercise.getId(), exercise.getName(), exercise.getMuscleGroup(), exercise.getNotes(),
				exercise.getCreatedAt().toString(), exercise.getUpdatedAt().toString());
	}

	public record ExerciseUpsertRequest(@NotBlank @Size(max = 120) String name,
			@NotBlank @Size(max = 80) String muscleGroup, @Size(max = 1000) String notes) {
	}

	public record ExerciseResponse(Long id, String name, String muscleGroup, String notes, String createdAt,
			String updatedAt) {
	}
}
