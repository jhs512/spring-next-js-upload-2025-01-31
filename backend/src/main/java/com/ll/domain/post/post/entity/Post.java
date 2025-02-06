package com.ll.domain.post.post.entity;

import com.ll.domain.member.member.entity.Member;
import com.ll.domain.post.comment.entity.PostComment;
import com.ll.domain.post.genFile.entity.PostGenFile;
import com.ll.global.exceptions.ServiceException;
import com.ll.global.jpa.entity.BaseTime;
import com.ll.global.rsData.RsData;
import com.ll.standard.base.Empty;
import com.ll.standard.util.Ut;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Post extends BaseTime {
    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;

    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "post", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<PostComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<PostGenFile> genFiles = new ArrayList<>();

    private boolean published;

    private boolean listed;

    public PostComment addComment(Member author, String content) {
        PostComment comment = PostComment.builder()
                .post(this)
                .author(author)
                .content(content)
                .build();

        comments.add(comment);

        return comment;
    }

    public List<PostComment> getCommentsByOrderByIdDesc() {
        return comments.reversed();
    }

    public Optional<PostComment> getCommentById(long commentId) {
        return comments.stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst();
    }

    public void removeComment(PostComment postComment) {
        comments.remove(postComment);
    }


    public RsData<Empty> getCheckActorCanDeleteRs(Member actor) {
        if (actor == null) return new RsData<>("401-1", "로그인 후 이용해주세요.");

        if (actor.isAdmin()) return RsData.OK;

        if (actor.equals(author)) return RsData.OK;

        return new RsData<>("403-1", "작성자만 글을 삭제할 수 있습니다.");
    }

    public void checkActorCanDelete(Member actor) {
        Optional.of(
                        getCheckActorCanDeleteRs(actor)
                )
                .filter(RsData::isFail)
                .ifPresent(rsData -> {
                    throw new ServiceException(rsData.getResultCode(), rsData.getMsg());
                });
    }


    public RsData<Empty> getCheckActorCanModifyRs(Member actor) {
        if (actor == null) return new RsData<>("401-1", "로그인 후 이용해주세요.");

        if (actor.equals(author)) return RsData.OK;

        return new RsData<>("403-1", "작성자만 글을 수정할 수 있습니다.");
    }

    public void checkActorCanModify(Member actor) {
        Optional.of(
                        getCheckActorCanModifyRs(actor)
                )
                .filter(RsData::isFail)
                .ifPresent(rsData -> {
                    throw new ServiceException(rsData.getResultCode(), rsData.getMsg());
                });
    }


    public RsData<Empty> getCheckActorCanReadRs(Member actor) {
        if (actor == null) return new RsData<>("401-1", "로그인 후 이용해주세요.");

        if (actor.isAdmin()) return RsData.OK;

        if (actor.equals(author)) return RsData.OK;

        return new RsData<>("403-1", "비공개글은 작성자만 볼 수 있습니다.");
    }

    public void checkActorCanRead(Member actor) {
        Optional.of(
                        getCheckActorCanReadRs(actor)
                )
                .filter(RsData::isFail)
                .ifPresent(rsData -> {
                    throw new ServiceException(rsData.getResultCode(), rsData.getMsg());
                });
    }

    public PostGenFile addGenFile(PostGenFile.TypeCode typeCode, String filePath) {
        return addGenFile(typeCode, 0, filePath);
    }

    private PostGenFile addGenFile(PostGenFile.TypeCode typeCode, int fileNo, String filePath) {
        String originalFileName = Ut.file.getOriginalFileName(filePath);
        String fileExt = Ut.file.getFileExt(filePath);
        String fileExtTypeCode = Ut.file.getFileExtTypeCodeFromFileExt(fileExt);
        String fileExtType2Code = Ut.file.getFileExtType2CodeFromFileExt(fileExt);

        Map<String, Object> metadata = Ut.file.getMetadata(filePath);

        String metadataStr = metadata
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + "-" + entry.getValue())
                .collect(Collectors.joining(";"));

        String fileName = UUID.randomUUID() + "." + fileExt;
        int fileSize = Ut.file.getFileSize(filePath);
        fileNo = fileNo == 0 ? getNextGenFileNo(typeCode) : fileNo;

        PostGenFile genFile = PostGenFile.builder()
                .post(this)
                .typeCode(typeCode)
                .fileNo(fileNo)
                .originalFileName(originalFileName)
                .metadata(metadataStr)
                .fileDateDir(Ut.date.getCurrentDateFormatted("yyyy_MM_dd"))
                .fileExt(fileExt)
                .fileExtTypeCode(fileExtTypeCode)
                .fileExtType2Code(fileExtType2Code)
                .fileName(fileName)
                .fileSize(fileSize)
                .build();

        genFiles.add(genFile);

        Ut.file.mv(filePath, genFile.getFilePath());

        return genFile;
    }

    private int getNextGenFileNo(PostGenFile.TypeCode typeCode) {
        return genFiles.stream()
                .filter(genFile -> genFile.getTypeCode().equals(typeCode))
                .mapToInt(PostGenFile::getFileNo)
                .max()
                .orElse(0) + 1;
    }

    public Optional<PostGenFile> getGenFileById(long id) {
        return genFiles.stream()
                .filter(genFile -> genFile.getId().equals(id))
                .findFirst();
    }

    public Optional<PostGenFile> getGenFileByTypeCodeAndFileNo(PostGenFile.TypeCode typeCode, int fileNo) {
        return genFiles.stream()
                .filter(genFile -> genFile.getTypeCode().equals(typeCode))
                .filter(genFile -> genFile.getFileNo() == fileNo)
                .findFirst();
    }

    public void deleteGenFile(PostGenFile.TypeCode typeCode, int fileNo) {
        getGenFileByTypeCodeAndFileNo(typeCode, fileNo)
                .ifPresent(this::deleteGenFile);
    }

    public void deleteGenFile(PostGenFile postGenFile) {
        Ut.file.rm(postGenFile.getFilePath());
        genFiles.remove(postGenFile);
    }

    public void modifyGenFile(PostGenFile.TypeCode typeCode, int fileNo, String filePath) {
        getGenFileByTypeCodeAndFileNo(
                typeCode,
                fileNo
        )
                .ifPresent(genFile -> {
                    Ut.file.rm(genFile.getFilePath());

                    String originalFileName = Ut.file.getOriginalFileName(filePath);
                    String fileExt = Ut.file.getFileExt(filePath);
                    String fileExtTypeCode = Ut.file.getFileExtTypeCodeFromFileExt(fileExt);
                    String fileExtType2Code = Ut.file.getFileExtType2CodeFromFileExt(fileExt);

                    Map<String, Object> metadata = Ut.file.getMetadata(filePath);

                    String metadataStr = metadata
                            .entrySet()
                            .stream()
                            .map(entry -> entry.getKey() + "-" + entry.getValue())
                            .collect(Collectors.joining(";"));

                    String fileName = UUID.randomUUID() + "." + fileExt;
                    int fileSize = Ut.file.getFileSize(filePath);

                    genFile.setOriginalFileName(originalFileName);
                    genFile.setMetadata(metadataStr);
                    genFile.setFileDateDir(Ut.date.getCurrentDateFormatted("yyyy_MM_dd"));
                    genFile.setFileExt(fileExt);
                    genFile.setFileExtTypeCode(fileExtTypeCode);
                    genFile.setFileExtType2Code(fileExtType2Code);
                    genFile.setFileName(fileName);
                    genFile.setFileSize(fileSize);

                    Ut.file.mv(filePath, genFile.getFilePath());
                });
    }

    public void putGenFile(PostGenFile.TypeCode typeCode, int fileNo, String filePath) {
        Optional<PostGenFile> opPostGenFile = getGenFileByTypeCodeAndFileNo(
                typeCode,
                fileNo
        );

        if (opPostGenFile.isPresent()) {
            modifyGenFile(typeCode, fileNo, filePath);
        } else {
            addGenFile(typeCode, fileNo, filePath);
        }
    }

    public void checkActorCanMakeNewGenFile(Member actor) {
        Optional.of(
                        getCheckActorCanMakeNewGenFileRs(actor)
                )
                .filter(RsData::isFail)
                .ifPresent(rsData -> {
                    throw new ServiceException(rsData.getResultCode(), rsData.getMsg());
                });
    }

    public RsData<Empty> getCheckActorCanMakeNewGenFileRs(Member actor) {
        if (actor == null) return new RsData<>("401-1", "로그인 후 이용해주세요.");

        if (actor.equals(author)) return RsData.OK;

        return new RsData<>("403-1", "작성자만 파일을 업로드할 수 있습니다.");
    }

    public boolean isTemp() {
        return !published && "임시글".equals(title);
    }
}
