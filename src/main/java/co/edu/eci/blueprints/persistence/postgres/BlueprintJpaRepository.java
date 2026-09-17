package co.edu.eci.blueprints.persistence.postgres;

import co.edu.eci.blueprints.persistence.postgres.entity.BlueprintEntity;
import co.edu.eci.blueprints.persistence.postgres.entity.BlueprintId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlueprintJpaRepository extends JpaRepository<BlueprintEntity, BlueprintId> {
    List<BlueprintEntity> findByAuthor(String author);
}
