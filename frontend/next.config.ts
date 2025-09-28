import type { NextConfig } from "next";

const nextConfig: NextConfig = {
    webpack(config) {
        config.module.rules.push({
            test: /\.svg$/i,
            resourceQuery: /url/,
            type: "asset",
        });

        config.module.rules.push({
            test: /\.svg$/i,
            issuer: /\.[jt]sx?$/,
            resourceQuery: { not: [/url/] },
            use: [
                {
                    loader: "@svgr/webpack",
                    options: {
                        icon: true,
                        svgo: true,
                        svgoConfig: {
                            plugins: [
                                { name: "removeAttrs", params: { attrs: "(fill|stroke)" } },
                                { name: "removeViewBox", active: false },
                            ],
                        },
                    },
                },
            ],
        });

        return config;
    },

    async rewrites() {
        return [
            {
                source: "/backend/:path*",
                destination: "http://localhost:8888/api/:path*",
            },
        ];
    },
};

module.exports = nextConfig;
