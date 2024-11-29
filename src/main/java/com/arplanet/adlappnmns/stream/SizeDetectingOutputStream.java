package com.arplanet.adlappnmns.stream;

import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Getter
@Setter
public class SizeDetectingOutputStream extends OutputStream {
    private static final int MULTIPART_THRESHOLD = 5 * 1024 * 1024; // 5MB
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();  // 直接初始化
    private boolean exceedThreshold = false;
    private final byte[] oneByte = new byte[1];
    private ExceedThresholdCallback callback;
    private OutputStream redirectStream;

    public interface ExceedThresholdCallback {
        OutputStream onExceedThreshold();
    }


    @Override
    public void write(int b) throws IOException {
        if (!exceedThreshold) {
            oneByte[0] = (byte) b;
            write(oneByte, 0, 1);
        } else if (redirectStream != null) {
            redirectStream.write(b);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (!exceedThreshold) {
            if (buffer.size() + len > MULTIPART_THRESHOLD) {
                exceedThreshold = true;
                OutputStream newStream = callback.onExceedThreshold();
                // 1. 把之前的資料寫到新 stream
                newStream.write(buffer.toByteArray());
                // 2. 把當前的資料寫到新 stream
                newStream.write(b, off, len);
                // 3. 保存這個 stream 供後續使用
                this.redirectStream = newStream;
            } else {
                buffer.write(b, off, len);
            }
        } else if (redirectStream != null) {
            redirectStream.write(b, off, len);
        }
    }

    public byte[] getBuffer() {
        return buffer.toByteArray();  // 需要這個方法來取得 byte array
    }
}
