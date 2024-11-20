package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.record.ZipEntryData;

import java.util.List;

public abstract class SimpleNmnsServiceBase<T> extends NmnsServiceBase<T> {

    @Override
    public List<ZipEntryData> doProcess(String date, ProcessContext processContext) {
        List<T> dataList = findByDate(date);

        processData(dataList);

        return createZipEntries(dataList, date, processType.getTypeName());
    }

    protected abstract List<T> findByDate(String date);
}
