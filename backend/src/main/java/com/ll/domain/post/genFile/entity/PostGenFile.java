package com.ll.domain.post.genFile.entity;

import com.ll.domain.post.post.entity.Post;
import com.ll.global.jpa.entity.GenFile;
import com.ll.standard.util.Ut;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PostGenFile extends GenFile {
    @ManyToOne
    private Post post;

    public PostGenFile(Post post, String typeCode, int fileNo, String originFileName, String filePath) {
        super(typeCode, fileNo, originFileName, filePath);
        this.post = post;
    }

    public static String getCurrentDirPath() {
        return GenFile.getCurrentDirPath() + "/post/" + Ut.date.getCurrentDateFormatted("yyyy_MM_dd");
    }
}
