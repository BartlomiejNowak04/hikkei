"use client";
import { useEffect } from "react";

export default function LogoutPage() {
    useEffect(() => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        window.location.assign("/");
    }, []);
    return <p>Wylogowuję…</p>;
}
