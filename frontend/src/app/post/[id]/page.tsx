import type { Metadata } from "next";

import { cookies } from "next/headers";

import client from "@/lib/backend/client";

import { stripMarkdown } from "@/lib/business/utils";

import ClientPage from "./ClientPage";

async function getPost(id: string) {
  const res = await client.GET("/api/v1/posts/{id}", {
    params: {
      path: {
        id: parseInt(id),
      },
    },
    headers: {
      cookie: (await cookies()).toString(),
    },
  });

  return res;
}

export async function generateMetadata({
  params,
}: {
  params: { id: string };
}): Promise<Metadata> {
  const { id } = await params;
  const postResponse = await getPost(id);

  if (postResponse.error) {
    return {
      title: postResponse.error.msg,
      description: postResponse.error.msg,
    };
  }

  const post = postResponse.data;

  return {
    title: post.title,
    description: stripMarkdown(post.content),
  };
}

export default async function Page({ params }: { params: { id: string } }) {
  const { id } = await params;
  const postResponse = await getPost(id);

  if (postResponse.error) {
    return (
      <div className="flex-1 flex items-center justify-center">
        {postResponse.error.msg}
      </div>
    );
  }

  const post = postResponse.data;

  const genFilesResponse = await client.GET("/api/v1/posts/{postId}/genFiles", {
    params: { path: { postId: post.id } },
  });

  if (genFilesResponse.error) {
    return (
      <div className="flex-1 flex items-center justify-center">
        {genFilesResponse.error.msg}
      </div>
    );
  }

  const genFiles = genFilesResponse.data;

  return <ClientPage post={post} genFiles={genFiles} />;
}
