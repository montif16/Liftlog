package Hoveopgave.Hovedopgave.template;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutTemplateRepository extends JpaRepository<WorkoutTemplate, Long> {

	@EntityGraph(attributePaths = { "items", "items.exercise" })
	List<WorkoutTemplate> findAllByOrderByNameAsc();

	@EntityGraph(attributePaths = { "items", "items.exercise" })
	Optional<WorkoutTemplate> findById(Long id);
}
