package kz.av.dao;

import kz.av.entity.Entity;

import java.util.List;

public interface Dao<T extends Entity> {

    public List<T> getAll() throws DaoException;
}
