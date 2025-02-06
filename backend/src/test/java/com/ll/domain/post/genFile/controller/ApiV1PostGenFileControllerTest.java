package com.ll.domain.post.genFile.controller;

import com.ll.domain.member.member.service.MemberService;
import com.ll.domain.post.genFile.entity.PostGenFile;
import com.ll.domain.post.post.service.PostService;
import com.ll.global.app.AppConfig;
import com.ll.standard.sampleResource.SampleResource;
import com.ll.standard.util.Ut;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiV1PostGenFileControllerTest {
    @Autowired
    private PostService postService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("다건 조회")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/9/genFiles")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostGenFileController.class))
                .andExpect(handler().methodName("items"))
                .andExpect(status().isOk());

        List<PostGenFile> postGenFiles = postService
                .findById(9).get().getGenFiles();

        for (int i = 0; i < postGenFiles.size(); i++) {
            PostGenFile postGenFile = postGenFiles.get(i);
            resultActions
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(postGenFile.getId()))
                    .andExpect(jsonPath("$[%d].createDate".formatted(i)).value(Matchers.startsWith(postGenFile.getCreateDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$[%d].modifyDate".formatted(i)).value(Matchers.startsWith(postGenFile.getModifyDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$[%d].postId".formatted(i)).value(postGenFile.getPost().getId()))
                    .andExpect(jsonPath("$[%d].typeCode".formatted(i)).value(postGenFile.getTypeCode().name()))
                    .andExpect(jsonPath("$[%d].fileExtTypeCode".formatted(i)).value(postGenFile.getFileExtTypeCode()))
                    .andExpect(jsonPath("$[%d].fileExtType2Code".formatted(i)).value(postGenFile.getFileExtType2Code()))
                    .andExpect(jsonPath("$[%d].fileSize".formatted(i)).value(postGenFile.getFileSize()))
                    .andExpect(jsonPath("$[%d].fileNo".formatted(i)).value(postGenFile.getFileNo()))
                    .andExpect(jsonPath("$[%d].fileExt".formatted(i)).value(postGenFile.getFileExt()))
                    .andExpect(jsonPath("$[%d].fileDateDir".formatted(i)).value(postGenFile.getFileDateDir()))
                    .andExpect(jsonPath("$[%d].originalFileName".formatted(i)).value(postGenFile.getOriginalFileName()))
                    .andExpect(jsonPath("$[%d].downloadUrl".formatted(i)).value(postGenFile.getDownloadUrl()))
                    .andExpect(jsonPath("$[%d].publicUrl".formatted(i)).value(postGenFile.getPublicUrl()))
                    .andExpect(jsonPath("$[%d].fileName".formatted(i)).value(postGenFile.getFileName()));
        }
    }

    @Test
    @DisplayName("새 파일 등록")
    @WithUserDetails("user4")
    void t2() throws Exception {
        String newFilePath = SampleResource.IMG_JPG_SAMPLE1.makeCopy();

        ResultActions resultActions = mvc
                .perform(
                        multipart("/api/v1/posts/9/genFiles/" + PostGenFile.TypeCode.attachment)
                                .file(new MockMultipartFile("files", SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName(), SampleResource.IMG_JPG_SAMPLE1.getContentType(), new FileInputStream(newFilePath)))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostGenFileController.class))
                .andExpect(handler().methodName("makeNewItems"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("1개의 파일이 생성되었습니다."))
                .andExpect(jsonPath("$.data[0].id").isNumber())
                .andExpect(jsonPath("$.data[0].createDate").isString())
                .andExpect(jsonPath("$.data[0].modifyDate").isString())
                .andExpect(jsonPath("$.data[0].postId").value(9))
                .andExpect(jsonPath("$.data[0].typeCode").value(PostGenFile.TypeCode.attachment.name()))
                .andExpect(jsonPath("$.data[0].fileExtTypeCode").value(SampleResource.IMG_JPG_SAMPLE1.getFileExtTypeCode()))
                .andExpect(jsonPath("$.data[0].fileExtType2Code").value(SampleResource.IMG_JPG_SAMPLE1.getFileExtType2Code()))
                .andExpect(jsonPath("$.data[0].fileSize").isNumber())
                .andExpect(jsonPath("$.data[0].fileNo").value(4))
                .andExpect(jsonPath("$.data[0].fileExt").value(SampleResource.IMG_JPG_SAMPLE1.getFileExt()))
                .andExpect(jsonPath("$.data[0].fileDateDir").isString())
                .andExpect(jsonPath("$.data[0].originalFileName").value(SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName()))
                .andExpect(jsonPath("$.data[0].downloadUrl").isString())
                .andExpect(jsonPath("$.data[0].publicUrl").isString())
                .andExpect(jsonPath("$.data[0].fileName").isString());

        Ut.file.rm(newFilePath);
    }

    @Test
    @DisplayName("단건 조회")
    void t3() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/9/genFiles/1")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostGenFileController.class))
                .andExpect(handler().methodName("item"))
                .andExpect(status().isOk());

        PostGenFile postGenFile = postService
                .findById(9).get().getGenFileById(1).get();

        resultActions
                .andExpect(jsonPath("$.id").value(postGenFile.getId()))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(postGenFile.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.modifyDate").value(Matchers.startsWith(postGenFile.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.postId").value(postGenFile.getPost().getId()))
                .andExpect(jsonPath("$.typeCode").value(postGenFile.getTypeCode().name()))
                .andExpect(jsonPath("$.fileExtTypeCode").value(postGenFile.getFileExtTypeCode()))
                .andExpect(jsonPath("$.fileExtType2Code").value(postGenFile.getFileExtType2Code()))
                .andExpect(jsonPath("$.fileSize").value(postGenFile.getFileSize()))
                .andExpect(jsonPath("$.fileNo").value(postGenFile.getFileNo()))
                .andExpect(jsonPath("$.fileExt").value(postGenFile.getFileExt()))
                .andExpect(jsonPath("$.fileDateDir").value(postGenFile.getFileDateDir()))
                .andExpect(jsonPath("$.originalFileName").value(postGenFile.getOriginalFileName()))
                .andExpect(jsonPath("$.downloadUrl").value(postGenFile.getDownloadUrl()))
                .andExpect(jsonPath("$.publicUrl").value(postGenFile.getPublicUrl()))
                .andExpect(jsonPath("$.fileName").value(postGenFile.getFileName()));
    }

    @Test
    @DisplayName("새 파일 등록(다건)")
    @WithUserDetails("user4")
    void t4() throws Exception {
        String newFilePath1 = SampleResource.IMG_JPG_SAMPLE1.makeCopy();
        String newFilePath2 = SampleResource.IMG_JPG_SAMPLE2.makeCopy();

        ResultActions resultActions = mvc
                .perform(
                        multipart("/api/v1/posts/9/genFiles/" + PostGenFile.TypeCode.attachment)
                                .file(new MockMultipartFile("files", SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName(), SampleResource.IMG_JPG_SAMPLE1.getContentType(), new FileInputStream(newFilePath1)))
                                .file(new MockMultipartFile("files", SampleResource.IMG_JPG_SAMPLE2.getOriginalFileName(), SampleResource.IMG_JPG_SAMPLE2.getContentType(), new FileInputStream(newFilePath2)))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostGenFileController.class))
                .andExpect(handler().methodName("makeNewItems"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("2개의 파일이 생성되었습니다."))
                .andExpect(jsonPath("$.data[0].id").isNumber())
                .andExpect(jsonPath("$.data[0].createDate").isString())
                .andExpect(jsonPath("$.data[0].modifyDate").isString())
                .andExpect(jsonPath("$.data[0].postId").value(9))
                .andExpect(jsonPath("$.data[0].typeCode").value(PostGenFile.TypeCode.attachment.name()))
                .andExpect(jsonPath("$.data[0].fileExtTypeCode").value(SampleResource.IMG_JPG_SAMPLE1.getFileExtTypeCode()))
                .andExpect(jsonPath("$.data[0].fileExtType2Code").value(SampleResource.IMG_JPG_SAMPLE1.getFileExtType2Code()))
                .andExpect(jsonPath("$.data[0].fileSize").isNumber())
                .andExpect(jsonPath("$.data[0].fileNo").value(4))
                .andExpect(jsonPath("$.data[0].fileExt").value(SampleResource.IMG_JPG_SAMPLE1.getFileExt()))
                .andExpect(jsonPath("$.data[0].fileDateDir").isString())
                .andExpect(jsonPath("$.data[0].originalFileName").value(SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName()))
                .andExpect(jsonPath("$.data[0].downloadUrl").isString())
                .andExpect(jsonPath("$.data[0].publicUrl").isString())
                .andExpect(jsonPath("$.data[0].fileName").isString())
                .andExpect(jsonPath("$.data[0].id").isNumber())
                .andExpect(jsonPath("$.data[1].createDate").isString())
                .andExpect(jsonPath("$.data[1].modifyDate").isString())
                .andExpect(jsonPath("$.data[1].postId").value(9))
                .andExpect(jsonPath("$.data[1].typeCode").value(PostGenFile.TypeCode.attachment.name()))
                .andExpect(jsonPath("$.data[1].fileExtTypeCode").value(SampleResource.IMG_JPG_SAMPLE2.getFileExtTypeCode()))
                .andExpect(jsonPath("$.data[1].fileExtType2Code").value(SampleResource.IMG_JPG_SAMPLE2.getFileExtType2Code()))
                .andExpect(jsonPath("$.data[1].fileSize").isNumber())
                .andExpect(jsonPath("$.data[1].fileNo").value(5))
                .andExpect(jsonPath("$.data[1].fileExt").value(SampleResource.IMG_JPG_SAMPLE2.getFileExt()))
                .andExpect(jsonPath("$.data[1].fileDateDir").isString())
                .andExpect(jsonPath("$.data[1].originalFileName").value(SampleResource.IMG_JPG_SAMPLE2.getOriginalFileName()))
                .andExpect(jsonPath("$.data[1].downloadUrl").isString())
                .andExpect(jsonPath("$.data[1].publicUrl").isString())
                .andExpect(jsonPath("$.data[1].fileName").isString());

        Ut.file.rm(newFilePath1);
        Ut.file.rm(newFilePath2);
    }

    @Test
    @DisplayName("파일 삭제")
    @WithUserDetails("user4")
    void t5() throws Exception {
        PostGenFile postGenFile = postService
                .findById(9).get().getGenFileById(1).get();

        String originFilePath = postGenFile.getFilePath();
        String copyFilePath = AppConfig.getTempDirPath() + "/copy_" + postGenFile.getFileName();
        Ut.file.copy(originFilePath, copyFilePath);

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/9/genFiles/1")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostGenFileController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("1번 파일이 삭제되었습니다."));

        Ut.file.mv(copyFilePath, originFilePath);
    }

    @Test
    @DisplayName("파일 수정, with id")
    @WithUserDetails("user4")
    void t6() throws Exception {
        PostGenFile postGenFile = postService
                .findById(9).get().getGenFileById(1).get();

        String originFilePath = postGenFile.getFilePath();
        String copyFilePath = AppConfig.getTempDirPath() + "/copy_" + postGenFile.getFileName();
        Ut.file.copy(originFilePath, copyFilePath);

        String newFilePath = SampleResource.IMG_JPG_SAMPLE1.makeCopy();

        ResultActions resultActions = mvc
                .perform(
                        multipart("/api/v1/posts/9/genFiles/1")
                                .file(new MockMultipartFile(
                                        "file",
                                        SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName(),
                                        SampleResource.IMG_JPG_SAMPLE1.getContentType(),
                                        new FileInputStream(newFilePath))
                                )
                                .with(request -> {
                                    request.setMethod("PUT");
                                    return request;
                                })

                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostGenFileController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 파일이 수정되었습니다.".formatted(postGenFile.getId())))
                .andExpect(jsonPath("$.data.id").value(postGenFile.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(postGenFile.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(postGenFile.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.postId").value(postGenFile.getPost().getId()))
                .andExpect(jsonPath("$.data.typeCode").value(postGenFile.getTypeCode().name()))
                .andExpect(jsonPath("$.data.fileExtTypeCode").value(SampleResource.IMG_JPG_SAMPLE1.getFileExtTypeCode()))
                .andExpect(jsonPath("$.data.fileExtType2Code").value(SampleResource.IMG_JPG_SAMPLE1.getFileExtType2Code()))
                .andExpect(jsonPath("$.data.fileSize").value(postGenFile.getFileSize()))
                .andExpect(jsonPath("$.data.fileNo").value(postGenFile.getFileNo()))
                .andExpect(jsonPath("$.data.fileExt").value(SampleResource.IMG_JPG_SAMPLE1.getFileExt()))
                .andExpect(jsonPath("$.data.fileDateDir").value(postGenFile.getFileDateDir()))
                .andExpect(jsonPath("$.data.originalFileName").value(SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName()))
                .andExpect(jsonPath("$.data.downloadUrl").value(postGenFile.getDownloadUrl()))
                .andExpect(jsonPath("$.data.publicUrl").value(postGenFile.getPublicUrl()))
                .andExpect(jsonPath("$.data.fileName").value(postGenFile.getFileName()));

        Ut.file.mv(copyFilePath, originFilePath);
    }

    @Test
    @DisplayName("파일 수정, with typeCode And fileNo")
    @WithUserDetails("user4")
    void t7() throws Exception {
        PostGenFile postGenFile = postService
                .findById(9).get().getGenFileByTypeCodeAndFileNo(PostGenFile.TypeCode.thumbnail, 1).get();

        String originFilePath = postGenFile.getFilePath();
        String copyFilePath = AppConfig.getTempDirPath() + "/copy_" + postGenFile.getFileName();
        Ut.file.copy(originFilePath, copyFilePath);

        String newFilePath = SampleResource.IMG_JPG_SAMPLE1.makeCopy();

        ResultActions resultActions = mvc
                .perform(
                        multipart("/api/v1/posts/9/genFiles/" + PostGenFile.TypeCode.thumbnail + "/1")
                                .file(new MockMultipartFile(
                                        "file",
                                        SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName(),
                                        SampleResource.IMG_JPG_SAMPLE1.getContentType(),
                                        new FileInputStream(newFilePath))
                                )
                                .with(request -> {
                                    request.setMethod("PUT");
                                    return request;
                                })

                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostGenFileController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 파일이 수정되었습니다.".formatted(postGenFile.getId())))
                .andExpect(jsonPath("$.data.id").value(postGenFile.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(postGenFile.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(postGenFile.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.postId").value(postGenFile.getPost().getId()))
                .andExpect(jsonPath("$.data.typeCode").value(postGenFile.getTypeCode().name()))
                .andExpect(jsonPath("$.data.fileExtTypeCode").value(SampleResource.IMG_JPG_SAMPLE1.getFileExtTypeCode()))
                .andExpect(jsonPath("$.data.fileExtType2Code").value(SampleResource.IMG_JPG_SAMPLE1.getFileExtType2Code()))
                .andExpect(jsonPath("$.data.fileSize").value(postGenFile.getFileSize()))
                .andExpect(jsonPath("$.data.fileNo").value(postGenFile.getFileNo()))
                .andExpect(jsonPath("$.data.fileExt").value(SampleResource.IMG_JPG_SAMPLE1.getFileExt()))
                .andExpect(jsonPath("$.data.fileDateDir").value(postGenFile.getFileDateDir()))
                .andExpect(jsonPath("$.data.originalFileName").value(SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName()))
                .andExpect(jsonPath("$.data.downloadUrl").value(postGenFile.getDownloadUrl()))
                .andExpect(jsonPath("$.data.publicUrl").value(postGenFile.getPublicUrl()))
                .andExpect(jsonPath("$.data.fileName").value(postGenFile.getFileName()));

        Ut.file.mv(copyFilePath, originFilePath);
    }
}
