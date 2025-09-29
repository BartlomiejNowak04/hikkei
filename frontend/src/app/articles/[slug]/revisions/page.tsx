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

type Ok<T> = { ok: true; data: T };
type Err = { ok: false; status: number; text?: string };

async function sfetch<T>(path: string): Promise<Ok<T> | Err> {
    const cookieHeader = cookies().toString();
    const h = new Headers();
    if (cookieHeader) h.set("cookie", cookieHeader);

    const reqHeaders = await headers();
    const ua = reqHeaders.get("user-agent");
    const lang = reqHeaders.get("accept-language");
    if (ua) h.set("user-agent", ua);
    if (lang) h.set("accept-language", lang);

    const res = await fetch(`${API}${path}`, { cache: "no-store", headers: h });
    if (!res.ok) {
        let text: string | undefined;
        try { text = await res.text(); } catch {}
        return { ok: false, status: res.status, text };
    }

    const ct = (res.headers.get("content-type") || "").toLowerCase();
    if (ct.includes("application/json")) {
        const data = (await res.json()) as T;
        return { ok: true, data };
    }
    const text = (await res.text()) as unknown as T;
    return { ok: true, data: text };
}

// Fallback: najpierw /api/page/:url, potem /api/page/title/:title, na koniec /api/page/search/:title
async function getPageBySlug(slug: string): Promise<Page | null> {
    const byUrl = await sfetch<Page>(`/api/page/${encodeURIComponent(slug)}`);
    if (byUrl.ok) return byUrl.data;

    const decoded = decodeURIComponent(slug);
    const byTitle = await sfetch<Page>(`/api/page/title/${encodeURIComponent(decoded)}`);
    if (byTitle.ok) return byTitle.data;

    const search = await sfetch<Array<Page & { title: string; url: string }>>(
        `/api/page/search/${encodeURIComponent(decoded)}`
    );
    if (search.ok && Array.isArray(search.data)) {
        const exactUrl = search.data.find(p => p.url === slug);
        if (exactUrl) return exactUrl;
        const exactTitle = search.data.find(p => p.title === decoded);
        if (exactTitle) return exactTitle;
        if (search.data[0]) return search.data[0];
    }

    return null;
}

function sortRevisions(revs: Revision[]): Revision[] {
    const withDate = revs.filter(r => !!r.created_at);
    const noDate   = revs.filter(r => !r.created_at);

    withDate.sort((a, b) => new Date(b.created_at!).getTime() - new Date(a.created_at!).getTime());
    noDate.sort((a, b) => b.id - a.id);

    return [...withDate, ...noDate];
}

export default async function RevisionsPage({ params }: { params: { slug: string } }) {
    const slug = params.slug;
    if (!slug) notFound();

    // 1) znajdź stronę
    const page = await getPageBySlug(slug);
    if (!page) notFound();

    // 2) pobierz rewizje
    const revListRes = await sfetch<Revision[]>(`/api/revision/page/${page.id}`);
    if (!revListRes.ok) {
        if (revListRes.status === 404) {
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
        if (revListRes.status === 401) {
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
        throw new Error(`HTTP ${revListRes.status} on GET /api/revision/page/:id`);
    }

    const revisions = Array.isArray(revListRes.data) ? sortRevisions(revListRes.data) : [];

    return (
        <article className={styles.wrapper}>
            <a href={`/articles/${encodeURIComponent(slug)}`} className={styles.backLink}>
                &larr; Wróć do artykułu
            </a>
            <h1 className={styles.title}>Rewizje — {page.title}</h1>

            <RevisionListClient
                apiBase={API}
                slug={slug}
                pageId={page.id}
                revisions={revisions}
            />
        </article>
    );
}
