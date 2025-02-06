package com.ll.domain.post.post.service;

import com.ll.domain.member.member.entity.Member;
import com.ll.domain.post.post.entity.Post;
import com.ll.domain.post.post.repository.PostRepository;
import com.ll.global.rsData.RsData;
import com.ll.standard.search.PostSearchKeywordTypeV1;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public long count() {
        return postRepository.count();
    }

    public long countByPublished(boolean published) {
        return postRepository.countByPublished(published);
    }

    public long countByListed(boolean listed) {
        return postRepository.countByListed(listed);
    }

    public Post write(Member author, String title, String content, boolean published, boolean listed) {
        Post post = Post.builder()
                .author(author)
                .title(title)
                .content(content)
                .published(published)
                .listed(listed)
                .build();

        return postRepository.save(post);
    }

    public List<Post> findAllByOrderByIdDesc() {
        return postRepository.findAllByOrderByIdDesc();
    }

    public Optional<Post> findById(long id) {
        return postRepository.findById(id);
    }

    public void delete(Post post) {
        postRepository.delete(post);
    }

    public void modify(Post post, String title, String content, boolean published, boolean listed) {
        boolean wasTemp = post.isTemp();

        post.setTitle(title);
        post.setContent(content);
        post.setPublished(published);
        post.setListed(listed);

        if ( wasTemp && !post.isTemp() ) {
            post.setCreateDateNow();
        }
    }

    public void flush() {
        postRepository.flush(); // em.flush(); 와 동일
    }

    public Optional<Post> findLatest() {
        return postRepository.findFirstByOrderByIdDesc();
    }

    public Page<Post> findByListedPaged(boolean listed, int page, int pageSize) {
        return findByListedPaged(listed, null, null, page, pageSize);
    }

    public Page<Post> findByListedPaged(
            boolean listed,
            PostSearchKeywordTypeV1 searchKeywordType,
            String searchKeyword,
            int page,
            int pageSize
    ) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("id")));
        return postRepository.findByKw(searchKeywordType, searchKeyword, null, null, listed, pageable);
    }

    public Page<Post> findByAuthorPaged(Member author, int page, int pageSize) {
        return findByAuthorPaged(author, null, null, page, pageSize);
    }

    public Page<Post> findByAuthorPaged(
            Member author,
            PostSearchKeywordTypeV1 searchKeywordType,
            String searchKeyword,
            int page,
            int pageSize
    ) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("id")));
        return postRepository.findByKw(searchKeywordType, searchKeyword, author, null, null, pageable);
    }

    public RsData<Post> findTempOrMake(Member author) {
        AtomicBoolean isNew = new AtomicBoolean(false);

        Post post = postRepository.findTop1ByAuthorAndPublishedAndTitleOrderByIdDesc(
                author,
                false,
                "임시글"
        ).orElseGet(() -> {
            isNew.set(true);
            return write(author, "임시글", "# 요약\n- 이 글은 ...", false, false);
        });

        if (isNew.get()) {
            return new RsData(
                    "201-1",
                    "%d번 임시글이 생성되었습니다.".formatted(post.getId()),
                    post
            );
        }

        return new RsData(
                "200-1",
                "%d번 임시글을 불러옵니다.".formatted(post.getId()),
                post
        );
    }
}