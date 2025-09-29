package com.hospital.management.core.interfaces;

public interface BaseService<T, ID> {
    T findById(ID id) throws Exception;
    void save(T entity) throws Exception;
    void update(T entity) throws Exception;
    void delete(ID id) throws Exception;
}
