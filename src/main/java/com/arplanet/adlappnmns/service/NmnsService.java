package com.arplanet.adlappnmns.service;

import java.util.List;

public interface NmnsService<T> {
    List<T> findByDate(String date);
    void processData(List<T> dataList);
}
