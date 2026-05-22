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

import Hoveopgave.Hovedopgave.template.WorkoutTemplate;
import Hoveopgave.Hovedopgave.template.WorkoutTemplateItem;
import Hoveopgave.Hovedopgave.template.WorkoutTemplateService;
import Hoveopgave.Hovedopgave.template.WorkoutTemplateService.WorkoutTemplateCommand;
import Hoveopgave.Hovedopgave.template.WorkoutTemplateService.WorkoutTemplateItemCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/templates")
public class WorkoutTemplateController {

	private final WorkoutTemplateService templateService;

	public WorkoutTemplateController(WorkoutTemplateService templateService) {
		this.templateService = templateService;
	}

	@GetMapping
	public List<WorkoutTemplateResponse> findAll() {
		return templateService.findAll().stream().map(WorkoutTemplateController::toResponse).toList();
	}

	@GetMapping("/{id}")
	public WorkoutTemplateResponse findById(@PathVariable Long id) {
		return templateService.findById(id)
				.map(WorkoutTemplateController::toResponse)
				.orElseThrow(() -> notFound(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public WorkoutTemplateResponse create(@Valid @RequestBody WorkoutTemplateUpsertRequest request) {
		return toResponse(templateService.create(toCommand(request)));
	}

	@PutMapping("/{id}")
	public WorkoutTemplateResponse update(@PathVariable Long id, @Valid @RequestBody WorkoutTemplateUpsertRequest request) {
		return templateService.update(id, toCommand(request))
				.map(WorkoutTemplateController::toResponse)
				.orElseThrow(() -> notFound(id));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		if (!templateService.delete(id)) {
			throw notFound(id);
		}
	}

	private static ResponseStatusException notFound(Long id) {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + id);
	}

	private static WorkoutTemplateCommand toCommand(WorkoutTemplateUpsertRequest request) {
		List<WorkoutTemplateItemCommand> items = request.items() == null ? List.of() : request.items().stream()
				.map(item -> new WorkoutTemplateItemCommand(item.exerciseId(), item.orderIndex(), item.targetSets(),
						item.targetReps()))
				.toList();
		return new WorkoutTemplateCommand(request.name(), request.description(), items);
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
