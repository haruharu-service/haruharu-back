package org.kwakmunsu.haruhana.domain.category.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kwakmunsu.haruhana.global.entity.BaseEntity;

@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "deleted_at"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CategoryGroup extends BaseEntity {

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private String name;

    public static CategoryGroup create(Long categoryId, String name) {
        CategoryGroup categoryGroup = new CategoryGroup();

        categoryGroup.categoryId = categoryId;
        categoryGroup.name = name;

        return categoryGroup;
    }

    public void updateName(String name) {
        this.name = name;
    }

}