import ArticleClient from "@/components/Article/ArticleClient";

export default function Page({ params }: { params: { slug: string } }) {
    return <ArticleClient slug={params.slug} />;
}
