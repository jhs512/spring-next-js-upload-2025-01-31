export function getFileSizeHr(size: number) {
  return size >= 1024 * 1024
    ? `${(size / (1024 * 1024)).toFixed(1)}MB`
    : size >= 1024
      ? `${(size / 1024).toFixed(1)}KB`
      : `${size}B`;
}

export function getDateHr(date: string) {
  return new Date(date).toLocaleString("ko-KR", {
    year: "2-digit",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function getUplodableInputAccept() {
  return ".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.csv,.zip,.rar,.7z,.jpg,.jpeg,.png,.gif,.webp,.svg,.mp4,.m4a,.mov,.mp3,.xml,.hwp,.hwpx,.md";
}

export function getMetadataAttrValueFromQueryStr(url: string, attr: string) {
  const urlObj = new URL(url);
  const searchParams = new URLSearchParams(urlObj.search);
  return searchParams.get(attr);
}

export function getMetadataAttrValueAsNumberFromQueryStr(
  url: string,
  attr: string,
  defaultValue: number,
) {
  const value = getMetadataAttrValueFromQueryStr(url, attr);
  return value ? parseInt(value) : defaultValue;
}

export function stripMarkdown(input: string) {
  // 1. $$...$$ 또는 ```...``` 내용을 제거
  const cleanedContent = input.replace(
    /(\$\$[\s\S]*?\$\$|```[\s\S]*?```)/g,
    "",
  );

  // 2. 마크다운 제목 제거 (1~6개의 #으로 시작하는 줄 전체)
  const withoutHeadings = cleanedContent.replace(/^#{1,6}.*$/gm, "");

  // 3. 마크다운 링크에서 텍스트만 추출 ([text](url) -> text)
  const withoutLinks = withoutHeadings.replace(/\[([^\]]+)\]\([^)]+\)/g, "$1");

  // 4. 영어, 소괄호, 한글(자음/모음 포함), 특수문자(:;/,〈〉=\-_[]), 띄워쓰기, 줄바꿈만 허용
  // 5. 연속된 공백과 줄바꿈을 하나의 공백으로 변경하고 앞뒤 공백 제거
  return withoutLinks
    .replace(/[^a-zA-Z가-힣ㄱ-ㅎㅏ-ㅣ0-9().?!:;/,〈〉=\-_\[\]\s]/g, "")
    .replace(/\s+/g, " ")
    .trim()
    .slice(0, 157)
    .replace(/(.{157}).*/, "$1...");
}
