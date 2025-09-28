"use client";
import Header from "@/components/Header/Header";
import Sidebar from "@/components/SideBar/Sidebar";
import homeStyles from "@/app/Home.module.css";
import CategoryManager from "@/components/Category/CategoryManager";
import { useAuth } from "@/components/Auth/AuthContext";

export default function NewCategoryPage() {
    const { isLogged, user } = useAuth();

    return (
        <div className={homeStyles.container}>
            <Header />
            <div className={homeStyles.gridWithSidebars}>
                <Sidebar position="left" isLogged={isLogged} user={user ?? undefined} />
                <main className={homeStyles.main}>
                    <CategoryManager />
                </main>
                <Sidebar position="right" isLogged={isLogged} user={user ?? undefined} />
            </div>
            <footer className={homeStyles.footer}>© 2025 HIKKEI</footer>
        </div>
    );
}
