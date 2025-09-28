import Header from "@/components/Header/Header";
import Sidebar from "@/components/SideBar/Sidebar";
import Content from "@/components/Content/Content";
import styles from "./Home.module.css";

export default async function Home() {
    const isLogged = false;

    return (
        <div className={styles.container}>
            <Header />
            <div className={styles.gridWithSidebars}>
                <Sidebar position="left" isLogged={isLogged} />
                <main className={styles.main}>
                    <Content isLogged={isLogged} />
                </main>
                <Sidebar position="right" isLogged={isLogged} />
            </div>
            <footer className={styles.footer}>© 2025 HIKKEI</footer>
        </div>
    );
}


