export async function apiClient<T = unknown>(
    url: string,
    auth: boolean,
    error: boolean,
    init?: RequestInit
): Promise<T | null> {
    const res = await fetch(url, {
        ...init,
        credentials: "include",
        cache: "no-cache",
        mode: "cors",
        headers: {
            ...(init?.headers ?? {}),
        },
    });

    if (auth) {
        if (res.status === 401) {
            window.location.assign("/auth?reason=401");
            throw new Error("Unauthorized");
        }
        if (res.status === 403) {
            window.location.assign("/forbidden");
            throw new Error("Forbidden");
        }
    }

    if (!res.ok) {
        if (error) throw new Error(`HTTP ${res.status} for ${url}`);
        else return null;
    }

    if (res.status === 204) return {} as T;
    return (await res.json()) as T;
}
