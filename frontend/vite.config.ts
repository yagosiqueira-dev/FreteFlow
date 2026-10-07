import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    VitePWA({
      // Keep using the manually maintained manifest in public/.
      manifest: false,
      strategies: 'generateSW',
      injectRegister: 'inline',
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg,webp,woff2,webmanifest}'],
        navigateFallback: null,
      },
    }),
  ],
  resolve: {
    dedupe: ['react', 'react-dom'],
  },
})
