import { notFound } from "next/navigation";
import { cookies, headers } from "next/headers";
import styles from "@/components/Content/Content.module.css";
import RevisionListClient from "@/components/Article/RevisionListClient";

type Page = { id: number; title: string; url: string };
type Revision = {
    id: number;
    page_id: number;
    created_at?: string;
    author?: { id: number; name?: string } | null;
};

const API = process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:3000";

async function getForwardedHeaders() {
    const h = new Headers();
    const cookieHeader = cookies().toString();
    if (cookieHeader) h.set("cookie", cookieHeader);

    const reqHeaders = await headers();
    const ua = reqHeaders.get("user-agent");
    const lang = reqHeaders.get("accept-language");
    if (ua) h.set("user-agent", ua);
    if (lang) h.set("accept-language", lang);

    return h;
}

async function getPageBySlug(slug: string): Promise<Page | null> {
    const h = await getForwardedHeaders();

    {
        const res = await fetch(`${API}/api/page/${encodeURIComponent(slug)}`, {
            cache: "no-store",
            headers: h,
        });
        if (res.ok) return (await res.json()) as Page;
    }

    const decoded = decodeURIComponent(slug);
    {
        const res = await fetch(`${API}/api/page/title/${encodeURIComponent(decoded)}`, {
            cache: "no-store",
            headers: h,
        });
        if (res.ok) return (await res.json()) as Page;
    }

    {
        const res = await fetch(`${API}/api/page/search/${encodeURIComponent(decoded)}`, {
            cache: "no-store",
            headers: h,
        });
        if (res.ok) {
            const data = (await res.json()) as Array<Page & { title: string; url: string }>;
            if (Array.isArray(data)) {
                const exactUrl = data.find((p) => p.url === slug);
                if (exactUrl) return exactUrl;
                const exactTitle = data.find((p) => p.title === decoded);
                if (exactTitle) return exactTitle;
                if (data[0]) return data[0];
            }
        }
    }

    return null;
}

function sortRevisions(revs: Revision[]): Revision[] {
    const withDate = revs.filter((r) => !!r.created_at);
    const noDate = revs.filter((r) => !r.created_at);

    withDate.sort(
        (a, b) => new Date(b.created_at!).getTime() - new Date(a.created_at!).getTime()
    );
    noDate.sort((a, b) => b.id - a.id);

    return [...withDate, ...noDate];
}

export default async function RevisionsPage({ params }: { params: { slug: string } }) {
    const slug = params.slug;
    if (!slug) notFound();

    const page = await getPageBySlug(slug);
    if (!page) notFound();

    const h = await getForwardedHeaders();
    const res = await fetch(`${API}/api/revision/page/${page.id}`, {
        cache: "no-store",
        headers: h,
    });

    if (!res.ok) {
        if (res.status === 404) {
            return (
                <article className={styles.wrapper}>
                    <a href={`/articles/${encodeURIComponent(slug)}`} className={styles.backLink}>
                        &larr; Wróć do artykułu
                    </a>
                    <h1 className={styles.title}>Rewizje — {page.title}</h1>
                    <p className={styles.muted}>Ten artykuł nie ma jeszcze żadnych rewizji.</p>
                </article>
            );
        }
        if (res.status === 401) {
            return (
                <article className={styles.wrapper}>
                    <a href={`/articles/${encodeURIComponent(slug)}`} className={styles.backLink}>
                        &larr; Wróć do artykułu
                    </a>
                    <h1 className={styles.title}>Rewizje — {page.title}</h1>
                    <p className={styles.error}>Musisz być zalogowany, aby zobaczyć rewizje.</p>
                </article>
            );
        }
        throw new Error(`HTTP ${res.status} on GET /api/revision/page/:id`);
    }

    const data = (await res.json()) as Revision[] | unknown;
    const revisions = Array.isArray(data) ? sortRevisions(data as Revision[]) : [];

    return (
        <article className={styles.wrapper}>
            <a href={`/articles/${encodeURIComponent(slug)}`} className={styles.backLink}>
                &larr; Wróć do artykułu
            </a>
            <h1 className={styles.title}>Rewizje — {page.title}</h1>

            <RevisionListClient apiBase={API} slug={slug} pageId={page.id} revisions={revisions} />
        </article>
    );
}
