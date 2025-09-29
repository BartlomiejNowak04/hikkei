"use client";
import React, { useEffect, useState } from "react";
import styles from "./AccountSettings.module.css";
import { useRouter } from "next/navigation";

/** Pomocnik: fetch z obsługą 204/pustej odpowiedzi i nie-JSON
 *  + wyciąganie message z JSON przy błędzie (np. "This username is already taken")
 */
async function request<T = unknown>(url: string, init?: RequestInit): Promise<T> {
    const res = await fetch(url, { ...init, cache: "no-store", credentials: "include" });

    if (!res.ok) {
        let msg = `HTTP ${res.status}`;
        try {
            const t = await res.text();
            if (t) {
                try {
                    const parsed = JSON.parse(t);
                    msg = parsed?.message ? String(parsed.message) : `${msg}: ${t}`;
                } catch {
                    msg = `${msg}: ${t}`;
                }
            }
        } catch {}
        throw new Error(msg);
    }

    if (res.status === 204) return undefined as T;

    const ct = (res.headers.get("content-type") || "").toLowerCase();
    const raw = await res.text();
    if (!raw) return undefined as T;
    if (!ct.includes("application/json")) return raw as unknown as T;

    try {
        return JSON.parse(raw) as T;
    } catch {
        return raw as unknown as T;
    }
}

type Props = {
    /** przekaż id zalogowanego usera (np. z SSR). Jeśli brak – profil nie będzie refetchowany, ale update’y zadziałają. */
    userId?: number;
};

type UserProfile = {
    id: number;
    name: string;
    email?: string;
};

