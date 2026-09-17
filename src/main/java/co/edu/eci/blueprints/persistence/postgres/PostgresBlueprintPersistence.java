package co.edu.eci.blueprints.persistence.postgres;

import co.edu.eci.blueprints.model.Blueprint;
import co.edu.eci.blueprints.model.Point;
import co.edu.eci.blueprints.persistence.BlueprintNotFoundException;
import co.edu.eci.blueprints.persistence.BlueprintPersistence;
import co.edu.eci.blueprints.persistence.BlueprintPersistenceException;
import co.edu.eci.blueprints.persistence.postgres.entity.BlueprintEntity;
import co.edu.eci.blueprints.persistence.postgres.entity.BlueprintId;
import co.edu.eci.blueprints.persistence.postgres.entity.PointEntity;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final BlueprintJpaRepository repository;

    public PostgresBlueprintPersistence(BlueprintJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        BlueprintId id = new BlueprintId(bp.getAuthor(), bp.getName());
        if (repository.existsById(id)) {
            throw new BlueprintPersistenceException("Blueprint already exists: " + bp.getAuthor() + ":" + bp.getName());
        }
        BlueprintEntity entity = new BlueprintEntity(bp.getAuthor(), bp.getName());
        for (Point p : bp.getPoints()) {
            entity.addPoint(new PointEntity(p.x(), p.y()));
        }
        repository.save(entity);
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        BlueprintEntity entity = repository.findById(new BlueprintId(author, name))
                .orElseThrow(() -> new BlueprintNotFoundException("Blueprint not found: %s/%s".formatted(author, name)));
        return toDomain(entity);
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        List<BlueprintEntity> entities = repository.findByAuthor(author);
        if (entities.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }
        return entities.stream().map(this::toDomain).collect(Collectors.toSet());
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        return repository.findAll().stream().map(this::toDomain).collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        BlueprintEntity entity = repository.findById(new BlueprintId(author, name))
                .orElseThrow(() -> new BlueprintNotFoundException("Blueprint not found: %s/%s".formatted(author, name)));
        entity.addPoint(new PointEntity(x, y));
        repository.save(entity);
    }

    private Blueprint toDomain(BlueprintEntity entity) {
        List<Point> points = entity.getPoints().stream()
                .map(pe -> new Point(pe.getX(), pe.getY()))
                .toList();
        return new Blueprint(entity.getAuthor(), entity.getName(), points);
    }
}
