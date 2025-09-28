import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import {ThemeProvider} from "@/components/Theme/ThemeProvider";
import Header from "@/components/Header/Header";
import styles from "@/app/Home.module.css";
import Sidebar from "@/components/SideBar/Sidebar";

const geistSans = Geist({
    variable: "--font-geist-sans",
    subsets: ["latin"],
});

const geistMono = Geist_Mono({
    variable: "--font-geist-mono",
    subsets: ["latin"],
});

export const metadata: Metadata = {
    title: "HIKKEI",
    description: "By K. Kosiorowski, T. Kulis, M. Kycler and M. Mikołajczak",
    icons:{ icon: '/icons/favicon.svg' },
};

export default function RootLayout({
                                       children,
                                   }: Readonly<{ children: React.ReactNode }>) {
    return (
        <html lang="pl">
        <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        <ThemeProvider>
            <div className={styles.container}>
                <Header/>
                <div className={styles.gridWithSidebars}>
                    <Sidebar position="left" />
                    <main className={styles.main}>
                        {children}
                    </main>
                    <Sidebar position="right" />
                </div>
                <footer className={styles.footer}>© 2025 HIKKEI</footer>
            </div>
        </ThemeProvider>
        </body>
        </html>
    );
}
