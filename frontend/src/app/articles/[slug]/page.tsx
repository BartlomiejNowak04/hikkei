// app/articles/[slug]/page.tsx
import { notFound } from "next/navigation";
import { cookies, headers } from "next/headers";
import styles from "@/components/Content/Content.module.css";
import CommentsPanel from "@/components/Article/CommentsPanel";

type Page = { id: number; title: string; url: string };
type Revision = { id: number; page_id: number; created_at?: string };

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

function pickLatest(list: Revision[]): Revision | null {
    if (!Array.isArray(list) || list.length === 0) return null;
    const withDate = list.filter(r => !!r.created_at);
    if (withDate.length) {
        return withDate.sort((a,b) => new Date(b.created_at!).getTime() - new Date(a.created_at!).getTime())[0];
    }
    return list.sort((a,b) => b.id - a.id)[0];
}

// ——— Fallbackowe pobranie Page po slug ———
async function getPageBySlug(slug: string): Promise<Page | null> {
    // 1) po URL
    const byUrl = await sfetch<Page>(`/api/page/${encodeURIComponent(slug)}`);
    if (byUrl.ok) return byUrl.data;

    // 2) po Tytule (czasem frontend przesyła tytuł jako slug)
    const decoded = decodeURIComponent(slug);
    const byTitle = await sfetch<Page>(`/api/page/title/${encodeURIComponent(decoded)}`);
    if (byTitle.ok) return byTitle.data;

    // 3) search i dopasowanie
    const search = await sfetch<Array<Page & { title: string; url: string }>>(
        `/api/page/search/${encodeURIComponent(decoded)}`
    );
    if (search.ok && Array.isArray(search.data)) {
        // Preferuj dokładne dopasowanie po url, potem po tytule
        const exactUrl = search.data.find(p => p.url === slug);
        if (exactUrl) return exactUrl;
        const exactTitle = search.data.find(p => p.title === decoded);
        if (exactTitle) return exactTitle;
        // ostatnia deska ratunku: pierwszy wynik
        if (search.data[0]) return search.data[0];
    }

    return null;
}

export default async function ArticlePage({ params }: { params: { slug: string } }) {
    const slug = params.slug;
    if (!slug) notFound();

    // — 1) Page (z fallbackiem) —
    const page = await getPageBySlug(slug);
    if (!page) {
        // jeżeli nic nie znaleziono -> 404
        notFound();
    }

    // — 2) Rewizje —
    const revListRes = await sfetch<Revision[]>(`/api/revision/page/${page.id}`);
    if (!revListRes.ok) {
        if (revListRes.status === 404) notFound();
        throw new Error(`HTTP ${revListRes.status} on GET /api/revision/page/:id`);
    }
    const revisions = Array.isArray(revListRes.data) ? revListRes.data : [];
    const latest = pickLatest(revisions);

    // — 3) Spróbuj latest -> fallback do current (gdy 401/403/404) —
    let html: string | null = null;

    if (latest) {
        const latestHtmlRes = await sfetch<string>(`/api/revision/${latest.id}/rendered`);
        if (latestHtmlRes.ok) {
            html = latestHtmlRes.data as string;
        } else if ([401, 403, 404].includes(latestHtmlRes.status)) {
            const currentHtmlRes = await sfetch<string>(`/api/revision/current/${page.id}/rendered`);
            if (currentHtmlRes.ok) {
                html = currentHtmlRes.data as string;
            } else if (currentHtmlRes.status === 404) {
                html = null;
            } else {
                throw new Error(`HTTP ${currentHtmlRes.status} on GET /api/revision/current/:id/rendered`);
            }
        } else {
            throw new Error(`HTTP ${latestHtmlRes.status} on GET /api/revision/:id/rendered`);
        }
    } else {
        const currentHtmlRes = await sfetch<string>(`/api/revision/current/${page.id}/rendered`);
        if (currentHtmlRes.ok) {
            html = currentHtmlRes.data as string;
        } else if (currentHtmlRes.status === 404) {
            html = null;
        } else {
            throw new Error(`HTTP ${currentHtmlRes.status} on GET /api/revision/current/:id/rendered`);
        }
    }

    return (
        <article className={styles.wrapper}>
            <a href="/articles" className={styles.backLink} aria-label="Wróć do listy artykułów">
                &larr; Wróć do listy
            </a>

            <header className={styles.headerRow}>
                <h1 className={styles.title}>{page.title}</h1>
                <div className={styles.actionsRow}>
                    <a className={styles.buttonLink} href={`/articles/${encodeURIComponent(page.url)}/revisions`}>
                        Zobacz wszystkie rewizje
                    </a>
                    <CommentsPanel pageId={page.id} />
                </div>
            </header>

            {html ? (
                <div className={styles.prose} dangerouslySetInnerHTML={{ __html: html }} />
            ) : (
                <div className={styles.prose}>
                    <p>Ten artykuł nie ma jeszcze publicznej treści.</p>
                </div>
            )}
        </article>
    );
}
