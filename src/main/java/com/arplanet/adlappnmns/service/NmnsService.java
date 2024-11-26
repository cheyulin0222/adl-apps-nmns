package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.record.ZipEntryData;

import java.util.List;
import java.util.zip.ZipOutputStream;

public interface NmnsService<T> {
    void doProcess(String date, ProcessContext processContext, ZipOutputStream zipStream);
    List<T> findByDate(String date, ProcessContext processContext);
    List<T> processData(String date, List<T> dataList);
//    void writeToZip(String date, ProcessContext processContext, ZipOutputStream zipStream);
}
