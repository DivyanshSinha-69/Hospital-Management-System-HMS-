package com.hospital.management.core.abstracts;

import com.hospital.management.core.interfaces.BaseService;

public abstract class BaseController<T, ID> {
    protected BaseService<T, ID> service;

    public BaseController(BaseService<T, ID> service) {
        this.service = service;
    }

    public T getById(ID id) throws Exception {
        return service.findById(id);
    }

    public void create(T entity) throws Exception {
        service.save(entity);
    }

    public void update(T entity) throws Exception {
        service.update(entity);
    }

    public void delete(ID id) throws Exception {
        service.delete(id);
    }
}
