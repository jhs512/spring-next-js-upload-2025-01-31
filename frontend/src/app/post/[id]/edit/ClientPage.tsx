"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";

import { useRouter } from "next/navigation";

import client from "@/lib/backend/client";

import { components } from "@/lib/backend/apiV1/schema";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";

import { useToast } from "@/hooks/use-toast";

const writeFormSchema = z.object({
  title: z
    .string()
    .min(1, "제목을 입력해주세요.")
    .min(2, "제목을 2자 이상이여야 합니다.")
    .max(50, "제목은 50자 이하여야 합니다."),
  content: z
    .string()
    .min(1, "내용을 입력해주세요.")
    .min(2, "내용은 2자 이상이어야 합니다.")
    .max(10_000_000, "내용은 1,000만자 이하여야 합니다."),
  published: z.boolean(),
  listed: z.boolean(),
  attachment_1: z.instanceof(File).optional(),
});

type WriteFormInputs = z.infer<typeof writeFormSchema>;

export default function ClientPage({
  post,
}: {
  post: components["schemas"]["PostWithContentDto"];
}) {
  const router = useRouter();
  const { toast } = useToast();

  const form = useForm<WriteFormInputs>({
    resolver: zodResolver(writeFormSchema),
    defaultValues: {
      title: post.title,
      content: post.content,
      published: post.published,
      listed: post.listed,
    },
  });

  const onSubmit = async (data: WriteFormInputs) => {
    try {
      // 게시글 수정 요청
      const response = await client.PUT("/api/v1/posts/{id}", {
        params: {
          path: {
            id: post.id,
          },
        },
        body: {
          title: data.title,
          content: data.content,
          published: data.published,
          listed: data.listed,
        },
      });

      if (response.error) {
        toast({
          title: response.error.msg,
          variant: "destructive",
        });
        return;
      }

      // 파일 업로드 처리
      if (data.attachment_1) {
        const formData = new FormData();
        formData.append("file", data.attachment_1);

        const uploadResponse = await client.POST(
          "/api/v1/posts/{postId}/genFiles/{typeCode}",
          {
            params: {
              path: {
                postId: post.id,
                typeCode: "attachment",
              },
            },
            body: formData,
          },
        );

        if (uploadResponse.error) {
          toast({
            title: uploadResponse.error.msg,
            variant: "destructive",
          });
          return;
        }
      }

      toast({
        title: response.data.msg,
      });

      router.replace("/post/list");
    } catch (error) {
      console.error("Error updating post:", error);
      toast({
        title: "글 수정 중 오류가 발생했습니다.",
        variant: "destructive",
      });
    }
  };

  return (
    <div className="container mx-auto px-4">
      <h1 className="text-2xl font-bold my-4 flex items-center gap-2 justify-center">
        {post.id}번 글 수정
      </h1>

      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(onSubmit)}
          className="flex flex-col gap-4"
        >
          <FormField
            control={form.control}
            name="title"
            render={({ field }) => (
              <FormItem>
                <FormLabel>제목</FormLabel>
                <FormControl>
                  <Input
                    {...field}
                    type="text"
                    placeholder={post.title}
                    autoComplete="off"
                    autoFocus
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <div className="flex gap-4">
            <label className="flex items-center space-x-2 cursor-pointer">
              <Checkbox
                checked={form.watch("published")}
                onCheckedChange={(checked) =>
                  form.setValue("published", checked === true)
                }
              />
              <span className="text-sm font-medium leading-none">공개</span>
            </label>
            <label className="flex items-center space-x-2 cursor-pointer">
              <Checkbox
                checked={form.watch("listed")}
                onCheckedChange={(checked) =>
                  form.setValue("listed", checked === true)
                }
              />
              <span className="text-sm font-medium leading-none">검색</span>
            </label>
            <Badge variant="outline">작성자 : {post.authorName}</Badge>
          </div>
          <FormField
            control={form.control}
            name="attachment_1"
            render={({ field: { onChange, ...field } }) => (
              <FormItem>
                <FormLabel>첨부파일</FormLabel>
                <FormControl>
                  <Input
                    type="file"
                    onChange={(e) => {
                      const file = e.target.files?.[0];
                      onChange(file);
                    }}
                    {...field}
                    value={undefined}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="content"
            render={({ field }) => (
              <FormItem>
                <FormLabel>내용</FormLabel>
                <FormControl>
                  <Textarea
                    {...field}
                    className="h-[calc(100dvh-380px)] min-h-[300px]"
                    placeholder={post.content}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <Button
            type="submit"
            disabled={form.formState.isSubmitting}
            className="mt-2"
          >
            {form.formState.isSubmitting ? "수정 중..." : "수정"}
          </Button>
        </form>
      </Form>
    </div>
  );
}
