package com.example.todo.infrastructure.persistence;

import com.example.todo.domain.model.Todo;
import com.example.todo.domain.model.TodoId;
import com.example.todo.domain.model.TodoStatus;
import com.example.todo.domain.repository.TodoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.todo.domain.model.PageResult;
import com.example.todo.domain.model.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional
public class JpaTodoRepository implements TodoRepository {
    private final TodoJpaRepository jpaRepository;

    public JpaTodoRepository(TodoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Todo save(Todo todo) {
        var entity = TodoMapper.toJpaEntity(todo);
        var savedEntity = jpaRepository.save(entity);
        return TodoMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Todo> findById(TodoId id) {
        return jpaRepository.findById(id.value())
                .map(TodoMapper::toDomainEntity);
    }

    @Override
    public PageResult<Todo> findByUserId(String userId, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize());
    Page<TodoJpaEntity> page = jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    var content = page.map(TodoMapper::toDomainEntity).getContent();
    return new PageResult<>(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    public PageResult<Todo> findByUserIdAndStatus(String userId, TodoStatus status, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize());
    Page<TodoJpaEntity> page = jpaRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
    var content = page.map(TodoMapper::toDomainEntity).getContent();
    return new PageResult<>(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    public List<Todo> findOverdueTodosByUserId(String userId, Instant currentTime) {
        return jpaRepository.findOverdueTodosByUserId(userId, currentTime).stream()
                .map(TodoMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Todo> findByUserIdAndDueDateBetween(String userId, Instant from, Instant to) {
        return jpaRepository.findByUserIdAndDueDateBetweenOrderByDueDateAsc(userId, from, to).stream()
                .map(TodoMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long countByUserIdAndStatus(String userId, TodoStatus status) {
        return jpaRepository.countByUserIdAndStatus(userId, status);
    }

    @Override
    public boolean existsById(TodoId id) {
        return jpaRepository.existsById(id.value());
    }

    @Override
    public void deleteById(TodoId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public void deleteByUserId(String userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
