package fr.vpm.audiorss.db;

import java.text.ParseException;
import java.util.List;

/**
 * Created by vince on 20/10/14.
 */
public interface DbItem<T> {

    T readById(long id) throws ParseException;

    List<T> readAll() throws ParseException;

    T add(T item) throws ParseException;

    T update(T existingItem, T newItem) throws ParseException;

    void deleteById(long id);


}
