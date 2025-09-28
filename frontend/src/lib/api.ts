import { redirect } from "next/navigation";

export async function apiServer<T = unknown>(
    url: string,
    init?: RequestInit
): Promise<T> {
    const res = await fetch(url, {
        ...init,
        // cookies z OAuth będą wysyłane
        credentials: "include",
        cache: "no-store",
    });

    if (res.status === 401) redirect("/auth?reason=401");
    if (res.status === 403) redirect("/forbidden");

    if (!res.ok) {
        throw new Error(`HTTP ${res.status} for ${url}`);
    }
    return (await res.json()) as T;
}


export async function apiClient<T = unknown>(
    url: string,
    init?: RequestInit
): Promise<T> {
    const res = await fetch(url, {
        ...init,
        credentials: "include",
        cache: "no-store",
        headers: {
            ...(init?.headers ?? {}),
        },
    });

    if (res.status === 401) {
        window.location.assign("/auth?reason=401");
        throw new Error("Unauthorized");
    }
    if (res.status === 403) {
        window.location.assign("/forbidden");
        throw new Error("Forbidden");
    }

    if (!res.ok) {
        throw new Error(`HTTP ${res.status} for ${url}`);
    }
    return (await res.json()) as T;
}
