package Hoveopgave.Hovedopgave.template;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import Hoveopgave.Hovedopgave.exercise.Exercise;
import Hoveopgave.Hovedopgave.exercise.ExerciseRepository;
import Hoveopgave.Hovedopgave.session.TrainingSessionRepository;

@Service
public class WorkoutTemplateService {

	private final WorkoutTemplateRepository templateRepository;
	private final ExerciseRepository exerciseRepository;
	private final TrainingSessionRepository sessionRepository;

	public WorkoutTemplateService(WorkoutTemplateRepository templateRepository, ExerciseRepository exerciseRepository,
			TrainingSessionRepository sessionRepository) {
		this.templateRepository = templateRepository;
		this.exerciseRepository = exerciseRepository;
		this.sessionRepository = sessionRepository;
	}

	@Transactional(readOnly = true)
	public List<WorkoutTemplate> findAll() {
		return templateRepository.findAllByOrderByNameAsc();
	}

	@Transactional(readOnly = true)
	public Optional<WorkoutTemplate> findById(Long id) {
		return templateRepository.findById(id);
	}

	@Transactional
	public WorkoutTemplate create(WorkoutTemplateCommand command) {
		WorkoutTemplate template = new WorkoutTemplate();
		applyCommand(template, command);
		return templateRepository.save(template);
	}

	@Transactional
	public Optional<WorkoutTemplate> update(Long id, WorkoutTemplateCommand command) {
		return templateRepository.findById(id)
				.map(template -> {
					applyCommand(template, command);
					return templateRepository.save(template);
				});
	}

	@Transactional
	public boolean delete(Long id) {
		Optional<WorkoutTemplate> template = templateRepository.findById(id);
		if (template.isEmpty()) {
			return false;
		}

		sessionRepository.clearTemplateReferences(id);
		templateRepository.delete(template.get());
		return true;
	}

	private void applyCommand(WorkoutTemplate template, WorkoutTemplateCommand command) {
		template.setName(command.name().trim());
		template.setDescription(normalizeNullable(command.description()));
		template.getItems().clear();

		List<WorkoutTemplateItemCommand> itemCommands = command.items() == null ? List.of() : command.items();
		Map<Long, Exercise> exercises = loadExercises(itemCommands);
		itemCommands.stream().sorted(Comparator.comparing(WorkoutTemplateItemCommand::orderIndex)).forEach(itemCommand -> {
			WorkoutTemplateItem item = new WorkoutTemplateItem();
			item.setTemplate(template);
			item.setExercise(exercises.get(itemCommand.exerciseId()));
			item.setOrderIndex(itemCommand.orderIndex());
			item.setTargetSets(itemCommand.targetSets());
			item.setTargetReps(itemCommand.targetReps());
			template.getItems().add(item);
		});
	}

	private Map<Long, Exercise> loadExercises(List<WorkoutTemplateItemCommand> itemCommands) {
		Set<Long> exerciseIds = new LinkedHashSet<>();
		for (WorkoutTemplateItemCommand itemCommand : itemCommands) {
			exerciseIds.add(itemCommand.exerciseId());
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

	public record WorkoutTemplateCommand(String name, String description, List<WorkoutTemplateItemCommand> items) {
	}

	public record WorkoutTemplateItemCommand(Long exerciseId, Integer orderIndex, Integer targetSets,
			Integer targetReps) {
	}
}
