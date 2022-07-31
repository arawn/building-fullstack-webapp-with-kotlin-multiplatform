package todoapp.application

import mu.KLogging
import org.springframework.transaction.annotation.Transactional
import todoapp.domain.*

/**
 * 할 일 관리 프로세서
 *
 * @author springrunner.kr@gmail.com
 */
@Transactional
internal class TodoManageProcessor(
    private val todoIdGenerator: TodoIdGenerator,
    private val todoRepository: TodoRepository
): TodoFind, TodoRegistry, TodoModification, TodoCleanup {

    override suspend fun all(): Todos {
        logger.debug { "find todos" }

        return todoRepository.findByOrderByCreatedDateAsc()
    }

    override suspend fun byId(id: TodoId): Todo {
        logger.debug { "find todo (id: $id)" }

        return loadTodoById(id)
    }

    override suspend fun register(text: String): TodoId {
        logger.debug { "register todo (text: $text)" }

        return Todo.create(text = text, idGenerator = todoIdGenerator).apply {
            todoRepository.save(this)
        }.id
    }

    override suspend fun modify(id: TodoId, text: String, completed: Boolean) {
        logger.debug { "modify todo (id: $id, text: $text, completed: $completed)" }

        todoRepository.save(
            loadTodoById(id).update(text = text, completed = completed)
        )
    }

    override suspend fun clear(id: TodoId) {
        logger.debug { "clear todo (id: $id)" }

        todoRepository.delete(loadTodoById(id))
    }

    override suspend fun clearAllCompleted() {
        logger.debug { "clear all completed todos" }

        todoRepository.findByOrderByCreatedDateAsc().filter(TodoFilter.COMPLETED).forEach {
            todoRepository.delete(it)
        }
    }

    private suspend fun loadTodoById(id: TodoId) =
        todoRepository.findById(id) ?: throw TodoExceptions.notFound(id)

    companion object : KLogging()
}
