package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.enums.ProcessType;
import com.arplanet.adlappnmns.log.LogContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.S3Repository;
import com.arplanet.adlappnmns.utils.DataConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.arplanet.adlappnmns.utils.ServiceUtil.APPLICATION_JSON;

public abstract class NmnsServiceBase<T> implements NmnsService<T> {

    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected S3Repository s3Repository;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Logger logger;
    @Autowired
    private DataConverter dataConverter;
    @Autowired
    private LogContext logContext;

    protected String beanName;

    @Value("${aws.s3.write.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.write.folder}")
    private String destinationFolder;

    @PostConstruct
    private void init() {
        this.beanName = applicationContext.getBeanNamesForType(this.getClass())[0];
    }

    @Override
    public void processData(List<T> dataList) {

        ProcessType type = getProcessType();
        boolean enableS3Backup = type.isEnableS3Backup();

        String date = logContext.getCurrentDate();

        if (enableS3Backup) {
            dataList.parallelStream().forEach(data -> {
                logContext.setCurrentDate(date);
                Map<String, Object> dataMap = dataConverter.convertToMap(data);
                try {
                    validateData(data);

                    saveToS3(data);
                } catch (NullPointerException e) {
                    logger.error(type.getTypeName() + "資料驗證失敗", e, dataMap);
                    throw new RuntimeException(e);
                } catch (S3Exception e) {
                    logger.error("上傳S3失敗", e, dataMap);
                    throw new RuntimeException(e);
                } catch (JsonProcessingException e) {
                    logger.error("資料轉換JSON字串失敗", e, dataMap);
                    throw new RuntimeException(e);
                } catch (UnsupportedOperationException e) {
                    logger.error("上傳到S3的Service沒有Override getId()", e, dataMap);
                    throw new RuntimeException(e);
                }
            });
        } else {
            dataList.parallelStream().forEach(data -> {
                logContext.setCurrentDate(date);
                Map<String, Object> dataMap = dataConverter.convertToMap(data);
                try {
                    validateData(data);
                } catch (NullPointerException e) {
                    logger.error("資料驗證失敗", e, dataMap);
                    throw new RuntimeException(e);
                }
            });
        }
    }

    protected abstract void validateData(T data);

    private void saveToS3(T data) throws JsonProcessingException {
        String jsonContent = objectMapper.writeValueAsString(data);
        s3Repository.putFile(bucketName, generatePath(data), APPLICATION_JSON, jsonContent.getBytes(StandardCharsets.UTF_8));
    }

    protected String generatePath(T data) {
        // 產生S3路徑
        return destinationFolder + getPathType() + "/" + getId(data) + ".json";
    }

    protected String getPathType() {
        return ProcessType.getByNmnsService(beanName).getPathType();
    }
    protected String getTypeName() { return ProcessType.getByNmnsService(beanName).getTypeName();}

    protected String getId(T data) {
        throw new UnsupportedOperationException("getId is not implemented for this service");
    }

    private ProcessType getProcessType() {
        return ProcessType.getByNmnsService(this.beanName);
    }

}
