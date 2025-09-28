// "use client";
// import Header from "@/components/Header/Header";
// import Sidebar from "@/components/SideBar/Sidebar";
// import Content from "@/components/Content/Content";
// import styles from "./Home.module.css";
// import { useAuth } from "@/components/Auth/AuthContext";
//
// export default function Home() {
//     const { isLogged, user } = useAuth();
//
//     return (
//         <div className={styles.container}>
//             <Header />
//             <div className={styles.gridWithSidebars}>
//                 <Sidebar position="left" isLogged={isLogged} user={user ?? undefined} />
//                 <main className={styles.main}>
//                     <Content isLogged={isLogged} user={user ?? undefined} />
//                 </main>
//                 <Sidebar position="right" isLogged={isLogged} user={user ?? undefined} />
//             </div>
//             <footer className={styles.footer}>© 2025 HIKKEI</footer>
//         </div>
//     );
// }

import Header from "@/components/Header/Header";
import Sidebar from "@/components/SideBar/Sidebar";
import Content from "@/components/Content/Content";
import styles from "./Home.module.css";
import AuthWrapper from "@/components/Auth/AuthWrapper";

export default function Home() {
    return (
        <div className={styles.container}>
            <Header />
            <div className={styles.gridWithSidebars}>
                <AuthWrapper position="left" />
                <main className={styles.main}>
                    {/*<Content isLogged={false} />*/}
                </main>
                <AuthWrapper position="right" />
            </div>
            <footer className={styles.footer}>© 2025 HIKKEI</footer>
        </div>
    );
}