export default function AccountSettings({ userId }: Props) {
    const router = useRouter();

    // dane
    const [me, setMe] = useState<UserProfile | null>(null);
    const [loading, setLoading] = useState<boolean>(!!userId);
    const [err, setErr] = useState<string | null>(null);

    // nazwa
    const [name, setName] = useState("");
    const [savingName, setSavingName] = useState(false);

    // avatar
    const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
    const [avatarFile, setAvatarFile] = useState<File | null>(null);
    const [savingAvatar, setSavingAvatar] = useState(false);
    const [dragActive, setDragActive] = useState(false);
    const [pfpBust, setPfpBust] = useState<string>(""); // cache buster query
    const [hasPfpError, setHasPfpError] = useState(false);
    const [imgKey, setImgKey] = useState(0); // wymusza re-render <img>

    // delete
    const [deleting, setDeleting] = useState(false);

    // helper: pobierz profil po userId
    async function refetchMe() {
        if (userId == null) return;
        try {
            const data = await request<UserProfile>(`/api/user/${encodeURIComponent(String(userId))}`);
            setMe(data ?? null);
            if (data?.name) setName(data.name);
        } catch (e) {
            console.error("refetchMe error", e);
        }
    }

    // init: jeśli mamy userId, pobierz profil
    useEffect(() => {
        if (userId == null) return;
        let cancelled = false;
        (async () => {
            setLoading(true);
            setErr(null);
            try {
                const data = await request<UserProfile>(`/api/user/${encodeURIComponent(String(userId))}`);
                if (!cancelled) {
                    setMe(data ?? null);
                    setName(data?.name ?? "");
                }
            } catch (e) {
                if (!cancelled) {
                    console.error(e);
                    setErr("Nie udało się pobrać danych konta.");
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        })();
        return () => {
            cancelled = true;
        };
    }, [userId]);

    function handleFile(file: File) {
        if (!/^image\/(png|jpe?g|webp|gif)$/i.test(file.type)) {
            setErr("Nieobsługiwany format. Dozwolone: PNG, JPG, WEBP, GIF.");
            return;
        }
        const MAX = 5 * 1024 * 1024; // 5 MB
        if (file.size > MAX) {
            setErr("Plik jest zbyt duży (max 5MB).");
            return;
        }
        setErr(null);
        setAvatarFile(file);
        setAvatarPreview(URL.createObjectURL(file)); // natychmiastowy podgląd
    }

    // DnD (deklaratywnie)
    const onDragEnter: React.DragEventHandler<HTMLDivElement> = (e) => {
        e.preventDefault(); e.stopPropagation(); setDragActive(true);
    };
    const onDragOver: React.DragEventHandler<HTMLDivElement> = (e) => {
        e.preventDefault(); e.stopPropagation(); setDragActive(true);
    };
    const onDragLeave: React.DragEventHandler<HTMLDivElement> = (e) => {
        e.preventDefault(); e.stopPropagation(); setDragActive(false);
    };
    const onDrop: React.DragEventHandler<HTMLDivElement> = (e) => {
        e.preventDefault(); e.stopPropagation(); setDragActive(false);
        const file = e.dataTransfer?.files?.[0];
        if (file) handleFile(file);
    };

    async function saveName(e: React.FormEvent) {
        e.preventDefault();
        if (!name.trim()) return;
        setSavingName(true);
        setErr(null);
        try {
            await request<void>("/api/user/update/name", {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name }),
            });
            // optymistycznie:
            setMe((m) => (m ? { ...m, name } : m));
            // dociągnij świeże dane (żeby header/layout też widział zmianę)
            await refetchMe();
            router.refresh();
        } catch (e) {
            console.error(e);
            // teraz request() zwraca message z backendu (np. "This username is already taken")
            setErr((e as Error).message);
        } finally {
            setSavingName(false);
        }
    }

    async function saveAvatar() {
        if (!avatarFile) return;
        setSavingAvatar(true);
        setErr(null);
        try {
            const fd = new FormData();
            fd.append("file", avatarFile);
            await request<void>("/api/user/update/pfp", { method: "PUT", body: fd });
            // wyczyść local preview
            setAvatarFile(null);
            setAvatarPreview(null);
            // resetuj błąd, dodaj cache-buster i zmień key, żeby IMG NA PEWNO odświeżyć
            setHasPfpError(false);
            const buster = `?_=${Date.now()}`;
            setPfpBust(buster);
            setImgKey((k) => k + 1);
            // dociągnij świeże dane i odśwież layout
            await refetchMe();
            router.refresh();
        } catch (e) {
            console.error(e);
            setErr("Nie udało się zaktualizować zdjęcia.");
        } finally {
            setSavingAvatar(false);
        }
    }

    async function deleteAccountClick() {
        if (!confirm("Na pewno chcesz usunąć konto? Tej operacji nie można cofnąć.")) return;
        setDeleting(true);
        setErr(null);
        try {
            await request<void>("/api/user/delete", { method: "DELETE" });
            router.push("/");
            router.refresh();
        } catch (e) {
            console.error(e);
            setErr("Nie udało się usunąć konta.");
        } finally {
            setDeleting(false);
        }
    }

    if (loading) {
        return (
            <section className={styles.wrapper}>
                <div className={styles.skeleton} />
            </section>
        );
    }

    // IMG src: preview > API (gdy jest userId) > kiwi (fallback)
    const pfpSrc =
        avatarPreview ??
        (hasPfpError || userId == null
            ? "/icons/kiwi-bird-solid.svg"
            : `/api/user/pfp/${userId}${pfpBust}`);

    return (
        <section className={styles.wrapper}>
            <h1 className={styles.pageTitle}>Ustawienia konta</h1>

            {err && <p className={styles.error} role="alert">{err}</p>}

            {/* Sekcja: Avatar */}
            <div className={styles.card}>
                <h2 className={styles.sectionTitle}>Zdjęcie profilowe</h2>

                <div className={styles.avatarRow}>
                    <img
                        key={imgKey} // wymusza repaint po zapisie
                        src={pfpSrc}
                        alt="Podgląd awatara"
                        className={styles.avatar}
                        onError={() => setHasPfpError(true)}
                    />

                    <div className={styles.avatarActions}>
                        <div
                            className={`${styles.dropzone} ${dragActive ? styles.dropActive : ""}`}
                            onDragEnter={onDragEnter}
                            onDragOver={onDragOver}
                            onDragLeave={onDragLeave}
                            onDrop={onDrop}
                        >
                            <p>Przeciągnij i upuść tutaj plik</p>
                            <p className={styles.muted}>albo</p>
                            <label className={styles.buttonGhost}>
                                Wybierz plik
                                <input
                                    type="file"
                                    accept="image/png,image/jpeg,image/webp,image/gif"
                                    hidden
                                    onChange={(e) => {
                                        const f = e.target.files?.[0];
                                        if (f) handleFile(f);
                                    }}
                                />
                            </label>
                        </div>

                        <div className={styles.actions}>
                            <button
                                className={styles.buttonPrimary}
                                onClick={saveAvatar}
                                disabled={!avatarFile || savingAvatar}
                            >
                                {savingAvatar ? "Zapisywanie…" : "Zapisz zdjęcie"}
                            </button>
                            {avatarFile && (
                                <button
                                    className={styles.buttonGhost}
                                    onClick={() => { setAvatarFile(null); setAvatarPreview(null); }}
                                >
                                    Anuluj
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Sekcja: Nazwa */}
            <form className={styles.card} onSubmit={saveName}>
                <h2 className={styles.sectionTitle}>Nazwa wyświetlana</h2>
                <label className={styles.label} htmlFor="name">Nowa nazwa</label>
                <div className={styles.inputRow}>
                    <input
                        id="name"
                        className={styles.inputLg}
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        placeholder={me?.name ? `Obecnie: ${me.name}` : "Twoja nazwa"}
                        required
                        aria-invalid={Boolean(err)}
                        aria-describedby={err ? "name-error" : undefined}
                    />
                    <button className={styles.buttonPrimary} disabled={savingName}>
                        {savingName ? "Zapisywanie…" : "Zapisz"}
                    </button>
                </div>
                {err && (
                    <p id="name-error" className={styles.error} role="alert">
                        {err}
                    </p>
                )}
            </form>

            {/* Sekcja: Usuwanie konta — tylko przycisk, bez pola */}
            <div className={styles.cardDanger}>
                <h2 className={styles.sectionTitle}>Usuń konto</h2>
                <p className={styles.muted}>Tej operacji nie można cofnąć.</p>
                <button
                    type="button"
                    className={styles.buttonDanger}
                    disabled={deleting}
                    onClick={deleteAccountClick}
                >
                    {deleting ? "Usuwanie…" : "Usuń konto"}
                </button>
            </div>
        </section>
    );
}
