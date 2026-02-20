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
        if (!validate(entity)) {
            return false;
        }
        return dao.insert(entity);
    }

    public boolean update(T entity) {
        if (!validate(entity)) {
            return false;
        }
        return dao.update(entity);
    }

    public boolean delete(int id) {
        return dao.delete(id);
    }

    /**
     * Xác thực dữ liệu trước khi thêm/sửa.
     * Validate data before insert/update.
     *
     * @param entity Entity cần xác thực
     * @return true nếu hợp lệ
     */
    protected abstract boolean validate(T entity);
}
