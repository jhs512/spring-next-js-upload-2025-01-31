package com.ll.domain.post.genFile.controller;

import com.ll.domain.member.member.entity.Member;
import com.ll.domain.post.genFile.dto.PostGenFileDto;
import com.ll.domain.post.genFile.entity.PostGenFile;
import com.ll.domain.post.post.entity.Post;
import com.ll.domain.post.post.service.PostService;
import com.ll.global.app.AppConfig;
import com.ll.global.exceptions.ServiceException;
import com.ll.global.rq.Rq;
import com.ll.global.rsData.RsData;
import com.ll.standard.base.Empty;
import com.ll.standard.util.Ut;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/api/v1/posts/{postId}/genFiles")
@RequiredArgsConstructor
@Tag(name = "ApiV1PostGenFileController", description = "API 글 파일 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
public class ApiV1PostGenFileController {
    private final PostService postService;
    private final ApplicationEventPublisher eventPublisher;
    private final Rq rq;

    @PostMapping(value = "/{typeCode}", consumes = MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "다건등록")
    @Transactional
    public RsData<List<PostGenFileDto>> makeNewItems(
            @PathVariable long postId,
            @PathVariable PostGenFile.TypeCode typeCode,
            @NonNull @RequestPart("files") MultipartFile[] files
    ) {
        Member actor = rq.getActor();

        Post post = postService.findById(postId).orElseThrow(
                () -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다.".formatted(postId))
        );

        post.checkActorCanMakeNewGenFile(actor);

        List<PostGenFile> postGenFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String filePath = Ut.file.toFile(file, AppConfig.getTempDirPath());

            postGenFiles.add(
                    post.addGenFile(
                            typeCode,
                            filePath
                    ));
        }

        postService.flush();

        return new RsData<>(
                "201-1",
                "%d개의 파일이 생성되었습니다.".formatted(postGenFiles.size()),
                postGenFiles.stream().map(PostGenFileDto::new).toList()
        );
    }


    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "다건조회")
    public List<PostGenFileDto> items(
            @PathVariable long postId
    ) {
        Post post = postService.findById(postId).orElseThrow(
                () -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다.".formatted(postId))
        );

        return post
                .getGenFiles()
                .stream()
                .map(PostGenFileDto::new)
                .toList();
    }


    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "단건조회")
    public PostGenFileDto item(
            @PathVariable long postId,
            @PathVariable long id
    ) {
        Post post = postService.findById(postId).orElseThrow(
                () -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다.".formatted(postId))
        );

        PostGenFile postGenFile = post.getGenFileById(id).orElseThrow(
                () -> new ServiceException("404-2", "%d번 파일은 존재하지 않습니다.".formatted(id))
        );

        return new PostGenFileDto(postGenFile);
    }


    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "삭제")
    public RsData<Empty> delete(
            @PathVariable long postId,
            @PathVariable long id
    ) {
        Post post = postService.findById(postId).orElseThrow(
                () -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다.".formatted(postId))
        );

        PostGenFile postGenFile = post.getGenFileById(id).orElseThrow(
                () -> new ServiceException("404-2", "%d번 파일은 존재하지 않습니다.".formatted(id))
        );

        post.deleteGenFile(postGenFile);

        return new RsData<>(
                "200-1",
                "%d번 파일이 삭제되었습니다.".formatted(id)
        );
    }


    @PutMapping(value = "/{id}", consumes = MULTIPART_FORM_DATA_VALUE)
    @Transactional
    @Operation(summary = "수정")
    public RsData<PostGenFileDto> modify(
            @PathVariable long postId,
            @PathVariable long id,
            @NonNull @RequestPart("file") MultipartFile file
    ) {
        Post post = postService.findById(postId).orElseThrow(
                () -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다.".formatted(postId))
        );

        PostGenFile postGenFile = post.getGenFileById(id).orElseThrow(
                () -> new ServiceException("404-2", "%d번 파일은 존재하지 않습니다.".formatted(id))
        );

        String filePath = Ut.file.toFile(file, AppConfig.getTempDirPath());

        post.modifyGenFile(postGenFile, filePath);

        return new RsData<>(
                "200-1",
                "%d번 파일이 수정되었습니다.".formatted(id),
                new PostGenFileDto(postGenFile)
        );
    }


    @PutMapping(value = "/{typeCode}/{fileNo}", consumes = MULTIPART_FORM_DATA_VALUE)
    @Transactional
    @Operation(summary = "수정")
    public RsData<PostGenFileDto> modify(
            @PathVariable long postId,
            @PathVariable PostGenFile.TypeCode typeCode,
            @PathVariable int fileNo,
            @NonNull @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "") String metaStr
    ) {
        if (typeCode == PostGenFile.TypeCode.thumbnail && fileNo > 1) {
            throw new ServiceException("400-1", "썸네일은 1개만 등록할 수 있습니다.");
        }

        Post post = postService.findById(postId).orElseThrow(
                () -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다.".formatted(postId))
        );

        String filePath = Ut.file.toFile(
                file,
                AppConfig.getTempDirPath(),
                Ut.str.isNotBlank(metaStr) ? metaStr + Ut.file.META_STR_SEPARATOR : ""
        );

        if (typeCode == PostGenFile.TypeCode.thumbnail && !Ut.file.getFileExtTypeCodeFromFilePath(filePath).equals("img")) {
            throw new ServiceException("400-2", "썸네일은 이미지 파일만 등록할 수 있습니다.");
        }

        PostGenFile postGenFile = post.putGenFile(typeCode, fileNo, filePath);

        if (typeCode == PostGenFile.TypeCode.thumbnail) {
            post.setThumbnailGenFile(postGenFile);
        }

        postService.flush();

        return new RsData<>(
                "200-1",
                "%d번 파일이 수정되었습니다.".formatted(postGenFile.getId()),
                new PostGenFileDto(postGenFile)
        );
    }
}