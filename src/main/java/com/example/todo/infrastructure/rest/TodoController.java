package com.example.todo.infrastructure.rest;

import com.example.todo.application.dto.CreateTodoCommand;
import com.example.todo.application.dto.TodoResponse;
import com.example.todo.application.dto.UpdateTodoCommand;
import com.example.todo.application.usecase.*;
import com.example.todo.domain.model.TodoStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TodoController
 *
 * Contrôleur REST pour la gestion des Todos.
 * Expose l'API publique et orchestre les use cases de l'application.
 *
 * Concepts DDD appliqués :
 * - Presentation Layer : Interface utilisateur (API REST)
 * - Anti-Corruption Layer : Validation et transformation des données d'entrée
 * - Use Case Orchestration : Délégation vers les use cases applicatifs
 *
 * @author Todo Team
 */
@RestController
@RequestMapping("/api/v1/todos")
@Tag(name = "Todos", description = "API de gestion des Todos")
public class TodoController {

    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);

    private final CreateTodoUseCase createTodoUseCase;
    private final GetTodoUseCase getTodoUseCase;
    private final UpdateTodoUseCase updateTodoUseCase;
    private final CompleteTodoUseCase completeTodoUseCase;
    private final ListTodosUseCase listTodosUseCase;
    private final DeleteTodoUseCase deleteTodoUseCase;

    public TodoController(CreateTodoUseCase createTodoUseCase,
                         GetTodoUseCase getTodoUseCase,
                         UpdateTodoUseCase updateTodoUseCase,
                         CompleteTodoUseCase completeTodoUseCase,
                         ListTodosUseCase listTodosUseCase,
                         DeleteTodoUseCase deleteTodoUseCase) {
        this.createTodoUseCase = createTodoUseCase;
        this.getTodoUseCase = getTodoUseCase;
        this.updateTodoUseCase = updateTodoUseCase;
        this.completeTodoUseCase = completeTodoUseCase;
        this.listTodosUseCase = listTodosUseCase;
        this.deleteTodoUseCase = deleteTodoUseCase;
    }

    @Operation(
        summary = "Créer un nouveau Todo",
        description = "Crée un nouveau Todo pour l'utilisateur authentifié"
    )
    @ApiResponse(responseCode = "201", description = "Todo créé avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(
            @Valid @RequestBody CreateTodoCommand command,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") String userId) {

        logger.info("Creating todo for user: {}", userId);

        TodoResponse response = createTodoUseCase.execute(command, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Récupérer un Todo",
        description = "Récupère un Todo spécifique par son identifiant"
    )
    @ApiResponse(responseCode = "200", description = "Todo trouvé")
    @ApiResponse(responseCode = "404", description = "Todo non trouvé")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getTodo(
            @Parameter(description = "Identifiant du Todo") @PathVariable String id,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") String userId) {

        logger.debug("Getting todo {} for user: {}", id, userId);

        TodoResponse response = getTodoUseCase.execute(id, userId);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Lister les Todos",
        description = "Liste les Todos de l'utilisateur avec pagination et filtrage optionnel"
    )
    @ApiResponse(responseCode = "200", description = "Liste des Todos")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    @GetMapping
    public ResponseEntity<com.example.todo.domain.model.PageResult<TodoResponse>> listTodos(
            @Parameter(description = "Filtre par statut (optionnel)") @RequestParam(required = false) TodoStatus status,
            @Parameter(hidden = true) @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") String userId) {
        logger.debug("Listing todos for user: {} with status: {}", userId, status);

        com.example.todo.domain.model.PageResult<TodoResponse> response = status != null
            ? listTodosUseCase.executeByStatus(userId, status, pageable)
            : listTodosUseCase.execute(userId, pageable);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Mettre à jour un Todo",
        description = "Met à jour un Todo existant (mise à jour partielle supportée)"
    )
    @ApiResponse(responseCode = "200", description = "Todo mis à jour")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Todo non trouvé")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> updateTodo(
            @Parameter(description = "Identifiant du Todo") @PathVariable String id,
            @Valid @RequestBody UpdateTodoCommand command,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") String userId) {

        logger.info("Updating todo {} for user: {}", id, userId);

        TodoResponse response = updateTodoUseCase.execute(id, command, userId);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Compléter un Todo",
        description = "Marque un Todo comme complété"
    )
    @ApiResponse(responseCode = "200", description = "Todo complété")
    @ApiResponse(responseCode = "404", description = "Todo non trouvé")
    @ApiResponse(responseCode = "400", description = "Todo ne peut pas être complété")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    @PostMapping("/{id}/complete")
    public ResponseEntity<TodoResponse> completeTodo(
            @Parameter(description = "Identifiant du Todo") @PathVariable String id,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") String userId) {

        logger.info("Completing todo {} for user: {}", id, userId);

        TodoResponse response = completeTodoUseCase.execute(id, userId);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Supprimer un Todo",
        description = "Supprime définitivement un Todo"
    )
    @ApiResponse(responseCode = "204", description = "Todo supprimé")
    @ApiResponse(responseCode = "404", description = "Todo non trouvé")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(
            @Parameter(description = "Identifiant du Todo") @PathVariable String id,
            @Parameter(hidden = true) @RequestHeader("X-User-ID") String userId) {

        logger.info("Deleting todo {} for user: {}", id, userId);

        deleteTodoUseCase.execute(id, userId);

        return ResponseEntity.noContent().build();
    }
}
