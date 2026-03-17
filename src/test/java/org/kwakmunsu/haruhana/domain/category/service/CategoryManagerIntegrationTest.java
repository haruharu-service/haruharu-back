package org.kwakmunsu.haruhana.domain.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.kwakmunsu.haruhana.IntegrationTestSupport;
import org.kwakmunsu.haruhana.domain.category.repository.CategoryGroupJpaRepository;
import org.kwakmunsu.haruhana.domain.category.repository.CategoryJpaRepository;
import org.kwakmunsu.haruhana.domain.category.repository.CategoryTopicJpaRepository;
import org.kwakmunsu.haruhana.global.entity.EntityStatus;
import org.kwakmunsu.haruhana.global.support.error.ErrorType;
import org.kwakmunsu.haruhana.global.support.error.HaruHanaException;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class CategoryManagerIntegrationTest extends IntegrationTestSupport {

    final CategoryManager categoryManager;
    final CategoryJpaRepository categoryJpaRepository;
    final CategoryGroupJpaRepository categoryGroupJpaRepository;
    final CategoryTopicJpaRepository categoryTopicJpaRepository;


    @Test
    void 카테고리를_생성한다() {
        // given
        var name = "알고리즘";

        // when
        var category = categoryManager.createCategory(name);

        // then
        assertThat(category).isNotNull();
        assertThat(category.getId()).isNotNull();
        assertThat(category.getName()).isEqualTo(name);

        // DB 확인
        var saved = categoryJpaRepository.findById(category.getId()).orElseThrow();
        assertThat(saved.getName()).isEqualTo(name);
    }

    @Test
    void 중복된_카테고리_이름으로_생성_시_예외가_발생한다() {
        // given
        var name = "알고리즘";
        categoryManager.createCategory(name);

        // when & then
        assertThatThrownBy(() -> categoryManager.createCategory(name))
                .isInstanceOf(HaruHanaException.class)
                .hasMessage(ErrorType.DUPLICATE_CATEGORY_NAME.getMessage());
    }

    @Test
    void 카테고리_그룹을_생성한다() {
        // given
        var category = categoryManager.createCategory("알고리즘");
        var groupName = "자료구조";

        // when
        var categoryGroup = categoryManager.createCategoryGroup(category.getId(), groupName);

        // then
        assertThat(categoryGroup).isNotNull();
        assertThat(categoryGroup.getId()).isNotNull();
        assertThat(categoryGroup.getName()).isEqualTo(groupName);
        assertThat(categoryGroup.getCategoryId()).isEqualTo(category.getId());

        // DB 확인
        var saved = categoryGroupJpaRepository.findById(categoryGroup.getId()).orElseThrow();
        assertThat(saved.getName()).isEqualTo(groupName);
    }

    @Test
    void 존재하지_않는_카테고리_ID로_그룹_생성_시_예외가_발생한다() {
        // given
        var invalidCategoryId = 999L;
        var groupName = "자료구조";

        // when & then
        assertThatThrownBy(() -> categoryManager.createCategoryGroup(invalidCategoryId, groupName))
                .isInstanceOf(HaruHanaException.class)
                .hasMessage(ErrorType.NOT_FOUND_CATEGORY.getMessage());
    }

    @Test
    void 중복된_그룹_이름으로_생성_시_예외가_발생한다() {
        // given
        var category = categoryManager.createCategory("알고리즘");
        var groupName = "자료구조";
        categoryManager.createCategoryGroup(category.getId(), groupName);

        // when & then
        assertThatThrownBy(() -> categoryManager.createCategoryGroup(category.getId(), groupName))
                .isInstanceOf(HaruHanaException.class)
                .hasMessageContaining(ErrorType.DUPLICATE_CATEGORY_GROUP_NAME.getMessage());
    }

    @Test
    void 카테고리_토픽을_생성한다() {
        // given
        var category = categoryManager.createCategory("알고리즘");
        var group = categoryManager.createCategoryGroup(category.getId(), "자료구조");
        var topicName = "배열";

        // when
        var categoryTopic = categoryManager.createCategoryTopic(group.getId(), topicName);

        // then
        assertThat(categoryTopic).isNotNull();
        assertThat(categoryTopic.getId()).isNotNull();
        assertThat(categoryTopic.getName()).isEqualTo(topicName);
        assertThat(categoryTopic.getGroupId()).isEqualTo(group.getId());

        // DB 확인
        var saved = categoryTopicJpaRepository.findById(categoryTopic.getId()).orElseThrow();
        assertThat(saved.getName()).isEqualTo(topicName);
    }

    @Test
    void 존재하지_않는_그룹_ID로_토픽_생성_시_예외가_발생한다() {
        // given
        var invalidGroupId = 999L;
        var topicName = "배열";

        // when & then
        assertThatThrownBy(() -> categoryManager.createCategoryTopic(invalidGroupId, topicName))
                .isInstanceOf(HaruHanaException.class)
                .hasMessageContaining(ErrorType.NOT_FOUND_CATEGORY_GROUP.getMessage());
    }

    @Test
    void 중복된_토픽_이름으로_생성_시_예외가_발생한다() {
        // given
        var category = categoryManager.createCategory("알고리즘");
        var group = categoryManager.createCategoryGroup(category.getId(), "자료구조");
        var topicName = "배열";
        categoryManager.createCategoryTopic(group.getId(), topicName);

        // when & then
        assertThatThrownBy(() -> categoryManager.createCategoryTopic(group.getId(), topicName))
                .isInstanceOf(HaruHanaException.class)
                .hasMessageContaining(ErrorType.DUPLICATE_CATEGORY_TOPIC_NAME.getMessage());
    }

    @Test
    void 카테고리_삭제_후_동일한_이름으로_재생성_시_성공한다() {
        // given
        var name = "알고리즘";
        var category = categoryManager.createCategory(name);
        categoryManager.deleteCategory(category.getId());

        // when
        var newCategory = categoryManager.createCategory(name);

        // then
        assertThat(newCategory.getName()).isEqualTo(name);
        assertThat(newCategory.getId()).isNotEqualTo(category.getId());
    }

    @Test
    void 그룹_삭제_후_동일한_이름으로_재생성_시_성공한다() {
        // given
        var category = categoryManager.createCategory("알고리즘");
        var groupName = "자료구조";
        var group = categoryManager.createCategoryGroup(category.getId(), groupName);
        categoryManager.deleteCategoryGroup(group.getId());

        // when
        var newGroup = categoryManager.createCategoryGroup(category.getId(), groupName);

        // then
        assertThat(newGroup.getName()).isEqualTo(groupName);
        assertThat(newGroup.getId()).isNotEqualTo(group.getId());
    }

    @Test
    void 토픽_삭제_후_동일한_이름으로_재생성_시_성공한다() {
        // given
        var category = categoryManager.createCategory("알고리즘");
        var group = categoryManager.createCategoryGroup(category.getId(), "자료구조");
        var topicName = "배열";
        var topic = categoryManager.createCategoryTopic(group.getId(), topicName);
        categoryManager.deleteCategoryTopic(topic.getId());

        // when
        var newTopic = categoryManager.createCategoryTopic(group.getId(), topicName);

        // then
        assertThat(newTopic.getName()).isEqualTo(topicName);
        assertThat(newTopic.getId()).isNotEqualTo(topic.getId());
    }

    @Test
    void 카테고리_삭제_시_하위_그룹과_토픽도_soft_delete_된다() {
        // given
        var category = categoryManager.createCategory("알고리즘");
        var group = categoryManager.createCategoryGroup(category.getId(), "자료구조");
        var topic = categoryManager.createCategoryTopic(group.getId(), "배열");

        // when
        categoryManager.deleteCategory(category.getId());

        // then
        var deletedCategory = categoryJpaRepository.findById(category.getId()).orElseThrow();
        var deletedGroup = categoryGroupJpaRepository.findById(group.getId()).orElseThrow();
        var deletedTopic = categoryTopicJpaRepository.findById(topic.getId()).orElseThrow();

        assertThat(deletedCategory.getStatus()).isEqualTo(EntityStatus.DELETED);
        assertThat(deletedCategory.getDeletedAt()).isNotNull();
        assertThat(deletedGroup.getStatus()).isEqualTo(EntityStatus.DELETED);
        assertThat(deletedGroup.getDeletedAt()).isNotNull();
        assertThat(deletedTopic.getStatus()).isEqualTo(EntityStatus.DELETED);
        assertThat(deletedTopic.getDeletedAt()).isNotNull();
    }

    @Test
    void 그룹_삭제_시_하위_토픽도_soft_delete_된다() {
        // given
        var category = categoryManager.createCategory("알고리즘");
        var group = categoryManager.createCategoryGroup(category.getId(), "자료구조");
        var topic = categoryManager.createCategoryTopic(group.getId(), "배열");

        // when
        categoryManager.deleteCategoryGroup(group.getId());

        // then
        var deletedGroup = categoryGroupJpaRepository.findById(group.getId()).orElseThrow();
        var deletedTopic = categoryTopicJpaRepository.findById(topic.getId()).orElseThrow();

        assertThat(deletedGroup.getStatus()).isEqualTo(EntityStatus.DELETED);
        assertThat(deletedGroup.getDeletedAt()).isNotNull();
        assertThat(deletedTopic.getStatus()).isEqualTo(EntityStatus.DELETED);
        assertThat(deletedTopic.getDeletedAt()).isNotNull();
    }

}