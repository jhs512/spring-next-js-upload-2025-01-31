"use client";

import { useState } from "react";

import Image from "next/image";
import { useRouter } from "next/navigation";

import { components } from "@/lib/backend/apiV1/schema";
import PaginationType1Responsive from "@/lib/business/components/PaginationType1Responsive";
import { getDateHr } from "@/lib/business/utils";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { FormItem } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

import { Search, X } from "lucide-react";

export default function ClientPage({
  searchKeyword,
  searchKeywordType,
  pageSize,
  itemPage,
}: {
  searchKeyword: string;
  searchKeywordType: "all" | "username" | "nickname";
  page: number;
  pageSize: number;
  itemPage: components["schemas"]["PageDtoMemberWithUsernameDto"];
}) {
  const router = useRouter();
  const [open, setOpen] = useState(false);

  return (
    <>
      <div className="container mx-auto px-4">
        <h1 className="text-2xl font-bold text-center my-4">회원목록</h1>

        <div className="flex items-center flex-wrap gap-2 mb-6">
          <div
            className="relative w-full sm:w-auto"
            role="button"
            onClick={() => setOpen(true)}
          >
            <Input
              readOnly
              placeholder="검색어를 입력해주세요."
              className="pr-10 w-full cursor-pointer"
              value={
                searchKeyword
                  ? `${
                      {
                        all: "전체",
                        username: "아이디",
                        nickname: "별명",
                      }[searchKeywordType]
                    } : ${searchKeyword}`
                  : ""
              }
              autoComplete="off"
            />
            <Button
              variant="ghost"
              size="sm"
              className="absolute right-0 top-0 px-3 hover:bg-transparent"
            >
              <Search />
            </Button>
          </div>

          <div className="flex-grow"></div>

          {searchKeyword && (
            <div className="flex items-center gap-2 text-sm bg-muted px-3 py-1.5 rounded-md">
              <span className="text-muted-foreground">
                {
                  {
                    all: "전체",
                    username: "아이디",
                    nickname: "별명",
                  }[searchKeywordType]
                }
                :
              </span>
              <span className="font-medium">{searchKeyword}</span>
              <Button
                variant="ghost"
                size="icon"
                className="h-5 w-5 ml-1 hover:bg-background/50"
                onClick={() => {
                  router.push(`?page=1&pageSize=${pageSize}`);
                }}
              >
                <X className="h-3 w-3" />
              </Button>
            </div>
          )}

          <span className="text-sm text-muted-foreground">
            총 {itemPage.totalItems}개
          </span>

          <Select
            value={pageSize.toString()}
            onValueChange={(value) => {
              router.push(
                `?page=1&pageSize=${value}&searchKeywordType=${searchKeywordType}&searchKeyword=${searchKeyword}`,
              );
            }}
          >
            <SelectTrigger className="w-[130px]">
              <SelectValue placeholder="페이지 당 행 수" />
            </SelectTrigger>
            <SelectContent>
              <SelectGroup>
                <SelectItem value="30">30개씩 보기</SelectItem>
                <SelectItem value="60">60개씩 보기</SelectItem>
                <SelectItem value="90">90개씩 보기</SelectItem>
              </SelectGroup>
            </SelectContent>
          </Select>
        </div>

        <Dialog open={open} onOpenChange={setOpen}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle className="text-xl">검색</DialogTitle>
              <DialogDescription className="sr-only">검색</DialogDescription>
            </DialogHeader>
            <form
              className="flex flex-col gap-4"
              onSubmit={(e) => {
                e.preventDefault();
                const formData = new FormData(e.target as HTMLFormElement);
                const searchKeyword = formData.get("searchKeyword") as string;
                const searchKeywordType = formData.get(
                  "searchKeywordType",
                ) as string;
                const page = formData.get("page") as string;
                const pageSize = formData.get("pageSize") as string;

                router.push(
                  `?page=${page}&pageSize=${pageSize}&searchKeywordType=${searchKeywordType}&searchKeyword=${searchKeyword}`,
                );
                setOpen(false);
              }}
            >
              <input type="hidden" name="page" value="1" />

              <FormItem>
                <Label>페이지 당 행 수</Label>
                <Select name="pageSize" defaultValue={pageSize.toString()}>
                  <SelectTrigger>
                    <SelectValue placeholder="페이지 당 행 수" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      <SelectItem value="30">30개씩 보기</SelectItem>
                      <SelectItem value="60">60개씩 보기</SelectItem>
                      <SelectItem value="90">90개씩 보기</SelectItem>
                    </SelectGroup>
                  </SelectContent>
                </Select>
              </FormItem>

              <FormItem>
                <Label>검색어 타입</Label>
                <Select
                  name="searchKeywordType"
                  defaultValue={searchKeywordType}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="검색어 타입" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      <SelectItem value="all">전체</SelectItem>
                      <SelectItem value="username">아이디</SelectItem>
                      <SelectItem value="nickname">별명</SelectItem>
                    </SelectGroup>
                  </SelectContent>
                </Select>
              </FormItem>

              <FormItem>
                <Label>검색어</Label>
                <Input
                  placeholder="검색어를 입력해주세요."
                  type="text"
                  name="searchKeyword"
                  defaultValue={searchKeyword}
                  autoComplete="off"
                  autoFocus
                />
              </FormItem>

              <Button type="submit">검색</Button>
            </form>
          </DialogContent>
        </Dialog>

        <PaginationType1Responsive
          className="my-6"
          baseQueryString={`pageSize=${pageSize}&searchKeywordType=${searchKeywordType}&searchKeyword=${searchKeyword}`}
          totalPages={itemPage.totalPages}
          currentPageNumber={itemPage.currentPageNumber}
        />

        {itemPage.items.length === 0 ? (
          <div className="flex flex-col min-h-[calc(100dvh-280px)] items-center justify-center py-12 text-muted-foreground">
            <Search />
            <p>검색 결과가 없습니다.</p>
          </div>
        ) : (
          <ul className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {itemPage.items.map((item) => (
              <li key={item.id}>
                <Card className="hover:bg-accent/50 transition-colors">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Badge variant="outline">{item.id}</Badge>
                      {item.username}
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center space-x-4">
                      <div className="flex-shrink-0">
                        <Image
                          src={item.profileImgUrl}
                          alt={item.nickname}
                          width={40}
                          height={40}
                          quality={100}
                          objectFit="cover"
                          className="rounded-full ring-2 ring-primary/10"
                        />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-foreground">
                          {item.nickname}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {getDateHr(item.createDate)}
                        </p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </li>
            ))}
          </ul>
        )}

        <PaginationType1Responsive
          className="my-6"
          baseQueryString={`pageSize=${pageSize}&searchKeywordType=${searchKeywordType}&searchKeyword=${searchKeyword}`}
          totalPages={itemPage.totalPages}
          currentPageNumber={itemPage.currentPageNumber}
        />
      </div>
    </>
  );
}
