package com.hospital.dao;

import java.util.List;

/**
 * Interface DAO cơ sở — định nghĩa các thao tác CRUD chung.
 * Base DAO interface — defines common CRUD operations.
 *
 * @param <T> Kiểu entity
 */
public interface BaseDAO<T> {
    T findById(int id);

    List<T> findAll();

    boolean insert(T entity);

    boolean update(T entity);

    boolean delete(int id);
}
