package com.hospital.management.core.interfaces;

import java.util.List;

public interface BaseDAO<T, ID> {
    T findById(ID id) throws Exception;
    List<T> findAll() throws Exception;
    void save(T entity) throws Exception;
    void update(T entity) throws Exception;
    void delete(ID id) throws Exception;
}
