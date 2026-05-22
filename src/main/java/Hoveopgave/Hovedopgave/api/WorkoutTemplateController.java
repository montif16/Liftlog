package Hoveopgave.Hovedopgave.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import Hoveopgave.Hovedopgave.session.TrainingSessionRepository;
import Hoveopgave.Hovedopgave.template.WorkoutTemplate;
import Hoveopgave.Hovedopgave.template.WorkoutTemplateItem;
import Hoveopgave.Hovedopgave.template.WorkoutTemplateRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/templates")
public class WorkoutTemplateController {

	private final WorkoutTemplateRepository templateRepository;
	private final ExerciseRepository exerciseRepository;
	private final TrainingSessionRepository sessionRepository;

	public WorkoutTemplateController(WorkoutTemplateRepository templateRepository, ExerciseRepository exerciseRepository,
			TrainingSessionRepository sessionRepository) {
		this.templateRepository = templateRepository;
		this.exerciseRepository = exerciseRepository;
		this.sessionRepository = sessionRepository;
	}

	@GetMapping
	public List<WorkoutTemplateResponse> findAll() {
		return templateRepository.findAllByOrderByNameAsc().stream().map(WorkoutTemplateController::toResponse).toList();
	}

	@GetMapping("/{id}")
	public WorkoutTemplateResponse findById(@PathVariable Long id) {
		return toResponse(getTemplateOrThrow(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public WorkoutTemplateResponse create(@Valid @RequestBody WorkoutTemplateUpsertRequest request) {
		WorkoutTemplate template = new WorkoutTemplate();
		applyRequest(template, request);
		return toResponse(templateRepository.save(template));
	}

	@PutMapping("/{id}")
	public WorkoutTemplateResponse update(@PathVariable Long id, @Valid @RequestBody WorkoutTemplateUpsertRequest request) {
		WorkoutTemplate template = getTemplateOrThrow(id);
		applyRequest(template, request);
		return toResponse(templateRepository.save(template));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		WorkoutTemplate template = getTemplateOrThrow(id);
		sessionRepository.clearTemplateReferences(id);
		templateRepository.delete(template);
	}

	private WorkoutTemplate getTemplateOrThrow(Long id) {
		return templateRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + id));
	}

	private void applyRequest(WorkoutTemplate template, WorkoutTemplateUpsertRequest request) {
		template.setName(request.name().trim());
		template.setDescription(normalizeNullable(request.description()));
		template.getItems().clear();

		List<WorkoutTemplateItemRequest> itemRequests = request.items() == null ? List.of() : request.items();
		Map<Long, Exercise> exercises = loadExercises(itemRequests);
		itemRequests.stream().sorted(Comparator.comparing(WorkoutTemplateItemRequest::orderIndex)).forEach(itemRequest -> {
			WorkoutTemplateItem item = new WorkoutTemplateItem();
			item.setTemplate(template);
			item.setExercise(exercises.get(itemRequest.exerciseId()));
			item.setOrderIndex(itemRequest.orderIndex());
			item.setTargetSets(itemRequest.targetSets());
			item.setTargetReps(itemRequest.targetReps());
			template.getItems().add(item);
		});
	}

	private Map<Long, Exercise> loadExercises(List<WorkoutTemplateItemRequest> itemRequests) {
		Set<Long> exerciseIds = new LinkedHashSet<>();
		for (WorkoutTemplateItemRequest itemRequest : itemRequests) {
			exerciseIds.add(itemRequest.exerciseId());
		}

		if (exerciseIds.isEmpty()) {
			return Map.of();
		}

		Map<Long, Exercise> byId = new LinkedHashMap<>();
		for (Exercise exercise : exerciseRepository.findAllById(exerciseIds)) {
			byId.put(exercise.getId(), exercise);
		}

		if (byId.size() != exerciseIds.size()) {
			List<Long> missing = new ArrayList<>();
			for (Long exerciseId : exerciseIds) {
				if (!byId.containsKey(exerciseId)) {
					missing.add(exerciseId);
				}
			}
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown exercise IDs: " + missing);
		}

		return byId;
	}

	private static String normalizeNullable(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static WorkoutTemplateResponse toResponse(WorkoutTemplate template) {
		List<WorkoutTemplateItemResponse> items = template.getItems().stream()
				.sorted(Comparator.comparing(WorkoutTemplateItem::getOrderIndex))
				.map(item -> new WorkoutTemplateItemResponse(item.getId(), item.getOrderIndex(), item.getTargetSets(),
						item.getTargetReps(), item.getExercise().getId(), item.getExercise().getName(),
						item.getExercise().getMuscleGroup()))
				.toList();

		return new WorkoutTemplateResponse(template.getId(), template.getName(), template.getDescription(),
				template.getCreatedAt().toString(), template.getUpdatedAt().toString(), items);
	}

	public record WorkoutTemplateUpsertRequest(@NotBlank @Size(max = 120) String name, @Size(max = 1000) String description,
			@Valid List<WorkoutTemplateItemRequest> items) {
	}

	public record WorkoutTemplateItemRequest(@NotNull Long exerciseId, @NotNull @Min(1) Integer orderIndex,
			@NotNull @Min(1) Integer targetSets, @NotNull @Min(1) Integer targetReps) {
	}

	public record WorkoutTemplateResponse(Long id, String name, String description, String createdAt, String updatedAt,
			List<WorkoutTemplateItemResponse> items) {
	}

	public record WorkoutTemplateItemResponse(Long id, Integer orderIndex, Integer targetSets, Integer targetReps,
			Long exerciseId, String exerciseName, String muscleGroup) {
	}
}
