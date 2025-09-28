"use client";
import { useEffect, useState } from "react";
import Header from "@/components/Header/Header";
import Sidebar from "@/components/SideBar/Sidebar";
import styles from "@/app/Home.module.css";
import { apiClient } from "@/lib/api";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/components/Auth/AuthContext";

type Page = {
  id: number;
  title: string;
};
type Content = {
  content: string;
};
type Revision = {
  id: number;
};

export default function NewArticleRevision() {
  const { isLogged, user } = useAuth();
  const router = useRouter();

  if (!isLogged) {
    alert("Zaloguj się, aby utworzyć rewizję strony.");
    router.push("/auth/");
    router.refresh();
    return null;
  }

  const searchParams = useSearchParams();
  const pageTitle = searchParams.get("page");

  if (pageTitle === null) {
    alert("W adresie strony brakuje identyfikatora artykułu");
    router.back();
    return null;
  }

  const [title, setTitle] = useState("[...]");
  const [content, setContent] = useState("Loading previous draft...");

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  var thisPage: Page | null = null;

  useEffect(() => {
    (async () => {
      try {
        const data = await apiClient<Page>(`/api/page/${pageTitle}`);
        setTitle(data.title);
        thisPage = data;
      } catch (err) {
        console.error("Nie udało się załadować strony: ", err);
        alert(
          `Nie udało się pobrać informacji o artykule ${pageTitle.replaceAll("_", " ")
          }`,
        );
        router.back();
      }
    })().then(async () => {
      if (thisPage == null) return;
      try {
        const data = await apiClient<Content>(
          `/api/revision/draft/${thisPage?.id}`,
        );
        setContent(data.content);
        setLoading(false);
      } catch (err) {
        setContent("");
        setLoading(false);
      }
    });
  }, []);

  const handleSubmit = (autoApprove: boolean) => async (e: FormData) => {
    setLoading(true);
    setError(null);

    try {
      var revision = await apiClient<Revision>(`/api/revision/create`, {
        body: JSON.stringify({
          page: thisPage?.id,
          content: e.get("content"),
        }),
        method: "POST",
      });

      if (autoApprove) {
        try {
          await apiClient(`/api/revision/update/status`, {
            body: JSON.stringify({ id: revision.id, status: true }),
            method: "PUT",
          });
          alert("Dodano i zatwierdzono rewizję!");
        } catch (err) {
          console.error(err);
          alert("Dodano rewizję, lecz nie udało się jej zatwierdzić.");
        }
      } else {
        alert("Dodano rewizję!");
      }
      router.push(
        `TODO: strona z listą rewizji dla danego artykułu, tam gdzie chcesz to dać madziu`,
      );
    } catch (err) {
      console.error(err);
      setError("Nie udało się dodać rewizji.");
    } finally {
      setLoading(false);
    }
  };

  const saveDraft = async (e: FormData) => {
    setLoading(true);
    setError(null);

    try {
      await apiClient(`/api/revision/draft/save`, {
        body: JSON.stringify({
          page: thisPage?.id,
          content: e.get("content"),
        }),
        method: "POST",
      });
      alert("Zapisano wersję roboczą!");
    } catch (err) {
      console.error(err);
      setError("Nie udało się zapisać wersji roboczej.");
    } finally {
      setLoading(false);
    }
  };

  const loadDraft = async (e: FormData) => {
    setLoading(true);
    setError(null);

    try {
      var data = await apiClient<Content>(
        `/api/revision/draft/${thisPage?.id}`,
      );
      setContent(data.content);
      alert("Załadowano wersję roboczą!");
    } catch (err) {
      console.error(err);
      setError("Nie udało się załadować wersji roboczej.");
    } finally {
      setLoading(false);
    }
  };

  const loadCurrent = async (e: FormData) => {
    setLoading(true);
    setError(null);

    try {
      var data = await apiClient<Content>(
        `/api/revision/current/${thisPage?.id}/raw`,
      );
      setContent(data.content);
      alert("Załadowano obecną rewizję!");
    } catch (err) {
      console.error(err);
      setError("Nie udało się załadować obecnej rewizji.");
    } finally {
      setLoading(false);
    }
  };

  const deleteDraft = async (e: FormData) => {
    setLoading(true);
    setError(null);

    try {
      await apiClient(`/api/revision/draft/delete`, {
        body: JSON.stringify({
          page: thisPage?.id,
        }),
        method: "DELETE",
      });
      alert("Usunięto wersję roboczą!");
    } catch (err) {
      console.error(err);
      setError("Nie udało się usunąć wersji roboczej.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <Header />
      <div className={styles.gridWithSidebars}>
        <Sidebar position="left" isLogged={isLogged} user={user ?? undefined} />
        <main className={styles.main}>
          <h1>Dodaj nową rewizję dla strony {title}</h1>
          <form action={handleSubmit(false)} className={styles.form}>
            <button
              type="submit"
              formAction={loadCurrent}
              className={styles.button}
              disabled={loading}
            >
              {loading ? "Zapisywanie…" : "Załaduj obecną rewizję"}
            </button>
            <button
              type="submit"
              formAction={loadDraft}
              className={styles.button}
              disabled={loading}
            >
              {loading ? "Zapisywanie…" : "Załaduj wersję roboczą"}
            </button>
            <button
              type="submit"
              formAction={saveDraft}
              className={styles.button}
              disabled={loading}
            >
              {loading ? "Zapisywanie…" : "Zapisz wersję roboczą"}
            </button>
            <button
              type="submit"
              formAction={deleteDraft}
              className={styles.button}
              disabled={loading}
            >
              {loading ? "Zapisywanie…" : "Usuń wersję roboczą"}
            </button>

            <label className={styles.label}>
              Treść (Markdown)
              <textarea
                name="content"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                required
                rows={12}
                className={styles.textarea}
                disabled={loading}
              />
            </label>

            {error && <p className={styles.error}>{error}</p>}

            <button type="submit" className={styles.button} disabled={loading}>
              {loading ? "Zapisywanie…" : "Dodaj rewizję"}
            </button>
            <button
              type="submit"
              formAction={handleSubmit(true)}
              className={styles.button}
              disabled={loading}
            >
              {loading ? "Zapisywanie…" : "Dodaj i zatwierdź rewizję"}
            </button>
          </form>
        </main>
        <Sidebar
          position="right"
          isLogged={isLogged}
          user={user ?? undefined}
        />
      </div>
      <footer className={styles.footer}>© 2025 HIKKEI</footer>
    </div>
  );
}
