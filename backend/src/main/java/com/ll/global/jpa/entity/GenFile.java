package com.ll.global.jpa.entity;

import com.ll.global.app.AppConfig;
import com.ll.standard.util.Ut;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.File;
import java.util.UUID;

@Getter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@NoArgsConstructor
public class GenFile extends BaseTime {
    private String fileName;
    private String typeCode;
    private String fileExtTypeCode;
    private String fileExtType2Code;
    private int fileSize;
    private int fileNo;
    private String fileExt;
    private String fileDir;
    private String originFileName;

    public String getUrl() {
        return "/gen/" + getDirParents() + "/" + getFileName();
    }

    private String getOwnerModelName() {
        return getModelName().replace("GenFile", "");
    }

    private String getDirParents() {
        return getOwnerModelName() + "/" + getFileDir();
    }

    public String getDownloadUrl() {
        return "/genFile/download/" + getFileName();
    }

    public String getFilePath() {
        return AppConfig.getGenFileDirPath() + "/" + getDirParents() + "/" + getFileName();
    }

    public void deleteOnDisk() {
        String filePath = getFilePath();
        new File(filePath).delete();
    }

    public GenFile(String typeCode, int fileNo, String originFileName, String filePath) {
        this.typeCode = typeCode;
        this.fileNo = fileNo;
        this.originFileName = originFileName;

        fileDir = Ut.file.getDirName(filePath);
        fileExt = Ut.file.getExt(originFileName);
        fileName = UUID.randomUUID() + "." + fileExt;
        fileExtTypeCode = Ut.file.getFileExtTypeCodeFromFileExt(fileExt);
        fileExtType2Code = Ut.file.getFileExtType2CodeFromFileExt(fileExt);
        fileSize = (int) new File(filePath).length();
    }

    protected static String getCurrentDirPath() {
        return AppConfig.getGenFileDirPath();
    }
}
