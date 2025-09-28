"use client";
import Sidebar from "@/components/SideBar/Sidebar";
import { useAuth } from "@/components/Auth/AuthContext";

export default function AuthWrapper({ position }: { position: "left" | "right" }) {
    const { isLogged, user } = useAuth();
    return <Sidebar position={position} isLogged={isLogged} user={user ?? undefined} />;
}
