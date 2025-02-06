"use client";

import { forwardRef } from "react";

import dynamic from "next/dynamic";

const MarkdownViewerWrapper = dynamic(() => import("./MarkdownViewerWrapper"), {
  ssr: false,
  loading: () => <div>로딩중...</div>,
});

interface ViewerProps {
  initialValue: string;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const MarkdownViewer = forwardRef<any, ViewerProps>((props, ref) => {
  return <MarkdownViewerWrapper ref={ref} {...props} />;
});

MarkdownViewer.displayName = "MarkdownViewer";

export default MarkdownViewer;
