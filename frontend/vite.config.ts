import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    VitePWA({
      // The manifest is served from public; keep SW generation/registration off in this stage.
      disable: true,
      manifest: false,
    }),
  ],
  resolve: {
    dedupe: ['react', 'react-dom'],
  },
})
