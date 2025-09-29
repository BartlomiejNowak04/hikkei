"use client";
import { useEffect, useRef, useState } from "react";
import styles from "./CommentsPanel.module.css";

type Comment = {
  id: number;
  page_id: number;
  content: string;
  author?: { id: number; name?: string } | null;
  created_at?: string;
};

type Props = { pageId: number };

export default function CommentsPanel({ pageId }: Props) {
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);
  const [comments, setComments] = useState<Comment[] | null>(null);
  const formRef = useRef<HTMLFormElement>(null);

  const [commentAuthors, setCommentAuthors] = useState<Map<number, string>>(
    new Map(),
  );

  const count = comments?.length ?? 0;

  async function load() {
    setLoading(true);
    setErr(null);
    try {
      const res = await fetch(`/api/comment/page/${pageId}`, {
        credentials: "include",
        cache: "no-store",
      });
      if (!res.ok) throw new Error(String(res.status));
      const data = (await res.json()) as Comment[] | null;
      setComments(Array.isArray(data) ? data : []);
    } catch (e) {
      console.error(e);
      setErr("Nie udało się pobrać komentarzy.");
    } finally {
      setLoading(false);
    }
  }

  async function toggle() {
    if (!open && comments == null) {
      await load();
    }
    setOpen(!open);
  }

  const deleteComment = (id: number) => async () => {
    setErr(null);
    try {
      const res = await fetch(`/api/comment/delete`, {
        method: "DELETE",
        credentials: "include",
        cache: "no-store",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id }),
      });
      if (!res.ok) throw new Error(String(res.status));
      setComments((prev) => (prev ? prev.filter((c) => c.id !== id) : prev));
    } catch (e) {
      console.error(e);
      setErr("Nie udało się usunąć komentarza.");
    }
  };

  const createComment = async (fd: FormData) => {
    setErr(null);
    try {
      const content = String(fd.get("content") ?? "").trim();
      if (!content) return;

      const res = await fetch(`/api/comment/create`, {
        method: "POST",
        credentials: "include",
        cache: "no-store",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ page_id: pageId, content }),
      });
      if (!res.ok) throw new Error(String(res.status));

      formRef.current?.reset();
      await load();
    } catch (e) {
      console.error(e);
      setErr("Nie udało się dodać komentarza.");
    }
  };

  return (
    <div className={styles.inline}>
      <button type="button" onClick={toggle} className={styles.buttonGhost}>
        {open ? "Ukryj komentarze" : "Pokaż komentarze"}
        <span className={styles.counterBadge}>{count}</span>
      </button>

      {open && (
        <div className={styles.panel}>
          {loading && (
            <div className={styles.skeletonList}>
              <div className={styles.skeletonItem} />
              <div className={styles.skeletonItem} />
            </div>
          )}

          {err && <p className={styles.error}>{err}</p>}

          {!loading && !err && (
            <>
              {(!comments || comments.length === 0)
                ? <p className={styles.muted}>Brak komentarzy.</p>
                : (
                  <ul className={styles.longList} role="list">
                    {comments.map((c) => {
                      const when = c.created_at
                        ? new Date(c.created_at).toLocaleString()
                        : null;

                      if (commentAuthors.get(c.id) === undefined) {
                        if (c.id === null) {
                          setCommentAuthors(
                            new Map(commentAuthors).set(c.id, "Anomim"),
                          );
                        } else {
                          useEffect(() => {
                            (async () => {
                              const res = await fetch(
                                `/api/user/${c.author?.id}`,
                                {
                                  credentials: "include",
                                  cache: "no-store",
                                },
                              );
                              if (!res.ok) {
                                setCommentAuthors(
                                  new Map(commentAuthors).set(c.id, "Nieznany"),
                                );
                              } else {
                                const name =
                                  (await res.json())["name"] as string;
                                setCommentAuthors(
                                  new Map(commentAuthors).set(c.id, name),
                                );
                              }
                            })();
                          }, [commentAuthors]);
                        }
                      }

                      return (
                        <li
                          key={c.id}
                          className={styles.longItem}
                          aria-label={`Komentarz ${c.id}`}
                        >
                          <div className={styles.row}>
                            <div className={styles.commentBody}>
                              <div className={styles.meta}>
                                {commentAuthors.get(c.id) ?? "Ładuję autora..."}
                                {" "}
                                {when ? `• ${when}` : ""}
                              </div>
                              <div className={styles.content}>{c.content}</div>
                            </div>
                            <div className={styles.rowActions}>
                              <button
                                type="button"
                                onClick={deleteComment(c.id)}
                                className={styles.buttonDanger}
                                aria-label={`Usuń komentarz #${c.id}`}
                              >
                                Usuń
                              </button>
                            </div>
                          </div>
                        </li>
                      );
                    })}
                  </ul>
                )}

              <div className={styles.card}>
                <form ref={formRef} action={createComment}>
                  <label className={styles.label} htmlFor="content">
                    Dodaj nowy komentarz
                  </label>
                  <textarea
                    id="content"
                    name="content"
                    rows={4}
                    className={styles.textarea}
                    placeholder="Napisz coś miłego…"
                  />
                  <div className={styles.actions}>
                    <button type="submit" className={styles.buttonPrimary}>
                      Dodaj komentarz
                    </button>
                    <button
                      type="button"
                      className={styles.buttonGhost}
                      onClick={load}
                    >
                      Odśwież komentarze
                    </button>
                  </div>
                </form>
              </div>
            </>
          )}
        </div>
      )}
    </div>
  );
}
