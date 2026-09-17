package co.edu.eci.blueprints.persistence.postgres.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blueprints")
@IdClass(BlueprintId.class)
public class BlueprintEntity {

    @Id
    @Column(name = "author")
    private String author;

    @Id
    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "blueprint", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "point_order")
    private List<PointEntity> points = new ArrayList<>();

    public BlueprintEntity() { }

    public BlueprintEntity(String author, String name) {
        this.author = author;
        this.name = name;
    }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<PointEntity> getPoints() { return points; }

    public void addPoint(PointEntity p) {
        p.setBlueprint(this);
        points.add(p);
    }
}
