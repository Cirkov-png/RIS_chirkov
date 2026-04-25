/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        display: ['"Fraunces"', 'Georgia', 'serif'],
        sans: ['"DM Sans"', 'system-ui', 'sans-serif'],
      },
      colors: {
        ink: { DEFAULT: '#0c1222', muted: '#3d4a63', light: '#8b9ab5' },
        surface: { DEFAULT: '#141c2e', card: '#1a2438', hover: '#222e48' },
        accent: { DEFAULT: '#2dd4bf', dim: '#115e59' },
        coral: { DEFAULT: '#fb7185', dim: '#9f1239' },
      },
      boxShadow: {
        glow: '0 0 40px -10px rgba(45, 212, 191, 0.35)',
      },
    },
  },
  plugins: [],
};
