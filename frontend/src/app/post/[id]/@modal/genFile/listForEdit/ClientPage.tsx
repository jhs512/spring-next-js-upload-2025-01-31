"use client";

import { useEffect } from "react";

import Link from "next/link";
import { useRouter } from "next/navigation";

import { components } from "@/lib/backend/apiV1/schema";
import { getFileSizeHr } from "@/lib/business/utils";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

import { Download, Eye, Pencil, Trash } from "lucide-react";

export default function ClientPage({
  post,
  genFiles,
}: {
  post: components["schemas"]["PostWithContentDto"];
  genFiles: components["schemas"]["PostGenFileDto"][];
}) {
  const router = useRouter();

  useEffect(() => {
    const needToRefresh = window.sessionStorage.getItem("needToRefresh");

    if (needToRefresh === "true") {
      window.sessionStorage.removeItem("needToRefresh");
      router.refresh();
    }
  }, [router]);

  return (
    <Dialog
      open
      onOpenChange={() => {
        router.back();
      }}
    >
      <DialogContent className="max-w-[100dvh] max-h-[100dvh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>파일 관리</DialogTitle>
          <DialogDescription>{post.id}번 글의 파일들</DialogDescription>
        </DialogHeader>

        <div className="grid gap-6">
          {genFiles.filter((genFile) => genFile.typeCode === "attachment")
            .length === 0 ? (
            <div className="text-center text-muted-foreground">
              첨부파일이 없습니다.
            </div>
          ) : (
            genFiles
              .filter((genFile) => genFile.typeCode === "attachment")
              .map((genFile) => (
                <div key={genFile.id} className="grid gap-2">
                  <Button variant="link" asChild className="justify-start">
                    <a
                      href={genFile.downloadUrl}
                      className="flex items-center gap-2"
                    >
                      <Download />

                      <span>
                        {genFile.originalFileName}
                        <br />({getFileSizeHr(genFile.fileSize)}) 다운로드
                      </span>
                    </a>
                  </Button>

                  <div className="flex flex-wrap">
                    <Button variant="link" className="justify-start" asChild>
                      <Link
                        href={`/post/${post.id}/genFile/${genFile.id}/preview`}
                      >
                        <Eye />
                        <span>미리보기</span>
                      </Link>
                    </Button>

                    <Button variant="link" className="justify-start" asChild>
                      <Link
                        href={`/post/${post.id}/genFile/${genFile.id}/edit`}
                      >
                        <Pencil />
                        <span>수정</span>
                      </Link>
                    </Button>

                    <Button variant="link" className="justify-start" asChild>
                      <Link
                        href={`/post/${post.id}/genFile/${genFile.id}/delete`}
                      >
                        <Trash />
                        <span>삭제</span>
                      </Link>
                    </Button>
                  </div>
                </div>
              ))
          )}
        </div>

        <DialogFooter className="gap-2">
          <Button
            variant="outline"
            onClick={() => {
              router.back();
            }}
          >
            닫기
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
