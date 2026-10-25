package edu.mit.academicclock.repository;

import java.util.List;
import java.util.Optional;

/**
 * Unit IV: Generic Classes and Generic Methods
 * Unit II: Interfaces
 * Standard CRUD repository contract parameterized by entity type T and ID type ID.
 */
public interface CrudRepository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean deleteById(ID id);
    long count();
}
