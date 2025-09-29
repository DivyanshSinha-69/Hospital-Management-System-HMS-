package com.hospital.management.core.abstracts;

import com.hospital.management.core.interfaces.BaseService;

public abstract class BaseServiceImpl<T, ID> implements BaseService<T, ID> {

    @Override
    public abstract T findById(ID id) throws Exception;

    @Override
    public abstract void save(T entity) throws Exception;

    @Override
    public abstract void update(T entity) throws Exception;

    @Override
    public abstract void delete(ID id) throws Exception;
}
