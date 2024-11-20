package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.record.ZipEntryData;

import java.util.List;

public interface NmnsService<T> {
    List<ZipEntryData> doProcess(String date, ProcessContext processContext);
    List<T> findByDate(String date, ProcessContext processContext);
    void processData(String date, List<T> dataList);
}
