package com.ll.domain.post.genFile.controller;

import com.ll.domain.member.member.entity.Member;
import com.ll.domain.post.genFile.dto.PostGenFileDto;
import com.ll.domain.post.genFile.entity.PostGenFile;
import com.ll.domain.post.post.entity.Post;
import com.ll.domain.post.post.service.PostService;
import com.ll.global.exceptions.ServiceException;
import com.ll.global.rq.Rq;
import com.ll.global.rsData.RsData;
import com.ll.standard.util.Ut;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/api/v1/posts/{postId}/genFiles")
@RequiredArgsConstructor
@Tag(name = "ApiV1PostGenFileController", description = "API 글 파일 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
public class ApiV1PostGenFileController {
    @Autowired
    @Lazy
    private ApiV1PostGenFileController self;
    private final PostService postService;
    private final Rq rq;

    @PostMapping(value = "/{typeCode}", consumes = MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "등록")
    public RsData<PostGenFileDto> write(
            @PathVariable long postId,
            @PathVariable String typeCode,
            @NonNull @RequestParam("file") @Schema(type = "string", format = "binary") MultipartFile file
    ) {
        Member actor = rq.getActor();

        Post post = postService.findById(postId).orElseThrow(
                () -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다.".formatted(postId))
        );

        post.checkActorCanUploadGenFile(actor);

        String filePath = Ut.file.toFile(file, PostGenFile.getCurrentDirPath());

        PostGenFile postGenFile = self._write(post, typeCode, filePath);

        Ut.file.mv(filePath, postGenFile.getFilePath());

        return new RsData<>(
                "201-1",
                "%d번 파일이 생성되었습니다.".formatted(postGenFile.getId()),
                new PostGenFileDto(postGenFile)
        );
    }

    @Transactional
    public PostGenFile _write(Post post, String typeCode, String filePath) {
        PostGenFile postGenFile = post.addGenFile(
                typeCode,
                filePath
        );

        return postGenFile;
    }
}
