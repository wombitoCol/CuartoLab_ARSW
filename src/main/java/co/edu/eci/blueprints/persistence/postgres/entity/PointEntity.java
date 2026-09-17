package co.edu.eci.blueprints.persistence.postgres.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "points")
public class PointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "x")
    private int x;

    @Column(name = "y")
    private int y;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "blueprint_author", referencedColumnName = "author"),
            @JoinColumn(name = "blueprint_name", referencedColumnName = "name")
    })
    private BlueprintEntity blueprint;

    public PointEntity() { }

    public PointEntity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Long getId() { return id; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public BlueprintEntity getBlueprint() { return blueprint; }
    public void setBlueprint(BlueprintEntity blueprint) { this.blueprint = blueprint; }
}
