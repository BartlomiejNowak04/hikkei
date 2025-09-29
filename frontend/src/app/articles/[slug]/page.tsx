import ArticleClient from "@/components/Article/ArticleClient";

export default async function Page({ params }: { params: Promise<{ slug: string }> }) {
    return <ArticleClient slug={(await params).slug} />;
}
