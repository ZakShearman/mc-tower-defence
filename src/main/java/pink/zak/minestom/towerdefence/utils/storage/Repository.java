package pink.zak.minestom.towerdefence.utils.storage;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Optional;

@ApiStatus.Internal
public interface Repository<ID, T> {

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param id     must not be {@literal null}
     * @param entity must not be {@literal null}.
     * @return the saved entity; will never be {@literal null}.
     */
    @NotNull <S extends T> S save(@NotNull ID id, @NotNull S entity);

    /**
     * Saves all given entities.
     *
     * @param entities must not be {@literal null} nor must it contain {@literal null}.
     * @return the saved entities; will never be {@literal null}. The returned {@literal Iterable} will have the same size
     * as the {@literal Iterable} passed as an argument.
     */
    default <S extends T> @NotNull Iterable<S> saveAll(@NotNull Iterable<ID> ids, @NotNull Iterable<S> entities) {
        Iterator<ID> idIterator = ids.iterator();
        Iterator<S> entityIterator = entities.iterator();
        while (idIterator.hasNext()) {
            ID id = idIterator.next();
            S entity = entityIterator.next();
            this.save(id, entity);
        }
        return entities;
    }

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal Optional#empty()} if none found.
     */
    @NotNull Optional<T> findById(@NotNull ID id);

    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    @NotNull Iterable<T> findAll();

    /**
     * Returns whether an entity with the given id exists.
     *
     * @param id must not be {@literal null}.
     * @return {@literal true} if an entity with the given id exists, {@literal false} otherwise.
     */
    boolean existsById(@NotNull ID id);

    /**
     * Returns the number of entities available.
     *
     * @return the number of entities.
     */
    long count();

    /**
     * Deletes the entity with the given ID.
     *
     * @param id - must not be null.
     */
    void deleteById(@NotNull ID id);

    /**
     * Deletes all entities with the given IDs.
     *
     * @param ids - must not be null. Must not contain null elements.
     */
    default void deleteAllById(@NotNull Iterable<? extends ID> ids) {
        for (ID id : ids)
            this.deleteById(id);
    }

    /**
     * Deletes all entities managed by the repository.
     */
    void deleteAll();
}
