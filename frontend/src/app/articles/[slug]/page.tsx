import { notFound } from "next/navigation";
import { cookies, headers } from "next/headers";
import styles from "@/components/Content/Content.module.css";
import CommentsPanel from "@/components/Article/CommentsPanel";

type Page = { id: number; title: string; url: string };

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

// Fallback: /api/page/:url -> /api/page/title/:title -> /api/page/search/:title
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

export default async function ArticlePage({ params }: { params: { slug: string } }) {
    const slug = params.slug;
    if (!slug) notFound();

    const page = await getPageBySlug(slug);
    if (!page) notFound();

    const htmlRes = await sfetch<string>(`/api/revision/current/${page.id}/rendered`);
    if (!htmlRes.ok) {
        if (htmlRes.status === 404) {
            return (
                <article className={styles.wrapper}>
                    <a href="/articles" className={styles.backLink} aria-label="Wróć do listy artykułów">
                        &larr; Wróć do listy
                    </a>

                    <header className={styles.headerRow}>
                        <h1 className={styles.title}>{page.title}</h1>
                        <div className={styles.actionsRow}>
                            <a className={styles.buttonLink} href={`/articles/${encodeURIComponent(slug)}/revisions`}>
                                Zobacz wszystkie rewizje
                            </a>
                            {/* NOWE: przycisk edycji */}
                            <a className={styles.buttonLink} href={`/revision?page=${encodeURIComponent(page.title)}`}>
                                Edytuj artykuł
                            </a>
                            <CommentsPanel pageId={page.id} />
                        </div>
                    </header>

                    <div className={styles.prose}>
                        <p>Ten artykuł nie ma jeszcze publicznej treści.</p>
                    </div>
                </article>
            );
        }
        throw new Error(`HTTP ${htmlRes.status} on GET /api/revision/current/:id/rendered`);
    }
    const html = htmlRes.data as string;

    return (
        <article className={styles.wrapper}>
            <a href="/articles" className={styles.backLink} aria-label="Wróć do listy artykułów">
                &larr; Wróć do listy
            </a>

            <header className={styles.headerRow}>
                <h1 className={styles.title}>{page.title}</h1>
                <div className={styles.actionsRow}>
                    <a className={styles.buttonLink} href={`/articles/${encodeURIComponent(slug)}/revisions`}>
                        Zobacz wszystkie rewizje
                    </a>
                    <a
                        className={styles.buttonLink}
                        href={`/revision/new?page=${encodeURIComponent(page.title)}`}
                    >
                        Edytuj artykuł
                    </a>
                    <CommentsPanel pageId={page.id} />
                </div>
            </header>

            <div className={styles.prose} dangerouslySetInnerHTML={{ __html: html }} />
        </article>
    );
}
