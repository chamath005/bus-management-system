package com.busapp.storage;

import com.busapp.exception.NotFoundException;
import com.busapp.model.Identifiable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/** Generic in-memory collection for any Identifiable entity. */
public class Repository<T extends Identifiable> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String entityName;
    private final String prefix;
    private final Map<String, T> items = new LinkedHashMap<>();
    private int counter = 0;

    public Repository(String entityName, String prefix) { this.entityName = entityName; this.prefix = prefix; }

    public String nextId() { return String.format("%s%03d", prefix, ++counter); }
    public void add(T item) { items.put(item.getId(), item); }
    public Optional<T> find(String id) { return Optional.ofNullable(id == null ? null : items.get(id.trim().toUpperCase())); }

    public T get(String id) throws NotFoundException {
        return find(id).orElseThrow(() -> new NotFoundException(entityName, id));
    }
    public List<T> all() { return new ArrayList<>(items.values()); }
    public List<T> filter(Predicate<T> test) {
        List<T> out = new ArrayList<>();
        for (T t : items.values()) if (test.test(t)) out.add(t);
        return out;
    }
    public boolean isEmpty() { return items.isEmpty(); }
}
