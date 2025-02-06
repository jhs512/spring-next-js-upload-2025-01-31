package com.ll.global.initData;

import com.ll.domain.member.member.entity.Member;
import com.ll.domain.member.member.service.MemberService;
import com.ll.domain.post.genFile.entity.PostGenFile;
import com.ll.domain.post.post.entity.Post;
import com.ll.domain.post.post.service.PostService;
import com.ll.global.app.AppConfig;
import com.ll.global.app.CustomConfigProperties;
import com.ll.standard.sampleResource.SampleResource;
import com.ll.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    private final CustomConfigProperties customConfigProperties;
    private final MemberService memberService;
    private final PostService postService;
    @Autowired
    @Lazy
    private BaseInitData self;

    @Bean
    public ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.work1();
            self.work2();
        };
    }

    @Transactional
    public void work1() {
        if (memberService.count() > 0) return;

        if (AppConfig.isTest()) {
            Ut.file.rm(AppConfig.getGenFileDirPath());
        }

        Member memberSystem = memberService.join("system", "1234", "시스템", "");
        if (AppConfig.isNotProd()) memberSystem.setApiKey("system");

        Member memberAdmin = memberService.join("admin", "1234", "관리자", "");
        if (AppConfig.isNotProd()) memberAdmin.setApiKey("admin");

        Member memberUser1 = memberService.join("user1", "1234", "유저1", "");
        if (AppConfig.isNotProd()) memberUser1.setApiKey("user1");

        Member memberUser2 = memberService.join("user2", "1234", "유저2", "");
        if (AppConfig.isNotProd()) memberUser2.setApiKey("user2");

        Member memberUser3 = memberService.join("user3", "1234", "유저3", "");
        if (AppConfig.isNotProd()) memberUser3.setApiKey("user3");

        Member memberUser4 = memberService.join("user4", "1234", "유저4", "");
        if (AppConfig.isNotProd()) memberUser4.setApiKey("user4");

        Member memberUser5 = memberService.join("user5", "1234", "유저5", "");
        if (AppConfig.isNotProd()) memberUser5.setApiKey("user5");

        Member memberUser6 = memberService.join("user6", "1234", "유저6", "");
        if (AppConfig.isNotProd()) memberUser6.setApiKey("user6");

        for (var notProdMember : customConfigProperties.getNotProdMembers()) {
            var member = memberService.join(
                    notProdMember.username(),
                    "",
                    notProdMember.nickname(),
                    notProdMember.profileImgUrl()
            );

            if (AppConfig.isNotProd()) member.setApiKey(notProdMember.apiKey());
        }
    }

    @Transactional
    public void work2() {
        if (postService.count() > 0) return;

        Member memberUser1 = memberService.findByUsername("user1").get();
        Member memberUser2 = memberService.findByUsername("user2").get();
        Member memberUser3 = memberService.findByUsername("user3").get();
        Member memberUser4 = memberService.findByUsername("user4").get();
        Member memberUser5 = memberService.findByUsername("user5").get();
        Member memberUser6 = memberService.findByUsername("user6").get();

        Post post1 = postService.write(
                memberUser1,
                "축구 하실 분?",
                "14시 까지 22명을 모아야 합니다.",
                true,
                true
        );
        post1.addComment(memberUser2, "저요!");
        post1.addComment(memberUser3, "저도 할래요.");

        Post post2 = postService.write(
                memberUser1,
                "배구 하실 분?",
                "15시 까지 12명을 모아야 합니다.",
                true,
                true
        );
        post2.addComment(memberUser4, "저요!, 저 배구 잘합니다.");

        Post post3 = postService.write(
                memberUser2,
                "농구 하실 분?",
                "16시 까지 10명을 모아야 합니다.",
                true,
                true
        );

        Post post4 = postService.write(
                memberUser3,
                "발야구 하실 분?",
                "17시 까지 14명을 모아야 합니다.",
                true,
                true
        );

        Post post5 = postService.write(
                memberUser4,
                "피구 하실 분?",
                "18시 까지 18명을 모아야 합니다.",
                true,
                true
        );

        Post post6 = postService.write(
                memberUser4,
                "발야구를 밤에 하실 분?",
                "22시 까지 18명을 모아야 합니다.",
                false,
                false
        );

        Post post7 = postService.write(
                memberUser4,
                "발야구를 새벽 1시에 하실 분?",
                "새벽 1시 까지 17명을 모아야 합니다.",
                true,
                false
        );

        Post post8 = postService.write(
                memberUser4,
                "발야구를 새벽 3시에 하실 분?",
                "새벽 3시 까지 19명을 모아야 합니다.",
                false,
                true
        );

        Post post9 = postService.write(
                memberUser4,
                "테이블테니스를 하실 분있나요?",
                "테이블테니스 강력 추천합니다.",
                true,
                true
        );

        String genFile1FilePath = SampleResource.IMG_GIF_SAMPLE1.makeCopy();
        post9.addGenFile(PostGenFile.TypeCode.attachment, genFile1FilePath);

        String genFile2FilePath = SampleResource.IMG_JPG_SAMPLE1.makeCopy();
        post9.addGenFile(PostGenFile.TypeCode.attachment, genFile2FilePath);
        post9.deleteGenFile(PostGenFile.TypeCode.attachment, 2);

        genFile2FilePath = SampleResource.IMG_JPG_SAMPLE2.makeCopy();
        post9.putGenFile(PostGenFile.TypeCode.attachment, 3, genFile2FilePath);

        String genFile3FilePath = SampleResource.IMG_JPG_SAMPLE3.makeCopy();
        post9.addGenFile(PostGenFile.TypeCode.thumbnail, genFile3FilePath);

        String newGenFile3FilePath = SampleResource.IMG_JPG_SAMPLE5.makeCopy();
        PostGenFile postGenFile3 = post9.modifyGenFile(PostGenFile.TypeCode.thumbnail, 1, newGenFile3FilePath);

        post9.setThumbnailGenFile(postGenFile3);

        Post post10 = postService.write(
                memberUser4,
                "테니스 하실 분있나요?",
                "테니스 강력 추천합니다.",
                true,
                true
        );

        String genFile4FilePath = SampleResource.IMG_WEBP_SAMPLE1.makeCopy();
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile4FilePath);

        String genFile5FilePath = SampleResource.AUDIO_M4A_SAMPLE1.makeCopy();
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile5FilePath);

        String genFile6FilePath = SampleResource.AUDIO_MP3_SAMPLE1.makeCopy();
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile6FilePath);

        String genFile7FilePath = SampleResource.AUDIO_MP3_SAMPLE2.makeCopy();
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile7FilePath);

        String genFile8FilePath = SampleResource.VIDEO_MOV_SAMPLE1.makeCopy();
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile8FilePath);

        String genFile9FilePath = SampleResource.VIDEO_MP4_SAMPLE1.makeCopy();
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile9FilePath);

        String genFile10FilePath = SampleResource.VIDEO_MP4_SAMPLE2.makeCopy();
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile10FilePath);

        IntStream.rangeClosed(11, 100).forEach(
                i -> postService.write(
                        memberUser5,
                        "테스트 게시물 " + i,
                        "테스트 게시물 " + i + " 내용",
                        i % 3 != 0,
                        i % 4 != 0
                )
        );

        IntStream.rangeClosed(101, 200).forEach(
                i -> postService.write(
                        memberUser6,
                        "테스트 게시물 " + i,
                        "테스트 게시물 " + i + " 내용",
                        i % 5 != 0,
                        i % 6 != 0
                )
        );
    }
}