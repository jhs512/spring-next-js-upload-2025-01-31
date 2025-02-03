package com.ll.domain.post.genFile.dto;

import com.ll.domain.post.genFile.entity.PostGenFile;
import lombok.Getter;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;

@Getter
public class PostGenFileDto {
    @NonNull
    private long id;

    @NonNull
    private LocalDateTime createDate;

    @NonNull
    private LocalDateTime modifyDate;

    @NonNull
    private long postId;

    @NonNull
    private String fileName;

    @NonNull
    private String typeCode;

    @NonNull
    private String fileExtTypeCode;

    @NonNull
    private String fileExtType2Code;

    @NonNull
    private long fileSize;

    @NonNull
    private long fileNo;

    @NonNull
    private String fileExt;

    @NonNull
    private String fileDir;

    @NonNull
    private String originFileName;

    public PostGenFileDto(PostGenFile postGenFile) {
        this.id = postGenFile.getId();
        this.createDate = postGenFile.getCreateDate();
        this.modifyDate = postGenFile.getModifyDate();
        this.postId = postGenFile.getPost().getId();
        this.fileName = postGenFile.getFileName();
        this.typeCode = postGenFile.getTypeCode();
        this.fileExtTypeCode = postGenFile.getFileExtTypeCode();
        this.fileExtType2Code = postGenFile.getFileExtType2Code();
        this.fileSize = postGenFile.getFileSize();
        this.fileNo = postGenFile.getFileNo();
        this.fileExt = postGenFile.getFileExt();
        this.fileDir = postGenFile.getFileDir();
        this.originFileName = postGenFile.getOriginFileName();
    }
}
