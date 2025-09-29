package com.hospital.management.core.interfaces;

import java.util.List;

public interface Searchable<T> {
    List<T> search(String query) throws Exception;
}
