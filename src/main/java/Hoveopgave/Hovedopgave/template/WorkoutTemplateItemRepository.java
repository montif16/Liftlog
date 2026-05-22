package Hoveopgave.Hovedopgave.template;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface WorkoutTemplateItemRepository extends JpaRepository<WorkoutTemplateItem, Long> {

	@Transactional
	void deleteByExerciseId(Long exerciseId);
}
