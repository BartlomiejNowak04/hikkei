import {User} from "@/lib/types";

const API = process.env.NEXT_PUBLIC_API_BASE;

export async function getUser() {
    const res = await fetch(`${API}/api/user/`, {
       credentials: "include",
        cache: "no-cache",
        mode: "cors",
    });

    console.log(res.status);

    if (!res.ok) return null;
    return (await res.json()) as User;
}

export async function getLogged() {
    return Boolean(await getUser());
}
