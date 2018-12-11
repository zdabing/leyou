package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 〈一句话功能简述〉<br>
 *
 * @author Administrator
 * @create 2018/11/17
 * @since 1.0.0
 */
@Service
@Slf4j
//@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {

//    public static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg", "image/png", "image/bmb");

    @Autowired
    private UploadProperties properties;

    @Autowired
    private FastFileStorageClient storageClient;

    public String uploadImage(MultipartFile file) {
        try {
            // 校验文件类型
            String contentType = file.getContentType();
            if (!properties.getAllowTypes().contains(contentType)) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            // 检验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            // 上传到本地
//            File dest = new File("C:\\Work", file.getOriginalFilename());
//            file.transferTo(dest);
//            return "http://image.leyou.com/" + file.getOriginalFilename();
            // 上传到FastDFS
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(),".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(),file.getSize(),extension,null);
            return properties.getBaseUrl() + storePath.getFullPath();
        } catch (IOException e) {
            log.error("上传文件失败", e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }
}
