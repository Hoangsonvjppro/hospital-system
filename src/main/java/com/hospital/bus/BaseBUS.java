package com.hospital.bus;

import com.hospital.dao.BaseDAO;
import java.util.List;

/**
 * Lớp BUS cơ sở — chứa logic nghiệp vụ chung.
 * Base BUS class — contains common business logic.
 *
 * @param <T> Kiểu entity
 */
public abstract class BaseBUS<T> {
    protected BaseDAO<T> dao;

    public BaseBUS(BaseDAO<T> dao) {
        this.dao = dao;
    }

    public T findById(int id) {
        return dao.findById(id);
    }

    public List<T> findAll() {
        return dao.findAll();
    }

    public boolean insert(T entity) {
        validate(entity); // throws BusinessException if invalid
        return dao.insert(entity);
    }

    public boolean update(T entity) {
        validate(entity); // throws BusinessException if invalid
        return dao.update(entity);
    }

    public boolean delete(int id) {
        return dao.delete(id);
    }

    /**
     * Xác thực dữ liệu trước khi thêm/sửa.
     * Ném BusinessException nếu dữ liệu không hợp lệ.
     *
     * @param entity Entity cần xác thực
     * @throws com.hospital.exception.BusinessException nếu không hợp lệ
     */
    protected abstract void validate(T entity);
}
