"use client";
import Header from "@/components/Header/Header";
import Sidebar from "@/components/SideBar/Sidebar";
import homeStyles from "@/app/Home.module.css";
import NewArticle from "@/components/Article/NewArticle";

export default function NewArticlePage() {
    return (
        <div className={homeStyles.container}>
            <Header />
            <div className={homeStyles.gridWithSidebars}>
                <Sidebar position="left"/>
                <main className={homeStyles.main}>
                    <NewArticle />
                </main>
                <Sidebar position="right"/>
            </div>
            <footer className={homeStyles.footer}>© 2025 HIKKEI</footer>
        </div>
    );
}
