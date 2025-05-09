package org.example.expert.domain.todo.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.QUser;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;
    private final JPAQueryFactory jpaQueryFactory;


    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail(), user.getNickname())
        );
    }

    public Page<TodoResponse> getTodos(int page, int size, String weather, LocalDate startDate, LocalDate endDate) {
        // 수정일 기준으로 내림차순 정렬
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "modifiedAt"));
        Page<Todo> todoPage = todoRepository.searchTodos(weather, startDate, endDate, pageable);

        List<TodoResponse> content = todoPage.stream()
                .map(todo -> new TodoResponse(
                        todo.getId(),
                        todo.getTitle(),
                        todo.getContents(),
                        todo.getWeather(),
                        new UserResponse(
                                todo.getUser().getId(),
                                todo.getUser().getEmail(),
                                todo.getUser().getNickname()
                        ),
                        todo.getCreatedAt(),
                        todo.getModifiedAt()
                ))
                .toList();

        return new PageImpl<>(content, pageable, todoPage.getTotalElements());
    }

    public TodoResponse getTodo(long todoId) {
        QTodo todo = QTodo.todo;

        Todo result = jpaQueryFactory
                .selectFrom(todo)
                .join(todo.user, QUser.user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        if (result == null) {
            throw new InvalidRequestException("Todo not found");
        }

        User user = result.getUser();

        return new TodoResponse(
                result.getId(),
                result.getTitle(),
                result.getContents(),
                result.getWeather(),
                new UserResponse(user.getId(), user.getEmail(), user.getNickname()),
                result.getCreatedAt(),
                result.getModifiedAt()
        );
    }
}
