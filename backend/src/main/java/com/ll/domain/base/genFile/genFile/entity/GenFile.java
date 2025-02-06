package com.ll.domain.base.genFile.genFile.entity;

import com.ll.global.app.AppConfig;
import com.ll.global.jpa.entity.BaseTime;
import com.ll.standard.util.Ut;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class GenFile extends BaseTime {
    private int fileNo;
    private String originalFileName;
    private String metadata;
    private String fileDateDir;
    private String fileExt;
    private String fileExtTypeCode;
    private String fileExtType2Code;
    private String fileName;
    private int fileSize;

    public String getFilePath() {
        return AppConfig.getGenFileDirPath() + "/" + getModelName() + "/" + getTypeCodeAsStr() + "/" + fileDateDir + "/" + fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (getId() != null) return super.equals(o);

        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GenFile that = (GenFile) o;
        return fileNo == that.getFileNo() && Objects.equals(getTypeCodeAsStr(), that.getTypeCodeAsStr());
    }

    @Override
    public int hashCode() {
        if (getId() != null) return super.hashCode();

        return Objects.hash(super.hashCode(), getTypeCodeAsStr(), fileNo);
    }

    private String getOwnerModelName() {
        return this.getModelName().replace("GenFile", "");
    }

    public String getDownloadUrl() {
        return AppConfig.getSiteBackUrl() + "/" + getOwnerModelName() + "/genFile/download/" + getOwnerModelId() + "/" + fileName;
    }

    public String getPublicUrl() {
        String publicUrl = AppConfig.getSiteBackUrl() + "/gen/" + getModelName() + "/" + getTypeCodeAsStr() + "/" + fileDateDir + "/" + fileName + "?modifyDate=" + getModifyDate();

        if (Ut.str.isNotBlank(metadata)) {
            publicUrl += "&" + metadata;
        }

        return publicUrl;
    }

    abstract protected long getOwnerModelId();

    abstract protected String getTypeCodeAsStr();
}
